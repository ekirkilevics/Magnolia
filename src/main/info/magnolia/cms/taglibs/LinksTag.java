package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.gui.misc.Sources;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;


/**
 * Draw css and js links for admin controls. Separated from mainbar since css links must be inside html head.
 * @author Fabrizio Giustina
 * @version $Revision: $ ($Author: $)
 * @sice 2.1
 */
public class LinksTag extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(MainBar.class);

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        if (Server.isAdmin()) {

            HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();

            // check if links have already been added.
            if (request.getAttribute(Sources.REQUEST_LINKS_DRAWN) == null) {

                Sources src = new Sources(request.getContextPath());
                JspWriter out = this.pageContext.getOut();
                try {
                    out.write(src.getHtmlCss());
                    out.write(src.getHtmlJs());
                }
                catch (IOException e) {
                    // should never happen
                    throw new NestableRuntimeException(e);
                }

                request.setAttribute(Sources.REQUEST_LINKS_DRAWN, Boolean.TRUE);
            }
        }

        return EVAL_PAGE;
    }
}
