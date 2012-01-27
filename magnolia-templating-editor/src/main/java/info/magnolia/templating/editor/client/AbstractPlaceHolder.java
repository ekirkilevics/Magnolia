/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.templating.editor.client;

import info.magnolia.templating.editor.client.dom.MgnlElement;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Abstract Widget for area and component placeholder.
 *
 * @version $Id$
 */
public class AbstractPlaceHolder extends FlowPanel {

    public AbstractPlaceHolder() {
        super();

        setStylePrimaryName("mgnlPlaceHolder");
    }

    /**
     *  TODO: we should not have to call onAttach ourself?
     */
    public void attach(Node node) {
        final Node parentNode = node.getParentNode();
        parentNode.insertAfter(getElement(), node);
        onAttach();
    }

    public void attach(MgnlElement mgnlElement) {
        if (mgnlElement.getFirstElement() != null) {
            if (mgnlElement.getFirstElement() == mgnlElement.getLastElement()) {
                mgnlElement.getFirstElement().appendChild(getElement());
            }
            else {
                Element parent = mgnlElement.getFirstElement().getParentElement();
                parent.insertAfter(getElement(), mgnlElement.getLastElement());
            }
        }
        else {
            PageEditor.model.getEditBar(mgnlElement).getElement().getParentElement().appendChild(getElement());
        }
        onAttach();
    }

    public void toggleVisible() {
        isVisible();
        setVisible(!isVisible());
    }

}