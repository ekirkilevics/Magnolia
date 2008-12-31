/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.FactoryUtil;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public final class ControlsManager extends ObservedManager {

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
        log.info("Loading dialog controls configuration from {}", configNode.getHandle()); //$NON-NLS-1$

        Iterator iterator = configNode.getChildren(ItemType.CONTENTNODE).iterator();

        while (iterator.hasNext()) {
            Content controlNode = (Content) iterator.next();

            log.debug("Initializing control [{}]", controlNode); //$NON-NLS-1$

            String classNodeData = controlNode.getNodeData(DATA_CONTROL_CLASS).getString();
            String nameNodeData = controlNode.getNodeData(DATA_CONTROL_NAME).getString();

            if (StringUtils.isEmpty(nameNodeData)) {
                nameNodeData = controlNode.getName();
            }

            if (StringUtils.isEmpty(classNodeData) || StringUtils.isEmpty(nameNodeData)) {
                log.warn("Can't add custom control with name [{}] and class [{}] specified in node [{}]", new Object[]{nameNodeData, classNodeData, controlNode.getName()});
                continue;
            }
            Class controlClass = null;

            try {
                controlClass = ClassUtil.classForName(classNodeData);
            }
            catch (ClassNotFoundException e) {
                log.error("Failed to load dialog control with class [" + classNodeData + "]", e); //$NON-NLS-1$
                continue;
            }

            if (!DialogControl.class.isAssignableFrom(controlClass)) {
                log.error("Invalid class specified for control [{}]: does not implement DialogControl", nameNodeData);
                continue;
            }

            DialogFactory.registerDialog(nameNodeData, controlClass);

        }
    }

    /**
     * @return Returns the instance.
     */
    public static ControlsManager getInstance() {
        return (ControlsManager) FactoryUtil.getSingleton(ControlsManager.class);
    }

    protected void onClear() {
    }

}
