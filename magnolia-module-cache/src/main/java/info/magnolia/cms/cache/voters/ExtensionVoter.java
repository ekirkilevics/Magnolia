package info.magnolia.cms.cache.voters;

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.beans.config.Server;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class ExtensionVoter extends BaseCacheVoterImpl {

    private String[] allow;

    private String[] deny;

    public void setAllow(String allow) {
        this.allow = StringUtils.split(allow, ',');
    }

    public void setDeny(String deny) {
        this.deny = StringUtils.split(deny, ',');
    }

    /**
     * {@inheritDoc}
     */
    public boolean allowCaching(HttpServletRequest request) {
        if (StringUtils.isEmpty(MIMEMapping.getMIMEType(request))) {
            return false; // check for MIMEMapping, extension must exist
        }

        String extension = StringUtils.substringAfterLast(request.getRequestURI(), "."); //$NON-NLS-1$
        if (StringUtils.isEmpty(extension) || StringUtils.contains(extension, "/")) {
            extension = Server.getDefaultExtension();
        }

        if (allow != null && allow.length > 0 && !ArrayUtils.contains(allow, extension)) {
            return false;
        }

        if (deny != null && deny.length > 0 && ArrayUtils.contains(deny, extension)) {
            return false;
        }

        return true;
    }

}
