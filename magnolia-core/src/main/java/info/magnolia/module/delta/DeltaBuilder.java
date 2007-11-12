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

import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DeltaBuilder implements Delta {

    /**
     * Convenience factory method for an Update Delta with a single task.
     */
    public static DeltaBuilder update(Version v, String description, Task task) {
        return update(v, description).addTask(task);
    }

    public static DeltaBuilder update(String versionStr, String description) {
        return update(Version.parseVersion(versionStr), description);
    }

    public static DeltaBuilder update(Version version, String description) {
        return new DeltaBuilder(version, description, DeltaType.update);
    }

    public static DeltaBuilder install(Version version, String description) {
        return new DeltaBuilder(version, description, DeltaType.install);
    }

    public static DeltaBuilder startup(ModuleDefinition moduleDef, List tasks) {
        final String description = "Tasks executed before starting up module " + moduleDef.getDescription();
        return startup(moduleDef.getVersionDefinition(), description).addTasks(tasks);
    }

    /**
     * TODO : it seems irrelevant to have a Version in startup tasks. These should probably be moved to ModuleLifecycle.
     */
    public static DeltaBuilder startup(Version version, String description) {
        return new DeltaBuilder(version, description, DeltaType.startup);
    }

    private final Version version;
    private final String description;
    private final DeltaType type;
    private final List tasks;
    private final List conditions;

    private DeltaBuilder(Version version, String description, DeltaType type) {
        this.version = version;
        this.description = description;
        this.type = type;
        this.tasks = new ArrayList();
        this.conditions = new ArrayList();
    }

    public DeltaBuilder addTask(Task t) {
        tasks.add(t);
        return this;
    }

    public DeltaBuilder addTasks(List tasks) {
        this.tasks.addAll(tasks);
        return this;
    }

    public DeltaBuilder addCondition(Condition c) {
        conditions.add(c);
        return this;
    }

    public DeltaBuilder addConditions(List conditions) {
        this.conditions.addAll(conditions);
        return this;
    }

    public Version getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public List getConditions() {
        return conditions;
    }

    public List getTasks() {
        return tasks;
    }

    public DeltaType getType() {
        return type;
    }

}
