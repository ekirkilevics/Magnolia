/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.model;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Servlet as defined in a module descriptor.
 *
 * @see ModuleDefinition
 * 
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

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection getMappings() {
        return this.mappings;
    }

    public void addMapping(String mapping) {
        this.mappings.add(mapping);
    }

    public Collection getParams() {
        return this.params;
    }

    public void addParam(ServletParameterDefinition param) {
        this.params.add(param);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
