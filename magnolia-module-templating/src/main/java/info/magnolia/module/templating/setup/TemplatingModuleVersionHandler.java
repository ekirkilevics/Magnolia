/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.templating.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.BootstrapSingleResourceAndOrderBefore;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.OrderNodeBeforeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.templating.setup.for3_5.IntroduceParagraphRenderers;
import info.magnolia.module.templating.setup.for4_0.DeprecateDialogPathAllModules;
import info.magnolia.module.templating.setup.for4_0.FixTemplatePathTask;
import info.magnolia.module.templating.setup.for4_0.RenamePropertyAllModulesNodeTask;
import info.magnolia.module.templating.setup.for4_0.NestPropertiesAllModulesNodeTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Module's version handler.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class TemplatingModuleVersionHandler extends DefaultModuleVersionHandler {

    private OrderNodeBeforeTask orderBackwardCompatibilityFilter = new OrderNodeBeforeTask("Move backward compatibility filter", "", ContentRepository.CONFIG, "/server/filters/cms/backwardCompatibility", "rendering");

    public TemplatingModuleVersionHandler() {
        register(DeltaBuilder.update("3.5", "")
                .addTask(new IntroduceParagraphRenderers())
                .addTask(new BootstrapSingleResourceAndOrderBefore(
                "Compatibility Filter",
                "${actpage} is deprecated. Adds a compatibility filter still supporting it but writing warn messages to the log.",
                "/mgnl-bootstrap/templating/config.server.filters.cms.backwardCompatibility.xml",
                "rendering"))
        );

        // move the filter in case it is in the wrong place
        // this happened in case of a fresh install of former version
        register(DeltaBuilder.update("3.5.4", "")
                .addTask(orderBackwardCompatibilityFilter)
        );

        register(DeltaBuilder.update("4.0", "")
                .addTask(new BootstrapSingleResource("Freemarker Template Renderer", "Adds Freemarker template renderer configuration.", "/mgnl-bootstrap/templating/config.modules.templating.template-renderers.freemarker.xml"))
                .addTask(new CheckAndModifyPropertyValueTask("Rendering filter", "The rendering filter is now part of the templating module.", ContentRepository.CONFIG, "/server/filters/cms/rendering", "class", "info.magnolia.cms.filters.RenderingFilter", "info.magnolia.module.templating.RenderingFilter"))
                .addTask(new BootstrapSingleResource("Freemarker Model for RenderableDefinition", "Plugs in a specific Freemarker model for RenderableDefinition implementations.", "/mgnl-bootstrap/templating/config.server.rendering.freemarker.modelFactories.renderable.xml"))
                .addTask(new RenamePropertyAllModulesNodeTask("Templates configuration", "Property path is now templatePath.", "templates", "path", "templatePath"))
                .addTask(new NestPropertiesAllModulesNodeTask("Templates configuration", "Property path is now templatePath.", "templates",
                        Arrays.asList(new String[]{"name", "type", "templatePath", "title", "description", "i18nBasename", "visible", "class"}), "parameters", ItemType.CONTENTNODE.getSystemName()))
                .addTask(new RenamePropertyAllModulesNodeTask("Paragraphs configuration", "Property templateType is now type.", "paragraphs", "templateType", "type"))
                .addTask(new DeprecateDialogPathAllModules("Paragraphs configuration", "Property dialogPath changed to dialog."))
        );

        // TODO 4.1
        // .addTask( move from admin module: ("Paragraph edit dialog", "The paragraph edition dialog is now a regular dialog.", "/mgnl-bootstrap/templating/config.modules.templating.dialogs.editParagraph.xml"))
//                .addTask(new ArrayDelegateTask("Paragraph selection dialog", "The paragraph selection dialog is now part of the Templating module.",
//                        new RemoveNodeTask(null, null, ContentRepository.CONFIG, "/modules/adminInterface/dialogs/selectParagraph"),
//                        new CreateNodeTask(null, null, ContentRepository.CONFIG, "/modules/templating/dialogs", "selectParagraph", ItemType.CONTENT.getSystemName()),
//                        new NewPropertyTask(null, null, ContentRepository.CONFIG, "/modules/templating/dialogs/selectParagraph", "class", ParagraphSelectDialog.class.getName())))

      //since 4.0 templatePath property was moved into parameters content node
        //has to be fixed in 4.0.3 and 4.1.1
        register(DeltaBuilder.update("4.0.3", "")
                .addTask(new FixTemplatePathTask("Fix templatePath property", "Moves templatePath property if is not set correct."))
        );

        //since 4.0 templatePath property was moved into parameters content node
        //has to be fixed in 4.0.3 and 4.1.1
        register(DeltaBuilder.update("4.1.1", "")
                .addTask(new FixTemplatePathTask("Fix templatePath property", "Moves templatePath property if is not set correct."))
        );

        register(DeltaBuilder.update("4.3", "")
            .addTask(new BootstrapSingleModuleResource("Rendering Engine", "Add configuration for the new rendering engine.", "config.server.rendering.engine.xml"))
        );

        register(DeltaBuilder.update("4.4", "")
            .addTask(new BootstrapSingleResourceAndOrderBefore(
                        "Model Execution Filter",
                        "Add Model Execution Filter",
                        "/mgnl-bootstrap/templating/config.server.filters.cms.modelExecution.xml",
                        "backwardCompatibility"))
        );
    }

    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final ArrayList<Task> tasks = new ArrayList<Task>();
        tasks.add(orderBackwardCompatibilityFilter);
        tasks.add(new OrderNodeBeforeTask("Order model execution filter", "", ContentRepository.CONFIG, "/server/filters/cms/modelExecution", "backwardCompatibility"));
        // TODO : make sure the RenderingFilter is the last one ?
        return tasks;
    }
}
