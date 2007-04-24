package info.magnolia.cms.cache.voters;

import info.magnolia.cms.beans.config.Server;

import javax.servlet.http.HttpServletRequest;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class NotOnAdminVoter extends BaseCacheVoterImpl {

    /**
     * {@inheritDoc}
     */
    public boolean allowCaching(HttpServletRequest request) {
        return !Server.isAdmin();
    }

}
