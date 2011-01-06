/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.delta;

import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to build Delta instances.
 * 
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

    public static DeltaBuilder startup(ModuleDefinition moduleDef, List<Task> tasks) {
        final String description = "Tasks executed before starting up module " + moduleDef.getDescription();
        return startup(moduleDef.getVersion(), description).addTasks(tasks);
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
    private final List<Task> tasks;
    private final List<Condition> conditions;

    private DeltaBuilder(Version version, String description, DeltaType type) {
        this.version = version;
        this.description = description;
        this.type = type;
        this.tasks = new ArrayList<Task>();
        this.conditions = new ArrayList<Condition>();
    }

    public DeltaBuilder addTask(Task t) {
        tasks.add(t);
        return this;
    }

    public DeltaBuilder addTasks(List<Task> tasks) {
        this.tasks.addAll(tasks);
        return this;
    }

    public DeltaBuilder addCondition(Condition c) {
        conditions.add(c);
        return this;
    }

    public DeltaBuilder addConditions(List<Condition> conditions) {
        this.conditions.addAll(conditions);
        return this;
    }

    public Version getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public DeltaType getType() {
        return type;
    }

}
