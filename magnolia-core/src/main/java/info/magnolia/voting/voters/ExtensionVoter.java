package info.magnolia.voting.voters;

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.core.Aggregator;
import info.magnolia.context.Context;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class ExtensionVoter extends AbstractBoolVoter {

    private String[] allow;

    private String[] deny;

    public void setAllow(String allow) {
        this.allow = StringUtils.split(allow, ',');
    }

    public void setDeny(String deny) {
        this.deny = StringUtils.split(deny, ',');
    }

    protected boolean boolVote(Context ctx) {
        if (StringUtils.isEmpty(MIMEMapping.getMIMEType())) {
            return false; // check for MIMEMapping, extension must exist
        }

        String extension = Aggregator.getExtension();

        if (allow != null && allow.length > 0 && !ArrayUtils.contains(allow, extension)) {
            return false;
        }

        if (deny != null && deny.length > 0 && ArrayUtils.contains(deny, extension)) {
            return false;
        }

        return true;
    }

}
