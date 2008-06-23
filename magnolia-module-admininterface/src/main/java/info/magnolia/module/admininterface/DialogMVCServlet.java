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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.servlets.MVCServlet;
import info.magnolia.cms.servlets.MVCServletHandler;
import info.magnolia.cms.util.RequestFormUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Philipp Bracher
 * @version $Id$
 */
public class DialogMVCServlet extends MVCServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DialogMVCServlet.class);

    /**
     *
     */
    protected MVCServletHandler getHandler(HttpServletRequest request, HttpServletResponse response) {
        String dialogName = RequestFormUtil.getParameter(request, "mgnlDialog"); //$NON-NLS-1$

        if (StringUtils.isEmpty(dialogName)) {
            if (StringUtils.isEmpty(dialogName)) {
                dialogName = (String) request.getAttribute("javax.servlet.include.request_uri"); //$NON-NLS-1$
                if (StringUtils.isEmpty(dialogName)) {
                    dialogName = (String) request.getAttribute("javax.servlet.forward.servlet_path"); //$NON-NLS-1$
                }
                if (StringUtils.isEmpty(dialogName)) {
                    dialogName = request.getRequestURI();
                }
                dialogName = StringUtils.replaceOnce(StringUtils.substringAfterLast(dialogName, "/dialogs/"), ".html", //$NON-NLS-1$ //$NON-NLS-2$
                    StringUtils.EMPTY);
            }
        }

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

}
