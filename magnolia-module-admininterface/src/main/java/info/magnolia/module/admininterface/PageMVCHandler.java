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

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.servlets.CommandBasedMVCServletHandler;
import info.magnolia.cms.util.RequestFormUtil;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * This is the MVCHandler for simple pages. The properties with coresponding request parameters are set with BeanUtils.
 * @author Philipp Bracher
 * @version $Revision$
 */

public abstract class PageMVCHandler extends CommandBasedMVCServletHandler {

    /**
     * Logger
     */
    Logger log = LoggerFactory.getLogger(PageMVCHandler.class);

    /**
     * The name of the parameter passed by the request. Not used for simple pages.
     */
    protected static final String COMMAND_PARAMETER_NAME = "command";

    protected static final String COMMAND_SHOW = "show"; //$NON-NLS-1$

    protected static final String VIEW_SHOW = "show"; //$NON-NLS-1$

    /**
     * The posted multipart form. Use params for easy access.
     */
    private MultipartForm form;

    /**
     * The messages used for this page
     */
    private Messages msgs;

    /**
     * Parameters passed by the request.
     */
    private RequestFormUtil params;

    /**
     * Basename used to get messages
     */
    private String i18nBasename = MessagesManager.DEFAULT_BASENAME;

    /**
     * Constuctor
     * @param name
     * @param request
     * @param response
     */
    public PageMVCHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);

        setForm(MgnlContext.getPostedForm());
        setParams(new RequestFormUtil(request, getForm()));
    }

    /**
     * @see info.magnolia.cms.servlets.MVCServletHandlerImpl#init()
     * 
     * TODO ! init is called twice !!! once by content2bean, once by MVCServlet !
     */
    public void init() {
        super.init();
        if (StringUtils.isEmpty(this.getCommand())) {
            this.setCommand(COMMAND_SHOW);
        }
    }

    /**
     * This is an empty implementation return the default show view.
     * @return the view name
     */
    public String show() {
        return VIEW_SHOW;
    }

    /**
     * @param form The form to set.
     */
    protected void setForm(MultipartForm form) {
        this.form = form;
    }

    /**
     * @return Returns the form.
     */
    protected MultipartForm getForm() {
        return form;
    }

    /**
     * @param msgs The msgs to set.
     */
    protected void setMsgs(Messages msgs) {
        this.msgs = msgs;
    }

    /**
     * @return Returns the msgs.
     */
    protected Messages getMsgs() {
        if(msgs == null){
            msgs = MessagesManager.getMessages(getI18nBasename());
        }
        return msgs;
    }

    /**
     * @param params The params to set.
     */
    protected void setParams(RequestFormUtil params) {
        this.params = params;
    }

    /**
     * @return Returns the params.
     */
    protected RequestFormUtil getParams() {
        return params;
    }


    public String getI18nBasename() {
        return this.i18nBasename;
    }


    public void setI18nBasename(String basename) {
        this.i18nBasename = basename;
    }
}
