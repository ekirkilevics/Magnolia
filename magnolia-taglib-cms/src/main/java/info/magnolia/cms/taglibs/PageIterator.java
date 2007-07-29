package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.Resource;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * Iterates over subpages.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @version $Revision: $ ($Author: $)
 */
public class PageIterator extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private Iterator contentIterator;

    private String hiddenAttribute = "hidden";

    /**
     * s{@inheritDoc}
     */
    public int doAfterBody() throws JspException {
        boolean hasNext = nextContent();

        if (hasNext) {
            return EVAL_BODY_AGAIN;
        }

        return SKIP_BODY;
    }

    /**
     * {@inheritDoc}
     */
    public int doEndTag() throws JspException {
        Resource.restoreCurrentActivePage();
        return EVAL_PAGE;
    }

    /**
     * {@inheritDoc}
     */
    public int doStartTag() throws JspException {
        initContentIterator();

        boolean hasNext = nextContent();
        if (hasNext) {
            return EVAL_BODY_INCLUDE;
        }

        return SKIP_BODY;
    }

    /**
     * {@inheritDoc}
     */
    public void release() {
        this.contentIterator = null;
        hiddenAttribute = "hidden";
        super.release();
    }

    public void setHiddenAttribute(String hiddenAttribute) {
        this.hiddenAttribute = hiddenAttribute;
    }

    private void initContentIterator() {
        Content activePage = Resource.getCurrentActivePage();
        Collection children = activePage.getChildren(ItemType.CONTENT);

        for (Iterator i = children.iterator(); i.hasNext();) {
            Content c = (Content) i.next();
            boolean hidden = c.getNodeData(this.hiddenAttribute).getBoolean();
            if (hidden) {
                i.remove();
            }
        }

        this.contentIterator = children.iterator();
    }

    /**
     * @return <code>true</code> if the next page was activated, <code>false</code> if there are no pages left
     */
    private boolean nextContent() {
        if (this.contentIterator.hasNext()) {
            Content page = (Content) this.contentIterator.next();
            Resource.setCurrentActivePage(page);
            return true;
        }

        return false;
    }
}
