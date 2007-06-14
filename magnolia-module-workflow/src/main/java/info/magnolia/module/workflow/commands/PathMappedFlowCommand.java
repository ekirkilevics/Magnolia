/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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