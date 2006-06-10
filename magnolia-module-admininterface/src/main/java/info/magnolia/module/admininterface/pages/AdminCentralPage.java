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
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.license.LicenseFileExtractor;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.Navigation;
import info.magnolia.module.admininterface.TemplatedMVCHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class AdminCentralPage extends TemplatedMVCHandler {

    /**
     * Required constructor.
     * @param name page name
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    public AdminCentralPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    public Navigation getNavigation() {
        Navigation navigation = new Navigation("/modules/adminInterface/config/menu", "mgnlNavigation");
        return navigation;
    }

    public String getVersionString() {
        return "("
            + LicenseFileExtractor.getInstance().get(LicenseFileExtractor.EDITION)
            + ", "
            + LicenseFileExtractor.getInstance().get(LicenseFileExtractor.VERSION_NUMBER)
            + ")";
    }

    public User getUser() {
        return MgnlContext.getUser();
    }

    public Messages getMessages() {
        return MessagesManager.getMessages();
    }

}
