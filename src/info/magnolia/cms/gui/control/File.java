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

/**
 *
 * User: enz
 * Date: Jun 7, 2004
 * Time: 2:18:26 PM
 *
 */
public class File extends ControlSuper {
	public static final String FILEPROPNAME_PROPERTIES="properties";
	public static final String FILEPROPNAME_CONTENTTYPE="contentType";
	public static final String FILEPROPNAME_SIZE="size";
	public static final String FILEPROPNAME_TEMPLATE="nodeDataTemplate";
	public static final String FILEPROPNAME_EXTENSION="extension";
	public static final String FILEPROPNAME_FILENAME="fileName";
	public static final String FILEPROPNAME_REMOVE="remove";


	public String cssClassFileName="";


	private String nodeDataTemplate=null;


	public File() {

	}

	public File(String name,String value) {
		super(name,value);
	}


	public File(String name,Content websiteNode) {
		super(name,websiteNode);
	}


	public void setCssClassFileName(String s) {this.cssClassFileName = s;}
	public String getCssClassFileName() {return this.cssClassFileName;}
	public String getHtmlCssClassFileName() {
		if (!this.getCssClassFileName().equals("")) return " class=\""+this.getCssClassFileName()+"\"";
		else return "";
	}

	public String getHtml() {
		String html="";
		html+=this.getHtmlBrowse();
		html+=this.getHtmlFileName();
		html+=this.getHtmlNodeDataTemplate();
		html+=this.getHtmlRemove();
		return html;
	}

	public String getHtmlBrowse() {
		String html="";

		html+="<input type=\"file\"";
		html+=" name=\""+this.getName()+"\"";
		html+=" id=\""+this.getName()+"\"";
		html+=" onchange=\"mgnlControlFileSetFileName('"+this.getName()+"')\"";
		html+=" onblur=\"mgnlControlFileSetFileName('"+this.getName()+"')\"";
		html+=this.getHtmlCssClass();
		//html+=this.getHtmlCssStyles();
		html+=">";

		Hidden control0=new Hidden(this.getName()+"_remove","");
		control0.setSaveInfo(false);
		html+=control0.getHtml();

		if (this.getSaveInfo()) html+=this.getHtmlSaveInfo();
		return html;
	}


	public String getFileName() {
		String fileName="";
		try {
			fileName=this.getWebsiteNode().getContentNode(this.getName()+"_"+FILEPROPNAME_PROPERTIES).getNodeData(FILEPROPNAME_FILENAME).getString();
		}
		catch (Exception e) {}
		return fileName;
	}


	public void setNodeDataTemplate(String s) {this.nodeDataTemplate=s;}
	public String getNodeDataTemplate() {
		String template=this.nodeDataTemplate;
		if (template==null) {
			try {
				template=this.getWebsiteNode().getContentNode(this.getName()+"_"+FILEPROPNAME_PROPERTIES).getNodeData(FILEPROPNAME_TEMPLATE).getString();
			}
			catch (Exception e) {}
		}
		return template;
	}


	public String getExtension() {
		String ext="";
		try {
			ext=this.getWebsiteNode().getContentNode(this.getName()+"_"+FILEPROPNAME_PROPERTIES).getNodeData(FILEPROPNAME_EXTENSION).getString();
		}
		catch (Exception e) {}
		return ext;
	}


	public String getHtmlFileName() {
		Edit control=new Edit(this.getName()+"_"+FILEPROPNAME_FILENAME,this.getFileName());
		control.setSaveInfo(false);
		control.setCssClass(this.getCssClassFileName());
		//control.setCssStyles(this.getCssStyles());
		control.setCssStyles("width","45%");

		return control.getHtml();
	}


	public String getHtmlNodeDataTemplate() {
		Hidden control=new Hidden(this.getName()+"_"+FILEPROPNAME_TEMPLATE,this.getNodeDataTemplate());
		control.setSaveInfo(false);

		return control.getHtml();
	}



	public String getHtmlRemove() {
		return getHtmlRemove("");
	}

	public String getHtmlRemove(String additionalOnclick) {

		Button control1=new Button();
		control1.setLabel("Remove file");
		control1.setCssClass("mgnlControlButtonSmall");
		control1.setOnclick(additionalOnclick+"mgnlControlFileRemove('"+this.getName()+"')");

		return control1.getHtml();
	}

	public String getHandle() {
		String path="";
		try {
			path=this.getWebsiteNode().getHandle()+"/"+this.getName();
		}
		catch (Exception e) {}
		return path;
	}

	public String getPath() {
		String path="";
		try {
			path=this.getWebsiteNode().getHandle()+"/"+this.getName()+"/"+this.getFileName()+"."+this.getExtension();
		}
		catch (Exception e) {}
		return path;
	}


}
