package info.magnolia.cms.taglibs.util;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.StringUtils;


/**
 * Converts text in the body of the tag adding %lt;br /> tags at new lines or wrapping lines in paragraphs.
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class ConvertNewLineTag extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Use paragraphs.
     */
    private boolean para;

    /**
     * Setter for the <code>para</code> attribute.
     * @param paragraphs <code>true</code> is each line should be wrapped in a %lt;p> tag.
     */
    public void setPara(boolean paragraphs) {
        this.para = paragraphs;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() throws JspException {
        String bodyText = bodyContent.getString();

        if (StringUtils.isNotEmpty(bodyText)) {
            StringTokenizer bodyTk = new StringTokenizer(bodyText, "\n", false);
            JspWriter out = pageContext.getOut();

            try {
                if (this.para) {
                    // wrap text in p
                    while (bodyTk.hasMoreTokens()) {
                        out.write("<p>");
                        out.write(StringUtils.replaceChars(bodyTk.nextToken(), (char) 63, '\''));
                        out.write("</p>");
                    }
                }
                else {
                    // add newlines
                    while (bodyTk.hasMoreTokens()) {
                        out.write(StringUtils.replaceChars(bodyTk.nextToken(), (char) 63, '\''));
                        if (bodyTk.hasMoreTokens()) {
                            out.write("<br/>");
                        }
                    }
                }
            }
            catch (IOException e) {
                throw new JspTagException(e.getMessage());
            }
        }
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        para = false;
        super.release();
    }

}