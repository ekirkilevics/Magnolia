/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.objectfactory.configuration;

import info.magnolia.cms.beans.config.ContentRepository;


/**
 * Configuration for a components which is configured in a workspace. Will be loaded by content2bean.
 * @param <T> the type
 */
public class ConfiguredComponentConfiguration<T> implements Cloneable {

    private Class<T> type;

    private String workspace = ContentRepository.CONFIG;

    private String path;

    private boolean observed = false;

    // content2bean
    public ConfiguredComponentConfiguration() {
    }

    public ConfiguredComponentConfiguration(Class<T> type, String workspace, String path, boolean observed) {
        this.type = type;
        this.workspace = workspace;
        this.path = path;
        this.observed = observed;
    }

    public ConfiguredComponentConfiguration(Class<T> type, String path) {
        this.type = type;
        this.path = path;
    }

    public Class<T> getType() {
        return type;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isObserved() {
        return observed;
    }

    public void setObserved(boolean observed) {
        this.observed = observed;
    }

    @Override
    public ConfiguredComponentConfiguration<T> clone() {
        try {
            ConfiguredComponentConfiguration<T> clone = (ConfiguredComponentConfiguration<T>) super.clone();
            clone.type = type;
            clone.workspace = workspace;
            clone.path = path;
            clone.observed = observed;
            return clone;
            } catch (CloneNotSupportedException e) {
                // should never happen
                throw new RuntimeException(e);
            }
        }
}
