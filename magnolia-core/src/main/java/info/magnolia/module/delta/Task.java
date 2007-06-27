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
 * A Task represents an atomic operation to be performed when installing,
 * updating or uninstalling a module, as part of a Delta.
 *
 * TODO : this is work in progress / sketch
 *
 * @see info.magnolia.module.delta.Delta
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface Task {
    String getName();
    String getDescription();
    
    void execute();

}
