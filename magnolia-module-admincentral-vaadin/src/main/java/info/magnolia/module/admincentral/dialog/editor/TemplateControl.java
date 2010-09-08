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

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;

import info.magnolia.module.admincentral.control.AbstractDialogControl;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

/**
 * Marker for template controls.
 * @author had
 * @version $Id: $
 */
public class TemplateControl extends Label {

    private AbstractDialogControl control;

    public TemplateControl(String string, AbstractDialogControl control) {
        super(string);
        this.control = control;
    }

    public DialogEditorField getControlComponent(Window window) {
        final Component comp = this.control.getControl(null, window);
        final String caption = StringUtils.isBlank(comp.getCaption()) ? (super.getValue() + ": ") : comp.getCaption();

        return new DialogEditorField(caption, comp, this);
    }

    public void configureIn(DialogEditorField fieldInstance, FormLayout fieldEditingTarget) {
        fieldEditingTarget.removeAllComponents();
        fieldEditingTarget.addComponent(new Label("Set Properties for <b>" + fieldInstance.getCaption() + "</b>", Label.CONTENT_XHTML));
        //List<Method> setters = new ArrayList<Method>();
        for (Method m :control.getClass().getMethods()) {
            if ("setParent".equals(m.getName()) || "setFocus".equals(m.getName()) || "setSecret".equals(m.getName())) {
                // skip some props
                continue;
            }
            if (m.getName().startsWith("set") && m.getParameterTypes().length == 1) {
                //setters.add(m);
                String name = StringUtils.uncapitalize(StringUtils.substringAfter(m.getName(), "set") + ":");
                Component comp;
                if (boolean.class.equals(m.getParameterTypes()[0])) {
                    comp = new CheckBox(name);
                } else {
                    comp = new TextField(name);
                    comp.setWidth("200px");
                }
                fieldEditingTarget.addComponent(comp);
            }
        }


    }
}
