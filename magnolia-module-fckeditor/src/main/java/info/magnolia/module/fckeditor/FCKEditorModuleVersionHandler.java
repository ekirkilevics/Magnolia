/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.module.fckeditor;

import info.magnolia.cms.core.ItemType;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.RegisterModuleServletsTask;
import info.magnolia.module.fckeditor.dialogs.FckEditorDialog;
import info.magnolia.module.fckeditor.servlets.FCKEditorSimpleUploadServlet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vsteller
 */
public class FCKEditorModuleVersionHandler extends DefaultModuleVersionHandler {

    public FCKEditorModuleVersionHandler() {
        register(DeltaBuilder.update("4.0", "")
                .addTask(new CheckAndModifyPropertyValueTask("Dialog", "Replaces the deprecated dialog class by a new one.", "config",
                        "/modules/fckEditor/controls/fckEdit", "class", "info.magnolia.cms.gui.dialog.DialogFckEdit", FckEditorDialog.class.getName()))
                .addTask(new CheckAndModifyPropertyValueTask("Servlet", "Replaces the deprecated servlet class by a new one.", "config",
                "/server/filters/servlets/FCKEditorSimpleUploadServlet", "class", "info.magnolia.cms.gui.fckeditor.FCKEditorSimpleUploadServlet", FCKEditorSimpleUploadServlet.class.getName()))
        );
    }

    protected List getBasicInstallTasks(InstallContext installContext) {
        final List basicInstallTasks = new ArrayList();
        basicInstallTasks.add(new BootstrapSingleResource("New FCKEditor browser", "Bootstraps the new configuration for the browser page", "/mgnl-bootstrap/fckEditor/config.modules.fckEditor.pages.repositoryBrowser.xml"));
        basicInstallTasks.add(new BootstrapSingleResource("Browsable repositories", "Bootstraps the default configuration for the browsable repositories", "/mgnl-bootstrap/fckEditor/config.modules.fckEditor.config.browsableRepositories.xml"));
        basicInstallTasks.add(new NodeExistsDelegateTask("Check for existing fckEdit control", "Check if fckEdit control is registered in adminInterface module", "config", "/modules/adminInterface/controls/fckEdit",
                new ArrayDelegateTask("Add fckEdit control",
                        new CreateNodeTask("Create controls node", "Add the controls node in the FCKEditor module", "config", "/modules/fckEditor", "controls", ItemType.CONTENT.getSystemName()),
                        new MoveNodeTask("Move fckEdit control", "Move fckEdit control to FCKEditor module since it is a separate module now", "config", "/modules/adminInterface/controls/fckEdit", "/modules/fckEditor/controls/fckEdit", true)
                ),
                new BootstrapSingleResource("Add fckEdit control", "Bootstraps the configuration of the fckEdit control", "/mgnl-bootstrap/fckEditor/config.modules.fckEditor.controls.fckEdit.xml")));
        basicInstallTasks.add(new RegisterModuleServletsTask());
        return basicInstallTasks;
    }
}
