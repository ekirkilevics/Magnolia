package info.magnolia.voting.voters;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class RequestHasParametersVoter extends AbstractBoolVoter {

    protected boolean boolVote(Object value) {
        HttpServletRequest request;
        if(value instanceof HttpServletRequest){
            request = (HttpServletRequest) value;
        }
        else{
            Context ctx = MgnlContext.getInstance();
            if(ctx instanceof WebContext){
                request = ((WebContext)ctx).getRequest();
            }
            else{
                return false;
            }
        }
        if (StringUtils.equalsIgnoreCase(request.getMethod(), "POST")) {
            return true;
        }

        if (!request.getParameterMap().isEmpty()) {
            return true;
        }

        return true;
    }

}
