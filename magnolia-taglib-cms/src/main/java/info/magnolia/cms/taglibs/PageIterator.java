package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.Resource;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * @author Andreas Brenk
 * @since 18.01.2006
 */
public class PageIterator extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private Iterator contentIterator;

    private String hiddenAttribute = "hidden";

    public int doAfterBody() throws JspException {
        boolean hasNext = nextContent();

        if (hasNext) {
            return EVAL_BODY_AGAIN;
        }

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
        Resource.restoreCurrentActivePage(request);

        return EVAL_PAGE;
    }

    public int doStartTag() throws JspException {
        initContentIterator();

        boolean hasNext = nextContent();
        if (hasNext) {
            return EVAL_BODY_INCLUDE;
        }

        return SKIP_BODY;
    }

    public void release() {
        this.contentIterator = null;
        super.release();
    }

    public void setHiddenAttribute(String hiddenAttribute) {
        this.hiddenAttribute = hiddenAttribute;
    }

    private void initContentIterator() {
        HttpServletRequest req = (HttpServletRequest) this.pageContext.getRequest();
        Content activePage = Resource.getCurrentActivePage(req);
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
            HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
            Content page = (Content) this.contentIterator.next();

            Resource.setCurrentActivePage(request, page);

            return true;
        }

        return false;
    }
}
