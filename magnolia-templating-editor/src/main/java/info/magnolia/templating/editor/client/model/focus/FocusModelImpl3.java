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

import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;

/**
 * Helper class to keep tack on selected items.
 */
public class FocusModelImpl3 implements FocusModel {

    private ModelStorage storage;

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
            return;
        }

        if (mgnlElement == storage.getSelectedMgnlElement()) {
            return;
        }

        MgnlElement area = mgnlElement.getParentArea();

        if (area != null) {
            deSelect();
            hideRoot();
            boolean selected = select(area);
            if (!selected) {
                reset();
                return;
            }
        }
        computeOverlay();
        storage.setSelectedMgnlElement(mgnlElement);

    }


    @Override
    public void onMouseDown(Element element) {}

    @Override
    public void reset() {
        deSelect();
        showRoot();
        computeOverlay();
    }

    protected boolean select(MgnlElement mgnlElement) {

        boolean selected = false;

        if (storage.getEditBar(mgnlElement) != null) {
            storage.getEditBar(mgnlElement).setVisible(true);
            selected = true;
        }

        for (MgnlElement component : mgnlElement.getComponents()) {

            if (storage.getEditBar(component) != null) {
                storage.getEditBar(component).setVisible(true);
                selected = true;
            }
        }

        return selected;
    }

    public void deSelect() {
        if (storage.getSelectedMgnlElement() != null && storage.getSelectedMgnlElement().getParentArea() != null) {
            deSelect(storage.getSelectedMgnlElement().getParentArea());
        }
    }

    public void deSelect(MgnlElement mgnlElement) {

        if (storage.getEditBar(mgnlElement) != null) {
            storage.getEditBar(mgnlElement).setVisible(false);
        }

        for (MgnlElement component : mgnlElement.getComponents()) {

            if (storage.getEditBar(component) != null) {
                storage.getEditBar(component).setVisible(false);
            }
        }
    }

    public void showRoot() {
        for (MgnlElement root : storage.getRootElements()) {
                if (storage.getEditBar(root) != null) {
                    storage.getEditBar(root).setVisible(true);
                }
        }
    }
    public void hideRoot() {
        for (MgnlElement root : storage.getRootElements()) {
            if (storage.getEditBar(root) != null) {
                storage.getEditBar(root).setVisible(false);

            }
        }
    }

    public void computeOverlay () {
        for (MgnlElement root : storage.getRootElements()) {
            List<MgnlElement> mgnlElements = root.getDescendants();
            mgnlElements.add(root);
            for (MgnlElement mgnlElement : mgnlElements) {

                if (storage.getOverlay(mgnlElement) == null) {
                    continue;
                }

                Element firstElement = mgnlElement.getFirstElement();
                if (firstElement == null) {
                    continue;
                }
                storage.getOverlay(mgnlElement).getElement().getStyle().setTop(firstElement.getAbsoluteTop(), Unit.PX);
                storage.getOverlay(mgnlElement).getElement().getStyle().setLeft(firstElement.getAbsoluteLeft(), Unit.PX);
                storage.getOverlay(mgnlElement).getElement().getStyle().setWidth(firstElement.getAbsoluteRight() - firstElement.getAbsoluteLeft(), Unit.PX);

                Element lastElement = mgnlElement.getLastElement();
                if (lastElement != null) {
                    storage.getOverlay(mgnlElement).getElement().getStyle().setHeight(lastElement.getAbsoluteBottom() - storage.getOverlay(mgnlElement).getElement().getAbsoluteTop(), Unit.PX);
                }
            }
        }
    }

}
