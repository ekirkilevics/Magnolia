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



package info.magnolia.cms.gui.control;

import info.magnolia.cms.core.Content;
import sun.misc.BASE64Decoder;

import java.io.IOException;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */ 
public class Password  extends ControlSuper {
	public Password() {

	}

	public Password(String name,String value) {
		super(name,value);
	}

	public Password(String name,Content websiteNode) {
		super(name,websiteNode);
	}

	public String getHtml() {
		StringBuffer html=new StringBuffer();

		String valueDecoded="";
		String value="";

		if (this.getEncoding()==ENCODING_BASE64) {
			//show number of characters (using spaces)
			try {
				BASE64Decoder decoder = new BASE64Decoder();
				valueDecoded = new String(decoder.decodeBuffer(this.getValue()));
			} catch (IOException ioe) {ioe.printStackTrace();}
			//System.out.println("\nvalue: "+this.getValue());
			//System.out.println("valueDecoded: "+valueDecoded);
			for (int i=0;i<valueDecoded.length();i++) {
				value+=" ";
			}
		}
		else if (this.getEncoding()==ENCODING_UNIX) {
			value="";
		}
		else {
			value=this.getValue();
		}


		html.append("<input type=\"password\"");
		html.append(" name=\""+this.getName()+"\"");
		html.append(" id=\""+this.getName()+"\"");
		html.append(" value=\""+value+"\"");
		html.append(getHtmlEvents());
		html.append(this.getHtmlCssClass());
		html.append(this.getHtmlCssStyles());
		html.append(">");
		if (this.getSaveInfo()) html.append(this.getHtmlSaveInfo());
		return html.toString();
	}

}
