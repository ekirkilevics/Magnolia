/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All permissions reserved.
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