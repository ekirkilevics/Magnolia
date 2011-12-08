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
package info.magnolia.module.exchangesimple.pages;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.MgnlKeyPair;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.SecurityUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.admininterface.TemplatedMVCHandler;
import info.magnolia.module.exchangesimple.ExchangeSimpleModule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Activation UI.
 * 
 * @version $Id$
 */
public class ActivationPage extends TemplatedMVCHandler {

    private int keyLength;

    private boolean generateKeys;

    private boolean success;

    /**
     * @param name
     * @param request
     * @param response
     */
    public ActivationPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        keyLength = ModuleRegistry.Factory.getInstance().getModuleInstance(ExchangeSimpleModule.class).getActivationKeyLength();
    }

    /**
     * @see info.magnolia.cms.servlets.MVCServletHandlerImpl#getCommand()
     */
    @Override
    public String getCommand() {
        if (this.generateKeys) {
            return "generateKeys";
        }
        return super.getCommand();
    }

    /**
     * Actually perform backup.
     */
    public String generateKeys() throws Exception {
        if (!checkPermissions(request, "config", "/", Permission.WRITE)) {
            throw new ServletException(new AccessDeniedException(
            "Write permission needed to generate new key. Please try again after logging in with appropriate permissions."));
        }

        MgnlKeyPair keyPair = SecurityUtil.generateKeyPair(keyLength);
        SecurityUtil.updateKeys(keyPair);
        this.success = true;

        // TODO: distribute key to public instances

        return this.show();
    }

    public String getCurrentPublicKey() {
        return SecurityUtil.getPublicKey();
    }

    /**
     * Uses access manager to authorize this request.
     * 
     * @param request
     *            HttpServletRequest as received by the service method
     * @return boolean true if read access is granted
     */
    protected boolean checkPermissions(HttpServletRequest request, String repository, String basePath,
            long permissionType) {

        AccessManager accessManager = MgnlContext.getAccessManager(repository);
        if (accessManager != null) {
            if (!accessManager.isGranted(basePath, permissionType)) {
                return false;
            }
        }
        return true;
    }

    public Messages getMessages() {
        return MessagesManager.getMessages("info.magnolia.module.exchangesimple.messages");
    }

    public int getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }

    public boolean isGenerateKeys() {
        return generateKeys;
    }

    public void setGenerateKeys(boolean generateKeys) {
        this.generateKeys = generateKeys;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
