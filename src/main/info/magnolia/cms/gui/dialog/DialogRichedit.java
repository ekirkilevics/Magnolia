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

import org.apache.log4j.Logger;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.SelectOption;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Edit;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.beans.config.ContentRepository;

import javax.servlet.jsp.JspWriter;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */ 
public class DialogRichedit extends DialogBox {
	private static Logger log = Logger.getLogger(DialogRichedit.class);
	private String richE="";
	private String richEPaste="";
	private ArrayList optionsToolboxStyleCssClasses=new ArrayList();
	private ArrayList optionsToolboxLinkCssClasses=new ArrayList();
	private ArrayList optionsToolboxLinkTargets=new ArrayList();

	public DialogRichedit(ContentNode configNode,Content websiteNode) throws RepositoryException {
		super(configNode,websiteNode);
	}

	public DialogRichedit() {

	}


	public void setRichE(String s) {this.richE=s;}
	public String getRichE() {return this.richE;}

	public void setRichEPaste(String s) {this.richE=s;}
	public String getRichEPaste() {return this.richEPaste;}

	public void setOptionsToolboxStyleCssClasses(ContentNode configNode) {
		ArrayList options=this.setOptionsToolbox(configNode,"optionsToolboxStyleCssClasses");
		this.setOptionsToolboxStyleCssClasses(options);
	}
	public void setOptionsToolboxStyleCssClasses(ArrayList l) {this.optionsToolboxStyleCssClasses=l;}
	public ArrayList getOptionsToolboxStyleCssClasses() {return this.optionsToolboxStyleCssClasses;}


	public void setOptionsToolboxLinkCssClasses(ContentNode configNode) {
		ArrayList options=this.setOptionsToolbox(configNode,"optionsToolboxLinkCssClasses");
		this.setOptionsToolboxLinkCssClasses(options);
	}
	public void setOptionsToolboxLinkCssClasses(ArrayList l) {this.optionsToolboxLinkCssClasses=l;}
	public ArrayList getOptionsToolboxLinkCssClasses() {return this.optionsToolboxLinkCssClasses;}

	public void setOptionsToolboxLinkTargets(ContentNode configNode) {
		ArrayList options=this.setOptionsToolbox(configNode,"optionsToolboxLinkTargets");
		this.setOptionsToolboxLinkTargets(options);
	}
	public void setOptionsToolboxLinkTargets(ArrayList l) {this.optionsToolboxLinkTargets=l;}
	public ArrayList getOptionsToolboxLinkTargets() {return this.optionsToolboxLinkTargets;}


	public ArrayList setOptionsToolbox(ContentNode configNode,String nodeName) {
		ArrayList options=new ArrayList();
		try {
			Iterator it=configNode.getContentNode(nodeName).getChildren().iterator();
			while (it.hasNext()) {
				ContentNode n=(ContentNode) it.next();
				String value=n.getNodeData("value").getString();
				SelectOption option=new SelectOption(null,value);
				if (n.getNodeData("label").isExist()) option.setLabel(n.getNodeData("label").getString());
				if (n.getNodeData("selected").getBoolean()==true) option.setId("default");
				options.add(option);
			}
			SelectOption lastOption=new SelectOption("","");
			lastOption.setSelected(true);
			options.add(lastOption);
		}
		catch (RepositoryException re) {}
		return options;
	}




	public void drawHtml(JspWriter out) {


		this.drawHtmlPre(out);


		if (this.getRichE().equals("true") || (this.getRichE().equals("") && this.getTopParent().getConfigValue("richE","").equals("true"))) {
			DialogSpacer spacer=new DialogSpacer();
			DialogLine line=new DialogLine();

			this.setSessionAttribute();
			//remove <span>s by <a>s so its readable by kupu
			String value=this.getValue("<br>");
			value=value.replaceAll("<span ","<a ");
			value=value.replaceAll("</span>","</a>");
			this.setValue(value);

			try {
				//modification of dialogBox
				out.println("</td></tr><tr><td style=\"padding-right:12px;\">");

				//#################
				//toolboxes
				//#################

				//toolbox paste
				String toolboxPasteType=this.getRichEPaste();
				if (toolboxPasteType.equals("")) toolboxPasteType=this.getTopParent().getConfigValue("richEPaste","false");

				if (this.getConfigValue("toolboxPaste","true").equals("true") && !toolboxPasteType.equals("false")) {
					//win only; clipboard on mac is clean already
					out.println(line.getHtml("100%"));

					out.println("<div class=\""+CSSCLASS_RICHETOOLBOXLABEL+"\">Clean copy/paste</b></div>");

					if (toolboxPasteType.equals("button")) {
						//ie/win
						out.println("<div class=\""+CSSCLASS_RICHETOOLBOXSUBLABEL+"\">");
						out.println("Paste text by using this button!<br><a href=javascript:mgnlDialogRichEPasteCleanHelp();>info</a>");
						out.println("</div>");

						out.println(spacer.getHtml(6));

						Button pastePaste=new Button();
						pastePaste.setLabel("Clean paste");
						pastePaste.setSmall(true);
						pastePaste.setOnclick("mgnlDialogRichEPasteClean('"+this.getName()+"',true);");
						out.println(pastePaste.getHtml());

					}
					else {
						//mozilla/win
						out.println("<div class=\""+CSSCLASS_RICHETOOLBOXSUBLABEL+"\">");
						out.println("Paste (Ctrl-V) text into the text field below, then use the buttons to add or insert it to the editor frame.<br><a href=javascript:mgnlDialogRichEPasteCleanHelp();>info</a>");
						out.println("</div>");

						out.println(spacer.getHtml(3));

						out.println("<textarea class=\""+CSSCLASS_EDIT+"\" name=\""+this.getName()+"-paste\" rows=\"2\" style=\"width:100%;\"></textarea>");

						out.println(spacer.getHtml(3));

						Button pasteAppend=new Button();
						pasteAppend.setLabel("Append text");
						pasteAppend.setSmall(true);
						pasteAppend.setOnclick("mgnlDialogRichEPasteTextarea('"+this.getName()+"',true);");
						out.println(pasteAppend.getHtml());

						Button pasteInsert=new Button();
						pasteInsert.setLabel("Insert text");
						pasteInsert.setSmall(true);
						pasteInsert.setOnclick("mgnlDialogRichEPasteTextarea('"+this.getName()+"',false);");
						out.println(pasteInsert.getHtml());
					}

					out.println(spacer.getHtml(36));
				}
				//END toolbox paste




				//toolbox link
				if (this.getConfigValue("toolboxLink","true").equals("true")) {
					out.println(line.getHtml("100%"));
					out.println("<div class=\""+CSSCLASS_RICHETOOLBOXLABEL+"\">Link</div>");

					//link: edit control (href)
					String linkEditName="kupu-link-input";
					Edit linkEdit=new Edit(linkEditName,"");
					linkEdit.setCssClass(CSSCLASS_EDIT);
					linkEdit.setSaveInfo(false);
					linkEdit.setCssStyles("width","100%");
					out.println(linkEdit.getHtml());

					out.println(spacer.getHtml(2));

					//link: button internal link browse
					Button linkButtonBrowse=new Button();
					//todo: extension
					String extension=this.getConfigValue("toolboxLinkExtension","html");
					String repository=this.getConfigValue("toolboxLinkRepository",ContentRepository.WEBSITE);
					linkButtonBrowse.setOnclick("mgnlDialogLinkOpenBrowser('"+linkEditName+"','"+repository+"','"+extension+"');");
					linkButtonBrowse.setSmall(true);
					linkButtonBrowse.setLabel("Internal link...");
					out.println(linkButtonBrowse.getHtml());

					//link: target
					if (this.getOptionsToolboxLinkTargets().size()>1) {
						out.println("<div class=\""+CSSCLASS_RICHETOOLBOXSUBLABEL+"\">Target</div>");
						Select control=new Select();
						control.setName("kupu-link-input-target");
						control.setSaveInfo(false);
						control.setCssClass(CSSCLASS_SELECT);
						control.setCssStyles("width","100%");
						control.setOptions(this.getOptionsToolboxLinkTargets());
						out.println(control.getHtml());
					}

					//link: css class
					if (this.getOptionsToolboxLinkCssClasses().size()>1) {
						out.println("<div class=\""+CSSCLASS_RICHETOOLBOXSUBLABEL+"\">Style</div>");
						Select control=new Select();
						control.setName("kupu-link-input-css");
						control.setSaveInfo(false);
						control.setCssClass(CSSCLASS_SELECT);
						control.setCssStyles("width","100%");
						control.setOptions(this.getOptionsToolboxLinkCssClasses());
						out.println(control.getHtml());
					}

					out.println(spacer.getHtml(3));

					//link: apply button
					Button linkButtonApply=new Button();
					linkButtonApply.setId("kupu-link-button");
					linkButtonApply.setLabel("Apply link");
					linkButtonApply.setSmall(true);
					out.println(linkButtonApply.getHtml());

					//link: remove button
					Button linkButtonRemove=new Button();
					linkButtonRemove.setId("kupu-link-button-remove");
					linkButtonRemove.setLabel("Remove link");
					linkButtonRemove.setSmall(true);
					out.println(linkButtonRemove.getHtml());


					out.println(spacer.getHtml(36));
				}
				//END toolbox link


				//toolbox css
				if (this.getConfigValue("toolboxStyle","false").equals("true")) {
					out.println(line.getHtml("100%"));
					out.println("<div class=\""+CSSCLASS_RICHETOOLBOXLABEL+"\">Text style</div>");

					if (this.getOptionsToolboxStyleCssClasses().size()>1) {
						Select control=new Select();
						control.setName(this.getName()+"-css-input-css");
						control.setSaveInfo(false);
						control.setCssClass(CSSCLASS_SELECT);
						control.setCssStyles("width","100%");
						control.setOptions(this.getOptionsToolboxStyleCssClasses());
						out.println(control.getHtml());
					}

					out.println(spacer.getHtml(3));

					//css: apply button
					Button cssButtonApply=new Button();
					cssButtonApply.setId(this.getName()+"-css-button");
					cssButtonApply.setLabel("Apply style");
					cssButtonApply.setSmall(true);
					out.println(cssButtonApply.getHtml());

					//css: remove button
					Button cssButtonRemove=new Button();
					cssButtonRemove.setId(this.getName()+"-css-button-remove");
					cssButtonRemove.setLabel("Remove style");
					cssButtonRemove.setSmall(true);
					out.println(cssButtonRemove.getHtml());
				}
				//END toolbox css



				//#################
				//END toolboxes
				//#################


				//modification of dialogBox
				out.println("</td><td>");


				//#################
				//toolbar
				//#################

				out.println("<div class=\"kupu-tb\" id=\"toolbar\">");
				out.println("<span id=\"kupu-tb-buttons\">");

				out.println("<span class=\"kupu-tb-buttongroup\">");
				if (this.getConfigValue("toolbarBold","true").equals("true")) out.println("<button type=\"button\" class=\"kupu-bold\" title=\"Bold\" onclick=\"kupuui.basicButtonHandler('bold');\">&nbsp;</button>");
				if (this.getConfigValue("toolbarItalic","true").equals("true")) out.println("<button type=\"button\" class=\"kupu-italic\" title=\"Italic\" onclick=\"kupuui.basicButtonHandler('italic');\">&nbsp;</button>");
				if (this.getConfigValue("toolbarUnderline","false").equals("true")) out.println("<button type=\"button\" class=\"kupu-underline\" title=\"Underline\" onclick=\"kupuui.basicButtonHandler('underline');\">&nbsp;</button>");
				out.println("</span>");

				out.println("<span class=\"kupu-tb-buttongroup\">");
				if (this.getConfigValue("toolbarSubscript","false").equals("true")) out.println("<button type=\"button\" class=\"kupu-subscript\" title=\"Subscript\" onclick=\"kupuui.basicButtonHandler('subscript');\">&nbsp;</button>");
				if (this.getConfigValue("toolbarSuperscript","false").equals("true")) out.println("<button type=\"button\" class=\"kupu-superscript\" title=\"Superscript\" onclick=\"kupuui.basicButtonHandler('superscript');\">&nbsp;</button>");
				out.println("</span>");

				if (this.getConfigValue("toolbarColors","false").equals("true")) {
					// kupu note: the event handlers are attached to these buttons dynamically, like for tools
					// mozilla (1.5) does not support font background color yet!
					out.println("<span class=\"kupu-tb-buttongroup\">");
					out.println("<button type=\"button\" class=\"kupu-forecolor\" id=\"kupu-forecolor\" title=\"Text Color\">&nbsp;</button>");
					out.println("<button type=\"button\" class=\"kupu-hilitecolor\" id=\"kupu-hilitecolor\" title=\"Background Color\">&nbsp;</button>");
					out.println("</span>");
				}

				if (this.getConfigValue("toolbarUndo","true").equals("true")) {
					out.println("	  <span class=\"kupu-tb-buttongroup\">");
				 	out.println("<button type=\"button\" class=\"kupu-undo\" title=\"Undo\" onclick=\"kupuui.basicButtonHandler('undo');\">&nbsp;</button>");
					out.println("<button type=\"button\" class=\"kupu-redo\" title=\"Redo\" onclick=\"kupuui.basicButtonHandler('redo');\">&nbsp;</button>");
					out.println("	  </span>");
				}

				if (this.getConfigValue("toolbarLists","true").equals("true")) {
					out.println("<span class=\"kupu-tb-buttongroup\">");
					//kupu note: list button events are set on the list tool
					out.println("<button type=\"button\" class=\"kupu-insertorderedlist\" title=\"Numbered List\" id=\"kupu-list-ol-addbutton\">&nbsp;</button>");
					out.println("<button type=\"button\" class=\"kupu-insertunorderedlist\" title=\"Unordered List\" id=\"kupu-list-ul-addbutton\">&nbsp;</button>");
					out.println("</span>");
					out.println("<select id=\"kupu-ulstyles\" class=\""+CSSCLASS_SELECT+"\">");
					out.println("  <option value=\"disc\">Disc</option>");
					out.println("  <option value=\"square\">Square</option>");
					out.println("  <option value=\"circle\">Circle</option>");
					out.println("  <option value=\"none\">no bullet</option>");
					out.println("</select>");
					out.println("<select id=\"kupu-olstyles\" class=\""+CSSCLASS_SELECT+"\">");
					out.println("  <option value=\"decimal\">1</option>");
					out.println("  <option value=\"upper-roman\">I</option>");
					out.println("  <option value=\"lower-roman\">i</option>");
					out.println("  <option value=\"upper-alpha\">A</option>");
					out.println("  <option value=\"lower-alpha\">a</option>");
					out.println("</select>");

				}



				out.println("</span>");
				out.println("</div>");

				//#################
				//END toolbar
				//#################



				//color palette
				out.println("<table id=\"kupu-colorchooser\" cellpadding=\"0\" cellspacing=\"0\" style=\"position: fixed; border-style: solid; border-color: #666666; border-width: 1px;\"> </table>");


				//#################
				//iframe
				//#################

				out.println("<iframe");
				out.println( "id=\""+this.getName()+"-kupu-editor\"");
				out.println(" class=\""+CSSCLASS_RICHEIFRAME+"\"");
				if (this.getConfigValue("height",null)!=null) out.println(" style=\"height:"+this.getConfigValue("height")+";\")");
				out.println(" frameborder=\"0\"");
			  	out.println(" src=\"/.magnolia/dialogs/richEIFrame.html?"+SESSION_ATTRIBUTENAME_DIALOGOBJECT+"="+this.getConfigValue(SESSION_ATTRIBUTENAME_DIALOGOBJECT)+"&mgnlCK="+new Date().getTime()+"\"");
			  	out.println(" reloadsrc=\"0\"");
				out.println(" usecss=\"1\"");
				out.println(" strict_output=\"1\"");
				out.println(" content_type=\"application/xhtml+xml\"");
				out.println(" scrolling=\"auto\"");
				out.println("></iframe>");
				out.println("<script>");
				out.println("mgnlRichEditors[mgnlRichEditors.length]='"+this.getName()+"';");
				out.println("</script>");

				//#################
				//END iframe
				//#################


				//#################
			   	//textarea to save data (data will be put into textarea on submit of form)
				//#################
				out.println("<div style=visibility:hidden;position:absolute;top:0px;left:-500px;>");
				Edit hiddenTextarea=new Edit(this.getName(),"");
				hiddenTextarea.setRows("5");
				hiddenTextarea.setIsRichEditValue(1);
				out.println(hiddenTextarea.getHtml());
				out.println("</div>");

			}
			catch (IOException ioe) {log.error("");}


		}
		else {
			// rich edit not supported: draw textarea
			Edit control=new Edit(this.getName(),this.getValue());
			control.setType(this.getConfigValue("type",PropertyType.TYPENAME_STRING));
			if (this.getConfigValue("saveInfo").equals("false")) control.setSaveInfo(false);
			control.setCssClass(CSSCLASS_EDIT);
			control.setRows(this.getConfigValue("rows","18"));
			control.setCssStyles("width",this.getConfigValue("width","100%"));

			try {
				out.println(control.getHtml());
			}
			catch (IOException ioe) {log.error("");}

		}

	this.drawHtmlPost(out);
	}


	public void drawHtmlEditor(JspWriter out) {
		try {
			out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
			out.println("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			out.println("<html><head>");

			//headers to prevent the browser from caching, these *must* be provided,
			//either in meta-tag form or as HTTP headers
			out.println("<meta http-equiv=\"Pragma\" content=\"no-cache\" />");
			out.println("<meta http-equiv=\"Cache-Control\" content=\"no-cache, must-revalidate\" />");
			out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
			out.println("<meta name=\"Effective_date\" content=\"None\" />");
			out.println("<meta name=\"Expiration_date\" content=\"None\" />");
			out.println("<meta name=\"Type\" content=\"Document\" />");
			//out.println("<meta name=\"Format\" content=\"text/html\" />");
			out.println("<meta name=\"Language\" content=\"\" />");
			out.println("<meta name=\"Rights\" content=\"\" />");
			out.println("<style type=\"text/css\">");
			out.println("body {font-family:verdana;font-size:11px;background-color:#ffffff;}");
			out.println("</style>");

			if (this.getConfigValue("cssFile",null)!=null) {
				out.println("<link href=\""+this.getConfigValue("cssFile")+"\" rel=\"stylesheet\" type=\"text/css\"/>");
			}

			out.println("<script>");
			out.println("document.insertText=function(value)");
			out.println("	{");
			out.println("	while (value.indexOf('\\n')!=-1)");
			out.println("		{");
			out.println("		value=value.replace('\\n','<br>');");
			out.println("		}");
			out.println("	var body=document.getElementsByTagName('body');");
			out.println("	value=body[0].innerHTML+value;");
			out.println("	body[0].innerHTML=value;");
			out.println("	}");
			out.println("</script>");

			out.println("</head>");
			out.println("<body leftmargin=\"1\" marginwidth=\"1\" topmargin=\"3\" marginheight=\"3\">");
			out.println(this.getValue());
			out.println("</body></html>");
		}
		catch (IOException ioe) {}
	}




}
