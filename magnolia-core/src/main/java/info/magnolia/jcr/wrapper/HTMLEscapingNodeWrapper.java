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
package info.magnolia.jcr.wrapper;

import info.magnolia.jcr.decoration.AbstractContentDecorator;
import info.magnolia.jcr.decoration.ContentDecoratorNodeWrapper;

import javax.jcr.Node;
import javax.jcr.Property;


/**
 * Node wrapper for escaping HTML in property values. This wrapper is essential to prevention of XSS vulnerabilities in templates.
 * 
 * @see HTMLEscapingPropertyWrapper
 */
public class HTMLEscapingNodeWrapper extends ContentDecoratorNodeWrapper {

    public HTMLEscapingNodeWrapper(Node wrapped, final boolean transformLineBreaks) {
        super(wrapped, new HTMLEscapingContentDecorator(transformLineBreaks));
    }

    /**
     * HTML escaping content decorator for use by node and property wrapper classes.
     */
    public static class HTMLEscapingContentDecorator extends AbstractContentDecorator {

        private final boolean transformLineBreaks;

        public HTMLEscapingContentDecorator(boolean transformLineBreaks) {
            this.transformLineBreaks = transformLineBreaks;
        }

        @Override
        public Node wrapNode(Node node) {
            if (node == null) {
                return null;
            }
            return new HTMLEscapingNodeWrapper(node, transformLineBreaks);
        }

        @Override
        public Property wrapProperty(Property property) {
            return new HTMLEscapingPropertyWrapper(property, transformLineBreaks);
        }
    }


}
