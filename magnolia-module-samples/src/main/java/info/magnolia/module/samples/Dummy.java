/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.module.samples;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a dummy class loaded from the module configuration, it defines 2 properties
 * (modules/samples/config/sampleList/sampleClass/)
 * name will be taken from the name of the node
 * active can be set by user is a boolean defined as boolean in the configuration
 * The parameter map gets loaded from the values set in the configuration (modules/samples/config/sampleList/sampleClass/parameters)
 * @author tmiyar
 *
 */
public class Dummy {

    final private static Logger log = LoggerFactory.getLogger(Dummy.class);

    private String name;

    private boolean active;

    private Map parameters = new HashMap();

    public String getName() {
        log.info("No need to define name node data, will take the name of the node for this property: " + name);
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        log.info("Need to define active node data: " + active);
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map getParameters() {
        log.info("Need to define parameters node data: " + parameters);
        return parameters;
    }

    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }



}
