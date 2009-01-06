/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.admininterface.DialogMVCHandler;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 */
public class ConfiguredDialog extends DialogMVCHandler {

    private static Logger log = LoggerFactory.getLogger(ConfiguredDialog.class);

    private Content configNode;

    public ConfiguredDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response);
        this.configNode = configNode;

        // TODO content2bean should be used
        this.setItemType(NodeDataUtil.getString(configNode, "itemType", this.getItemType()));
        this.setJsExecutedAfterSaving(NodeDataUtil.getString(configNode, "jsExecutedAfterSaving", this.getJsExecutedAfterSaving()));
    }

    /**
     * Returns the node with the dialog definition.
     * @return
     */
    public Content getConfigNode() {
        return configNode;
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
        String className = null;
        try {
            Class handlerClass = defaultClass;
            try {
                className = configNode.getNodeData("class").getString(); //$NON-NLS-1$
                if (StringUtils.isNotEmpty(className)) {
                    handlerClass = ClassUtil.classForName(className);
                }
            }
            catch (Exception e) {
                log.error(MessageFormat.format("Unable to load class {0}", new Object[]{className})); //$NON-NLS-1$
            }
            Constructor constructor = handlerClass.getConstructor(new Class[]{
                String.class,
                HttpServletRequest.class,
                HttpServletResponse.class,
                Content.class});
            return (ConfiguredDialog) constructor.newInstance(new Object[]{name, request, response, configNode});
        }
        catch (Exception e) {
            log.error("can't instantiate dialog: ", e); //$NON-NLS-1$
            return null;
        }
    }

}
