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
package info.magnolia.cms.mail.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.RegisterModuleServletsTask;
import info.magnolia.module.delta.WebXmlConditionsUtil;

import java.util.ArrayList;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MailModuleVersionHandler extends DefaultModuleVersionHandler {

    public MailModuleVersionHandler() {
        final ArrayList conditions = new ArrayList();
        final WebXmlConditionsUtil u = new WebXmlConditionsUtil(conditions);
        u.servletIsNowWrapped("Mail");

        final Delta for31 = DeltaBuilder.update("3.1", "")
                .addTask(new RegisterModuleServletsTask())
                .addConditions(conditions);
        register(for31);
    }
}
