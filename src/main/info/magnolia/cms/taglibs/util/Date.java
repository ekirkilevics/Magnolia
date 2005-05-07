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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.Resource;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


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

    private static Logger log = Logger.getLogger(Date.class);

    private String pattern = "yyyy.MM.dd - HH:mm:ss";

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
            this.contentNode = Resource.getCurrentActivePage((HttpServletRequest) pageContext.getRequest());
        }
        else {
            this.contentNode = Resource.getLocalContentNode((HttpServletRequest) pageContext.getRequest());
            if (this.contentNode == null) {
                this.contentNode = Resource.getGlobalContentNode((HttpServletRequest) pageContext.getRequest());
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
        String date = "";
        if (this.contentNode == null) {
            return "";
        }

        this.nodeData = this.contentNode.getNodeData(this.nodeDataName);

        if (!this.nodeData.isExist()) {
            return "";
        }
        if (this.nodeData.getDate() == null) {
            return "";
        }
        SimpleDateFormat formatter;
        if (StringUtils.isEmpty(this.language)) {
            formatter = new SimpleDateFormat(this.pattern);
        }
        else {
            formatter = new SimpleDateFormat(this.pattern, new Locale(this.language));
        }
        date = formatter.format(this.nodeData.getDate().getTime());
        // return this.nodeData.getDate().getTime().toString();
        return date;
    }
}
