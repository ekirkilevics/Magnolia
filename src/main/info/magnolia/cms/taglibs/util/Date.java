/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */



/**
 * patterns: http://java.sun.com/j2se/1.4.1/docs/api/java/text/SimpleDateFormat.html
 *   G	Era designator			Text				AD
 *   y	Year					Year				1996; 96
 *   M	Month in year			Month				July; Jul; 07
 *   w	Week in year			Number				27
 *   W	Week in month			Number				2
 *   D	Day in year				Number				189
 *   d	Day in month			Number				10
 *   F	Day of week in month	Number				2
 *   E	Day in week				Text				Tuesday; Tue
 *   a	Am/pm marker			Text				PM
 *   H	Hour in day (0-23)		Number				0
 *   k	Hour in day (1-24)		Number				24
 *   K	Hour in am/pm (0-11)	Number				0
 *   h	Hour in am/pm (1-12)	Number				12
 *   m	Minute in hour			Number				30
 *   s	Second in minute		Number				55
 *   S	Millisecond				Number				978
 *   z	Time zone				General time zone	Pacific Standard Time; PST; GMT-08:00
 *   Z	Time zone				RFC 822 time zone	-0800
 *
 *
 * */

package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.util.Resource;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;
import javax.jcr.PropertyType;
import java.util.Locale;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;


/**
 * Date: Apr 28, 2003
 * Time: 11:20:59 AM
 * @author Marcel Salathe
 * @version 1.1
 */


public class Date extends TagSupport{


    private static Logger log = Logger.getLogger(Date.class);

    private String pattern = "yyyy.MM.dd - HH:mm:ss";
    private String nodeDataName;
    private String language = "";

    private Content contentNode;
    private NodeData nodeData;
    private String actpage = "false";

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

	/**
	 * @deprecated
	 */
	public void setAtomName(String name) {
		this.setNodeDataName(name);
	}

    public void setNodeDataName(String nodeDataName) {
        this.nodeDataName = nodeDataName;
    }

    public void setActpage(String actpage) {
        this.actpage = actpage;
    }

    public void setLanguage(String language) {
        this.language = language;
    }



    public int doStartTag() {
        if (this.actpage == "true") {
            this.contentNode = Resource.getCurrentActivePage((HttpServletRequest)pageContext.getRequest());
        }
        else {
            this.contentNode = (Content) Resource.getLocalContentNode((HttpServletRequest)pageContext.getRequest());
            if (this.contentNode == null) {
                this.contentNode = (Content) Resource.getGlobalContentNode((HttpServletRequest)pageContext.getRequest());
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
        if (this.contentNode == null) return "";
        try {
            this.nodeData = this.contentNode.getNodeData(this.nodeDataName);
        } catch (AccessDeniedException e) {
            log.error(e.getMessage());
            return "";
        }
        if (!this.nodeData.isExist()) return "";
        if (this.nodeData.getDate() == null) return "";
        SimpleDateFormat formatter;
        if (this.language.equals("")) formatter = new SimpleDateFormat(this.pattern);
        else formatter = new SimpleDateFormat(this.pattern,new Locale(this.language));
        date = formatter.format(this.nodeData.getDate().getTime());
        //return this.nodeData.getDate().getTime().toString();
		return date;
    }
}
