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

import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.cms.core.Content;

import javax.jcr.RepositoryException;

/**
 * Control for editing text in dialogs.
 */
public class EditControl extends AbstractDialogControl {

    private TextField field;

    private int rows = 0;
    private boolean wordwrap = false;
    private boolean secret = false;
    private boolean required = false;
    private String requiredErrorMessage;
    private int maxLength = -1;
    private String inputPrompt;

    public void addControl(Content storageNode, VerticalLayout layout) {

        field = new TextField();
        field.setRequired(required);
        field.setRequiredError(requiredErrorMessage);
        field.setMaxLength(maxLength);
        field.setInputPrompt(inputPrompt);
        field.setWordwrap(wordwrap);
        field.setRows(rows);
        field.setSecret(secret);
        field.addValidator(new StringLengthValidator("String must not be empty", 2, -1, false));
        layout.addComponent(field);

        if (storageNode != null) {
            field.setValue(storageNode.getNodeData(getName()).getString());
        }
    }

    public void validate() {
        field.validate();
    }

    public void save(Content storageNode) throws RepositoryException {
        storageNode.setNodeData(getName(), (String) field.getValue());
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
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
}
