package info.magnolia.voting.voters;

import info.magnolia.context.Context;
import info.magnolia.context.WebContext;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class WithParametersVoter extends AbstractBoolVoter {

    protected boolean boolVote(Context ctx) {
        if (ctx instanceof WebContext) {
            WebContext webContext = (WebContext) ctx;
            HttpServletRequest request = webContext.getRequest();
            if (StringUtils.equalsIgnoreCase(request.getMethod(), "POST")) {
                return true; // don't cache POSTs
            }

            if (!request.getParameterMap().isEmpty()) {
                return true; // don't cache requests with parameters
            }
        }

        return true;
    }

}
