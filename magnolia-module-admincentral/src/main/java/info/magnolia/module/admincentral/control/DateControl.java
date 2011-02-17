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

import info.magnolia.cms.core.Content;

import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Window;

/**
 * Control for selecting dates in a dialog.
 */
public class DateControl extends AbstractDialogControl {

    private boolean required;
    private String requiredErrorMessage;
    private boolean time = false;

    // TODO vaadin has better resolution than just date or date+time

    // TODO locale and date format

    private DateField dateField;

    @Override
    protected Component getFieldComponent() {
        return dateField;
    }

    @Override
    public Component createFieldComponent(Content storageNode, Window mainWindow) {
        if (dateField != null) {
            throw new UnsupportedOperationException("Multiple calls to component creation are not supported.");
        }
        dateField = new DateField();
        dateField.setResolution(time ? DateField.RESOLUTION_MIN : DateField.RESOLUTION_DAY);
        if (isFocus()) {
            dateField.focus();
        }

        return dateField;
    }

    public void validate() {
        dateField.validate();
    }

    public void save(Content storageNode) {
    }

    public boolean isTime() {
        return time;
    }

    public void setTime(boolean time) {
        this.time = time;
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
