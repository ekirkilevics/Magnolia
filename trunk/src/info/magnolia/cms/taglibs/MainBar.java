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



package info.magnolia.cms.taglibs;


import info.magnolia.cms.util.Resource;
import info.magnolia.cms.gui.inline.BarMain;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * Date: Apr 28, 2003
 * Time: 11:20:59 AM
 * @author Marcel Salathe
 * @author Sameer Charles
 * @version 1.1
 */


public class MainBar extends TagSupport {

    private HttpServletRequest request;
	private String paragraph=null;


    /**
     * <p>starts Admin tag</p>
     *
     * @return int
     */
    public int doStartTag() {
        this.request = (HttpServletRequest)pageContext.getRequest();
        return EVAL_BODY_INCLUDE;
    }



    /**
     * <p>print out</p>
     *
     * @return int
     */
    public int doEndTag() {
		/*
        if (!ServerInfo.isAdmin())
            return EVAL_PAGE;
        if (!Resource.getActivePage(this.request).isGranted(Permission.WRITE_PROPERTY))
            return EVAL_PAGE;
		*/
        try {
            this.display();
        } catch (Exception e) {}
        return EVAL_PAGE;
    }




        /**
     * <p>get the content path (Page or Node)</p>
     *
     * @return String path
     */
    private String getPath() {
        try {
            return Resource.getCurrentActivePage(this.request).getHandle();
        } catch (Exception re) {return "";}

    }



    /**
     * <p>set current content type, could be any developer defined name</p>
     *
	 /**
	  * @deprecated
     * @param type , paragraph type
     */
    public void setParFile(String type) {
        this.setParagraph(type);
    }

	/**
	 * <p>set paragarph type</p>
	 *
	 /**
	 *
	 * @param s , pargarph type
	 */
	public void setParagraph(String s) {
		this.paragraph=s;
	}



	/**
	 * @return pargraph type
	 */
	private String getParagraph() {
		return this.paragraph;
	}



    /**
     * <p>displays main admin bar</p>
     *
     * @throws java.io.IOException
     */
    private void display() throws IOException {
		BarMain bar=new BarMain(this.request);
		bar.setPath(this.getPath());
		bar.setParagraph(this.getParagraph());
		bar.setDefaultButtons();
		bar.placeDefaultButtons();
		bar.drawHtml(pageContext.getOut());
    }


}
