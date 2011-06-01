// Copyright (C) 2010-2011 Yozons, Inc.
// CKEditor for Vaadin - Widget linkage for using CKEditor within a Vaadin application.
//
// This software is released under the Apache License 2.0 <http://www.apache.org/licenses/LICENSE-2.0.html>
//
// This software was originally based on the Vaadin incubator component TinyMCEEditor written by Matti Tahvonen.
// It was later converted to extend AbstractField instead of com.vaadin.ui.TextField, at which time some of the code from TextField was used here.
//
package org.vaadin.openesignforms.ckeditor;

import java.util.Map;
import java.util.Set;

import org.vaadin.openesignforms.ckeditor.widgetset.client.ui.VCKEditorTextField;

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
 */
@com.vaadin.ui.ClientWidget(VCKEditorTextField.class)
public class CKEditorTextField extends AbstractField
	implements FieldEvents.BlurNotifier, FieldEvents.FocusNotifier  {

	private static final long serialVersionUID = 2801471973845411928L;

	private CKEditorConfig config;
	private String version = "unknown";

	public CKEditorTextField() {
		super.setValue("");
		setWidth(100,UNITS_PERCENTAGE);
		setHeight(300,UNITS_PIXELS);
	}

	public CKEditorTextField(CKEditorConfig config) {
		this();
		setConfig(config);
	}

	public void setConfig(CKEditorConfig config) {
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
		    } else if(config.getCustomConfig() != null){
			target.addAttribute(VCKEditorTextField.ATTR_CUSTOMCONFIG, config.getCustomConfig());
		    }

			if ( config.hasWriterRules() ) {
				int i = 0;
				Set<String> tagNameSet = config.getWriterRulesTagNames();
				for( String tagName : tagNameSet ) {
					target.addAttribute(VCKEditorTextField.ATTR_WRITERRULES_TAGNAME+i, tagName);
					target.addAttribute(VCKEditorTextField.ATTR_WRITERRULES_JSRULE+i, config.getWriterRuleByTagName(tagName));
					++i;
				}
			}

			if ( config.hasWriterIndentationChars() ) {
				target.addAttribute(VCKEditorTextField.ATTR_WRITER_INDENTATIONCHARS, config.getWriterIndentationChars());
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
