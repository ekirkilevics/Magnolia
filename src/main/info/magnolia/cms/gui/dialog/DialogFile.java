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



package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.File;
import info.magnolia.cms.security.AccessDeniedException;

import javax.servlet.jsp.JspWriter;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */ 
public class DialogFile extends DialogBox {
	private static Logger log = Logger.getLogger(DialogFile.class);

	private ArrayList imageExtensions=new ArrayList();


	public DialogFile(ContentNode configNode,Content websiteNode) throws RepositoryException {
		super(configNode,websiteNode);
		initImageExtensions();
		initIconExtensions();
	}

	public DialogFile() {
		initImageExtensions();
		initIconExtensions();
	}


	public ArrayList getImageExtensions() {return this.imageExtensions;}
	public void setImageExtensions(ArrayList l) {this.imageExtensions=l;}

	public void initImageExtensions() {
		this.getImageExtensions().add("jpg");
		this.getImageExtensions().add("jpeg");
		this.getImageExtensions().add("gif");
	}




	public void drawHtml(JspWriter out) {
		File control=new File(this.getName(),this.getWebsiteNode());
		control.setType(this.getConfigValue("type",PropertyType.TYPENAME_STRING));
		control.setSaveInfo(false); //set manualy below
		control.setCssClass(CSSCLASS_FILE);
		control.setCssClassFileName(CSSCLASS_EDIT);
		control.setCssStyles("width",this.getConfigValue("width","100%"));

		DialogSpacer spacer=new DialogSpacer();

		this.drawHtmlPre(out);
		try {
			String width=this.getConfigValue("width","100%");

			boolean showImage=false;
			if (this.getImageExtensions().contains(control.getExtension().toLowerCase())) showImage=true;

			String htmlControlBrowse=control.getHtmlBrowse();
			StringBuffer htmlControlFileName=new StringBuffer();
			htmlControlFileName.append("<span class=\""+CSSCLASS_DESCRIPTION+"\">Filename</span>");
			htmlControlFileName.append(spacer.getHtml(1));
			htmlControlFileName.append(control.getHtmlFileName()+"<span id=\""+this.getName()+"_fileNameExtension\">."+control.getExtension()+"</span>");

			String htmlContentEmpty=htmlControlBrowse+spacer.getHtml()+htmlControlFileName;


			out.println("<div id=\""+this.getName()+"_contentDiv\" style=\"width:100%;\">");

            boolean exists = false;
            try {
                if (this.getWebsiteNode() != null)
                    exists = this.getWebsiteNode().getNodeData(this.getName()).isExist();
            } catch (AccessDeniedException e) {
                log.error(e.getMessage());
            }
			if (!exists) {
				out.println(htmlContentEmpty);
				out.println("</div>");
			}
			else {
				if (showImage) {
					out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\""+width+"\">");
					out.println("<tr><td class=\""+CSSCLASS_FILEIMAGE+"\">");
					//todo: image thumbnail template
					//out.println("<img src=\""+THUMB_PATH+"?src="+control.getHandle()+"\" class=\""+CSSCLASS_FILEIMAGE+"\">");
					//tmp workaround: resize in html ...
					out.println("<img width=\"150\" src=\""+control.getHandle()+"\" class=\""+CSSCLASS_FILEIMAGE+"\">");
					out.println("</td><td>");
				}

				out.println(htmlControlFileName);

				if (!showImage) {
					String iconPath=this.getIconPath(control.getExtension());
					/*
					String iconPath=ICONS_PATH+ICONS_GENERAL;
					if (this.getIconExtensions().containsKey(control.getExtension().toLowerCase())) {
						iconPath=(String) this.getIconExtensions().get(control.getExtension().toLowerCase());
						if (iconPath.equals("")) iconPath=ICONS_PATH+control.getExtension().toLowerCase()+".gif";
					}
					*/

					out.println(spacer.getHtml());
					out.print("<a href="+control.getPath()+" target=\"_blank\">");
					out.print("<img src=\""+iconPath+"\" class=\""+CSSCLASS_FILEICON+"\" border=\"0\">");
					out.print(control.getFileName()+"."+control.getExtension()+"</a>");
				}


				out.println(spacer.getHtml(12));
				out.println(control.getHtmlRemove("mgnlDialogFileRemove('"+this.getName()+"');"));


				if (showImage) {
					out.println("</td></tr></table>");
				}
				out.println("</div>");

				out.println("<div style=\"position:absolute;top:-500;left:-500;visibility:hidden;\"><textarea id=\""+this.getName()+"_contentEmpty\">"+htmlContentEmpty+"</textarea></div>");

			}
			control.setSaveInfo(true);
			out.println(control.getHtmlSaveInfo());
			control.setNodeDataTemplate(this.getConfigValue("nodeDataTemplate",null));
			out.println(control.getHtmlNodeDataTemplate());
		}
		catch (IOException ioe) {log.error("");}
		this.drawHtmlPost(out);

	}

}
