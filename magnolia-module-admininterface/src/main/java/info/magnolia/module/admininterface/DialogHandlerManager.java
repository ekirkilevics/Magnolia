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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.SystemContentWrapper;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * Manages all the dialog handlers.
 *
 * @author philipp
 */
public class DialogHandlerManager extends ObservedManager {

    private static final String CLASS = "class";

    private static final String ND_NAME = "name";

    /**
     * All handlers are registered here.
     */
    private final Map dialogHandlers = new HashMap();

    /**
     * register the dialogs from the config.
     */
    protected void onRegister(Content node) {
        List dialogs = ContentUtil.collectAllChildren(node, ItemType.CONTENT);
        for (Iterator iter = dialogs.iterator(); iter.hasNext();) {
            Content dialog = new SystemContentWrapper((Content) iter.next());

            String name = dialog.getNodeData(ND_NAME).getString();
            if (StringUtils.isEmpty(name)) {
                name = dialog.getName();
            }
            String className = NodeDataUtil.getString(dialog, CLASS);

            try {
                // dialog class is not mandatory
                Class dialogClass;
                if (StringUtils.isNotEmpty(className)) {
                    dialogClass = ClassUtil.classForName(className);
                } else {
                    dialogClass = null;
                }
                registerDialogHandler(name, dialogClass, dialog);
            }
            catch (ClassNotFoundException e) {
                log.warn("Can't find dialog handler class " + className, e); //$NON-NLS-1$
            }
        }
    }

    protected void onClear() {
        this.dialogHandlers.clear();
    }

    protected void registerDialogHandler(String name, Class dialogHandler, Content configNode) {
        log.debug("Registering dialog handler [{}] from {}", name, configNode.getHandle()); //$NON-NLS-1$

        // remember the uuid for a reload
        dialogHandlers.put(name, new Object[]{dialogHandler, configNode});
    }

    public Content getDialogConfigNode(String dialogName) {
        final Object[] handlerConfig = (Object[]) dialogHandlers.get(dialogName);
        if (handlerConfig == null) {
            throw new InvalidDialogHandlerException(dialogName);
        }
        return (Content) handlerConfig[1];
    }

    public DialogMVCHandler getDialogHandler(String name, HttpServletRequest request, HttpServletResponse response) {

        Object[] handlerConfig = (Object[]) dialogHandlers.get(name);

        if (handlerConfig == null) {
            throw new InvalidDialogHandlerException(name);
        }

        return instantiateHandler(name, request, response, handlerConfig);
    }

    protected DialogMVCHandler instantiateHandler(String name, HttpServletRequest request,
        HttpServletResponse response, Object[] handlerConfig) {

        try {
            Class dialogHandlerClass = (Class) handlerConfig[0];
            if (dialogHandlerClass == null) {
                dialogHandlerClass = ConfiguredDialog.class;
            }
            Content configNode = (Content) handlerConfig[1];
            if (configNode != null) {
                try {
                    Constructor constructor = dialogHandlerClass.getConstructor(new Class[]{
                        String.class,
                        HttpServletRequest.class,
                        HttpServletResponse.class,
                        Content.class});
                    return (DialogMVCHandler) constructor
                        .newInstance(new Object[]{name, request, response, configNode});
                }
                catch (NoSuchMethodException e) {
                    Constructor constructor = dialogHandlerClass.getConstructor(new Class[]{
                        String.class,
                        HttpServletRequest.class,
                        HttpServletResponse.class});
                    return (DialogMVCHandler) constructor.newInstance(new Object[]{name, request, response});
                }
            }

            Constructor constructor = dialogHandlerClass.getConstructor(new Class[]{
                String.class,
                HttpServletRequest.class,
                HttpServletResponse.class});
            return (DialogMVCHandler) constructor.newInstance(new Object[]{name, request, response});
        }
        catch (Exception e) {
            throw new InvalidDialogHandlerException(name, e);
        }
    }

    /**
     * @return Returns the instance.
     */
    public static DialogHandlerManager getInstance() {
        return (DialogHandlerManager) FactoryUtil.getSingleton(DialogHandlerManager.class);
    }

}
