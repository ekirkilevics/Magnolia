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

import javax.servlet.jsp.JspWriter;
import javax.jcr.RepositoryException;
import javax.jcr.PropertyType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.ButtonSet;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.security.AccessDeniedException;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */ 
public class DialogButtonSet extends DialogBox {
	private static Logger log = Logger.getLogger(DialogButtonSet.class);
	private int buttonType=ButtonSet.BUTTONTYPE_RADIO;

	public DialogButtonSet(ContentNode configNode,Content websiteNode) throws RepositoryException {
		super(configNode,websiteNode);
	}

	public DialogButtonSet() {

	}


	public void setOptions(ContentNode configNode,boolean setDefaultSelected) {
		//setDefaultSelected: does not work properly (no difference between never stored and removed...)
		//therefor do only use for radio, not for checkbox
		ArrayList options=new ArrayList();
		try {
			Iterator it=configNode.getContentNode("options").getChildren().iterator();
			while (it.hasNext()) {
				ContentNode n=((ContentNode) it.next());
				String value=n.getNodeData("value").getString();
				Button button=new Button(this.getName(),value);

				//if (n.getNodeData("label").isExist()) button.setLabel(n.getNodeData("label").getString());
				button.setLabel(n.getNodeData("label").getString());

				if (setDefaultSelected && n.getNodeData("selected").getBoolean()==true) button.setState(Button.BUTTONSTATE_PUSHED);
				options.add(button);
			}
		}
		catch (RepositoryException re) {}
		this.setOptions(options);
	}

	public void setOption(ContentNode configNode) {
		//checkboxSwitch -> only one option, value always true/false
		ArrayList options=new ArrayList();
		Button button=new Button(this.getName(),"");
        try {
		    button.setLabel(configNode.getNodeData("buttonLabel").getString());
        } catch (AccessDeniedException e) {
            log.error(e.getMessage());
            button.setLabel("");
        }
        try {
            if (configNode.getNodeData("selected").getBoolean()==true)
                button.setState(Button.BUTTONSTATE_PUSHED);
        } catch (AccessDeniedException e) {
            log.error(e.getMessage());
        }
		button.setValue("true");
		button.setOnclick("mgnlDialogShiftCheckboxSwitch('"+this.getName()+"');");
		options.add(button);
		this.setOptions(options);
	}



	public void drawHtmlPreSubs(JspWriter out) {
		this.drawHtmlPre(out);
	}

	public void drawHtmlPostSubs(JspWriter out) {
		this.drawHtmlPost(out);
	}

	public void setButtonType(int i) {this.buttonType=i;}
	public int getButtonType() {return this.buttonType;}

	public void drawHtml(JspWriter out) {
		this.drawHtmlPre(out);

		ButtonSet control;
		if (this.getConfigValue("valueType").equals("multiple")) {
			//checkbox
			ArrayList l=this.getValues();
			control=new ButtonSet(this.getName(),this.getValues());
			control.setValueType(ButtonSet.VALUETYPE_MULTIPLE);
		}
		else if (this.getButtonType()==ButtonSet.BUTTONTYPE_CHECKBOX) {
			//checkboxSwitch
			control=new ButtonSet(this.getName()+"_SWITCH",this.getValue());
		}
		else {
			//radio
			control=new ButtonSet(this.getName(),this.getValue());
		}


		control.setButtonType(this.getButtonType());
		control.setCssClass(CSSCLASS_BUTTONSETBUTTON);
		if (this.getConfigValue("saveInfo").equals("false")) control.setSaveInfo(false);

		control.setType(this.getConfigValue("type",PropertyType.TYPENAME_STRING));
				

		String width=this.getConfigValue("width",null);

		control.setButtonHtmlPre("<tr><td class=\""+CSSCLASS_BUTTONSETBUTTON+"\">");
		control.setButtonHtmlInter("</td><td class=\""+CSSCLASS_BUTTONSETLABEL+"\">");
		control.setButtonHtmlPost("</td></tr>");


		int cols=Integer.valueOf(this.getConfigValue("cols","1")).intValue();
		if (cols>1) {
			width="100%"; //outer table squeezes inner table if outer's width is not defined...
			control.setHtmlPre(control.getHtmlPre()+"<tr>");
			control.setHtmlPost("</tr>"+control.getHtmlPost());
			int item=1;
			int itemsPerCol=(int) Math.ceil(this.getOptions().size()/((double) cols));
			for (int i=0;i<this.getOptions().size();i++) {
				Button b=(Button) this.getOptions().get(i);
				if (item==1) b.setHtmlPre("<td><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"+control.getButtonHtmlPre());
				if (item==itemsPerCol) {
					b.setHtmlPost(control.getButtonHtmlPost()+"</table></td><td class=\""+CSSCLASS_BUTTONSETINTERCOL+"\"></td>");
					item=1;
				}
				else item++;
			}
			//very last button: close table
			((Button) this.getOptions().get(this.getOptions().size()-1)).setHtmlPost(control.getButtonHtmlPost()+"</table>");
		}


		if (width!=null) control.setHtmlPre("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\""+width+"\">");

		control.setButtons(this.getOptions());

		try {
			out.println(control.getHtml());
			if (control.getButtonType()==ButtonSet.BUTTONTYPE_CHECKBOX && control.getValueType()!=ButtonSet.VALUETYPE_MULTIPLE) {
				//checkboxSwitch: value is stored in a hidden field (allows default selecting)
				String value=this.getValue();
				if (value.equals("")) {
					if (this.getConfigValue("selected").equals("true")) value="true";
					else value="false";
				}
				out.println(new Hidden(this.getName(),value).getHtml());
			}
		}
		catch (IOException ioe) {log.error("");}

		this.drawHtmlPost(out);
	}


}
