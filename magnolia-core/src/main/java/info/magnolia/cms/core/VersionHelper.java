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
package info.magnolia.cms.core;

import org.apache.log4j.Logger;

import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.RepositoryException;
import javax.jcr.Repository;
import javax.jcr.Session;

import info.magnolia.cms.beans.config.ContentRepository;

import java.util.Iterator;

/**
 * This helper class provides all methods to add and restore versions
 * @author Sameer Charles
 * @version $Revision: 1871 $ ($Author: scharles $)
 */
public class VersionHelper {

    /**
     * Logger
     * */
    private static Logger log = Logger.getLogger(VersionHelper.class);

    /**
     * Add version
     * @param content
     * @param filter
     * @throws UnsupportedRepositoryOperationException
     * @throws RepositoryException
     * */
    protected static void addVersion(Content content, Content.ContentFilter filter)
            throws UnsupportedRepositoryOperationException, RepositoryException {
        Session jcrSession = content.getJCRNode().getSession();
        String repositoryId = jcrSession.getRepository().getDescriptor(Repository.REP_NAME_DESC);
        String workspaceId = jcrSession.getWorkspace().getName();
        HierarchyManager hm = ContentRepository.getHierarchyManager(repositoryId, workspaceId);
        Content source = hm.getContent(content.getHandle());
        version(source, filter);
    }

    /**
     * version recursively using the provided content filter
     * @param source
     * @param filter
     * @throws UnsupportedRepositoryOperationException
     * @throws RepositoryException
     * */
    private static void version(Content source, Content.ContentFilter filter)
            throws UnsupportedRepositoryOperationException, RepositoryException {
        source.checkIn();
        source.checkOut();
        Iterator children = source.getChildren(filter).iterator();
        while (children.hasNext()) {
            version((Content) children.next(), filter);
        }
    }

    /**
     * @param content
     * @param filter
     * @param versionName
     * */
    protected static void restore(Content content, Content.ContentFilter filter, String versionName) {

    }

    /**
     * Copy all properties from source to destination
     * @param source
     * @param destination
     * */
    private synchronized void copyProperties(Content source, Content destination) throws RepositoryException {
        // first remove all existing properties at the destination
        Iterator nodeDataIterator = destination.getNodeDataCollection().iterator();
        while (nodeDataIterator.hasNext()) {
            NodeData nodeData = (NodeData) nodeDataIterator.next();
            nodeData.delete();
        }
        // copy all properties
        nodeDataIterator = source.getNodeDataCollection().iterator();
        while (nodeDataIterator.hasNext()) {
            NodeData nodeData = (NodeData) nodeDataIterator.next();
            destination.createNodeData(nodeData.getName(), nodeData.getValue());
        }
    }


}
