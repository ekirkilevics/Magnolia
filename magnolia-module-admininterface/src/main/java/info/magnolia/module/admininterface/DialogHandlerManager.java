/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.config.ParagraphManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.SystemContentWrapper;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.admininterface.dialogs.ParagraphEditDialog;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * Manages all the dialog handlers
 * @todo The paragraph dialogs should not differ from the other ones.
 * @author philipp
 */
public class DialogHandlerManager extends ObservedManager {

    private static final String PARAGRAPH_EDIT_DIALOG = "info.magnolia.module.admininterface.dialogs.ParagraphEditDialog";

    private static final String CLASS = "class";

    private static final String ND_NAME = "name";

    /**
     * All handlers are registered here
     */
    private final Map dialogHandlers = new HashMap();

    /**
     * register the dialogs from the config
     * @param node
     */
    protected void onRegister(Content node) {
        List dialogs = ContentUtil.collectAllChildren(node, ItemType.CONTENT);
        for (Iterator iter = dialogs.iterator(); iter.hasNext();) {
            Content dialog = new SystemContentWrapper((Content) iter.next());
            // if this paragraph is used from a dialog register it under the name of the paragraph too
            registerAsParagraphDialog(node.getHandle(), dialog);

            String name = dialog.getNodeData(ND_NAME).getString();
            if (StringUtils.isEmpty(name)) {
                name = dialog.getName();
            }
            String className = NodeDataUtil.getString(dialog, CLASS, PARAGRAPH_EDIT_DIALOG);
            try {
                // there are paragraphs dialogs without a name!
                registerDialogHandler(name, ClassUtil.classForName(className), dialog);
            }
            catch (ClassNotFoundException e) {
                log.warn("Can't find dialog handler class " + className, e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Check if this dialog is used by a paragraph. If so register it under the paragraphs name.
     * @param dialog
     */
    private void registerAsParagraphDialog(String basePath, Content dialog) {

        String dialogPath = StringUtils.removeStart(dialog.getHandle(), basePath + "/");
        String dialogName = dialog.getNodeData(ND_NAME).getString();
        if (StringUtils.isEmpty(dialogName)) {
            dialogName = dialog.getName();
        }

        Map paragraphs = ParagraphManager.getInstance().getParagraphs();
        for (Iterator iter = paragraphs.entrySet().iterator(); iter.hasNext();) {
            Paragraph paragraph = (Paragraph) ((Entry) iter.next()).getValue();
            String paragraphDialogName = paragraph.getDialog();

            if (StringUtils.equals(paragraphDialogName, dialogName)) {
                Class handler = ParagraphEditDialog.class;

                String className = dialog.getNodeData(CLASS).getString();
                if (StringUtils.isNotEmpty(className)) {
                    try {
                        handler = ClassUtil.classForName(className);
                    }
                    catch (ClassNotFoundException e) {
                        log.error("Registering paragraph: class [" + className + "] not found", e); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }

                registerDialogHandler(paragraph.getName(), handler, dialog);
            }
        }

    }

    protected void onClear() {
        this.dialogHandlers.clear();
    }

    protected void registerDialogHandler(String name, Class dialogHandler, Content configNode) {
        log.debug("Registering dialog handler [{}]", name); //$NON-NLS-1$

        // remember the uuid for a reload
        dialogHandlers.put(name, new Object[]{dialogHandler, configNode});
    }

    public DialogMVCHandler getDialogHandler(String name, HttpServletRequest request, HttpServletResponse response) {

        Object[] handlerConfig = (Object[]) dialogHandlers.get(name);

        // fix for MAGNOLIA-1394 : try to see if the given name is a paragraph name, then use that paragraph's dialog instead.
        if (handlerConfig == null) {
            final Paragraph para = ParagraphManager.getInstance().getInfo(name);
            // it's not mandatory that a paragraph references this dialog :
            if (para != null) {
                final String dialogName = para.getDialog();
                handlerConfig = (Object[]) dialogHandlers.get(dialogName);
            }
        }

        if (handlerConfig == null) {
            throw new InvalidDialogHandlerException(name);
        }

        return instantiateHandler(name, request, response, handlerConfig);

    }

    protected DialogMVCHandler instantiateHandler(String name, HttpServletRequest request,
        HttpServletResponse response, Object[] handlerConfig) {

        try {

            Class dialogHandlerClass = (Class) handlerConfig[0];
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
