/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.rendering.template;


import java.util.Map;


/**
 * Definition for a Area.
 *
 * @version $Id$
 */
public interface AreaDefinition extends TemplateDefinition, Cloneable {

    static final String TYPE_NO_COMPONENT = "noComponent";
    static final String TYPE_LIST = "list";
    static final String TYPE_SINGLE = "single";
    static final String CONTENT_STRUCTURE_FLAT = "flat";
    static final String CONTENT_STRUCTURE_NODE = "node";
    static final String DEFAULT_TYPE = TYPE_LIST;

    static final String CMS_ADD = "cms:add";
    static final String CMS_PLACEHOLDER = "cms:placeholder";
    static final String CMS_EDIT = "cms:edit";

    Map<String, ComponentAvailability> getAvailableComponents();

    Boolean isEnabled();

    String getType();

    String getContentStructure();

    InheritanceConfiguration getInheritance();

    /**
     * If an area is optional it has first to be created explicitly (i.e. via a create button). Optional areas can be removed (i.e. via a remove button), whereas
     * non optional (namely required) areas are always created and can not be removed. <strong>By default, areas are required.</strong>
     */
    Boolean isOptional();

    Integer getMaxComponents();
}
