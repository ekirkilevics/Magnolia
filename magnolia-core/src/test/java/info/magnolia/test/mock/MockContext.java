/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.test.mock;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.AbstractMapBasedContext;
import info.magnolia.context.SystemContext;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.UnhandledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A mock context where you can set a mocked hierarchy manger on it.
 * @author philipp
 * @version $Id$
 *
 */
public class MockContext extends AbstractMapBasedContext implements SystemContext{

    private Map hierarchyManagers = new HashMap();

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MockContext.class);

    /**
     *
     */
    public MockContext() {
    }

    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        if(!hierarchyManagers.containsKey(repositoryId)){
            throw new IllegalArgumentException("repository [" + repositoryId + "] not initialized");
        }
        return (HierarchyManager) hierarchyManagers.get(repositoryId);
    }

    public void addHierarchyManager(String repositoryId, HierarchyManager hm){
        hierarchyManagers.put(repositoryId, hm);
    }

    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return null;
    }


    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return null;
    }

}
