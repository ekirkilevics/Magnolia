package info.magnolia.cms.gui.control;

import java.util.ArrayList;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */ 
public class TreeMenuItem extends ControlSuper {
	private String onclick="";
	private String javascriptTree="";
	private ArrayList javascriptConditions=new ArrayList();


	public TreeMenuItem() {

	}

	public void setOnclick(String s) {this.onclick=s;}
	public String getOnclick() {return this.onclick;}

	/**
	 * <p>set the name of the javascript tree object</p>
	 * @param variableName
	 */
	public void setJavascriptTree(String variableName) {this.javascriptTree=variableName;}
	public String getJavascriptTree() {return this.javascriptTree;}

	/**
	 * <p>to enable/disable menu items; the tree object will be passed to the method</p>
	 * @param methodName (without brackets! e.g "checkIfWriteAccess" not "checkIfWriteAccess();"
	 */
	public void addJavascriptCondition(String methodName) {this.javascriptConditions.add(methodName);}
	public ArrayList getJavascriptConditions() {return this.javascriptConditions;}
	public String getJavascriptCondition(int index) {return (String) this.javascriptConditions.get(index);}

	//todo: icons
	public String getHtml() {
		StringBuffer html=new StringBuffer();
		html.append("<div id=\""+this.getId()+"\" class=\"mgnlTreeMenuItem\" onclick=\""+this.getJavascriptTree()+".menuHide();"+this.getOnclick()+"\" onmouseover=\""+this.getJavascriptTree()+".menuItemHighlight(this);\"  onmouseout=\""+this.getJavascriptTree()+".menuItemReset(this);\">"+this.getLabel()+"</div>");
		return html.toString();
	}



}
