/**
 * This file Copyright (c) 2003-2012 Magnolia International
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

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.admininterface.DialogHandlerManager;
import info.magnolia.module.admininterface.DialogMVCHandler;
import info.magnolia.module.admininterface.SaveHandler;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * This dialog hander delegates to a dialog handler representing the dialog assigned to the paragraph we edit (or create).
 *
 * @author philipp
 */
public class ParagraphEditDialog extends ConfiguredDialog {

    private DialogMVCHandler dialogHandler;

    public ParagraphEditDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);

        // build the dialog handler we will delegate to
        String paragraphName = params.getParameter("mgnlParagraph");

        final String dialogName = getDialogUsedByTemplate(paragraphName);

        if (StringUtils.isEmpty(dialogName)) {
            dialogHandler = new NoDialogMVCHandler(request, response);
        } else {

            dialogHandler = DialogHandlerManager.getInstance().getDialogHandler(dialogName, request, response);

            // needed for the creation of new paragraphs
            dialogHandler.getDialog().setConfig("paragraph", paragraphName);
            dialogHandler.getDialog().setConfig("collectionNodeCreationItemType", ItemType.AREA.getSystemName());
            dialogHandler.getDialog().setConfig("creationItemType", ItemType.COMPONENT.getSystemName());
        }
    }

    private String getDialogUsedByTemplate(String templateId) {
        if (StringUtils.isEmpty(templateId)) {
            throw new IllegalStateException("No paragraph selected.");
        }
        TemplateDefinition templateDefinition;
        try {
            templateDefinition = Components.getComponent(TemplateDefinitionRegistry.class).getTemplateDefinition(templateId);
        } catch (RegistrationException e) {
            throw new IllegalStateException(e);
        }
        if (templateDefinition == null) {
            throw new IllegalStateException("No template registered with name " + templateId);
        }
        return templateDefinition.getDialog();
    }

    // methods delegating to the paragraph's dialog handler
    // this are the only methods called by the dialog mvc servlet

    @Override
    public String getCommand() {
        return dialogHandler.getCommand();
    }

    @Override
    public String execute(String command) {
        return dialogHandler.execute(command);
    }

    @Override
    public void renderHtml(String view) throws IOException {
        dialogHandler.renderHtml(view);
    }

    public static class NoDialogMVCHandler extends DialogMVCHandler {
        public NoDialogMVCHandler(HttpServletRequest request, HttpServletResponse response) {
            // we don't really need this call to super, we'll fake the form !
            super("auto-save", request, response);
            // at this stage, this.params is essentially a wrapper around the request parameters, and this.form is null.
        }

        @Override
        public String getCommand() {
            // bypass COMMAND_SHOW_DIALOG
            return DialogMVCHandler.COMMAND_SAVE;
        }

        @Override
        public String save() {
            // completely bypass the regular save() method - we have no validation and dialog, so we fake everything
            final SaveHandler saveHandler = Components.getComponentProvider().newInstance(SaveHandler.class);
            final MultipartForm dummyForm = new MultipartForm();
            dummyForm.addparameterValues("mgnlSaveInfo", new String[0]);
            saveHandler.init(dummyForm);

            // copied/adapted from info.magnolia.module.admininterface.DialogMVCHandler#DialogMVCHandler
            saveHandler.setPath(path); // protected variable init'd in constructor
            saveHandler.setNodeCollectionName(params.getParameter("mgnlNodeCollection")); // private, so we do this
            saveHandler.setNodeName(nodeName); // protected variable init'd in constructor
            saveHandler.setParagraph(params.getParameter("mgnlParagraph"));
            saveHandler.setRepository(repository); // protected variable init'd in constructor
            saveHandler.setCollectionNodeCreationItemType(ItemType.AREA);
            saveHandler.setCreationItemType(ItemType.COMPONENT);


            boolean result = saveHandler.save();
            return result ? DialogMVCHandler.VIEW_CLOSE_WINDOW : DialogMVCHandler.VIEW_ERROR;
        }

    }
}
