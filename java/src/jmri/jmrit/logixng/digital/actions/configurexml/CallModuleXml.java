package jmri.jmrit.logixng.digital.actions.configurexml;

import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.NamedBeanHandle;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.digital.actions.CallModule;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class CallModuleXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public CallModuleXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        CallModule p = (CallModule) o;

        Element element = new Element("action-call-module");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

//        NamedBeanHandle light = p.getLight();
//        if (light != null) {
//            element.addContent(new Element("light").addContent(light.getName()));
//        }
        
//        element.addContent(new Element("lightState").addContent(p.getLightState().name()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        CallModule h = new CallModule(sys, uname);

        loadCommon(h, shared);

//        Element lightName = shared.getChild("light");
//        if (lightName != null) {
//            Light t = InstanceManager.getDefault(LightManager.class).getLight(lightName.getTextTrim());
//            if (t != null) h.setLight(t);
//            else h.removeLight();
//        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static Logger log = LoggerFactory.getLogger(ActionLightXml.class);
}
