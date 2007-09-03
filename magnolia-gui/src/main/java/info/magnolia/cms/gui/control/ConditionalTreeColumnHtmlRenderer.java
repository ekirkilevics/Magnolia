/**
 * 
 */
package info.magnolia.cms.gui.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultNodeData;

/**
 * Renders a column value based on the outcome of the 
 * {@link #evaluate(Content)} method.
 *  
 * @author vsteller
 *
 */
public abstract class ConditionalTreeColumnHtmlRenderer implements TreeColumnHtmlRenderer {
    
	protected TreeColumnHtmlRenderer interceptedRenderer;
	
	public ConditionalTreeColumnHtmlRenderer(TreeColumnHtmlRenderer interceptedRenderer) {
		this.interceptedRenderer = interceptedRenderer;
	}
	
	public String renderHtml(TreeColumn treeColumn, Content content) {
		return (evaluate(content) ? interceptedRenderer.renderHtml(treeColumn, content) : " ");
	}
	
	public abstract boolean evaluate(Content content); 
}
