/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.module.samples.setup;

import java.util.Collections;
import java.util.List;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.IsAuthorInstanceDelegateTask;
import info.magnolia.module.delta.SetPropertyTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class SamplesVersionHandler extends DefaultModuleVersionHandler {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SamplesVersionHandler.class);
    
    protected List getExtraInstallTasks(InstallContext installContext) {
        
        SetPropertyTask setDefaultURI = new SetPropertyTask(
            ContentRepository.CONFIG, 
            "/modules/adminInterface/virtualURIMapping/default", 
            "toURI", 
            "redirect:/features.html");
        
        return Collections.singletonList(new IsAuthorInstanceDelegateTask("Default URI", "Sets the value to the default page redirect:/features.html", null, setDefaultURI));
    }

}
