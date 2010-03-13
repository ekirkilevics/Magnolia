/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.servlets.MVCServlet;
import info.magnolia.cms.servlets.MVCServletHandler;
import info.magnolia.cms.util.RequestFormUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A servlet which delegates to a DialogMVCHandler retrieved through DialogHandlerManager.
 *
 * @see DialogMVCHandler
 * @see DialogHandlerManager
 * 
 * @author Philipp Bracher
 * @version $Id$
 */
public class DialogMVCServlet extends MVCServlet {
    private static final Logger log = LoggerFactory.getLogger(DialogMVCServlet.class);

    protected MVCServletHandler getHandler(HttpServletRequest request, HttpServletResponse response) {
        final String dialogName = getDialogName(request);

        DialogMVCHandler handler = null;

        if (StringUtils.isNotBlank(dialogName)) {
            // try to get a registered handler
            try {
                handler = DialogHandlerManager.getInstance().getDialogHandler(dialogName, request, response);
            }
            catch (InvalidDialogHandlerException e) {
                log.error("no dialog registered for name: " + dialogName, e);
                throw new ConfigurationException("no dialog registered for name: " + dialogName); //$NON-NLS-1$
            }
        }

        if (handler == null) {
            throw new ConfigurationException("no dialog registered for name: " + dialogName); //$NON-NLS-1$
        }

        return handler;
    }

    protected String getDialogName(HttpServletRequest request) {
        String dialogName = RequestFormUtil.getParameter(request, "mgnlDialog"); //$NON-NLS-1$

        // /.magnolia/dialogs/dialogName.html?foo=bar
        if (StringUtils.isEmpty(dialogName)) {
            final String pathInfo = request.getPathInfo();
            final int extensionIdx = pathInfo.lastIndexOf('.') >= 0 ? pathInfo.lastIndexOf('.') : pathInfo.length();
            dialogName = pathInfo.substring(1, extensionIdx);
        }
        return dialogName;
    }

}
