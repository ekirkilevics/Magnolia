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
package info.magnolia.templating.editor.client;

import info.magnolia.templating.editor.client.dom.MgnlElement;
import info.magnolia.templating.editor.client.model.ModelStorage;


import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Label;

/**
 * Widget for area placeholders.
 */
public class AreaPlaceHolderWidget extends AbstractPlaceHolder {


    public AreaPlaceHolderWidget(MgnlElement mgnlElement) {

        super(mgnlElement);

        this.addStyleName("area");
        String label = mgnlElement.getComment().getAttribute("label");
        Label areaName = new Label(label + " Placeholder");

        if (mgnlElement.getRootArea() != mgnlElement) {
            setVisible(false);
        }
        add(areaName);

        ComponentPlaceHolderWidget placeHolder = new ComponentPlaceHolderWidget(mgnlElement);
        ModelStorage.getInstance().addComponentPlaceHolder(mgnlElement, placeHolder);

        add(placeHolder);
        attach();
    }

    public void attach() {
        Element parent = PageEditor.model.getEditBar(getMgnlElement()).getElement().getParentElement();
        parent.insertAfter(getElement(), PageEditor.model.getEditBar(getMgnlElement()).getElement());

        onAttach();
    }

}
