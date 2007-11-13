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
package info.magnolia.module.fckeditor;

import info.magnolia.cms.core.ItemType;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.RegisterModuleServletsTask;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vsteller
 *
 */
public class FCKEditorModuleVersionHandler extends DefaultModuleVersionHandler {
    
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
