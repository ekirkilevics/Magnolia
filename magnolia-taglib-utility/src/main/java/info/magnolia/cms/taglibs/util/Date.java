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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.taglibs.Resource;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @jsp.tag name="date" body-content="empty"
 * @deprecated see cms:out.
 *
 * @author Marcel Salathe
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
     * Date pattern. Defaults to "yyyy.MM.dd - HH:mm:ss".
     * See the java.text.SimpleDateFormat javadoc for details.
     *
     * @deprecated
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * @deprecated
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setAtomName(String name) {
        this.setNodeDataName(name);
    }

    /**
     * Where the date comes from.
     * @deprecated
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setNodeDataName(String nodeDataName) {
        this.nodeDataName = nodeDataName;
    }

    /**
     * If "true", atom is taken from the currently active page.
     * @deprecated
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setActpage(boolean actpage) {
        this.actpage = actpage;
    }

    /**
     * Locale string (see java.util.Locale)
     * @deprecated
     * @jsp.attribute required="false" rtexprvalue="true"
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
