/*
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.FactoryUtil;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public final class ControlsManager extends ObservedManager {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(ControlsManager.class);

    /**
     * The current implementation of the ParagraphManager. Defeined in magnolia.properties.
     */
    private static ControlsManager instance = (ControlsManager) FactoryUtil.getSingleton(ControlsManager.class);

    /**
     * Node data name for control class.
     */
    private static final String DATA_CONTROL_CLASS = "class"; //$NON-NLS-1$

    /**
     * Node data name for control name.
     */
    private static final String DATA_CONTROL_NAME = "name"; //$NON-NLS-1$

    /**
     * Registers dialog controls.
     */
    protected void onRegister(Content configNode) {
        log.info("Config : loading dialog controls configuration"); //$NON-NLS-1$

        Iterator iterator = configNode.getChildren(ItemType.CONTENTNODE).iterator();

        while (iterator.hasNext()) {
            Content controlNode = (Content) iterator.next();

            if (log.isDebugEnabled()) {
                log.debug("Initializing control [{}]", controlNode); //$NON-NLS-1$ //$NON-NLS-2$
            }

            String classNodeData = controlNode.getNodeData(DATA_CONTROL_CLASS).getString();
            String nameNodeData = controlNode.getNodeData(DATA_CONTROL_NAME).getString();

            if (StringUtils.isEmpty(nameNodeData)) {
                nameNodeData = controlNode.getName();
            }

            if (StringUtils.isEmpty(classNodeData) || StringUtils.isEmpty(nameNodeData)) {
                log.warn("Config : Can't add custom control with name [{}] and class [{}] specified in node [{}]", //$NON-NLS-1$
                    new Object[]{nameNodeData, classNodeData, controlNode.getName()});
                continue;
            }
            Class controlClass = null;

            try {
                controlClass = Class.forName(classNodeData);
            }
            catch (ClassNotFoundException e) {
                log.error("Config : Failed to load dialog control with class [" + classNodeData + "]", e); //$NON-NLS-1$
                continue;
            }

            if (!DialogControl.class.isAssignableFrom(controlClass)) {
                log.error("Config : Invalid class specified for control [{}]: does not implement DialogControl", //$NON-NLS-1$
                    nameNodeData);
                continue;
            }

            DialogFactory.registerDialog(nameNodeData, controlClass);

        }
    }

    /**
     * @return Returns the instance.
     */
    public static ControlsManager getInstance() {
        return instance;
    }

    protected void onClear() {
    }

}
