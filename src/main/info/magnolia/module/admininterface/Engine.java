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
package info.magnolia.module.adminInterface;

import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.ModuleConfig;


/**
 * Date: Jul 13, 2004 Time: 4:25:17 PM
 * @author Sameer Charles
 * @version 2.0
 */
public class Engine implements Module {

    public void init(ModuleConfig config) {
        // set local store to be accessed via admin interface classes or JSP
        Store.getInstance().setStore(config.getLocalStore());
    }

    public void destroy() {
    }
}
