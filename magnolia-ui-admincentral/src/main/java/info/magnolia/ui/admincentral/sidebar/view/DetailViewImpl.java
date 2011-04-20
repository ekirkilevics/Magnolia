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
package info.magnolia.ui.admincentral.sidebar.view;

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.util.JCRMetadataUtil;
import info.magnolia.ui.admincentral.util.UIUtil;

import java.util.Calendar;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Form;
import com.vaadin.ui.TextField;

/**
 * Implementation for {@link DetailView}.
 *
 * @author fgrilli
 */
public class DetailViewImpl implements DetailView {

    private static final String STATUS = "Status";
    private static final int DEFAULT_FIELD_WIDTH = 250;
    private static final String UUID = "UUID";
    private static final String PATH = "Path";
    private static final String LAST_MOD = "LastMod";
    private Form form = new Form();

    public DetailViewImpl() {
        TextField pathField = new TextField(PATH);
        pathField.setWidth(DEFAULT_FIELD_WIDTH, Sizeable.UNITS_PIXELS);
        pathField.setEnabled(false);
        form.addField(PATH, pathField);

        TextField uuid = new TextField(UUID);
        uuid.setWidth(DEFAULT_FIELD_WIDTH, Sizeable.UNITS_PIXELS);
        uuid.setEnabled(false);
        form.addField(UUID, uuid);

        DateField lastMod = new DateField(LAST_MOD);
        lastMod.setDateFormat(UIUtil.DEFAULT_DATE_PATTERN);
        lastMod.setEnabled(false);
        form.addField(LAST_MOD, lastMod);

        TextField statusField = new TextField(STATUS);
        statusField.setWidth(25, Sizeable.UNITS_PIXELS);
        statusField.setEnabled(false);
        form.addField(STATUS, statusField);
    }

    public void showDetails(Item item) {
        try {
            form.getField(PATH).setValue(item.getPath());
            if (item.isNode()) {
                Node node = (Node) item;
                form.getField(UUID).setValue(node.getIdentifier());

                Calendar lastMod = JCRMetadataUtil.getLastModification(node);
                if (lastMod != null) {
                    form.getField(LAST_MOD).setValue(JCRMetadataUtil.getLastModification(node).getTime());
                }

                form.getField(STATUS).setIcon(new ExternalResource(UIUtil.getActivationStatusIconURL(node)));
                form. getField(STATUS).setValue(JCRMetadataUtil.getMetaData(node).getActivationStatus());
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public Component asVaadinComponent() {
        return form;
    }
}