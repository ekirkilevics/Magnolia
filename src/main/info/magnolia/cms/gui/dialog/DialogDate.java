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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.ContextMessages;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogDate extends DialogEditWithButton {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogLink.class);

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogDate() {
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        // set buttonlabel in config
        this.getButton().setLabel(ContextMessages.get(getRequest(), "dialog.date.select"));
        this.getButton().setSaveInfo(false);
        this.getButton().setOnclick(
            "mgnlDialogOpenCalendar('" + this.getName() + "'," + this.getConfigValue("time", "false") + ");");
        String format = "yyyy-MM-dd";
        String pattern = "XXXX-XX-XX";
        if (!this.getConfigValue("time", "false").equals("false")) {
            format += "'T'HH:mm:ss";
            pattern += "TXX:XX:XX";
        }
        this.setConfig("onchange", "mgnlDialogDatePatternCheck(this,'" + pattern + "');");
        if (this.getWebsiteNode() != null && this.getWebsiteNode().getNodeData(this.getName()).isExist()) {
            Calendar valueCalendar = this.getWebsiteNode().getNodeData(this.getName()).getDate();
            Date valueDate = valueCalendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            this.setValue(sdf.format(valueDate));
            this.clearWebsiteNode(); // workaround so the value is taken... hm, pfusch
        }
        // check this!
        this.setConfig("type", this.getConfigValue("type", PropertyType.TYPENAME_DATE));
    }

}
