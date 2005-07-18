/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.servlets.MVCServlet;
import info.magnolia.cms.servlets.MVCServletHandler;
import info.magnolia.cms.util.RequestFormUtil;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


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
    private static Logger log = Logger.getLogger(DialogMVCServlet.class);

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
                handler = Store.getInstance().getDialogHandler(dialogName, request, response);
            }
            catch (InvalidDialogHandlerException e) {
                log.info("can't find handler will try to load directly from the config", e); //$NON-NLS-1$
                Content configNode = ConfiguredDialog.getConfigNode(request, dialogName);
                // try to find a class property or return a ConfiguredDialog
                if (configNode != null) {
                    handler = ConfiguredDialog.getConfiguredDialog(dialogName, configNode, request, response);
                }
                else {
                    log.error("no config node found for dialog : " + dialogName); //$NON-NLS-1$
                }
            }
        }

        if (handler == null) {
            log.error("no dialog found: " + dialogName); //$NON-NLS-1$
        }

        return handler;
    }

}