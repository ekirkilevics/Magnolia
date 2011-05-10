/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.DateUtil;

import java.util.Calendar;

import javax.jcr.PropertyType;

import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Vinzenz Wyser
 * @version $Revision$ ($Author$)
 */
public class DialogDate extends DialogEditWithButton {

    Logger log = LoggerFactory.getLogger(DialogDate.class);

    /**
     * Customize the dialog.
     * @see info.magnolia.cms.gui.dialog.DialogEditWithButton#doBeforeDrawHtml()
     */
    @Override
    protected void doBeforeDrawHtml() {
        super.doBeforeDrawHtml();

        this.getButton().setLabel(MessagesManager.get("dialog.date.select")); //$NON-NLS-1$
        this.getButton().setSaveInfo(false);


        String format = "yyyy-MM-dd"; //$NON-NLS-1$
        String jsFormat = "%Y-%m-%d"; //$NON-NLS-1$
        boolean displayTime = !this.getConfigValue("time", "false").equals("false");
        boolean singleClick = this.getConfigValue("doubleClick", "false").equals("false");
        if (displayTime) {
            format += "' 'HH:mm:ss";
            jsFormat += " %k:%M:%S";
        }

        String inputFieldId = this.getName();
        getButton().setId("butt_"+inputFieldId);
        String buttonId = this.getButton().getId();
        String calId = "cal_"+buttonId;
        getButton().setOnclick(calId+".show()");

        final String calendarScript = "<script type=\"text/javascript\">" +
                "            var "+calId+" = Calendar.setup({\n" +
                "                inputField     :    \""+inputFieldId+"\"," +
                "                ifFormat       :    \""+jsFormat+"\"," +
                "                showsTime      :    "+String.valueOf(displayTime)+"," +
                "                timeFormat     :    \"24\"," +
                "                cache          :    true,"+
                "                button         :    \""+buttonId+"\"," +
                "                singleClick    :    \""+String.valueOf(singleClick)+"\"," +
                //"                eventName      :    \"focus\", "+
                "                step           :    1" +
                "            });</script>";

        this.getButton().setHtmlPost(calendarScript);

        if (this.getStorageNode() != null && this.getStorageNode().getNodeData(this.getName()).isExist()) {
            Calendar valueCalendar = this.getStorageNode().getNodeData(this.getName()).getDate();

            // valueCalendar is in UTC turn it back into the current timezone
            if (valueCalendar != null) {
                Calendar local = DateUtil.getLocalCalendarFromUTC(valueCalendar);
                String value = DateFormatUtils.format(local.getTime(), format);
                this.setValue(value);
            }
        }

        this.setConfig("type", this.getConfigValue("type", PropertyType.TYPENAME_DATE)); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
