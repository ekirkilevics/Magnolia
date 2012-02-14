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

import com.google.gwt.core.client.GWT;

import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.templating.editor.client.PageEditor;
import info.magnolia.templating.editor.client.dom.MgnlElement;
import info.magnolia.templating.editor.client.widget.controlbar.AreaBar;
import info.magnolia.templating.editor.client.widget.controlbar.AreaEndBar;
import info.magnolia.templating.editor.client.widget.placeholder.AreaPlaceHolder;
import info.magnolia.templating.editor.client.widget.placeholder.ComponentPlaceHolder;

/**
 * Factory Class for MgnlElement processors.
 */
public class AreaProcessor extends MgnlElementProcessor {

    public AreaProcessor(MgnlElement mgnlElement) {
        super(mgnlElement);
    }

    @Override
    public void process() {
        AreaBar areaBar = null;
        try {
            areaBar = new AreaBar(getMgnlElement());

            boolean noComponent = getMgnlElement().getComment().getAttribute("type").equals(AreaDefinition.TYPE_NO_COMPONENT);
            if (getMgnlElement().getComponents().isEmpty() && !noComponent) {
                new AreaPlaceHolder(getMgnlElement());
            }

            try {
                ComponentPlaceHolder placeHolder = new ComponentPlaceHolder(getMgnlElement());
                placeHolder.attach();
            }
            catch(IllegalArgumentException e) {
                GWT.log("Not creating componentPLaceHolder for this element. Missing parameters.");
            }

            new AreaEndBar(getMgnlElement());
        }
        catch (IllegalArgumentException e) {
            GWT.log("Not creating areabar for this element. Missing parameters. Will be deleted.");
        }

        if (areaBar == null) {
            // if the area has no controls we, don't want it in the structure.

            // delete the element from the tree
            // set all child parents to parent
            getMgnlElement().delete();


            // remove it from the Model
            PageEditor.model.removeMgnlElement(getMgnlElement());
        }
    }

}
