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
package info.magnolia.templating.editor.client.model.focus;

import info.magnolia.templating.editor.client.dom.MgnlElement;
import info.magnolia.templating.editor.client.model.ModelStorage;

import com.google.gwt.dom.client.Element;

/**
 * Helper class to keep tack on selected items.
 */
public class FocusModelImpl3 implements FocusModel {

    private ModelStorage storage;
    private boolean rootSelected = false;

    public FocusModelImpl3(ModelStorage storage) {
        super();
        this.storage = storage;
    }

    @Override
    public void onMouseUp(MgnlElement mgnlElement) {

    }

    @Override
    public void onMouseUp(Element element) {

        MgnlElement mgnlElement = storage.getMgnlElement(element);

        if (mgnlElement == null) {
            reset();
        }

        else if (mgnlElement.getParentArea() != null) {
            MgnlElement area = mgnlElement.getParentArea();
            if (area != storage.getSelectedMgnlElement()) {
                deSelect();
                hideRoot();
                toggleSelection(area, true);
            }
        }
    }


    @Override
    public void onMouseDown(Element element) {}

    @Override
    public void reset() {
        deSelect();
        showRootPlaceHolder();
    }

    protected void toggleSelection(MgnlElement mgnlElement, boolean visible) {

        if (storage.getEditBar(mgnlElement) != null) {
            storage.getEditBar(mgnlElement).setVisible(visible);
        }
        if (storage.getAreaEndBar(mgnlElement) != null) {
            storage.getAreaEndBar(mgnlElement).setVisible(visible);
        }

        // toggle all direct child-areas placeholders visibility
        for (MgnlElement area : mgnlElement.getAreas()) {

            if (storage.getAreaPlaceHolder(area) != null) {
                storage.getAreaPlaceHolder(area).setVisible(visible);
            }
        }

        for (MgnlElement component : mgnlElement.getComponents()) {

            // toggle all child-components editbar visibility - does this case occure?
            if (storage.getEditBar(component) != null) {
                storage.getEditBar(component).setVisible(visible);
            }
            // toggle all child-components-area placeholder visibility
            for (MgnlElement area : component.getAreas()) {

                if (storage.getAreaPlaceHolder(area) != null) {
                    storage.getAreaPlaceHolder(area).setVisible(visible);
                }
            }
        }

        if (storage.getAreaPlaceHolder(mgnlElement)!= null) {
            if (mgnlElement.getParent() != null) {
                storage.getAreaPlaceHolder(mgnlElement).setVisible(visible);
            }
            storage.getAreaPlaceHolder(mgnlElement).setActive(visible);
        }

        if (storage.getComponentPlaceHolder(mgnlElement) != null) {
            storage.getComponentPlaceHolder(mgnlElement).setVisible(visible);
        }
        storage.setSelectedMgnlElement(mgnlElement);

    }

    public void deSelect() {
        if (storage.getSelectedMgnlElement() != null && storage.getSelectedMgnlElement().getParentArea() != null) {
            toggleSelection(storage.getSelectedMgnlElement().getParentArea(), false);
            storage.setSelectedMgnlElement(null);
        }
    }

    @Override
    public void toggleRootSelection() {
        deSelect();

        this.rootSelected = !this.rootSelected;
        for (MgnlElement root : storage.getRootElements()) {
            if (storage.getEditBar(root) != null) {
                storage.getEditBar(root).setVisible(true);
            }
            if (storage.getAreaEndBar(root) != null) {
                storage.getAreaEndBar(root).setVisible(true);
            }
            if (storage.getAreaPlaceHolder(root) != null) {
                storage.getAreaPlaceHolder(root).setVisible(true);
            }
        }
    }

    public void showRootPlaceHolder() {
        for (MgnlElement root : storage.getRootElements()) {
            if (storage.getAreaPlaceHolder(root) != null) {
                storage.getAreaPlaceHolder(root).setVisible(true);
                storage.getAreaPlaceHolder(root).setActive(false);
            }
        }
    }

    public void hideRoot() {
        for (MgnlElement root : storage.getRootElements()) {
            if (storage.getEditBar(root) != null) {
                storage.getEditBar(root).setVisible(false);
            }
            if (storage.getAreaEndBar(root) != null) {
                storage.getAreaEndBar(root).setVisible(false);
            }
            if (storage.getComponentPlaceHolder(root) != null) {
                storage.getComponentPlaceHolder(root).setVisible(false);
            }
            if (storage.getAreaPlaceHolder(root) != null) {
                storage.getAreaPlaceHolder(root).addStyleName("inactive");
            }
        }
    }

}
