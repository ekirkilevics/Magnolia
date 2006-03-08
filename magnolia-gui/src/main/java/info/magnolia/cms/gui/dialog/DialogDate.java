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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.i18n.MessagesManager;

import java.util.Calendar;
import java.util.Date;

import javax.jcr.PropertyType;

import org.apache.commons.lang.time.DateFormatUtils;


/**
 * @author Vinzenz Wyser
 * @version $Revision$ ($Author$)
 */
public class DialogDate extends DialogEditWithButton {

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogDate() {
    }

    /**
     * Customize the dialog.
     * @see info.magnolia.cms.gui.dialog.DialogEditWithButton#doBeforeDrawHtml()
     */
    protected void doBeforeDrawHtml() {
        super.doBeforeDrawHtml();

        // set buttonlabel in config
        this.getButton().setLabel(MessagesManager.get("dialog.date.select")); //$NON-NLS-1$
        this.getButton().setSaveInfo(false);
        this.getButton().setOnclick(
            "mgnlDialogOpenCalendar('" + this.getName() + "'," + this.getConfigValue("time", "false") + ");"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        String format = "yyyy-MM-dd"; //$NON-NLS-1$
        String pattern = "XXXX-XX-XX"; //$NON-NLS-1$
        if (!this.getConfigValue("time", "false").equals("false")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            format += "'T'HH:mm:ss"; //$NON-NLS-1$
            pattern += "TXX:XX:XX"; //$NON-NLS-1$
        }
        this.setConfig("onchange", "mgnlDialogDatePatternCheck(this,'" + pattern + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (this.getWebsiteNode() != null && this.getWebsiteNode().getNodeData(this.getName()).isExist()) {
            Calendar valueCalendar = this.getWebsiteNode().getNodeData(this.getName()).getDate();

            if (valueCalendar != null) {
                Date valueDate = valueCalendar.getTime();
                this.setValue(DateFormatUtils.formatUTC(valueDate, format));
            }
        }
        // check this!
        this.setConfig("type", this.getConfigValue("type", PropertyType.TYPENAME_DATE)); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
