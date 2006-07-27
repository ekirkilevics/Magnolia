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
import info.magnolia.cms.util.DateUtil;

import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;

import javax.jcr.PropertyType;

import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version $Revision$ ($Author$)
 */
public class DialogDate extends DialogEditWithButton {

    Logger log = LoggerFactory.getLogger(DialogDate.class);

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

        this.getButton().setLabel(MessagesManager.get("dialog.date.select")); //$NON-NLS-1$
        this.getButton().setSaveInfo(false);
        this.getButton().setOnclick("Calendar.show()");

        String format = "yyyy-MM-dd"; //$NON-NLS-1$
        String jsFormat = "%Y-%m-%d"; //$NON-NLS-1$
        boolean displayTime = !this.getConfigValue("time", "false").equals("false");
        boolean singleClick = this.getConfigValue("doubleClick", "false").equals("false");
        if (displayTime) {
            format += "' 'HH:mm:ss";
            jsFormat += " %k:%M:%S";
        }

        String inputFieldId = this.getName();
        String buttonId = this.getButton().getId();

        final String calendarScript = "<script type=\"text/javascript\">" +
                "            Calendar.setup({\n" +
                "                inputField     :    \""+inputFieldId+"\"," +
                "                ifFormat       :    \""+jsFormat+"\"," +
                "                showsTime      :    "+String.valueOf(displayTime)+"," +
                "                timeFormat     :    \"24\"," +
                "                cache          :    true,"+
                "                button         :    \""+buttonId+"\"," +
                "                singleClick    :    \""+String.valueOf(singleClick)+"\"," +
                "                step           :    1" +
                "            });</script>";

        this.getButton().setHtmlPost(calendarScript);

        if (this.getWebsiteNode() != null && this.getWebsiteNode().getNodeData(this.getName()).isExist()) {
            Calendar valueCalendar = this.getWebsiteNode().getNodeData(this.getName()).getDate();

            // valueCalendar is in UTC turn it back into the current timezone
            if (valueCalendar != null) {
                Calendar local = DateUtil.getLocalCalendarFromUTC(valueCalendar);
                String value = DateFormatUtils.format(local.getTime(), format);
                log.info(value);
                this.setValue(value);
            }
        }
        // check this!
        this.setConfig("type", this.getConfigValue("type", PropertyType.TYPENAME_DATE)); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
