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
package info.magnolia.module.admincentral.dialog.editor;

import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import info.magnolia.module.admincentral.control.AbstractDialogControl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Marker for template controls.
 * @author had
 * @version $Id: $
 */
public class TemplateControl extends Label {

    private static final Logger log = LoggerFactory.getLogger(TemplateControl.class);
    private AbstractDialogControl controlTemplate;

    public TemplateControl(String string, AbstractDialogControl control) {
        super(string);
        this.controlTemplate = control;
    }

    public DialogEditorField createControlComponent(Window window) {
        AbstractDialogControl actualControl = this.controlTemplate.clone();
        final Component comp = actualControl.createField(null, window);
        final String caption = StringUtils.isBlank(comp.getCaption()) ? (super.getValue() + ": ") : comp.getCaption();

        return new DialogEditorField(caption, comp, actualControl);
    }

    public static void configureIn(final DialogEditorField dragableEditorComponentRepresentingFieldWithLabelInTheEditor, FormLayout formForRenderingConfigurableFieldProperties) {
        formForRenderingConfigurableFieldProperties.removeAllComponents();
        final AbstractDialogControl control = dragableEditorComponentRepresentingFieldWithLabelInTheEditor.getFieldInstance();
        for (final Method m :control.getClass().getMethods()) {
            if ("setParent".equals(m.getName()) || "setFocus".equals(m.getName()) || "setSecret".equals(m.getName())) {
                // skip some props
                continue;
            }
            if (m.getName().startsWith("set") && m.getParameterTypes().length == 1) {
                final Class parameterType = m.getParameterTypes()[0];
                //list all setters to populate configuration form
                final String name = StringUtils.uncapitalize(StringUtils.substringAfter(m.getName(), "set") + ":");
                Component comp;
                if (boolean.class.equals(parameterType)) {
                    comp = new CheckBox(name);
                } else {
                    TextField tf = new TextField(name);
                    tf.setWidth("150px");
                    try {
                        // invoke getter on the control instance to preset current value
                        Object val = control.getClass().getMethod("g" + m.getName().substring(1)).invoke(control);
                        // set only not null (already set or existing) values
                        if (val != null) {
                            tf.setValue(val);
                        }
                    } catch (Exception e) {
                        // TODO: deal with the situation when we fail to set value
                    }
                    tf.addListener(new Property.ValueChangeListener() {
                        public void valueChange(Property.ValueChangeEvent event) {
                            try {
                                Object val = event.getProperty().getValue();
                                if (int.class.equals(parameterType)) {
                                    val = Integer.parseInt("" + val);
                                } else if (long.class.equals(parameterType)) {
                                    val = Long.parseLong("" + val);
                                }
                                m.invoke(control, val);
                            } catch (IllegalArgumentException e) {
                                log.error("Field " + name + " in " + control + " can't be set to value " + event.getProperty().getValue() + " of type " + event.getProperty().getValue().getClass() + ". Required value type is " +parameterType, e);
                            } catch (IllegalAccessException e) {
                                // should not happen
                                throw new RuntimeException(e);
                            } catch (InvocationTargetException e) {
                                log.error("Failed to set value of the field " + name + " in " + control + " to " + event.getProperty().getValue() + " of type " + event.getProperty().getValue().getClass() + ". Required value type is " + parameterType, e);
                            }
                        }
                    });
                    // FYI: we need to react on the change of text field value immediately, otherwise visual effect doesn't work
                    tf.setImmediate(true);
                    comp = tf;
                }
                formForRenderingConfigurableFieldProperties.addComponent(comp);
            }
        }


    }
}
