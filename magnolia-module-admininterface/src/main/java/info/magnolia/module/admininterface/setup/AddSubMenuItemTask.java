/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface.setup;

import info.magnolia.cms.core.Content;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AddSubMenuItemTask extends AddMainMenuItemTask {
    private final String parent;

    public AddSubMenuItemTask(String parent, String menuName, String label, String i18nBasename, String onClick, String icon, String orderBefore) {
        super(menuName, label, i18nBasename, onClick, icon, orderBefore);
        this.parent = parent;
    }

    public AddSubMenuItemTask(String parent, String menuName, String label, String onClick, String icon) {
        this(parent, menuName, label, null, onClick, icon, null);
    }

    protected Content getParentNode(InstallContext ctx) throws RepositoryException {
        final Content mainMenu = super.getParentNode(ctx);
        return mainMenu.getContent(parent);
    }
}
