/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.admininterface.config;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class RepositoryConfiguration extends BaseConfiguration implements Comparable{

    private List permissions = new ArrayList();

    private List aclTypes = new ArrayList();

    boolean chooseButton = true;

    public List getPermissions() {
        return this.permissions;
    }

    public void setPermissions(List permissions) {
        this.permissions = permissions;
    }

    public void addPermission(PermissionConfiguration permission) {
        this.permissions.add(permission);
    }

    public List getAclTypes() {
        return this.aclTypes;
    }

    public void setAclTypes(List patternTypes) {
        this.aclTypes = patternTypes;
    }

    public void addAclType(AclTypeConfiguration type) {
        this.aclTypes.add(type);
    }

    public String toViewPattern(String path) {
        String cleanPattern = StringUtils.removeEnd(path, "/*");

        if (StringUtils.isEmpty(cleanPattern)) {
            return "/";
        }
        return cleanPattern;
    }


    public boolean isChooseButton() {
        return this.chooseButton;
    }


    public void setChooseButton(boolean chooseButton) {
        this.chooseButton = chooseButton;
    }

    public int compareTo(Object o) {
        return this.getI18nLabel().compareToIgnoreCase(((RepositoryConfiguration)o).getI18nLabel());
    }

}
