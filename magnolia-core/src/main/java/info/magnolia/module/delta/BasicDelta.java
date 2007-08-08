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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A convenient bean-like implementation of Delta.
 * Use static factory methods to instanciate.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BasicDelta implements Delta {
    private final String title;
    private final String description;
    private final DeltaType type;
    private final List tasks;

    /**
     * Convenience factory method which creates an Update Delta and wraps the given array of
     * tasks into a writable List.
     */
    public static Delta createBasicDelta(String title, String description, Task[] tasks) {
        // not using Arrays.asList directly because we know the version handler could add a task.
        return createBasicDelta(title, description, new ArrayList(Arrays.asList(tasks)));
    }

    /**
     * Convenience factory method for an Update Delta with a single task.
     */
    public static Delta createBasicDelta(String title, String description, Task task) {
        // not using Collections.singletonList directly because we know the version handler could add a task.
        return createBasicDelta(title, description, new ArrayList(Collections.singleton(task)));
    }

    /**
     * Creates an Update Delta.
     * @param tasks should not be a read-only List, as the ModuleVersionHandler might add tasks to it.
     */
    public static Delta createBasicDelta(String title, String description, List tasks) {
        return new BasicDelta(title, description, DeltaType.update, tasks);
    }

    /**
     * Creates an Install Delta.
     * @param tasks should not be a read-only List, as the ModuleVersionHandler might add tasks to it.
     */
    public static Delta createInstallDelta(String title, String description, List tasks) {
        return new BasicDelta(title, description, DeltaType.install, tasks);
    }

    /**
     * TODO : maybe the title could be generated from module name + version + DeltaType
     */
    protected BasicDelta(String title, String description, DeltaType type, List tasks) {
        this.tasks = tasks;
        this.title = title;
        this.type = type;
        this.description = description;
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

    public DeltaType getType() {
        return type;
    }

}
