package info.magnolia.cms.cache.voters;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class NotWithParametersVoter extends BaseCacheVoterImpl {

    /**
     * {@inheritDoc}
     */
    public boolean allowCaching(HttpServletRequest request) {
        if (StringUtils.equalsIgnoreCase(request.getMethod(), "POST")) {
            return false; // don't cache POSTs
        }

        if (!request.getParameterMap().isEmpty()) {
            return false; // don't cache requests with parameters
        }

        return true;
    }

}
