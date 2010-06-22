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
package info.magnolia.module.rest.dialogx;

import info.magnolia.cms.core.Content;

import javax.jcr.RepositoryException;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDialogItemContainer extends AbstractDialogItem implements DialogItemContainer {

    private List<DialogItem> subs = new ArrayList<DialogItem>();

    public List<DialogItem> getSubs() {
        return subs;
    }

    public void setSubs(List<DialogItem> subs) {
        this.subs = subs;
    }

    public void addSub(DialogItem sub) {
        sub.setParent(this);
        this.subs.add(sub);
    }

    public DialogItem getSubByName(String name) {
        for (DialogItem sub : subs) {
            if (sub.getName().equals(name))
                return sub;
        }
        return null;
    }

    public void bind(Content storageNode) throws RepositoryException {
        for (DialogItem sub : subs) {
            sub.bind(storageNode);
        }
    }

    public void bind(MultivaluedMap<String, String> parameters) throws Exception {
        for (DialogItem sub : subs) {
            sub.bind(parameters);
        }
    }

    public void validate(ValidationResult validationResult) {
        for (DialogItem sub : subs) {
            sub.validate(validationResult);
        }
    }

    public void save(Content storageNode) throws RepositoryException {
        for (DialogItem sub : subs) {
            sub.save(storageNode);
        }
    }
}
