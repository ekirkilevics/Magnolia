/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.templating.editor.client.dom.processor;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;

import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.templating.editor.client.PageEditor;
import info.magnolia.templating.editor.client.dom.MgnlElement;

/**
 * Processor for DOM elements.
 */
public class ElementProcessor {

    public static void process(Node node, MgnlElement mgnlElement) {

        Element element = node.cast();
        if (element.hasTagName("A")) {
            disableLink(element);
            removeHover(element);
        }
        PageEditor.model.addElement(mgnlElement, element);

        if (element.hasAttribute(AreaDefinition.CMS_ADD)) {
            mgnlElement.setComponentElement(element);
        }

        else if (element.hasAttribute(AreaDefinition.CMS_PLACEHOLDER)) {
            mgnlElement.setAreaElement(element);
        }

        else if (element.hasAttribute(AreaDefinition.CMS_EDIT)) {
            mgnlElement.setEditElement(element);
        }

        else {
            if (mgnlElement.getFirstElement() == null) {
                mgnlElement.setFirstElement(element);
            }

            if (mgnlElement.getLastElement() == null || !mgnlElement.getLastElement().isOrHasChild(element)) {
                mgnlElement.setLastElement(element);
            }
        }

    }

    public static void removeHover (Element element) {
        element.addClassName("disabled");
    }

    public native static void disableLink(Element element) /*-{
        if (element.onclick == null) {
            element.onclick = function() {
              return false;
            };
        }
    }-*/;
}
