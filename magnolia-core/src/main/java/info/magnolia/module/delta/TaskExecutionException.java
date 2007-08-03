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

/**
 * An exception thrown when a task can not be executed and can not be recovered from.
 * (i.e manually fixed or just skipped).
 * Not providing the TaskExecutionException(Throwable cause), to try and force developers
 * into providing a meaningful exception message.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class TaskExecutionException extends Exception {

    public TaskExecutionException(String message) {
        super(message);
    }

    public TaskExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

}
