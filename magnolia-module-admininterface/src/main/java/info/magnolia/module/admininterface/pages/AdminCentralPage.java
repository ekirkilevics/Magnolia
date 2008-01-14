/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.license.LicenseFileExtractor;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.AlertUtil;
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
    
    public String getMessage(){
        if(AlertUtil.isMessageSet(MgnlContext.getSystemContext())){
            String msg = AlertUtil.getMessage(MgnlContext.getSystemContext());
            return "<b>" + MessagesManager.getWithDefault(msg, msg) + "</b>";
        }
        return null;
    }
    
    public String getMessageCSSClass(){
        if(AlertUtil.isExceptionSet()){
            return "messageBoxError";           
        }
        else{
            return "messageBoxWarn";
        }
    }

}
