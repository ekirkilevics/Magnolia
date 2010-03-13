/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.content2bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 */
public class SimpleBean {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SimpleBean.class);

    private String prop1;

    private String prop2;

    /**
     * @return the prop1
     */
    public String getProp1() {
        return this.prop1;
    }

    /**
     * @param prop1 the prop1 to set
     */
    public void setProp1(String prop1) {
        this.prop1 = prop1;
    }

    /**
     * @return the prop2
     */
    public String getProp2() {
        return this.prop2;
    }

    /**
     * @param prop2 the prop2 to set
     */
    public void setProp2(String prop2) {
        this.prop2 = prop2;
    }

    public String getIndexed(int index) {
        return "indexed";
    }

}
