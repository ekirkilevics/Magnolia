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

import org.apache.commons.lang.time.DateFormatUtils;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

/**
 * Implementation for {@link PreviewView}.
 *
 * @author fgrilli
 */
public class PreviewViewImpl implements PreviewView {

    private static final String STATUS = "Status";
    private static final int DEFAULT_FIELD_WIDTH = 150;
    private static final String UUID = "UUID";
    private static final String PATH = "Path";
    private static final String LAST_MOD = "LastMod";
    private Label pathField = new Label();
    private Label uuid = new Label();
    private Label lastModField = new Label();
    private Label statusField = new Label();
    private GridLayout grid = new GridLayout(2,4);

    public PreviewViewImpl() {
        grid.setSizeFull();
        grid.setMargin(true);

        pathField.setWidth(DEFAULT_FIELD_WIDTH, Sizeable.UNITS_PIXELS);
        grid.addComponent(new Label(PATH),0,0);
        grid.addComponent(pathField,1,0);

        uuid.setWidth(DEFAULT_FIELD_WIDTH, Sizeable.UNITS_PIXELS);
        grid.addComponent(new Label(UUID),0,1);
        grid.addComponent(uuid,1,1);

        lastModField.setWidth(DEFAULT_FIELD_WIDTH, Sizeable.UNITS_PIXELS);
        grid.addComponent(new Label(LAST_MOD),0,2);
        grid.addComponent(lastModField,1,2);

        statusField.setWidth(25, Sizeable.UNITS_PIXELS);
        grid.addComponent(new Label(STATUS),0,3);
        grid.addComponent(statusField,1,3);
    }

    public void show(Item item) {
        try {
            pathField.setValue(item.getPath());
            if (item.isNode()) {
                Node node = (Node) item;
                uuid.setValue(node.getIdentifier());

                Calendar lastMod = JCRMetadataUtil.getLastModification(node);
                if (lastMod != null) {
                    lastModField.setValue(DateFormatUtils.format(JCRMetadataUtil.getLastModification(node).getTime(),UIUtil.DEFAULT_DATE_PATTERN));
                }

                statusField.setIcon(new ExternalResource(UIUtil.getActivationStatusIconURL(node)));
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public Component asVaadinComponent() {
        return grid;
    }
}