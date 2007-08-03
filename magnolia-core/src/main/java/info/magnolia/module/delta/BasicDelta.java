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

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

/**
 * A convenient bean-like implementation of Delta.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BasicDelta implements Delta {
    private final String title;
    private final String description;
    private final List tasks;

    /**
     *
     * @param tasks should not be a read-only List, as the ModuleVersionHandler might add tasks to it.
     */
    public BasicDelta(String title, String description, List tasks) {
        this.tasks = tasks;
        this.title = title;
        this.description = description;
    }

    /**
     * Convenience constructor which wraps the given array of tasks into a writable List.
     */
    public BasicDelta(String title, String description, Task[] tasks) {
        this(title, description, new ArrayList(Arrays.asList(tasks))); // not using Arrays.asList directly because we know the version handler could add a task.
    }

    /**
     * Convenience constructor for a Delta with a single task.
     */
    public BasicDelta(String title, String description, Task task) {
        this(title, description, new ArrayList(Collections.singleton(task))); // not using Collections.singletonList directly because we know the version handler could add a task.
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List getTasks() {
        return tasks;
    }

}
