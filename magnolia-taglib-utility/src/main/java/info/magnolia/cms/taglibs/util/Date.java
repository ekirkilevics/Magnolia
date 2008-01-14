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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.Resource;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Marcel Salathe
 * @deprecated
 * @version $Revision $ ($Author $)
 */
public class Date extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static Logger log = LoggerFactory.getLogger(Date.class);

    private String pattern = "yyyy.MM.dd - HH:mm:ss"; //$NON-NLS-1$

    private String nodeDataName;

    private String language;

    private transient Content contentNode;

    private transient NodeData nodeData;

    private boolean actpage;

    /**
     * Date pattern. see http://java.sun.com/j2se/1.4.1/docs/api/java/text/SimpleDateFormat.html
     * 
     * <pre>
     *   G  Era designator          Text                AD
     *   y  Year                    Year                1996; 96
     *   M  Month in year           Month               July; Jul; 07
     *   w  Week in year            Number              27
     *   W  Week in month           Number              2
     *   D  Day in year             Number              189
     *   d  Day in month            Number              10
     *   F  Day of week in month    Number              2
     *   E  Day in week             Text                Tuesday; Tue
     *   a  Am/pm marker            Text                PM
     *   H  Hour in day (0-23)      Number              0
     *   k  Hour in day (1-24)      Number              24
     *   K  Hour in am/pm (0-11)    Number              0
     *   h  Hour in am/pm (1-12)    Number              12
     *   m  Minute in hour          Number              30
     *   s  Second in minute        Number              55
     *   S  Millisecond             Number              978
     *   z  Time zone               General time zone   Pacific Standard Time; PST; GMT-08:00
     *   Z  Time zone               RFC 822 time zone   -0800
     * </pre>
     * 
     * @deprecated
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * @deprecated
     */
    public void setAtomName(String name) {
        this.setNodeDataName(name);
    }

    /**
     * @deprecated
     * @param nodeDataName
     */
    public void setNodeDataName(String nodeDataName) {
        this.nodeDataName = nodeDataName;
    }

    /**
     * @deprecated
     * @param actpage
     */
    public void setActpage(boolean actpage) {
        this.actpage = actpage;
    }

    /**
     * @deprecated
     * @param language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        if (this.actpage) {
            this.contentNode = Resource.getCurrentActivePage();
        }
        else {
            this.contentNode = Resource.getLocalContentNode();
            if (this.contentNode == null) {
                this.contentNode = Resource.getGlobalContentNode();
            }
        }
        String printDate = getDateString();
        JspWriter out = pageContext.getOut();
        try {
            out.print(printDate);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return EVAL_PAGE;
    }

    public String getDateString() {
        String date = null;
        if (this.contentNode == null) {
            return StringUtils.EMPTY;
        }

        this.nodeData = this.contentNode.getNodeData(this.nodeDataName);

        if (!this.nodeData.isExist()) {
            return StringUtils.EMPTY;
        }
        if (this.nodeData.getDate() == null) {
            return StringUtils.EMPTY;
        }
        SimpleDateFormat formatter;
        if (StringUtils.isEmpty(this.language)) {
            formatter = new SimpleDateFormat(this.pattern);
        }
        else {
            formatter = new SimpleDateFormat(this.pattern, new Locale(this.language));
        }
        date = formatter.format(this.nodeData.getDate().getTime());

        return date;
    }
}
