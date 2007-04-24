package info.magnolia.cms.cache.voters;

import info.magnolia.cms.security.Authenticator;

import javax.servlet.http.HttpServletRequest;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class NotAuthenticatedVoter extends BaseCacheVoterImpl {

    /**
     * {@inheritDoc}
     */
    public boolean allowCaching(HttpServletRequest request) {
        return !Authenticator.isAuthenticated(request);
    }

}
