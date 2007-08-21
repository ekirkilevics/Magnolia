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
package info.magnolia.module.delta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractTask implements Task {
    private final String name;
    private final String description;

    /**
     * Logger that can be reused in subclasses.
     */
    protected Logger log = LoggerFactory.getLogger(getClass());

    public AbstractTask(String taskName, String taskDescription) {
        this.name = taskName;
        this.description = taskDescription;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return "task: " + name;
    }
}
