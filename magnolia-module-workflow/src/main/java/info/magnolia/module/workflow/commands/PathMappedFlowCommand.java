/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.workflow.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

/**
 * Map paths to workflows.
 * @author philipp
 * @version $Id$
 *
 */
public class PathMappedFlowCommand extends FlowCommand {

    private Collection mappings = new ArrayList();

    private String repository;

    private String path;

    /**
     * In case there is a mapping defined the mapping is used otherwise we fall back to the normal behavior.
     */
    public String getWorkflowName() {
        for (Iterator iter = getMappings().iterator(); iter.hasNext();) {
            Mapping mapping = (Mapping) iter.next();
            if(path.startsWith(mapping.getPath())){
                return mapping.getWorkflowName();
            }
        }
        return super.getWorkflowName();
    }
    public String getDialogName() {
        for (Iterator iter = getMappings().iterator(); iter.hasNext();) {
            Mapping mapping = (Mapping) iter.next();
            if(path.startsWith(mapping.getPath()) && StringUtils.isNotEmpty(mapping.getDialogName())){
                return mapping.getDialogName();
            }
        }
        return super.getDialogName();
    }

    public Collection getMappings() {
        return this.mappings;
    }

    public void setMappings(Collection mappings) {
        this.mappings = mappings;
    }

    public void addMapping(Mapping mapping) {
        if (mapping.isEnabled()) {
            this.mappings.add(mapping);
        }
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRepository() {
        return this.repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * Used to definen the mappings
     */
    public static class Mapping {

        private String path;

        private String workflowName;

        private String dialogName;

        private boolean enabled = true;

        public String getPath() {
            return this.path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getWorkflowName() {
            return this.workflowName;
        }

        public void setWorkflowName(String workflowName) {
            this.workflowName = workflowName;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }


        public String getDialogName() {
            return this.dialogName;
        }


        public void setDialogName(String dialogName) {
            this.dialogName = dialogName;
        }
    }

}
