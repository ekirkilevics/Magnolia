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
package info.magnolia.ui.admincentral.dialog.field;

import java.util.Collection;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

/**
 * TODO naming, package?
 * A base custom component/field comprising a text field and a button placed to its immediate right.
 * @author fgrilli
 *
 */
public class TextAndButtonField extends CustomComponent implements Field {

    private TextField field;
    private Button button;

    public TextAndButtonField() {
        field = new TextField();
        button = new Button();
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.addComponent(field);
        layout.addComponent(button);
        layout.setComponentAlignment(button, Alignment.MIDDLE_CENTER);
        setCompositionRoot(layout);
    }

    @Override
    public void focus() {
        super.focus();
    }

    @Override
    public boolean isInvalidCommitted() {
        return field.isInvalidAllowed();
    }

    @Override
    public void setInvalidCommitted(boolean isCommitted) {
        field.setInvalidCommitted(isCommitted);
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        field.commit();
    }

    @Override
    public void discard() throws SourceException {
        field.discard();
    }

    @Override
    public boolean isWriteThrough() {
        return field.isWriteThrough();
    }

    @Override
    public void setWriteThrough(boolean writeThrough) throws SourceException, InvalidValueException {
        field.setWriteThrough(writeThrough);
    }

    @Override
    public boolean isReadThrough() {
        return field.isReadThrough();
    }

    @Override
    public void setReadThrough(boolean readThrough) throws SourceException {
        field.setReadThrough(readThrough);
    }

    @Override
    public boolean isModified() {
        return field.isModified();
    }

    @Override
    public void addValidator(Validator validator) {
        field.addValidator(validator);
    }

    @Override
    public void removeValidator(Validator validator) {
        field.removeValidator(validator);
    }

    @Override
    public Collection<Validator> getValidators() {
        return field.getValidators();
    }

    @Override
    public boolean isValid() {
        return field.isValid();
    }

    @Override
    public void validate() throws InvalidValueException {
        field.validate();
    }

    @Override
    public boolean isInvalidAllowed() {
        return field.isInvalidAllowed();
    }

    @Override
    public void setInvalidAllowed(boolean invalidValueAllowed) throws UnsupportedOperationException {
        field.setInvalidAllowed(invalidValueAllowed);
    }

    @Override
    public Object getValue() {
        return field.getValue();
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
        field.setValue(newValue);
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

    @Override
    public void addListener(ValueChangeListener listener) {
        field.addListener(listener);
    }

    @Override
    public void removeListener(ValueChangeListener listener) {
        field.removeListener(listener);
    }

    @Override
    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
        field.valueChange(event);
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        field.setPropertyDataSource(newDataSource);
    }

    @Override
    public Property getPropertyDataSource() {
        return field.getPropertyDataSource();
    }

    @Override
    public int getTabIndex() {
        return field.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        field.setTabIndex(tabIndex);
    }

    @Override
    public boolean isRequired() {
        return field.isRequired();
    }

    @Override
    public void setRequired(boolean required) {
        field.setRequired(required);
    }

    @Override
    public void setRequiredError(String requiredMessage) {
        field.setRequiredError(requiredMessage);
    }

    @Override
    public String getRequiredError() {
        return field.getRequiredError();
    }

    public TextField getTextField() {
        return field;
    }

    public Button getButton() {
        return button;
    }
}