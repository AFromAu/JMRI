package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import jmri.util.FileUtil;
import jmri.util.swing.WindowInterface;

import org.jdom2.Element;

/**
 * Reload the entire JMRI Roster ({@link jmri.jmrit.roster.Roster}) from a file
 * previously stored by {@link jmri.jmrit.roster.FullBackupExportAction}.
 * <p>
 * Does not currently handle importing the group(s) that the entry belongs to.
 *
 * @author Bob Jacobsen Copyright 2014, 2018
 */
public class FullBackupImportAction extends ImportRosterItemAction {

    //private Component _who;
    public FullBackupImportAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public FullBackupImportAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * @param title  Name of this action, e.g. in menus
     * @param parent Component that action is associated with, used to ensure
     *               proper position in of dialog boxes
     */
    public FullBackupImportAction(String title, Component parent) {
        super(title, parent);
    }
    
    boolean acceptAll;
    boolean acceptAllDup;
    
    @Override
    public void actionPerformed(ActionEvent e) {

        // ensure preferences will be found for read
        FileUtil.createDirectory(Roster.getDefault().getRosterFilesLocation());

        // make sure instance loaded
        Roster.getDefault();

        // set up to read import file
        ZipInputStream zipper = null;
        FileInputStream inputfile = null;

        JFileChooser chooser = new JFileChooser();

        String roster_filename_extension = "roster";
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "JMRI full roster files", roster_filename_extension);
        chooser.addChoosableFileFilter(filter);

        int returnVal = chooser.showOpenDialog(mParent);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String filename = chooser.getSelectedFile().getAbsolutePath();
        
        try {

            inputfile = new FileInputStream(filename);
            zipper = new ZipInputStream(inputfile) {
                @Override
                public void close() {
                } // SaxReader calls close when reading XML stream, ignore
                // and close directly later
            };

            // now iterate through each item in the stream. The get next
            // entry call will return a ZipEntry for each file in the
            // stream
            ZipEntry entry;
            acceptAll = false; // skip prompting for each entry and accept all
            acceptAllDup = false;  // skip prompting for dups and accept all
            SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOI18N ISO8601
            
            while ((entry = zipper.getNextEntry()) != null) {
                log.debug("Entry: {} len {} ({}) added {} content: {}",
                                        entry.getName(), 
                                        entry.getSize(), 
                                        entry.getCompressedSize(), 
                                        isoDateFormat.format(entry.getTime()),
                                        entry.getComment()
                        );

                // Once we get the entry from the stream, the stream is
                // positioned read to read the raw data, and we keep
                // reading until read returns 0 or less.
                
                // find type and process
                // Unfortunately, the comment field doesn't carry through (see debug above)
                // so we check the filename
                if (entry.getName().endsWith(".xml") || entry.getName().endsWith(".XML")) {
                    boolean retval = processRosterFile(zipper);
                    if (!retval) break;
                } else {
                    processImageFile(zipper, entry, entry.getName());
                }
                
            }

        } catch (FileNotFoundException ex) {
            log.error("Unable to find {}", filename, ex);
        } catch (IOException ex) {
            log.error("Unable to read {}", filename, ex);
        } finally {
            if (inputfile != null) {
                try {
                    inputfile.close(); // zipper.close() is meaningless, see above, but this will do
                } catch (IOException ex) {
                    log.error("Unable to close {}", filename, ex);
                }
            }
        }

    }

    void processImageFile(ZipInputStream zipper, ZipEntry entry, String path) throws IOException {
        if (! path.startsWith("roster/")) {
            log.error("Can't cope with image files outside the roster/ directory: {}", path);
            return;
        }
        String fullPath = Roster.getDefault().getRosterFilesLocation()+path.substring(7);
        log.debug("fullpath: {}", fullPath);
        if (new File(fullPath).exists()) {
            log.info("skipping existing file: {}", path);
            return;
        }
        // and finally copy into place
        Files.copy(zipper, Paths.get(fullPath));
    }

    /**
     * @return true if OK to continue to next entry
     * @param zipper Stream to receive output
     * @throws IOException from underlying operations
     */

    protected boolean processRosterFile(ZipInputStream zipper) throws IOException {

        try {
            LocoFile xfile = new LocoFile();   // need a dummy object to do this operation in next line
            Element lroot = xfile.rootFromInputStream(zipper).clone();
            if (lroot.getChild("locomotive") == null) {
                return true;  // that's the roster file
            }
            mToID = lroot.getChild("locomotive").getAttributeValue("id");

            // see if user wants to do it
            int retval = 2; // accept if acceptall
            if (!acceptAll) {
                retval = JOptionPane.showOptionDialog(mParent,
                    Bundle.getMessage("ConfirmImportID", mToID),
                    Bundle.getMessage("ConfirmImport"),
                    0,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new Object[]{Bundle.getMessage("CancelImports"),
                        Bundle.getMessage("Skip"),
                        Bundle.getMessage("ButtonOK"),
                        Bundle.getMessage("ButtonAcceptAll")},
                    null);
            }
            if (retval == 0) {
                // cancel case
                return false;
            }
            if (retval == 1) {
                // skip case
                return true;
            }
            if (retval == 3) {
                // accept all case
                acceptAll = true;
            }

            // see if duplicate
            RosterEntry currentEntry = Roster.getDefault().getEntryForId(mToID);

            if (currentEntry != null) {
                if (!acceptAllDup) {
                    retval = JOptionPane.showOptionDialog(mParent,
                        Bundle.getMessage("ConfirmImportDup", mToID),
                        Bundle.getMessage("ConfirmImport"),
                        0,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new Object[]{Bundle.getMessage("CancelImports"),
                            Bundle.getMessage("Skip"),
                            Bundle.getMessage("ButtonOK"),
                            Bundle.getMessage("ButtonAcceptAll")},
                        null);
                }
                if (retval == 0) {
                    // cancel case
                    return false;
                }
                if (retval == 1) {
                    // skip case
                    return true;
                }
                if (retval == 3) {
                    // accept all case
                    acceptAllDup = true;
                }

                // turn file into backup
                LocoFile df = new LocoFile();   // need a dummy object to do this operation in next line
                df.makeBackupFile(Roster.getDefault().getRosterFilesLocation() + currentEntry.getFileName());

                // delete entry
                Roster.getDefault().removeEntry(currentEntry);

            }

            loadEntryFromElement(lroot);
            addToEntryToRoster();

            // use the new roster
            Roster.getDefault().reloadRosterFile();
        } catch (org.jdom2.JDOMException ex) {
            log.error("Unable to parse entry", ex);
        }

        return true;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FullBackupImportAction.class);
}
