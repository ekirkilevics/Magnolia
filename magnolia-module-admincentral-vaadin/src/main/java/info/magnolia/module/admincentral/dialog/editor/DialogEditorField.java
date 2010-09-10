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
package info.magnolia.module.admincentral.dialog.editor;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Special container used by the dialog editor to visualize controls.
 * FIXME: solve highlighting on select by other means then by usage of CssLayout
 * @author had
 * @version $Id: $
 */
public class DialogEditorField extends CssLayout {

    private boolean selected;
    private TemplateControl originalTemplate;


    // TODO: using extra layout just to be able to change colors dynamically is overkill, but the only other way is writing own theme and setting custom style names
    public DialogEditorField(String caption, Component comp, TemplateControl originalTemplate) {
        this.setCaption(caption);
        this.originalTemplate = originalTemplate;

        final HorizontalLayout innerLayout = new HorizontalLayout();
        innerLayout.setSizeUndefined();
        final Label label = new Label(caption);
        // TODO: find better way to keep it same across all rows in dialog, then hard coding the value
        label.setWidth("100px");
        //comp.setCaption(caption);
        innerLayout.addComponent(label);
        innerLayout.setExpandRatio(label, 0);
        //container.set
        innerLayout.addComponent(comp);
        innerLayout.setSpacing(true);
        innerLayout.setExpandRatio(comp, 1);
        addComponent(innerLayout);
        setWidth("100%");
    }

    @Override
    protected String getCss(Component c) {
        if (selected) {
            //colorize
            return "background: #DDDDDD;";
        } else {
            // white
            return "background: #FFFFFF;";
        }
    }

    public void setSelected(boolean b) {
        this.selected = b;
    }

    public TemplateControl getOriginalTemplate() {
        return originalTemplate;
    }

}
