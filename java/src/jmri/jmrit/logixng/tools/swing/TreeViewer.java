package jmri.jmrit.logixng.tools.swing;

import java.awt.Color;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;

import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.*;
import jmri.util.JmriJFrame;

/**
 * Show the action/expression tree.
 * <P>
 * Base class for ConditionalNG editors
 * 
 * @author Daniel Bergqvist 2018
 */
public class TreeViewer extends JmriJFrame implements PropertyChangeListener {

    private static final int panelWidth = 700;
    private static final int panelHeight = 500;
    
    private boolean _rootVisible = true;
    
    private static final Map<String, Color> FEMALE_SOCKET_COLORS = new HashMap<>();
    
    JTree tree;
    
    protected final FemaleSocket _femaleRootSocket;
    protected FemaleSocketTreeModel femaleSocketTreeModel;
    
    boolean _showReminder = false;
    
    
    /**
     * Construct a ConditionalEditor.
     *
     * @param femaleRootSocket the root of the tree
     */
    public TreeViewer(FemaleSocket femaleRootSocket) {
        _femaleRootSocket = femaleRootSocket;
        // Note!! This must be made dynamic, so that new socket types are recognized automaticly and added to the list
        // and the list must be saved between runs.
        FEMALE_SOCKET_COLORS.put("jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket", Color.RED);
        FEMALE_SOCKET_COLORS.put("jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket", Color.BLUE);
        
        _femaleRootSocket.forEntireTree((Base b) -> {
            b.addPropertyChangeListener(TreeViewer.this);
        });
    }
    
    @Override
    public void initComponents() {
        super.initComponents();
        
        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        JMenuItem closeWindowItem = new JMenuItem(Bundle.getMessage("CloseWindow"));
        closeWindowItem.addActionListener((ActionEvent e) -> {
            dispose();
        });
        fileMenu.add(closeWindowItem);
        menuBar.add(fileMenu);
        
/*        
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new TimeDiagram.CreateNewLogixNGAction("Create a LogixNG"));
        toolMenu.add(new CreateNewLogixNGAction(Bundle.getMessage("TitleOptions")));
        toolMenu.add(new PrintOptionAction());
        toolMenu.add(new BuildReportOptionAction());
        toolMenu.add(new BackupFilesAction(Bundle.getMessage("Backup")));
        toolMenu.add(new RestoreFilesAction(Bundle.getMessage("Restore")));
        toolMenu.add(new LoadDemoAction(Bundle.getMessage("LoadDemo")));
        toolMenu.add(new ResetAction(Bundle.getMessage("ResetOperations")));
        toolMenu.add(new ManageBackupsAction(Bundle.getMessage("ManageAutoBackups")));
        menuBar.add(toolMenu);
*/

        setJMenuBar(menuBar);
//        addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true); // NOI18N
        
        femaleSocketTreeModel = new FemaleSocketTreeModel(_femaleRootSocket);
        
        // Create a JTree and tell it to display our model
        tree = new JTree();
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setModel(femaleSocketTreeModel);
        tree.setCellRenderer(new FemaleSocketTreeRenderer());
        
        tree.setRootVisible(_rootVisible);
        
        // Expand the entire tree
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        
        // The JTree can get big, so allow it to scroll
        JScrollPane scrollpane = new JScrollPane(tree);

        // create panel
        JPanel pPanel = new JPanel();
        pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.Y_AXIS));
        
        // Display it all in a window and make the window appear
        pPanel.add(scrollpane, "Center");

        // add panels
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(pPanel);
        
        initMinimumSize(new Dimension(panelWidth, panelHeight));
    }

    public void initMinimumSize(Dimension dimension) {
        setMinimumSize(dimension);
        pack();
        setVisible(true);
    }
    
    public boolean getRootVisible() {
        return _rootVisible;
    }
    
    public void setRootVisible(boolean rootVisible) {
        _rootVisible = rootVisible;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        
        if (Base.PROPERTY_CHILD_COUNT.equals(evt.getPropertyName())) {
            
            // Remove myself as listener from sockets that has been removed
            if (evt.getOldValue() != null) {
                if (! (evt.getOldValue() instanceof List)) throw new RuntimeException("Old value is not a list");
                for (FemaleSocket socket : (List<FemaleSocket>)evt.getOldValue()) {
                    socket.removePropertyChangeListener(this);
                }
            }
            
            // Add myself as listener to sockets that has been added
            if (evt.getNewValue() != null) {
                if (! (evt.getNewValue() instanceof List)) throw new RuntimeException("New value is not a list");
                for (FemaleSocket socket : (List<FemaleSocket>)evt.getNewValue()) {
                    socket.addPropertyChangeListener(this);
                }
            }
            
            // Update the tree
            Base b = (Base)evt.getSource();
            FemaleSocket.FemaleSocketAndRow socketAndRow = new FemaleSocket.FemaleSocketAndRow();
            _femaleRootSocket.getFemaleSocketAndRow(b, socketAndRow);
            
            FemaleSocket femaleSocket = socketAndRow._socket;
            int row = socketAndRow._row;
            if (!_rootVisible) row--;
            TreePath path = tree.getPathForRow(row);
            updateTree(femaleSocket, path);
        }
        
        
        if (Base.PROPERTY_SOCKET_CONNECTED.equals(evt.getPropertyName())
                || Base.PROPERTY_SOCKET_DISCONNECTED.equals(evt.getPropertyName())) {
            
            FemaleSocket femaleSocket = ((FemaleSocket)evt.getSource());
            int row = femaleSocketTreeModel.getRow(femaleSocket);
            TreePath path = tree.getPathForRow(row);
            updateTree(femaleSocket, path);
        }
    }
    
    protected void updateTree(FemaleSocket currentFemaleSocket, TreePath currentPath) {
        for (TreeModelListener l : femaleSocketTreeModel.listeners) {
            TreeModelEvent tme = new TreeModelEvent(
                    currentFemaleSocket,
                    currentPath.getPath()
            );
            l.treeNodesChanged(tme);
        }
        tree.updateUI();
    }
    
    @Override
    public void dispose() {
        _femaleRootSocket.forEntireTree((Base b) -> {
            b.addPropertyChangeListener(TreeViewer.this);
        });
        super.dispose();
    }
    
    
    /**
     * The methods in this class allow the JTree component to traverse the
     * female sockets of the ConditionalNG tree.
     */
    public static class FemaleSocketTreeModel implements TreeModel {

        private final FemaleSocket root;
        protected final List<TreeModelListener> listeners = new ArrayList<>();

        public FemaleSocketTreeModel(FemaleSocket root) {
            this.root = root;
        }

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        public boolean isLeaf(Object node) {
            FemaleSocket socket = (FemaleSocket) node;
            if (!socket.isConnected()) {
                return true;
            }
            return socket.getConnectedSocket().getChildCount() == 0;
        }

        @Override
        public int getChildCount(Object parent) {
            FemaleSocket socket = (FemaleSocket) parent;
            if (!socket.isConnected()) {
                return 0;
            }
            return socket.getConnectedSocket().getChildCount();
        }

        @Override
        public Object getChild(Object parent, int index) {
            FemaleSocket socket = (FemaleSocket) parent;
            if (!socket.isConnected()) {
                return null;
            }
            return socket.getConnectedSocket().getChild(index);
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            FemaleSocket socket = (FemaleSocket) parent;
            if (!socket.isConnected()) {
                return -1;
            }
            
            MaleSocket connectedSocket = socket.getConnectedSocket();
            for (int i = 0; i < connectedSocket.getChildCount(); i++) {
                if (child == connectedSocket.getChild(i)) {
                    return i;
                }
            }
            return -1;
        }

        // This method is invoked by the JTree only for editable trees.  
        // This TreeModel does not allow editing, so we do not implement 
        // this method.  The JTree editable property is false by default.
        @Override
        public void valueForPathChanged(TreePath path, Object newvalue) {
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
        }

        public int getRow(Base base) {
            SearchTreeData data = new SearchTreeData();
            try {
                base.forEntireTree((Base b) -> {
                    if (base == b) throw new FoundException();
                    data.row++;
                });
            } catch (FoundException e) {
                return data.row;
            }
            throw new IllegalArgumentException("Item "+base.toString()+" not found in tree");
        }

//        private void notifyListeners() {
//            for (TreeModelListener l : listeners) {
//                l.treeNodesChanged(e);
//            }
//        }

        private static class SearchTreeData {
            int row = 0;
        }
        
        private static class FoundException extends RuntimeException {}
        
    }
    
    
    private static final class FemaleSocketTreeRenderer implements TreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
            FemaleSocket socket = (FemaleSocket)value;
            
//            FemaleSocketPanel panel = new FemaleSocketPanel(socket);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            panel.setOpaque(false);
            
            JLabel socketLabel = new JLabel(socket.getShortDescription());
            Font font = socketLabel.getFont();
            socketLabel.setFont(font.deriveFont((float)(font.getSize2D()*1.7)));
            socketLabel.setForeground(FEMALE_SOCKET_COLORS.get(socket.getClass().getName()));
//            socketLabel.setForeground(Color.red);
            panel.add(socketLabel);
            
            panel.add(javax.swing.Box.createRigidArea(new Dimension(5,0)));
            
            JLabel socketNameLabel = new JLabel(socket.getName());
            socketNameLabel.setForeground(FEMALE_SOCKET_COLORS.get(socket.getClass().getName()));
//            socketNameLabel.setForeground(Color.red);
            panel.add(socketNameLabel);
            
            panel.add(javax.swing.Box.createRigidArea(new Dimension(5,0)));
            
            JLabel connectedItemLabel = new JLabel();
            if (socket.isConnected()) {
                MaleSocket connectedSocket = socket.getConnectedSocket();
                connectedItemLabel.setText(connectedSocket.getLongDescription());
                if (connectedSocket.getComment() != null) {
                    panel.setToolTipText(connectedSocket.getComment());
                }
            }
            
            panel.add(connectedItemLabel);
            
            return panel;
        }
        
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TreeViewer.class);

}
