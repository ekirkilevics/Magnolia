package info.magnolia.voting.voters;

import info.magnolia.cms.beans.config.Server;

/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class OnAdminVoter extends AbstractBoolVoter {

    protected boolean boolVote(Object value) {
        return Server.isAdmin();
    }

}
