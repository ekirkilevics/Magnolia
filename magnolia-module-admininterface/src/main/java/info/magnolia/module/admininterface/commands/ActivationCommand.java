/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface.commands;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.context.Context;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the activation command which do real activation
 * @author jackie
 * $Id$
 */
public class ActivationCommand extends BaseActivationCommand {

    /**
     * Log
     */
    private static Logger log = LoggerFactory.getLogger(ActivationCommand.class);

    private boolean recursive;

    private String versionNumber;

    private Map versionMap;

    /**
     * Execute the activation
     */
    public boolean execute(Context ctx) {
        if (log.isDebugEnabled()) {
            log.debug("recursive = " + recursive);
            log.debug("user = " + ctx.getUser().getName());
        }

        try {
            Content thisState;
            if (StringUtils.isNotEmpty(getUuid())) {
                thisState = ctx.getHierarchyManager(getRepository()).getContentByUUID(getUuid());
            } else {
                thisState = ctx.getHierarchyManager(getRepository()).getContent(getPath());
            }
            String parentPath = StringUtils.substringBeforeLast(thisState.getHandle(), "/");
            if (StringUtils.isEmpty(parentPath)) {
                parentPath = "/";
            }
            // get ordering info now since this object might point to the version store later
            List orderInfo= getOrderingInfo(thisState);
            if (StringUtils.isNotEmpty(getVersion())) {
                try {
                    thisState = thisState.getVersionedContent(getVersion());
                } catch (RepositoryException re) {
                    log.error("Failed to get version "+getVersion()+" for "+thisState.getHandle(), re);
                }
            }
            // make multiple activations instead of a big bulp
            if (recursive) {
                Map versionMap = getVersionMap();
                if (versionMap == null) {
                    activateRecursive(parentPath, thisState, orderInfo, ctx);
                } else {
                    activateRecursive(ctx, orderInfo, versionMap);
                }
            }
            else {
                getSyndicator().activate(parentPath, thisState, orderInfo);
            }
        }
        catch (Exception e) {
            log.error("can't activate", e);
            AlertUtil.setException(MessagesManager.get("tree.error.deactivate"), e, ctx);
            return false;
        }
        log.info("exec successfully.");
        return true;
    }

    /**
     * Activate recursively. This is done one by one to send only small peaces (memory friendly).
     * @param parentPath
     * @param node
     * @param orderInfo
     * @throws ExchangeException
     * @throws RepositoryException
     */
    protected void activateRecursive(String parentPath, Content node, List orderInfo ,Context ctx)
            throws ExchangeException, RepositoryException {
        // activate this node using the rules
        getSyndicator().activate(parentPath, node, orderInfo);

        // proceed recursively
        Iterator children = node.getChildren(new Content.ContentFilter() {

            public boolean accept(Content content) {
                try {
                    return !getRule().isAllowed(content.getNodeTypeName());
                }
                catch (RepositoryException e) {
                    log.error("can't get nodetype", e);
                    return false;
                }
            }
        }).iterator();

        while (children.hasNext()) {
            // note: recursive activation does not need to set ordering info, except for the first node in a tree
            this.activateRecursive(parentPath, ((Content) children.next()), new ArrayList(),ctx);
        }
    }

    /**
     * @param ctx
     * @param orderInfo
     * @param versionMap
     * */
    protected void activateRecursive(Context ctx, List orderInfo, Map versionMap)
            throws ExchangeException, RepositoryException {
        // activate all uuid's present in versionMap
        Iterator keys = versionMap.keySet().iterator();
        while (keys.hasNext()) {
            String uuid = (String) keys.next();
            if (StringUtils.equalsIgnoreCase("class", uuid)) {
                // todo , this should not happen in between the serialized list, somewhere a bug
                // for the moment simply ignore it
                orderInfo = new ArrayList();
                continue;
            }
            String versionNumber = (String) versionMap.get(uuid);
            try {
                Content content = ctx.getHierarchyManager(getRepository()).getContentByUUID(uuid);
                String parentPath = content.getParent().getHandle();
                content = content.getVersionedContent(versionNumber);
                // add order info for the first node as it represents the parent in a tree
                getSyndicator().activate(parentPath, content, orderInfo);
            } catch (RepositoryException re) {
                log.error(re.getMessage());
            }
            // for rest of the nodes there is no need to set order since they will be activated as they were collected
            orderInfo = new ArrayList();
        }
    }

    /**
     * gets ordering info for the fiven node
     * @param node
     * */
    private List getOrderingInfo(Content node) {
        //do not use magnolia Content class since these objects are only meant for a single use to read UUID
        List siblings = new ArrayList();
        Node thisNode = node.getJCRNode();
        try {
            String thisNodeType = node.getNodeTypeName();
            String thisNodeUUID = node.getUUID();
            NodeIterator nodeIterator = thisNode.getParent().getNodes();
            while (nodeIterator.hasNext()) { // only collect elements after this node
                Node sibling = nodeIterator.nextNode();
                // skip till the actual position
                if (sibling.isNodeType(thisNodeType)) {
                    if (thisNodeUUID.equalsIgnoreCase(sibling.getUUID())) break;
                }
            }
            while (nodeIterator.hasNext()) {
                Node sibling = nodeIterator.nextNode();
                if (sibling.isNodeType(thisNodeType)) {
                    siblings.add(sibling.getUUID());
                }
            }
        } catch (RepositoryException re) {
            // do not throw this exception, if it fails simply do not add any ordering info
            log.error("Failed to get Ordering info", re);
        }
        return siblings;
    }

    /**
     * @return the recursive
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * @param recursive the recursive to set
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * @param number version number to be set for activation
     * */
    public void setVersion(String number) {
        this.versionNumber = number;
    }

    /**
     * @return version number
     * */
    public String getVersion() {
        return this.versionNumber;
    }

    /**
     * @param versionMap version map to be set for activation
     * */
    public void setVersionMap(Map versionMap) {
        this.versionMap = versionMap;
    }

    /**
     * @return version map
     * */
    public Map getVersionMap() {
        return this.versionMap;
    }


}
