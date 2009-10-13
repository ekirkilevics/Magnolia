/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admininterface.commands;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.context.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

    private List versionMap;

    /**
     * Execute activation
     */
    public boolean execute(Context ctx) {
        boolean success = false;
        try {
            log.debug("Will activate content from {} repository with uuid {} and path {}", new Object[] {getRepository(), getUuid(), getPath()});
            Content thisState = getNode(ctx);
            String parentPath = StringUtils.substringBeforeLast(thisState.getHandle(), "/");
            if (StringUtils.isEmpty(parentPath)) {
                parentPath = "/";
            }
            log.debug("Activate content {} as a child of {}", new Object[] {thisState.getName(), parentPath});
            // make multiple activations instead of a big bulk
            if (recursive) {
                List versionMap = getVersionMap();
                if (versionMap == null) {
                    activateRecursive(parentPath, thisState, ctx);
                } else {
                    activateRecursive(ctx, versionMap);
                }
            }
            else {
                List orderInfo = getOrderingInfo(thisState);
                if (StringUtils.isNotEmpty(getVersion())) {
                    try {
                        thisState = thisState.getVersionedContent(getVersion());
                    } catch (RepositoryException re) {
                        log.error("Failed to get version "+getVersion()+" for "+thisState.getHandle(), re);
                    }
                }
                getSyndicator().activate(parentPath, thisState, orderInfo);
            }
            log.debug("exec successfully.");
            success = true;
        }
        catch (Exception e) {
            log.error("can't activate", e);
            AlertUtil.setException(MessagesManager.get("tree.error.activate"), e, ctx);
        }
        return success;
    }

    /**
     * Activate recursively. This is done one by one to send only small peaces (memory friendly).
     * @param parentPath
     * @param node
     * @throws ExchangeException
     * @throws RepositoryException
     */
    protected void activateRecursive(String parentPath, Content node, Context ctx)
            throws ExchangeException, RepositoryException {

        getSyndicator().activate(parentPath, node, getOrderingInfo(node));

        Collection children = node.getChildren(new Content.ContentFilter() {
            public boolean accept(Content content) {
                try {
                    return !getRule().isAllowed(content.getNodeTypeName());
                }
                catch (RepositoryException e) {
                    log.error("can't get nodetype", e);
                    return false;
                }
            }
        });

        // FYI: need to reverse order of child activation since content ordering info is also bottom-up
        // Hackish at best. If changing this, don't forget to also change other activateRecursive() method
        // and most importantly ensure that ordering of siblings in ReceiveFilter is done in same direction!
        Content[] childArray = (Content[]) children.toArray(new Content[children.size()]);
        for (int i = childArray.length - 1; i >=0; i--) {
            this.activateRecursive(node.getHandle(), childArray[i], ctx);
        }
    }

    /**
     * @param ctx
     * @param versionMap
     * */
    protected void activateRecursive(Context ctx, List versionMap)
            throws ExchangeException, RepositoryException {
        // activate all uuid's present in versionMap
        Map<String, Object>[] versions = (Map<String, Object>[]) versionMap.toArray(new Map[0]);
        // add path and order info into the entries
        for (int i = 0; i < versions.length; i++) {
            Map<String, Object> entry = versions[i];
            String uuid = (String) entry.get("uuid");
            if (StringUtils.equalsIgnoreCase("class", uuid)) {
                // TODO: this should not happen in between the serialized list, somewhere a bug
                // for the moment simply ignore it
                versionMap.remove(entry);
            }
            try {
                Content content = ctx.getHierarchyManager(getRepository()).getContentByUUID(uuid);
                entry.put("handle", content.getHandle());
                entry.put("index", i);
            } catch (RepositoryException re) {
                log.error("Failed to activate node with UUID : "+uuid);
                log.error(re.getMessage());
                versionMap.remove(entry);
            }
        }
        versions = null;

        // versionMap is a flat list of all activated content. We need to ensure that the content is ordered from top down and siblings are activated from bottom up
        Collections.sort((List<Map<String, Object>>) versionMap, new Comparator<Map<String, Object>>() {

            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                String handle1 = (String) o1.get("handle");
                String handle2 = (String) o2.get("handle");
                if (handle2.startsWith(handle1)) {
                    // o2 is child of o1, say o1 is smaller to get it ordered BEFORE o2
                    return -1;
                }
                String parent1 = StringUtils.substringBeforeLast(handle1, "/");
                String parent2 = StringUtils.substringBeforeLast(handle2, "/");
                if (parent1.equals(parent2)) {
                    // siblings ... to reverse order, the higher index value get ordered before lower values index
                    int idx1 = (Integer) o1.get("index");
                    int idx2 = (Integer) o2.get("index");
                    // index is generated in the loop above and can be never same for 2 items ... skip equality case
                    return idx1 < idx2 ? 1 : -1;
                }

                // unrelated entries, the one closer to the root should be returned first
                int dirLevels1 = StringUtils.countMatches(handle1, "/");
                int dirLevels2 = StringUtils.countMatches(handle2, "/");
                // since parents are checked above, the equality case here means different hierarchy of same depth and is irrelevant to activation order
                return dirLevels1 < dirLevels2 ? -1 : 1;
            }});

        // FYI: need to reverse order of child activation since content ordering info is also bottom-up
        // Hackish at best. If changing this, don't forget to also change other activateRecursive() method
        // and most importantly ensure that ordering of siblings in ReceiveFilter is done in same direction!
        for (Map entry : (List<Map>) versionMap) {

            String uuid = (String) entry.get("uuid");
            String versionNumber = (String) entry.get("version");
            if (StringUtils.equalsIgnoreCase("class", uuid)) {
                // TODO: this should not happen in between the serialized list, somewhere a bug
                // for the moment simply ignore it
                continue;
            }
            try {
                Content content = ctx.getHierarchyManager(getRepository()).getContentByUUID(uuid);
                // NOTE : on activation of the version it uses current hierarchy to order
                // since admin interface does not protect the state of the hierarchy if its in workflow
                // we have to use the current state
                List orderedList = getOrderingInfo(content);
                String parentPath = content.getParent().getHandle();
                content = content.getVersionedContent(versionNumber);
                // add order info for the first node as it represents the parent in a tree
                getSyndicator().activate(parentPath, content, orderedList);
            } catch (RepositoryException re) {
                log.error("Failed to activate node with UUID : "+uuid);
                log.error(re.getMessage());
            }
        }
    }

    /**
     * collect node UUID of the siblings in the exact order as it should be written on
     * subscribers
     * @param node
     * */
    protected List getOrderingInfo(Content node) {
        //do not use magnolia Content class since these objects are only meant for a single use to read UUID
        // TODO: what's wrong with using magnolia content???
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
    public void setVersionMap(List versionMap) {
        this.versionMap = versionMap;
    }

    /**
     * @return version map
     * */
    public List getVersionMap() {
        return this.versionMap;
    }

    @Override
    public void release() {
        super.release();
        this.versionMap = null;
        this.recursive = false;
        this.versionNumber = null;
    }

}
