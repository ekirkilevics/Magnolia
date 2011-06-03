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
package info.magnolia.ckeditor;

import info.magnolia.ckeditor.dialog.definition.CKEditorFieldDefinition;
import info.magnolia.ckeditor.widgetset.client.ui.VCKEditorTextField;

import java.util.Map;


import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractField;

/**
 * Server side component for the VCKEditorTextField widget.
 *
 * Currently, this widget doesn't support being read-only because CKEditor doesn't.  But perhaps need the widgets
 * to only emit a DIV with the HTML code inside if it's read-only.
 * Adapted verbatim from http://vaadin.com/directory#addon/ckeditor-wrapper-for-vaadin.
 */
@com.vaadin.ui.ClientWidget(VCKEditorTextField.class)
public class CKEditorTextField extends AbstractField
implements FieldEvents.BlurNotifier, FieldEvents.FocusNotifier  {

    private static final long serialVersionUID = 2801471973845411928L;

    private CKEditorFieldDefinition config;
    private String version = "unknown";

    public CKEditorTextField() {
        super.setValue("");
        setWidth(100,UNITS_PERCENTAGE);
        setHeight(300,UNITS_PIXELS);
    }

    public CKEditorTextField(CKEditorFieldDefinition config) {
        this();
        setConfig(config);
    }

    public void setConfig(CKEditorFieldDefinition config) {
        this.config = config;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public void setValue(Object newValue) throws Property.ReadOnlyException, Property.ConversionException {
        if ( newValue == null )
            newValue = "";
        super.setValue(newValue, false);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        target.addVariable(this, VCKEditorTextField.VAR_TEXT, getValue().toString());

        target.addAttribute(VCKEditorTextField.ATTR_READONLY, isReadOnly());

        if (config != null) {
            if(config.getInPageConfig() != null) {
                target.addAttribute(VCKEditorTextField.ATTR_INPAGECONFIG, config.getInPageConfig());
            } else if(config.getJsConfigFile() != null){
                target.addAttribute(VCKEditorTextField.ATTR_CUSTOMCONFIG, config.getJsConfigFile());
            }
        }
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);

        // Sets the text
        if (variables.containsKey(VCKEditorTextField.VAR_TEXT) && ! isReadOnly()) {
            // Only do the setting if the string representation of the value
            // has been updated
            String newValue = (String)variables.get(VCKEditorTextField.VAR_TEXT);
            if ( newValue == null )
                newValue = "";

            final String oldValue = getValue().toString();
            if ( ! newValue.equals(oldValue) ) {
                boolean wasModified = isModified();
                setValue(newValue, true);

                // If the modified status changes repaint is needed after all.
                if (wasModified != isModified()) {
                    requestRepaint();
                }
            }
        }

        // Sets the CKEditor version
        if (variables.containsKey(VCKEditorTextField.VAR_VERSION)) {
            version = (String)variables.get(VCKEditorTextField.VAR_VERSION);
        }

        if (variables.containsKey(FocusEvent.EVENT_ID)) {
            fireEvent(new FocusEvent(this));
        }
        if (variables.containsKey(BlurEvent.EVENT_ID)) {
            fireEvent(new BlurEvent(this));
        }
    }


    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public void addListener(BlurListener listener) {
        addListener(BlurEvent.EVENT_ID, BlurEvent.class, listener,
                BlurListener.blurMethod);
    }

    @Override
    public void removeListener(BlurListener listener) {
        removeListener(BlurEvent.EVENT_ID, BlurEvent.class, listener);
    }

    @Override
    public void addListener(FocusListener listener) {
        addListener(FocusEvent.EVENT_ID, FocusEvent.class, listener,
                FocusListener.focusMethod);
    }

    @Override
    public void removeListener(FocusListener listener) {
        removeListener(FocusEvent.EVENT_ID, FocusEvent.class, listener);
    }

    @Override
    public void setHeight(float height, int unit) {
        super.setHeight(height,unit);
    }
    @Override
    public void setHeight(String height) {
        super.setHeight(height);
    }

    @Override
    public void detach() {
        super.detach();
    }
}
