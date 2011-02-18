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

import java.util.Map;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import info.magnolia.module.admincentral.jcr.JCRUtil;

/**
 * Control for selecting one of a set of options from a drop down select box.
 */
public class SelectControl extends AbstractOptionGroupControl {

    private ComboBox comboBox;

    private String defaultValue;
    private boolean nullSelectionAllowed = false;
    private boolean required = false;
    private String requiredErrorMessage;
    private String inputPrompt;

    @Override
    protected Component getFieldComponent() {
        return comboBox;
    }

    @Override
    public Component createFieldComponent(Node storageNode, Window mainWindow) throws RepositoryException {
        if (comboBox != null) {
            throw new UnsupportedOperationException("Multiple calls to component creation are not supported.");
        }

        comboBox = new ComboBox();
        comboBox.setNullSelectionAllowed(nullSelectionAllowed);
        comboBox.setRequired(required);
        comboBox.setRequiredError(requiredErrorMessage);
        comboBox.setInputPrompt(inputPrompt);

        for (Map.Entry<String, String> entry : options.entrySet()) {
            comboBox.addItem(entry.getKey());
            comboBox.setItemCaption(entry.getKey(), entry.getValue());
        }

        if (storageNode != null) {
            String value = JCRUtil.getPropertyString(storageNode, getName());
            if (StringUtils.isNotEmpty(value))
                comboBox.select(value);
        } else {
            if (StringUtils.isNotEmpty(defaultValue))
                comboBox.setValue(defaultValue);
        }
        if (isFocus()) {
            comboBox.focus();
        }

        return comboBox;
    }

    public void validate() {
        comboBox.validate();
    }

    public void save(Node storageNode) throws RepositoryException {
        storageNode.setProperty(getName(), (String) comboBox.getValue());
    }

    public String getInputPrompt() {
        return inputPrompt;
    }

    public void setInputPrompt(String inputPrompt) {
        this.inputPrompt = inputPrompt;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isNullSelectionAllowed() {
        return nullSelectionAllowed;
    }

    public void setNullSelectionAllowed(boolean nullSelectionAllowed) {
        this.nullSelectionAllowed = nullSelectionAllowed;
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
}
