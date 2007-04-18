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
package info.magnolia.module.samples.freemarker;

import java.util.Date;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SampleAction {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SampleAction.class);

    private Date executionTime;

    public String execute() {
        log.debug("Executing " + this.getClass().getName());
        executionTime = new Date();
        return "success";
    }

    public Date getExecutionTime() {
        return executionTime;
    }
}
