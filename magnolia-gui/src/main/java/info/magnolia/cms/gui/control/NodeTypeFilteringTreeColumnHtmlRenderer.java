/**
 * 
 */
package info.magnolia.cms.gui.control;

import info.magnolia.cms.core.Content;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vsteller
 *
 */
public class NodeTypeFilteringTreeColumnHtmlRenderer extends
		ConditionalTreeColumnHtmlRenderer {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ConditionalTreeColumnHtmlRenderer.class);
    
	protected String[] allowedNodeTypeNames;

	public NodeTypeFilteringTreeColumnHtmlRenderer(TreeColumnHtmlRenderer interceptedRenderer, String[] allowedNodeTypeNames) {
		super(interceptedRenderer);
		this.allowedNodeTypeNames = allowedNodeTypeNames;
	}

	public boolean evaluate(Content content) {
		try {
			return ArrayUtils.contains(allowedNodeTypeNames, content.getNodeTypeName());
		} catch (RepositoryException e) {
			log.debug(e.getMessage());
		}
		return false;
	}

}
