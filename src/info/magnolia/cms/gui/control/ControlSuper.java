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

import org.apache.log4j.Logger;

import javax.jcr.PropertyType;
import javax.servlet.http.HttpServletRequest;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Enumeration;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.beans.config.ItemType;

/**
 *
 * User: enz
 * Date: May 18, 2004
 * Time: 9:38:20 AM
 *
 */
public class ControlSuper implements ControlInterface{
	public static final int BUTTONTYPE_PUSHBUTTON=0;
	public static final int BUTTONTYPE_CHECKBOX=1;
	public static final int BUTTONTYPE_RADIO=2;

	public static final int BUTTONSTATE_NORMAL = 0;
	public static final int BUTTONSTATE_MOUSEOVER = 1;
	public static final int BUTTONSTATE_MOUSEDOWN = 2;
	public static final int BUTTONSTATE_PUSHED = 3;
	public static final int BUTTONSTATE_DISABLED = 4; //not yet supported

	public static final int VALUETYPE_SINGLE=0;
	public static final int VALUETYPE_MULTIPLE=1;
	private int valueType=VALUETYPE_SINGLE;

	public static final int ENCODING_NO=0;
	public static final int ENCODING_BASE64=1;
	public static final int ENCODING_UNIX=2;
	private int encoding=ENCODING_NO;

	private int isRichEditValue=0;

	public static final String CSSCLASS_CONTROLBUTTON="mgnlControlButton";
	public static final String CSSCLASS_CONTROLBUTTONSMALL="mgnlControlButtonSmall";
	public static final String CSSCLASS_CONTROLBAR="mgnlControlBar";
	public static final String CSSCLASS_CONTROLBARSMALL="mgnlControlBarSmall";

	private static Logger log = Logger.getLogger(ControlSuper.class);
	private String label;
	private String name;
	private String id;
	private String value=null;
	private ArrayList values=new ArrayList(); //mulitple values (checkbox)
	private Hashtable events=new Hashtable();
	private Content websiteNode=null;
	private String htmlPre=null;
	private String htmlInter=null;
	private String htmlPost=null;
	private int type=PropertyType.STRING;
	private boolean saveInfo=true;
	//private ArrayList defaultValues=new ArrayList();
	private String cssClass="";
	private Hashtable cssStyles=new Hashtable();


    private String path;
    private String nodeCollectionName;
    private String nodeName;
    private String paragraph;
	private HttpServletRequest request;


	ControlSuper() {

	}

	ControlSuper(String name,String value) {
		this.setName(name);
		this.setValue(value);
	}

	ControlSuper(String name,ArrayList values) {
		this.setName(name);
		this.setValues(values);
	}


	ControlSuper(String name,Content websiteNode) {
		this.setName(name);
		this.setWebsiteNode(websiteNode);
	}


    public void setPath(String path) {this.path=path;	}
    public String getPath() {return this.path;}

    public void setNodeCollectionName(String nodeCollectionName) {this.nodeCollectionName=nodeCollectionName;}
    public String getNodeCollectionName() {return this.nodeCollectionName;}
    public String getNodeCollectionName(String nullOrEmptyValue) {
        if (this.getNodeCollectionName()==null || this.getNodeCollectionName().equals("")) return nullOrEmptyValue;
        else return this.getNodeCollectionName();
    }

    public void setNodeName(String nodeName) {this.nodeName=nodeName;}
    public String getNodeName() {return this.nodeName;}
    public String getNodeName(String nullOrEmptyValue) {
        if (this.getNodeName()==null || this.getNodeName().equals("")) return nullOrEmptyValue;
        else return this.getNodeName();
    }

    public void setParagraph(String paragraph) {this.paragraph=paragraph;}
    public String getParagraph() {return this.paragraph;}

    public void setRequest(HttpServletRequest request) {this.request=request;}
    public HttpServletRequest getRequest() {return this.request;}

	public void setName(String s) {this.name=s;}
	public String getName() {return this.name;}

	public void setId(String s) {this.id=s;}
	public String getId() {return this.id;}
	public String getHtmlId() {
		if (this.getId()!=null) return " id=\""+this.getId()+"\"";
		else return "";
	}

	public void setValue(String value) {this.value=value;}
	public String getValue() {
		if (this.value==null) {
			try {
				return this.getWebsiteNode().getNodeData(this.getName()).getString();
			}
			catch (Exception e) {return "";}
		}
		else return this.value;
	}

	public void setValues(ArrayList values) {this.values=values;}
	public ArrayList getValues() {
		if (this.values.size()!=0) {
			return this.values;
		}
		try {
			Iterator it=this.getWebsiteNode().getContentNode(this.getName()).getChildren(ItemType.MAGNOLIA_NODE_DATA).iterator();
			ArrayList l=new ArrayList();
			while (it.hasNext()) {
				NodeData data=(NodeData) it.next();
				l.add(data.getString());
			}
			return l;
		}
		catch (Exception re) {
			return this.values;
		}
	}

	public void setWebsiteNode(Content c) {this.websiteNode=c;}
	public Content getWebsiteNode() {return this.websiteNode;}

	public void setLabel(String label) {this.label=label;}
	public String getLabel() {return this.label;}


	public void setEvent(String event,String action) {
		setEvent(event,action,false);
	}
	public void setEvent(String event,String action,boolean removeExisting) {
		event=event.toLowerCase();
		String existing="";
		if (!removeExisting) {
			existing=(String) this.getEvents().get(event);
			if (existing==null) existing="";
		}
		this.getEvents().put(event,existing+action);
	}
	public void setEvents(Hashtable h) {this.events=h;}
	public Hashtable getEvents() {return this.events;}
	public String getHtmlEvents() {
		String html="";
		Enumeration en=this.getEvents().keys();
		while (en.hasMoreElements()) {
			String key=(String) en.nextElement();
			html+=" "+key+"=\""+this.getEvents().get(key)+"\"";
		}
		//System.out.println(html);
		return html;
	}

	public String getHtml() {
		String html="";
		return html;
	}

	public void setHtmlPre(String s) {this.htmlPre = s;}
	public String getHtmlPre() {return this.getHtmlPre("");}
	public String getHtmlPre(String nullValue) {
		if (this.htmlPre!=null) return this.htmlPre;
		else return nullValue;
	}

	public void setHtmlInter(String s) {this.htmlInter = s;}
	public String getHtmlInter() { return this.getHtmlInter("");}
	public String getHtmlInter(String nullValue) {
		if (this.htmlInter!=null) return this.htmlInter;
		else return nullValue;
	}

	public void setHtmlPost(String s) {this.htmlPost = s;}
	public String getHtmlPost() {return this.getHtmlPost("");}
	public String getHtmlPost(String nullValue) {
		if (this.htmlPost!=null) return this.htmlPost;
		else return nullValue;
	}

	public void setType (int i) {this.type = i;}
	public void setType(String s) {this.type=PropertyType.valueFromName(s);}
	public int getType() {return this.type;}

	public void setSaveInfo(boolean b) {this.saveInfo=b;}
	public boolean getSaveInfo() {return this.saveInfo;}

	public void setCssClass(String s) {this.cssClass = s;}
	public String getCssClass() {return this.cssClass;}
	public String getHtmlCssClass() {
		if (!this.getCssClass().equals("")) return " class=\""+this.getCssClass()+"\"";
		else return "";
	}

	public String getHtmlSaveInfo() {
		String html="";
		if (this.getSaveInfo()) {
			html+="<input type=\"hidden\"";
			html+=" name=\"mgnlSaveInfo\"";
			html+=" value=\""+this.getName()+","+PropertyType.nameFromValue(this.getType())+","+this.getValueType()+","+this.getIsRichEditValue()+","+this.getEncoding()+"\"";
			html+=">";
		}
		return html;
	}

	public void setCssStyles(Hashtable h) {this.cssStyles=h;}
	public void setCssStyles(String key,String value) {this.getCssStyles().put(key,value);}
	public Hashtable getCssStyles() {return this.cssStyles;}
	public String getCssStyles(String key,String nullValue) {
		if (this.getCssStyles().containsKey(key)) return (String) this.getCssStyles().get(key);
		else return nullValue;
	}
	public String getCssStyles(String key) {
		return this.getCssStyles(key,"");
	}
	public String getHtmlCssStyles() {
		String html="";
		Enumeration en=this.getCssStyles().keys();
		while (en.hasMoreElements()) {
			String key=(String) en.nextElement();
			html+=key+":"+this.getCssStyles().get(key)+";";
		}
		if (!html.equals("")) html=" style=\""+html+"\"";
		return html;
	}



	public void setValueType(int i) {this.valueType=i;}
	public int getValueType() {return this.valueType;}

	public void setEncoding(int i) {this.encoding=i;}
	public int getEncoding() {return this.encoding;}


	public void setIsRichEditValue(int i) {this.isRichEditValue=i;}
	public int getIsRichEditValue() {return this.isRichEditValue;}



/*
	public void setDefaultValues(ArrayList a) {this.defaultValues=a;}
	public ArrayList getDefaultValues() {return this.defaultValues;}

	public void setDefaultValue(String s) {this.getDefaultValues().add(0,s);}
	public String getDefaultValue() {return (String) this.getDefaultValues().get(0);}
*/
}
