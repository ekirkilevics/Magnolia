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

import info.magnolia.cms.beans.config.ContentRepository;

/**
 * A task that depends on the value of the /server/admin config value. Depends on one or the other delegates
 * depending on its value, and fails is that property does not exist.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class IsAuthorInstanceDelegateTask extends PropertyValueDelegateTask {

    public IsAuthorInstanceDelegateTask(String taskName, String taskDescription, Task isAuthor, Task isPublic) {
        super(taskName, taskDescription, ContentRepository.CONFIG, "/server", "admin", "false", false, isPublic, isAuthor);
    }
}
