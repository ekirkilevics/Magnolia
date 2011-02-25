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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import info.magnolia.module.admincentral.dialog.I18nAwareComponent;

/**
 * A tab in a dialog. Holds a list of controls contained in the tab.
 */
public class DialogTab extends I18nAwareComponent {

    private String label;
    // do not leak internal instances anywhere or the issues arise later when content of the list is modified by observation.
    private List<DialogControl> fields = new ArrayList<DialogControl>();

    private DialogDefinition parent;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public synchronized List<DialogControl> getFields() {
        return new ArrayList<DialogControl>(fields);
    }

    public synchronized void setFields(Collection<DialogControl> fields) {
        this.fields.clear();
        this.fields.addAll(fields);
    }

    public synchronized void addField(DialogControl dialogField) {
        if (dialogField instanceof AbstractDialogControl) {
            // TODO: review - do we really want to do this? (enables possibility to inherit configuration from the parent, but delegates control over such inheritance down to the field impl itself)
           ((AbstractDialogControl) dialogField).setParent(this);
        }
        // automatically set focus on first field ... if other field request focus it can grab it as it will be rendered later
        if (fields.isEmpty()) {
            dialogField.setFocus(true);
        }
        fields.add(dialogField);
    }

    public DialogDefinition getParent() {
        return parent;
    }

    public void setParent(DialogDefinition parent) {
        this.parent = parent;
    }

    @Override
    public I18nAwareComponent getI18nAwareParent() {
        return this.parent;
    }
}