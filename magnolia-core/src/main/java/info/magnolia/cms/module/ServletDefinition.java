/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.module;

import java.util.ArrayList;
import java.util.Collection;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ServletDefinition {

    /**
     * The name of the servlet
     */
    private String name;

    /**
     * The class name of the servlet
     */
    private String className;

    /**
     * Comment added to this servlet
     */
    private String comment;

    /**
     * The mapping used for this servlet
     */
    private Collection mappings = new ArrayList();

    /**
     * The mapping used for this servlet
     */
    private Collection params = new ArrayList();

    /**
     * @return Returns the className.
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * @param className The className to set.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the mappings.
     */
    public Collection getMappings() {
        return this.mappings;
    }

    /**
     * Add a mapping to the mappings list
     * @param mapping
     */
    public void addMapping(String mapping) {
        this.mappings.add(mapping);
    }

    /**
     * @return Returns the parameters.
     */
    public Collection getParams() {
        return this.params;
    }

    public void addParam(ServletParameterDefinition param) {
        this.params.add(param);
    }

    /**
     * @return Returns the comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment The comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

}
