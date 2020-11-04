package jmri.jmrit.logixng.digital.boolean_actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.DigitalBooleanActionManager;
import jmri.jmrit.logixng.digital.boolean_actions.OnChange;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class OnChangeXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public OnChangeXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        OnChange p = (OnChange) o;

        Element element = new Element("on-change");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        element.setAttribute("trigger", p.getTrigger().name());
        
        Element e2 = new Element("socket");
        e2.addContent(new Element("socketName").addContent(p.getChild(1).getName()));
        MaleSocket socket = p.getActionSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getActionSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        
        Attribute triggerAttribute = shared.getAttribute("trigger");
        OnChange.Trigger trigger = OnChange.Trigger.valueOf(triggerAttribute.getValue());
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        OnChange h = new OnChange(sys, uname, trigger);

        loadCommon(h, shared);
        
        Element socketName = shared.getChild("socket").getChild("socketName");
        h.getChild(0).setName(socketName.getTextTrim());
        Element socketSystemName = shared.getChild("socket").getChild("systemName");
        if (socketSystemName != null) {
            h.setActionSocketSystemName(socketSystemName.getTextTrim());
        }
        
        InstanceManager.getDefault(DigitalBooleanActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OnChangeActionXml.class);
}
