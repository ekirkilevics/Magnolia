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
package info.magnolia.module.rest.dialog;

import info.magnolia.cms.core.Content;

import javax.jcr.RepositoryException;
import javax.ws.rs.core.MultivaluedMap;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateControl extends AbstractDialogItem {

    private String type;
    private String name;
    private String label;
    private Date value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Date getValue() {
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }

    public void bind(Content storageNode) throws RepositoryException {
        if (storageNode.hasNodeData(this.name)) {
            this.value = storageNode.getNodeData(this.name).getDate().getTime();
        }
    }

    public void bind(MultivaluedMap<String, String> parameters) throws ParseException {
        if (parameters.containsKey(this.name))
            this.value = new SimpleDateFormat("yyyy-MM-dd").parse(parameters.getFirst(this.name));
    }

    public void validate(ValidationResult validationResult) {
    }

    public void save(Content storageNode) throws RepositoryException {
        if (this.value != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.value);
            storageNode.setNodeData(this.name, calendar);
        }
    }
}
