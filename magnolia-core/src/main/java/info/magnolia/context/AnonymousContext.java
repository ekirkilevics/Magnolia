/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.context;

import info.magnolia.cms.security.User;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.callback.PlainTextCallbackHandler;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.WorkspaceAccessUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.cms.beans.config.ContentRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventIterator;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sameer Charles
 * $Id$
 */
public class AnonymousContext extends WebContextImpl {

    private static final Logger log = LoggerFactory.getLogger(AnonymousContext.class);

    private static final Map accessManagerMap = new HashMap();

    private static final Map queryManagerMap = new HashMap();

    private static final Map hierarchyManagerMap = new HashMap();

    /**
     * kept as static for performance reasons on live instance.
     * reinitialized on any modification event on anonymous role
     * */
    private static Subject anonymousSubject;

    private static User anonymousUser;

    static {
        // todo : Ideally we should allow anonymous user to have any role or group but in that case
        // we would need to observe and update this subject on any changes in users, userroles and usergroups workspaces
        ObservationUtil.registerChangeListener(ContentRepository.USER_ROLES, "/", new EventListener() {
            public void onEvent(EventIterator events) {
                reset();
            }
        });

        ObservationUtil.registerChangeListener(ContentRepository.USERS, "/anonymous", new EventListener() {
            public void onEvent(EventIterator events) {
                reset();
            }
        });

        ObservationUtil.registerChangeListener(ContentRepository.USER_GROUPS, "/", new EventListener() {
            public void onEvent(EventIterator events) {
                reset();
            }
        });
    }

    public void init(HttpServletRequest request, HttpServletResponse response) {
        super.init(request, response);

    }

    public User getUser() {
        return getAnonymousUser();
    }

    public HierarchyManager getHierarchyManager(String repositoryName, String workspaceName) {
        HierarchyManager hierarchyManager = (HierarchyManager) hierarchyManagerMap.get(repositoryName+workspaceName);
        if (null == hierarchyManager) {
            try {
                hierarchyManager = WorkspaceAccessUtil.getInstance().createHierarchyManager(getUser().getName(),
                        getRepositorySession(repositoryName, workspaceName),
                        getAccessManager(repositoryName, workspaceName),
                        getQueryManager(repositoryName, workspaceName));
                hierarchyManagerMap.put(repositoryName+workspaceName, hierarchyManager);
            } catch (RepositoryException re) {
                log.error("Failed to create HierarchyManager for anonymous user",re);
            }
        }
        return hierarchyManager;
    }

    public AccessManager getAccessManager(String repositoryName, String workspaceName) {
        AccessManager accessManager = (AccessManager) accessManagerMap.get(repositoryName+workspaceName);
        if (null == accessManager) {
            accessManager
                    = WorkspaceAccessUtil.getInstance().createAccessManager(getAnonymousSubject(), repositoryName, workspaceName);
            accessManagerMap.put(repositoryName+workspaceName, accessManager);
        }
        return accessManager;
    }

    public QueryManager getQueryManager(String repositoryName, String workspaceName) {
        QueryManager queryManager = (QueryManager) queryManagerMap.get(repositoryName+workspaceName);
        if (null == queryManager) {
            try {
                queryManager = WorkspaceAccessUtil.getInstance().createQueryManager(
                        getRepositorySession(repositoryName, workspaceName),
                        getAccessManager(repositoryName, workspaceName));
                queryManagerMap.put(repositoryName+workspaceName, queryManager);
            } catch (RepositoryException re) {
                log.error("Failed to create QueryManager for anonymous user", re);
            }
        }
        return queryManager;
    }

    private static Subject getAnonymousSubject() {
        if (null == anonymousSubject) {
            setAnonymousSubject();
        }
        return anonymousSubject;
    }

    private static void setAnonymousSubject() {
        CredentialsCallbackHandler callbackHandler = new PlainTextCallbackHandler(getAnonymousUser().getName(),
                getAnonymousUser().getPassword().toCharArray());
        try {
            LoginContext loginContext = new LoginContext("magnolia", callbackHandler);
            loginContext.login();
            anonymousSubject = loginContext.getSubject();
        }
        catch (LoginException le) {
            log.error("Failed to login as anonymous user", le);
        }
    }

    private static User getAnonymousUser() {
        if (null == anonymousUser) {
            setAnonymousUser();
        }
        return anonymousUser;
    }

    private static void setAnonymousUser() {
        anonymousUser = Security.getUserManager().getAnonymousUser();
    }

    private static void reset() {
        setAnonymousSubject();
        setAnonymousUser();
        accessManagerMap.clear();
        hierarchyManagerMap.clear();
        queryManagerMap.clear();
        log.info("Updated anonymous subject");
    }
}
