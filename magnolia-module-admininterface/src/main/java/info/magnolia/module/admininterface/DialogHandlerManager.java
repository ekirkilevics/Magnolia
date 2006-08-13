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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.config.ParagraphManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(DialogHandlerManager.class);

    /**
     * The current implementation of the DialogManager. Defeined in magnolia.properties.
     */
    private static DialogHandlerManager instance = (DialogHandlerManager) FactoryUtil
        .getSingleton(DialogHandlerManager.class);

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
            Content dialog = (Content) iter.next();
            // if this paragraph is used from a dialog register it under the name of the paragraph too
            registerAsParagraphDialog(node.getHandle(), dialog);

            String name = dialog.getNodeData(ND_NAME).getString();
            if(StringUtils.isEmpty(name)){
                name = dialog.getName();
            }
            String className = NodeDataUtil.getString(dialog, CLASS, PARAGRAPH_EDIT_DIALOG);
            try {
                // there are paragraphs dialogs without a name!
                registerDialogHandler(name, Class.forName(className), dialog);
            }
            catch (ClassNotFoundException e) {
                log.warn("can't find dialog handler class " + className, e); //$NON-NLS-1$
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
            String paragraphDialogPath = paragraph.getDialogPath();
            String paragraphDialogName = paragraph.getDialog();

            if (StringUtils.equals(paragraphDialogPath, dialogPath)
                || StringUtils.equals(paragraphDialogName, dialogName)) {
                Class handler = ParagraphEditDialog.class;

                String className = dialog.getNodeData(CLASS).getString();
                if (StringUtils.isNotEmpty(className)) {
                    try {
                        handler = Class.forName(className);
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
        if (log.isDebugEnabled()) {
            log.debug("Registering dialog handler [{}]", name); //$NON-NLS-1$ 
        }
        // remember the uuid for a reload
        dialogHandlers.put(name, new Object[]{dialogHandler, configNode});
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
        return instance;
    }

}
