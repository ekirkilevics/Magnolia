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
import info.magnolia.module.admininterface.dialogs.ParagraphEditDialog;
import info.magnolia.module.admininterface.dialogs.ParagraphSelectDialog;

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
        String dialogName = RequestFormUtil.getParameter(request, "mgnlDialog");
        if (StringUtils.isEmpty(dialogName)) {
            dialogName = StringUtils.substringAfterLast(request.getRequestURL().toString(), "/").replaceFirst(
                ".html",
                "");
        }

        DialogMVCHandler handler = null;
        // old paragrah dialog
        if (dialogName.equals("standard") || dialogName.equals("standard.jsp")) {
            // this is a workaround for the current paragraphs
            String paragraph = RequestFormUtil.getParameter(request, "mgnlParagraph");
            if (StringUtils.isNotEmpty(paragraph)) {
                if (paragraph.indexOf(",") == -1) {
                    Content configNode = ParagraphEditDialog.getConfigNode(request, paragraph);
                    handler = ConfiguredDialog.getConfiguredDialog(
                        paragraph,
                        configNode,
                        request,
                        response,
                        ParagraphEditDialog.class);
                }
                else {
                    handler = new ParagraphSelectDialog(request, response);
                }
            }
        }

        else {
            // try to get a registered handler
            handler = Store.getInstance().getDialogHandler(dialogName, request, response);

            // if not found asume that it is a path in the config repository
            if (handler == null) {
                Content configNode = ConfiguredDialog.getConfigNode(request, dialogName);
                // try to find a class property or return a ConfiguredDialog
                if (configNode != null) {
                    handler = ConfiguredDialog.getConfiguredDialog(dialogName, configNode, request, response);
                }
                else {
                    log.error("no config node found for dialog : " + dialogName);
                }
            }

            if (handler == null) {
                log.error("no dialog found: " + dialogName);
            }

        }
        return handler;
    }

}