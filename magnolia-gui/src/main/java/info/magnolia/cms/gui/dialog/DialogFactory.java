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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Factory for dialogs. This class handles the registration of native dialogs mantaing a map of (control name | dialog
 * class).
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public final class DialogFactory {

    /**
     * Registered controls.
     */
    private static Map controls = new HashMap();

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DialogFactory.class);

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

        log.info("Registering control [{}]", name); //$NON-NLS-1$ 

        controls.put(name, dialogClass);
    }

    /**
     * Load and initialize a dialog.
     * @param websiteNode current website node
     * @param configNode configuration node for the dialog. The type of the dialog is read from the "controlType"
     * nodeData
     * @throws RepositoryException for errors during initialization of dialog with repository data
     */
    public static DialogInterface loadDialog(HttpServletRequest request, HttpServletResponse response,
        Content websiteNode, Content configNode) throws RepositoryException {
        String controlType = configNode.getNodeData("controlType").getString(); //$NON-NLS-1$

        Class dialogClass = (Class) controls.get(controlType);

        if (dialogClass == null) {
            throw new IllegalArgumentException("Unknown control type: \"" + controlType + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DialogInterface dialog;
        try {
            dialog = (DialogInterface) dialogClass.newInstance();
        }
        catch (InstantiationException e) {
            // should never happen
            throw new NestableRuntimeException("Unable to instantiate " //$NON-NLS-1$
                + dialogClass
                + " due to: InstantiationException - " //$NON-NLS-1$
                + e.getMessage());
        }
        catch (IllegalAccessException e) {
            // should never happen
            throw new NestableRuntimeException("Unable to instantiate " //$NON-NLS-1$
                + dialogClass
                + " due to: IllegalAccessException - " //$NON-NLS-1$
                + e.getMessage());
        }

        // initialize dialog
        if (log.isDebugEnabled()) {
            log.debug("Calling init on " + dialogClass.getName()); //$NON-NLS-1$
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

    public static DialogSelect getDialogSelectInstance(HttpServletRequest request, HttpServletResponse response,
        Content websiteNode, Content configNode) throws RepositoryException {
        DialogSelect dialog = new DialogSelect();
        dialog.init(request, response, websiteNode, configNode);
        return dialog;
    }

    /**
     * Get a instance by the control type name. Those name class mappings are configured in the admin interface
     * configuration.
     * @param request
     * @param response
     * @param websiteNode the node holding the data (can be null)
     * @param configNode the node holding the configuration (can be null)
     * @param controlType the name of the control
     * @return the conrol
     * @throws RepositoryException
     */
    public static DialogInterface getDialogControlInstanceByName(HttpServletRequest request,
        HttpServletResponse response, Content websiteNode, Content configNode, String controlType)
        throws RepositoryException {

        Class dialogClass = (Class) controls.get(controlType);

        if (dialogClass == null) {
            throw new IllegalArgumentException("Unknown dialog type: \"" + controlType + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DialogInterface dialog = null;
        try {
            dialog = (DialogInterface) dialogClass.newInstance();
        }
        catch (Exception e) {
            // should never happen
            throw new NestableRuntimeException("Unable to instantiate " //$NON-NLS-1$
                + dialogClass
                + " due to: InstantiationException - " //$NON-NLS-1$
                + e.getMessage());
        }

        dialog.init(request, response, websiteNode, configNode);
        return dialog;
    }
}
