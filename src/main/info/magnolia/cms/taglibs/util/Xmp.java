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




package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.util.Resource;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.BodyTagSupport;
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


public class Xmp extends BodyTagSupport{


    private static Logger log = Logger.getLogger(Xmp.class);


    public int doEndTag() {
        JspWriter out = pageContext.getOut();
        String xmpString = getBodyContent().getString();
        try {
            xmpString = changeToXmp(xmpString);
            out.print(xmpString);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return EVAL_PAGE;
    }

    private String changeToXmp(String string) {
        string = string.replaceAll("<","&lt;");
        string = string.replaceAll(">","&gt;");
        return string;
    }

}
