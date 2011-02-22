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
package info.magnolia.module.admincentral.control;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import info.magnolia.module.admincentral.jcr.JCRUtil;

/**
 * Control for editing text in dialogs.
 */
public class EditControl extends AbstractDialogControl {

    private TextField field;

    private int rows = 1;
    private boolean wordwrap = true;
    private boolean secret = false;
    private boolean required = false;
    private String requiredErrorMessage;
    private int maxLength = -1;
    private String inputPrompt;
    private String validationPattern;
    private String validationMessage;

    @Override
    protected Component getFieldComponent() {
        return field;
    }

    @Override
    public Component createFieldComponent(Node storageNode) throws RepositoryException {
        if (field != null) {
            throw new UnsupportedOperationException("Multiple calls to component creation are not supported.");
        }
        field = new TextField();
        field.setRequired(required);
        field.setRequiredError(requiredErrorMessage);
        field.setMaxLength(maxLength);
        field.setInputPrompt(inputPrompt);
        field.setWordwrap(wordwrap);
        field.setRows(rows);
        field.setSecret(secret);
        if (StringUtils.isNotEmpty(validationPattern))
            field.addValidator(new RegexpValidator(validationPattern, validationMessage));

        if (storageNode != null) {
            field.setValue(JCRUtil.getPropertyString(storageNode, getName()));
        }

        if (isFocus()) {
            field.focus();
        }

        return field;
    }

    public void validate() {
        field.validate();
    }

    public void save(Node storageNode) throws RepositoryException {
        storageNode.setProperty(getName(), (String) field.getValue());
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (field != null) {
            field.setRows(rows);
        }
        this.rows = rows;
    }

    public boolean isWordwrap() {
        return wordwrap;
    }

    public void setWordwrap(boolean wordwrap) {
        this.wordwrap = wordwrap;
    }

    public boolean isSecret() {
        return secret;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getRequiredErrorMessage() {
        return requiredErrorMessage;
    }

    public void setRequiredErrorMessage(String requiredErrorMessage) {
        this.requiredErrorMessage = requiredErrorMessage;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public String getInputPrompt() {
        return inputPrompt;
    }

    public void setInputPrompt(String inputPrompt) {
        this.inputPrompt = inputPrompt;
    }

    public String getValidationPattern() {
        return validationPattern;
    }

    public void setValidationPattern(String validationPattern) {
        this.validationPattern = validationPattern;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

}
