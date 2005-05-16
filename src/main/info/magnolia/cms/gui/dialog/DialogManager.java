/*
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;

import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Revision: $ ($Author: $)
 */
public class DialogManager {

    /**
     * Logger.
     */
    protected static Logger log = Logger.getLogger(DialogManager.class);

    /**
     * Config node name: "controls".
     */
    private static String DIALOGCONTROLS_CONFIG_NAME = "controls";

    /**
     * "/modules/templating".
     */
    private static String MODULE_TEMPLATING_NODE_NAME = "/modules/templating";

    /**
     * Node data name for control class.
     */
    private static String DATA_CONTROL_CLASS = "class";

    /**
     * Node data name for control name.
     */
    private static String DATA_CONTROL_NAME = "name";

    /**
     * Called through the initialization precess
     */
    public static void init() {
        log.info("Config : loading dialog controls configuration");

        // reading the configuration from the repository
        HierarchyManager configHierarchyManager = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);

        try {
            Content serverNode = configHierarchyManager.getContent(MODULE_TEMPLATING_NODE_NAME);

            Content configNode;
            try {
                configNode = serverNode.getContent(DIALOGCONTROLS_CONFIG_NAME);
            }
            catch (javax.jcr.PathNotFoundException e) {

                log.info("Initialize default configuration for dialog controls");

                configNode = serverNode.createContent(DIALOGCONTROLS_CONFIG_NAME, ItemType.CONTENT);

                ValueFactory valueFactory = configHierarchyManager.getWorkspace().getSession().getValueFactory();

                // sample fckedit dialog
                Content fckedit = configNode.createContent("fckEdit", ItemType.CONTENTNODE);
                fckedit.createNodeData(DATA_CONTROL_NAME, valueFactory.createValue("fckEdit"));
                fckedit.createNodeData(DATA_CONTROL_CLASS, valueFactory.createValue(DialogFckEdit.class.getName()));
                configHierarchyManager.save();
            }

            if (configNode == null) {
                return;
            }

            Iterator iterator = configNode.getChildren(ItemType.CONTENTNODE, Content.SORT_BY_SEQUENCE).iterator();

            while (iterator.hasNext()) {
                Content controlNode = (Content) iterator.next();

                if (log.isDebugEnabled()) {
                    log.debug("Initializing control [" + controlNode + "]");
                }

                String classNodeData = controlNode.getNodeData(DATA_CONTROL_CLASS).getString();
                String nameNodeData = controlNode.getNodeData(DATA_CONTROL_NAME).getString();

                if (StringUtils.isEmpty(classNodeData) || StringUtils.isEmpty(nameNodeData)) {
                    log.warn("Config : Can't add custom control with name ["
                        + nameNodeData
                        + "] and class ["
                        + classNodeData
                        + "] specified in node ["
                        + controlNode.getName()
                        + "]");

                    continue;
                }
                Class controlClass = null;

                try {
                    controlClass = Class.forName(classNodeData);
                }
                catch (ClassNotFoundException e) {
                    log.error("Config : Failed to load dialog control with class [" + classNodeData, e);
                    continue;
                }

                if (!DialogInterface.class.isAssignableFrom(controlClass)) {
                    log.error("Config : Invalid class specified for control ["
                        + nameNodeData
                        + "]: does not implement DialogInterface");
                    continue;
                }

                DialogFactory.registerDialog(nameNodeData, controlClass);

            }

        }
        catch (RepositoryException e) {
            log.error("Config : Failed to load dialog controls configuration - "
                + MODULE_TEMPLATING_NODE_NAME
                + "/"
                + DIALOGCONTROLS_CONFIG_NAME, e);
        }

    }
}
