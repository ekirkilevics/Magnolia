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
import info.magnolia.cms.gui.inline.ButtonEdit;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * Date: Apr 28, 2003
 * Time: 11:20:59 AM
 * @author Marcel Salathe
 * @version 1.1
 */


public class EditButton extends TagSupport {


    private String nodeName;
    private String nodeCollectionName;
    private String paragraph;
    private String label;
    private String displayHandler;
	private String small="true";
    private HttpServletRequest request;



    /**
     * <p>starts Edit tag</p>
     *
     * @return int
     */
    public int doStartTag() {
        this.displayHandler = "";
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
		if (!ServerInfo.isAdmin() || Resource.showPreview(this.request))
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
	 * @see this.setNodeName()
	 * <p>set working contentNode</p>
	 *
	 * @param name , comtainer name which will be used to access/write content
	 */
	public void setContentNodeName(String name) {
		this.nodeName = name;
	}

	/**
	 *
	 */
	private String getNodeName() {
		if (this.nodeName == null)
			if (Resource.getLocalContentNode(this.request)==null)
				return null;
			else
				return Resource.getLocalContentNode(this.request).getName();
		else
			return this.nodeName;
	}



	/**
	 * @see this.setNodeCollectionName()
	 * <p>set working contentNode</p>
	 *
	 * @param name , comtainer name which will be used to access/write content
	 */
	public void setContentNodeCollectionName(String name) {
		this.nodeCollectionName = name;
	}




	/**
	 * @return content node collection name
	 */
	private String getNodeCollectionName() {
		if (this.nodeCollectionName == null) {
			if (Resource.getLocalContentNodeCollectionName(this.request) == null)
				return null;
			else
				return Resource.getLocalContentNodeCollectionName(this.request);
		}
		return this.nodeCollectionName;
	}




	/**
	 * @deprecated
	 * <p>set current content type, could be any developer defined name</p>
	 *
	 * @param type , content type
	 */
	public void setParFile(String type) {
		this.setParagraph(type);
	}

	/**
	 * <p>set current content type, could be any developer defined name</p>
	 *
	 * @param type , content type
	 */
	public void setParagraph(String type) {
		this.paragraph = type;
	}



	/**
	 * @return String paragraph (type of par)
	 */
	private String getParagraph() {
		if (this.paragraph==null) return Resource.getLocalContentNode(this.request).getNodeData("paragraph").getString();
		else return this.paragraph;
	}






    /**
     * <p>set display handler (JSP / Servlet), needs to know the relative path from WEB-INF</p>
     *
     * @param path , relative to WEB-INF
     */
    public void setTemplate(String path) {
        this.displayHandler = path;
    }



    /**
     * @return template path
     */
    private String getTemplate() {
        if (this.displayHandler == null) {
            return Resource.getLocalContentNode(this.request).getTemplate();
        }
        return this.displayHandler;
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
	 * @deprecated
	 * <p>set the edit label (default "Edit")</p>
	 *
	 * @param label , under which content must be stored
	 */
	public void setEditLabel(String label) {
		this.setLabel(label);
	}

	/**
	 * <p>set the edit label (default "Edit")</p>
	 *
	 * @param label , under which content must be stored
	 */
	public void setLabel(String label) {
		this.label = label;
	}


	/**
	 *
	 * @return String , label for the edit bar
	 */
	private String getLabel() {
		return this.label;
	}


	/**
	 * <p>sets the size of the button</p>
	 *
	 * @param s , true for a small button (default), false for a large
	 */
	public void setSmall(String s) {
		this.small=s;
	}
	private String getSmall() {
		return this.small;
	}





    /**
     * <p>displays edit bar</p>
     *
     * @throws IOException
     */
    private void display() throws IOException {

		if (this.getNodeCollectionName()!=null && this.getNodeName()==null) {
			//cannot draw edit button with nodeCllection and without node
			return;
		}

		JspWriter out = pageContext.getOut();
		ButtonEdit button=new ButtonEdit(this.request);
		button.setPath(this.getPath());
		button.setParagraph(this.getParagraph());
		button.setNodeCollectionName(this.getNodeCollectionName());
		button.setNodeName(this.getNodeName());
		button.setDefaultOnclick();

		if (this.getLabel()!=null) button.setLabel(this.getLabel());
		if (!this.getSmall().equals("false")) button.setSmall(true);
		button.drawHtml(out);

   }



}
