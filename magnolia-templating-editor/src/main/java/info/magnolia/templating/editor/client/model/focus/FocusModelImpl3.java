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
import info.magnolia.templating.editor.client.jsni.JavascriptUtils;
import info.magnolia.templating.editor.client.model.ModelStorage;

import com.google.gwt.dom.client.Element;

/**
 * Helper class to keep tack on selected items.
 */
public class FocusModelImpl3 implements FocusModel {

    private ModelStorage model;
    private boolean rootSelected = false;

    public FocusModelImpl3(ModelStorage model) {
        super();
        this.model = model;
    }

    @Override
    public void onMouseUp(Element element) {

        MgnlElement mgnlElement = model.getMgnlElement(element);

        if (mgnlElement == null) {
            reset();
        }

        else if (mgnlElement.getParentArea() != null) {
            MgnlElement area = mgnlElement.getParentArea();
            if (area != model.getSelectedMgnlElement()) {
                deSelect();
                hideRoot();
                toggleSelection(area, true);
            }
        }
    }

    @Override
    public void onLoadSelect(MgnlElement selectedMgnlElement) {
        model.setSelectedMgnlElement(selectedMgnlElement);
        toggleRootAreaBar(false);
        showRootPlaceHolder();
        toggleSelection(selectedMgnlElement, true);
    }
    @Override
    public void reset() {
        deSelect();
        toggleRootAreaBar(true);
        showRootPlaceHolder();
    }

    private void toggleSelection(MgnlElement mgnlElement, boolean visible) {
        if (visible) {
            String contentId = mgnlElement.getAttribute("content");
            JavascriptUtils.setEditorContentIdCookie(contentId);
        }
        else {
            JavascriptUtils.removeEditorContentIdCookie();
        }

        if (model.getEditBar(mgnlElement) != null) {
            model.getEditBar(mgnlElement).setVisible(visible);
        }
        if (model.getAreaEndBar(mgnlElement) != null) {
            model.getAreaEndBar(mgnlElement).setVisible(visible);
        }

        // toggle all direct child-areas placeholders visibility
        for (MgnlElement area : mgnlElement.getAreas()) {

            if (model.getAreaPlaceHolder(area) != null) {
                model.getAreaPlaceHolder(area).setVisible(visible);
            }
        }

        for (MgnlElement component : mgnlElement.getComponents()) {

            // toggle all child-components editbar visibility - does this case occure?
            if (model.getEditBar(component) != null) {
                model.getEditBar(component).setVisible(visible);
            }
            // toggle all child-components-area placeholder visibility
            for (MgnlElement area : component.getAreas()) {

                if (model.getAreaPlaceHolder(area) != null) {
                    model.getAreaPlaceHolder(area).setVisible(visible);
                }
            }
        }

        if (model.getAreaPlaceHolder(mgnlElement)!= null) {
            if (mgnlElement.getParent() != null) {
                model.getAreaPlaceHolder(mgnlElement).setVisible(visible);
            }
            model.getAreaPlaceHolder(mgnlElement).setActive(visible);
        }

        if (model.getComponentPlaceHolder(mgnlElement) != null) {
            model.getComponentPlaceHolder(mgnlElement).setVisible(visible);
        }
        model.setSelectedMgnlElement(mgnlElement);

    }

    private void deSelect() {
        if (model.getSelectedMgnlElement() != null && model.getSelectedMgnlElement().getParentArea() != null) {
            toggleSelection(model.getSelectedMgnlElement().getParentArea(), false);
            model.setSelectedMgnlElement(null);
        }
    }

    @Override
    public void toggleRootAreaBar(boolean visible) {
        deSelect();

        this.rootSelected = !this.rootSelected;
        for (MgnlElement root : model.getRootElements()) {
            if (model.getEditBar(root) != null) {
                model.getEditBar(root).setVisible(visible);
            }
            if (model.getAreaEndBar(root) != null) {
                model.getAreaEndBar(root).setVisible(visible);
            }
            if (model.getAreaPlaceHolder(root) != null) {
                model.getAreaPlaceHolder(root).setVisible(visible);
            }
        }
    }

    private void showRootPlaceHolder() {
        for (MgnlElement root : model.getRootElements()) {
            if (model.getAreaPlaceHolder(root) != null) {
                model.getAreaPlaceHolder(root).setVisible(true);
                model.getAreaPlaceHolder(root).setActive(false);
            }
        }
    }

    private void hideRoot() {
        for (MgnlElement root : model.getRootElements()) {
            if (model.getEditBar(root) != null) {
                model.getEditBar(root).setVisible(false);
            }
            if (model.getAreaEndBar(root) != null) {
                model.getAreaEndBar(root).setVisible(false);
            }
            if (model.getComponentPlaceHolder(root) != null) {
                model.getComponentPlaceHolder(root).setVisible(false);
            }
            if (model.getAreaPlaceHolder(root) != null) {
                model.getAreaPlaceHolder(root).setVisible(true);
            }
        }
    }
}
