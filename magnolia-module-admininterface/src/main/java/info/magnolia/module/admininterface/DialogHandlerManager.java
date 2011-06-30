/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.Components;
import info.magnolia.cms.util.ExtendingContentWrapper;
import info.magnolia.cms.util.SystemContentWrapper;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;


/**
 * Manages all the dialog handlers.
 *
 * @author philipp
 */
@Singleton
public class DialogHandlerManager extends ObservedManager {

    private static final String CLASS = "class";

    private static final String ND_NAME = "name";

    /**
     * All handlers are registered here.
     */
    private final Map<String, Object[]> dialogHandlers = new HashMap<String, Object[]>();

    /**
     * register the dialogs from the config.
     */
    @Override
    protected void onRegister(Content node) {
        final List<Content> dialogNodes = new ArrayList<Content>();
        try {
            collectDialogNodes(node, dialogNodes);
        }
        catch (RepositoryException e) {
            log.error("Can't collect dialog nodes for [" + node.getHandle() + "]: " + ExceptionUtils.getMessage(e), e);
            throw new IllegalStateException("Can't collect dialog nodes for [" + node.getHandle() + "]: " + ExceptionUtils.getMessage(e));
        }

        for (Iterator<Content> iter = dialogNodes.iterator(); iter.hasNext();) {
            Content dialogNode = new ExtendingContentWrapper(new SystemContentWrapper(iter.next()));
            try {
                if (dialogNode.getItemType().equals(ItemType.CONTENT)) {
                    log.warn("Dialog definitions should be of type contentNode but [" + dialogNode.getHandle() + "] is of type content.");
                }
            }
            catch (RepositoryException e) {
                log.error("Can't check for node type of the dialog node [" + dialogNode.getHandle() + "]: " + ExceptionUtils.getMessage(e), e);
                throw new IllegalStateException("Can't check for node type of the dialog node ["
                    + dialogNode.getHandle()
                    + "]: "
                    + ExceptionUtils.getMessage(e));
            }
            String name = dialogNode.getNodeData(ND_NAME).getString();
            if (StringUtils.isEmpty(name)) {
                name = dialogNode.getName();
            }
            String className = NodeDataUtil.getString(dialogNode, CLASS);

            try {
                // dialog class is not mandatory
                Class< ? extends DialogMVCHandler> dialogClass;
                if (StringUtils.isNotEmpty(className)) {
                    dialogClass = Classes.getClassFactory().forName(className);
                }
                else {
                    dialogClass = null;
                }
                registerDialogHandler(name, dialogClass, dialogNode);
            }
            catch (ClassNotFoundException e) {
                log.warn("Can't find dialog handler class " + className, e); //$NON-NLS-1$
            }
        }
    }

    @Override
    protected void onClear() {
        this.dialogHandlers.clear();
    }

    protected void registerDialogHandler(String name, Class< ? extends DialogMVCHandler> dialogHandler, Content configNode) {
        log.debug("Registering dialog handler [{}] from {}", name, configNode.getHandle()); //$NON-NLS-1$

        // remember the uuid for a reload
        dialogHandlers.put(name, new Object[]{dialogHandler, configNode});
    }

    /**
     * @deprecated since 4.3.2, is obsolete since fix for MAGNOLIA-2907
     */
    public Content getDialogConfigNode(String dialogName) {
        final Object[] handlerConfig = dialogHandlers.get(dialogName);
        if (handlerConfig == null) {
            throw new InvalidDialogHandlerException(dialogName);
        }
        return (Content) handlerConfig[1];
    }

    public DialogMVCHandler getDialogHandler(String name, HttpServletRequest request, HttpServletResponse response) {

        Object[] handlerConfig = dialogHandlers.get(name);

        if (handlerConfig == null) {
            throw new InvalidDialogHandlerException(name);
        }

        return instantiateHandler(name, request, response, handlerConfig);
    }

    /**
     * Caution: use this method with care, as it creates an Dialog instance having ServletRequest
     * and -Response as well as StorageNode being null.
     */
    public Dialog getDialog(String dialogName) throws RepositoryException {
        return DialogFactory.getDialogInstance(null, null, null, getDialogConfigNode(dialogName));
    }

    protected DialogMVCHandler instantiateHandler(String name, HttpServletRequest request,
        HttpServletResponse response, Object[] handlerConfig) {

        try {
            Class< ? extends DialogMVCHandler> dialogHandlerClass = (Class< ? extends DialogMVCHandler>) handlerConfig[0];
            if (dialogHandlerClass == null) {
                dialogHandlerClass = ConfiguredDialog.class;
            }
            Content configNode = (Content) handlerConfig[1];
            if (configNode != null) {
                try {
                    Constructor< ? extends DialogMVCHandler> constructor = dialogHandlerClass.getConstructor(new Class[]{
                        String.class,
                        HttpServletRequest.class,
                        HttpServletResponse.class,
                        Content.class});
                    return constructor.newInstance(name, request, response, configNode);
                }
                catch (NoSuchMethodException e) {
                    Constructor< ? extends DialogMVCHandler> constructor = dialogHandlerClass.getConstructor(new Class[]{
                        String.class,
                        HttpServletRequest.class,
                        HttpServletResponse.class});
                    return constructor.newInstance(name, request, response);
                }
            }

            Constructor< ? extends DialogMVCHandler> constructor = dialogHandlerClass.getConstructor(new Class[]{
                String.class,
                HttpServletRequest.class,
                HttpServletResponse.class});
            return constructor.newInstance(name, request, response);
        }
        catch (Exception e) {
            throw new InvalidDialogHandlerException(name, e);
        }
    }

    protected void collectDialogNodes(Content current, List<Content> dialogNodes) throws RepositoryException {
        if (isDialogNode(current)) {
            dialogNodes.add(current);
            return;
        }
        for (Content child : ContentUtil.getAllChildren(current)) {
            collectDialogNodes(child, dialogNodes);
        }
    }

    protected boolean isDialogNode(Content node) throws RepositoryException {
        if (isDialogControlNode(node)) {
            return false;
        }

        // if leaf
        if (ContentUtil.getAllChildren(node).isEmpty()) {
            return true;
        }

        // if has node datas
        if (!node.getNodeDataCollection().isEmpty()) {
            return true;
        }

        // if one subnode is a control
        for (Content child : node.getChildren(ItemType.CONTENTNODE)) {
            if (isDialogControlNode(child)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isDialogControlNode(Content node) throws RepositoryException {
        return node.hasNodeData("controlType") || node.hasNodeData("reference");
    }

    /**
     * @return Returns the instance.
     */
    public static DialogHandlerManager getInstance() {
        return Components.getSingleton(DialogHandlerManager.class);
    }

}
