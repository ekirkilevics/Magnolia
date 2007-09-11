/**
 *
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