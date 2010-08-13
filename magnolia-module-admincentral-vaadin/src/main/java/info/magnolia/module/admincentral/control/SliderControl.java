/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.control;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Slider;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.cms.core.Content;

import javax.jcr.RepositoryException;

/**
 * Slider control for selecting a number in a fixed range.
 */
public class SliderControl extends AbstractDialogControl {

    private int min;
    private int max;
    private int resolution;

    private Slider slider;

    public void addControl(Content storageNode, VerticalLayout layout) {

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        final Label value = new Label("0");
        value.setWidth("3em");

        slider = new Slider("Select a value between 0 and 100");
        slider.setWidth("100%");
        slider.setMin(min);
        slider.setMax(max);
        slider.setResolution(resolution);
        slider.setImmediate(true);
        slider.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                value.setValue(event.getProperty().getValue());
            }
        });

        horizontalLayout.addComponent(slider);
        horizontalLayout.setExpandRatio(slider, 1);
        horizontalLayout.addComponent(value);
        horizontalLayout.setComponentAlignment(value, Alignment.BOTTOM_LEFT);

        layout.addComponent(horizontalLayout);
    }

    public void validate() {
        slider.validate();
    }

    public void save(Content storageNode) throws RepositoryException {
        storageNode.setNodeData(getName(), (Double)slider.getValue());
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }
}
