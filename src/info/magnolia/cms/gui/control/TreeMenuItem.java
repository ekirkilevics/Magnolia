package info.magnolia.cms.gui.control;

/**
 *
 * User: enz
 * Date: Sep 20, 2004
 * Time: 1:36:19 PM
 *
 */
public class TreeMenuItem extends ControlSuper {
	private String onclick="";
	private String javascriptTree="";
	private String javascriptCondition=null;


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
	public void setJavascriptCondition(String methodName) {this.javascriptCondition=methodName;}
	public String getJavascriptCondition() {return this.javascriptCondition;}

	//todo: icons
	public String getHtml() {
		String html="";
		html+="<div id=\""+this.getId()+"\" class=\"mgnlTreeMenuItem\" onclick=\""+this.getOnclick()+" "+this.getJavascriptTree()+".menuHide();\" onmouseover=\""+this.getJavascriptTree()+".menuItemHighlight(this);\"  onmouseout=\""+this.getJavascriptTree()+".menuItemReset(this);\">"+this.getLabel()+"</div>";
		return html;
	}



}
