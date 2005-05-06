/*
 * Created on 06.05.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package info.magnolia.module.admininterface.dialogs;

import java.lang.reflect.Constructor;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.module.admininterface.DialogMVCHandler;


/**
 * @author philipp TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 * Style - Code Templates
 */
public class ConfiguredDialog extends DialogMVCHandler {

    private static Logger log = Logger.getLogger(ConfiguredDialog.class);

    private Content configNode;

    public ConfiguredDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response);
        this.configNode = configNode;
    }

    /**
     * Returns the node with the dialog definition.
     * @return
     */
    protected Content getConfigNode() {
        return configNode;
    }

    public static Content getConfigNode(HttpServletRequest request, String name) {
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(request, ContentRepository.CONFIG);
        try {
            return hm.getContent(name);
        }
        catch (RepositoryException e) {
            log.error("no configuration found for the dialog + " + name, e);
        }
        return null;
    }

    public static ConfiguredDialog getConfiguredDialog(String name, Content configNode, HttpServletRequest request,
        HttpServletResponse response) {
        return getConfiguredDialog(name, configNode, request, response, ConfiguredDialog.class);
    }

    /**
     * @param paragraph
     * @param configNode2
     * @param request
     * @param response
     * @param class1
     * @return
     */
    public static ConfiguredDialog getConfiguredDialog(String name, Content configNode, HttpServletRequest request,
        HttpServletResponse response, Class defaultClass) {
        // get class name
        String className;
        try {
            Class handlerClass = defaultClass;
            try{
            className = configNode.getNodeData("class").getString();
            handlerClass = Class.forName(className);
            }
            catch(Exception e){}
            Constructor constructor = handlerClass.getConstructor(new Class[]{
                String.class,
                HttpServletRequest.class,
                HttpServletResponse.class,
                Content.class});
            return (ConfiguredDialog) constructor.newInstance(new Object[]{name, request, response, configNode});
        }
        catch (Exception e) {
            log.error("can't instantiate dialog: ", e);
            return null;
        }
    }

}