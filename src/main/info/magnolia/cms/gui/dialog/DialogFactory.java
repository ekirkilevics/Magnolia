/**
 *
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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;


/**
 * Factory for dialogs. This class handles the registration of native dialogs mantaing a map of (control name | dialog
 * class).
 * @author Fabrizio Giustina
 * @version $Revision: 364 $ ($Author: fgiust $)
 */
public final class DialogFactory {

    /**
     * Registered dialogs.
     */
    private static Map dialogs = new HashMap();

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogFactory.class);

    static {
        // register magnolia default dialogs
        registerDialog("edit", DialogEdit.class);
        registerDialog("richEdit", DialogRichedit.class);
        registerDialog("fckEdit", DialogFckEdit.class);
        registerDialog("tab", DialogTab.class);
        registerDialog("buttonSet", DialogButtonSet.class);
        registerDialog("button", DialogButton.class);
        registerDialog("static", DialogStatic.class);
        registerDialog("file", DialogFile.class);
        registerDialog("link", DialogLink.class);
        registerDialog("date", DialogDate.class);
        registerDialog("radio", DialogButtonSet.class);
        registerDialog("checkbox", DialogButtonSet.class);
        registerDialog("checkboxSwitch", DialogButtonSet.class);
        registerDialog("select", DialogSelect.class);
        registerDialog("password", DialogPassword.class);
        registerDialog("include", DialogInclude.class);
        registerDialog("webDAV", DialogWebDAV.class);
    }

    /**
     * Utility class, don't instantiate.
     */
    private DialogFactory() {
        // unused
    }

    /**
     * Register a new dialog.
     * @param name dialog name (e.g. "richEdit")
     * @param dialogClass implementing class. Must implements <code>info.magnolia.cms.gui.dialog.DialogInterface</code>
     * @see info.magnolia.cms.gui.dialog.DialogInterface
     */
    public static void registerDialog(String name, Class dialogClass) {
        // @todo check if dialogClass is a valid dialog
        // @todo synchronize
        dialogs.put(name, dialogClass);
    }

    /**
     * Load and initialize a dialog.
     * @param websiteNode current website node
     * @param configNode configuration node for the dialog. The type of the dialog is read from the "controlType"
     * nodeData
     * @param pageContext jsp page context
     * @throws RepositoryException for errors during initialization of dialog with repository data
     */
    public static DialogInterface loadDialog(HttpServletRequest request, HttpServletResponse response,
        Content websiteNode, ContentNode configNode) throws RepositoryException {
        String controlType = configNode.getNodeData("controlType").getString();

        Class dialogClass = (Class) dialogs.get(controlType);

        if (dialogClass == null) {
            throw new IllegalArgumentException("Unknown dialog type: \"" + controlType + "\"");
        }

        DialogInterface dialog;
        try {
            dialog = (DialogInterface) dialogClass.newInstance();
        }
        catch (InstantiationException e) {
            // should never happen
            throw new NestableRuntimeException("Unable to instantiate "
                + dialogClass
                + " due to: InstantiationException - "
                + e.getMessage());
        }
        catch (IllegalAccessException e) {
            // should never happen
            throw new NestableRuntimeException("Unable to instantiate "
                + dialogClass
                + " due to: IllegalAccessException - "
                + e.getMessage());
        }

        // initialize dialog
        if (log.isDebugEnabled()) {
            log.debug("Calling init on " + dialogClass.getName());
        }
        dialog.init(request, response, websiteNode, configNode);
        return dialog;
    }

    public static DialogDialog getDialogDialogInstance(HttpServletRequest request, HttpServletResponse response,
        Content websiteNode, Content configNode) throws RepositoryException {
        DialogDialog dialog = new DialogDialog();
        dialog.init(request, response, websiteNode, configNode);
        return dialog;
    }

    public static DialogStatic getDialogStaticInstance(HttpServletRequest request, HttpServletResponse response,
        Content websiteNode, Content configNode) throws RepositoryException {
        DialogStatic dialog = new DialogStatic();
        dialog.init(request, response, websiteNode, configNode);
        return dialog;
    }

    public static DialogHidden getDialogHiddenInstance(HttpServletRequest request, HttpServletResponse response,
        Content websiteNode, Content configNode) throws RepositoryException {
        DialogHidden dialog = new DialogHidden();
        dialog.init(request, response, websiteNode, configNode);
        return dialog;
    }

    public static DialogEdit getDialogEditInstance(HttpServletRequest request, HttpServletResponse response,
        Content websiteNode, Content configNode) throws RepositoryException {
        DialogEdit dialog = new DialogEdit();
        dialog.init(request, response, websiteNode, configNode);
        return dialog;
    }

    public static DialogButton getDialogButtonInstance(HttpServletRequest request, HttpServletResponse response,
        Content websiteNode, Content configNode) throws RepositoryException {
        DialogButton dialog = new DialogButton();
        dialog.init(request, response, websiteNode, configNode);
        return dialog;
    }

    public static DialogPassword getDialogPasswordInstance(HttpServletRequest request, HttpServletResponse response,
        Content websiteNode, Content configNode) throws RepositoryException {
        DialogPassword dialog = new DialogPassword();
        dialog.init(request, response, websiteNode, configNode);
        return dialog;
    }

    public static DialogButtonSet getDialogButtonSetInstance(HttpServletRequest request, HttpServletResponse response,
        Content websiteNode, Content configNode) throws RepositoryException {
        DialogButtonSet dialog = new DialogButtonSet();
        dialog.init(request, response, websiteNode, configNode);
        return dialog;
    }

    public static DialogInclude getDialogIncludeInstance(HttpServletRequest request, HttpServletResponse response,
        Content websiteNode, Content configNode) throws RepositoryException {
        DialogInclude dialog = new DialogInclude();
        dialog.init(request, response, websiteNode, configNode);
        return dialog;
    }

}
