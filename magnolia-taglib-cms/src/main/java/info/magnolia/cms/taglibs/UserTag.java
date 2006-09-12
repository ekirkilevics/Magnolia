package info.magnolia.cms.taglibs;

import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class UserTag extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Name of the pagecontext variable where the user will be set.
     */
    private String var;

    /**
     * Returns also anonymous users?
     */
    private boolean anonymous;

    /**
     * Setter for <code>anonymous</code>.
     * @param anonymous The anonymous to set.
     */
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    /**
     * Setter for <code>var</code>.
     * @param var The var to set.
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    public int doEndTag() throws JspException {

        User user = MgnlContext.getUser();
        if (user != null && (anonymous || !UserManager.ANONYMOUS_USER.equals(user.getName()))) {
            pageContext.setAttribute(var, user);
        }
        else {
            pageContext.removeAttribute(var);
        }

        return super.doEndTag();
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();
        this.var = null;
        this.anonymous = false;
    }

}
