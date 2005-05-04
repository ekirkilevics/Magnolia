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

import info.magnolia.cms.servlets.MVCServlet;
import info.magnolia.cms.servlets.MVCServletHandler;
import info.magnolia.cms.util.RequestFormUtil;
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
     * TODO: Use the config to get a handler
     */
    protected MVCServletHandler getHandler(HttpServletRequest request, HttpServletResponse response) {
        // this is a workaround
        String paragraph = RequestFormUtil.getParameter(request, "mgnlParagraph");
        if(StringUtils.isNotEmpty(paragraph)){
            if(paragraph.indexOf(",") ==-1){
                return new ParagraphEditDialog(request, response);
            }
            else{
                return new ParagraphSelectDialog(request, response);
            }
        }
        else{
            return new DialogMVCHandler(request, response);
        }
        
    }
}