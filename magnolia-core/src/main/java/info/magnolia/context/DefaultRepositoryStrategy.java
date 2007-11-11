package info.magnolia.context;

import java.util.*;

import javax.security.auth.Subject;

import org.apache.log4j.spi.DefaultRepositorySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.util.WorkspaceAccessUtil;

public class DefaultRepositoryStrategy extends AbstractRepositoryStrategy {
	static final Logger log = LoggerFactory.getLogger(DefaultRepositoryStrategy.class);

	private static final long serialVersionUID = 222L;
	
	private Map accessManagers = new HashMap();
	
    protected UserContext context;	
	
	public DefaultRepositoryStrategy(UserContext context) {
		this.context = context;
	}
	
	public AccessManager getAccessManager(String repositoryId,
			String workspaceId) {
		AccessManager accessManager = null;

        final String amAttrName = repositoryId + "_" + workspaceId;
        accessManager = (AccessManager) accessManagers.get(amAttrName);
        

        if (accessManager == null) {
            accessManager = WorkspaceAccessUtil.getInstance().createAccessManager(
                getSubject(), repositoryId, workspaceId);
            accessManagers.put(amAttrName, accessManager);            
        }

        return accessManager;
	}

	protected Subject getSubject() {
        return this.context.getUser().getSubject();
    }

    protected String getUserId() {
        return this.context.getUser().getName();
    }

}
