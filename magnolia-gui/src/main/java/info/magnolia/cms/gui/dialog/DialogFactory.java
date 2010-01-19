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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.objectfactory.ClassFactory;
import info.magnolia.objectfactory.Classes;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


/**
 * Factory for dialogs. This class handles the registration of native dialogs mantaing a map of (control name | dialog
 * class).
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public final class DialogFactory {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DialogFactory.class);


    /**
     * Registered controls.
     */
    private static Map<String, Class<DialogControl>> controls = new HashMap<String, Class<DialogControl>>();

    /**
     * Utility class, don't instantiate.
     */
    private DialogFactory() {
        // unused
    }

    /**
     * Register a new dialog.
     * @param name dialog name (e.g. "richEdit")
     * @param dialogClass implementing class. Must implements <code>info.magnolia.cms.gui.dialog.DialogControl</code>
     * @see info.magnolia.cms.gui.dialog.DialogControl
     */
    public static void registerDialog(String name, Class<DialogControl> dialogClass) {
        // @todo synchronize

        log.debug("Registering control [{}]", name); //$NON-NLS-1$

        controls.put(name, dialogClass);
    }

    /**
     * Load and initialize a dialog.
     * @param storageNode current website node
     * @param configNode configuration node for the dialog. The type of the dialog is read from the "controlType"
     * nodeData
     * @throws RepositoryException for errors during initialization of dialog with repository data
     */
    public static DialogControl loadDialog(HttpServletRequest request, HttpServletResponse response,
                                           Content storageNode, Content configNode) throws RepositoryException {
        String controlType = configNode.getNodeData("controlType").getString(); //$NON-NLS-1$

        return getDialogControlInstanceByName(request, response, storageNode, configNode, controlType);
    }

    public static Dialog getDialogInstance(HttpServletRequest request, HttpServletResponse response,
                                           Content storageNode, Content configNode) throws RepositoryException {
        Dialog dialog = new Dialog();
        dialog.init(request, response, storageNode, configNode);
        return dialog;
    }

    public static DialogStatic getDialogStaticInstance(HttpServletRequest request, HttpServletResponse response,
                                                       Content storageNode, Content configNode) throws RepositoryException {
        DialogStatic dialog = new DialogStatic();
        dialog.init(request, response, storageNode, configNode);
        return dialog;
    }

    public static DialogHidden getDialogHiddenInstance(HttpServletRequest request, HttpServletResponse response,
                                                       Content storageNode, Content configNode) throws RepositoryException {
        DialogHidden dialog = new DialogHidden();
        dialog.init(request, response, storageNode, configNode);
        return dialog;
    }

    public static DialogEdit getDialogEditInstance(HttpServletRequest request, HttpServletResponse response,
                                                   Content storageNode, Content configNode) throws RepositoryException {
        DialogEdit dialog = new DialogEdit();
        dialog.init(request, response, storageNode, configNode);
        return dialog;
    }

    public static DialogButton getDialogButtonInstance(HttpServletRequest request, HttpServletResponse response,
                                                       Content storageNode, Content configNode) throws RepositoryException {
        DialogButton dialog = new DialogButton();
        dialog.init(request, response, storageNode, configNode);
        return dialog;
    }

    public static DialogPassword getDialogPasswordInstance(HttpServletRequest request, HttpServletResponse response,
                                                           Content storageNode, Content configNode) throws RepositoryException {
        DialogPassword dialog = new DialogPassword();
        dialog.init(request, response, storageNode, configNode);
        return dialog;
    }

    public static DialogButtonSet getDialogButtonSetInstance(HttpServletRequest request, HttpServletResponse response,
                                                             Content storageNode, Content configNode) throws RepositoryException {
        DialogButtonSet dialog = new DialogButtonSet();
        dialog.init(request, response, storageNode, configNode);
        return dialog;
    }

    public static DialogInclude getDialogIncludeInstance(HttpServletRequest request, HttpServletResponse response,
                                                         Content storageNode, Content configNode) throws RepositoryException {
        DialogInclude dialog = new DialogInclude();
        dialog.init(request, response, storageNode, configNode);
        return dialog;
    }

    public static DialogSelect getDialogSelectInstance(HttpServletRequest request, HttpServletResponse response,
                                                       Content storageNode, Content configNode) throws RepositoryException {
        DialogSelect dialog = new DialogSelect();
        dialog.init(request, response, storageNode, configNode);
        return dialog;
    }

    /**
     * Get a instance by the control type name. Those name class mappings are configured in the admin interface
     * configuration.
     * @param request
     * @param response
     * @param storageNode the node holding the data (can be null)
     * @param configNode the node holding the configuration (can be null)
     * @param controlType the name of the control
     * @return the conrol
     * @throws RepositoryException
     */
    public static DialogControl getDialogControlInstanceByName(HttpServletRequest request, HttpServletResponse response,
                                                               Content storageNode, Content configNode, String controlType) throws RepositoryException {

        final ClassFactory classFactory = Classes.getClassFactory();
        Class<DialogControl> dialogClass = controls.get(controlType);

        if (dialogClass == null) {
            try {
                dialogClass = classFactory.forName(controlType);
            }
            catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Unknown control type: \"" + controlType + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        // classFactory.newInstance wraps instantation exception in MgnlInstantiationException, which is a runtime, so no need to catch/wrap here again.
        final DialogControl control = classFactory.newInstance(dialogClass);
        control.init(request, response, storageNode, configNode);
        return control;
    }
}
