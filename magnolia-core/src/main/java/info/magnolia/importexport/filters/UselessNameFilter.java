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
package info.magnolia.importexport.filters;

import org.xml.sax.XMLReader;


/**
 * A filter that can be used to remove optional "name" attributes in template, dialogs or paragraph nodes. The name
 * attribute was required in magnolia 2, magnolia 3 makes use of the name of the containing node. This filter can be
 * used to cleanup old configurations that survived to copy and paste. Not enabled by default, you need to modify
 * DataTransporter in order to use it (see the comments in DataTransporter.importXmlStream())
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class UselessNameFilter extends SkipNodePropertyFilter {

    /**
     * Instantiates a new filter.
     * @param parent wrapped XMLReader
     */
    public UselessNameFilter(XMLReader parent) {
        super(parent);
    }

    protected String getFilteredPropertyName() {
        return "name";
    }

    protected boolean filter(String propertyValue, String parentNodeName) {
        boolean filter = parentNodeName.equals(propertyValue);
        if (filter) {
            log.info("Dropped useless name property with value \"{}\"", propertyValue);
        }
        else {
            log.info("Not removing name property. Property values is \"{}\", parent node is \"{}\"", new Object[]{
                propertyValue,
                lastNodeName});
        }
        return filter;
    }
}
