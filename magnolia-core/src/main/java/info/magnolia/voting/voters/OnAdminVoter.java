package info.magnolia.voting.voters;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.context.Context;

/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class OnAdminVoter extends AbstractBoolVoter {

    protected boolean boolVote(Context ctx) {
        return Server.isAdmin();
    }

}
