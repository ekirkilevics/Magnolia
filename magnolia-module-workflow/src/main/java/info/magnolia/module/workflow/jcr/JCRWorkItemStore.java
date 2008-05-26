/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.workflow.jcr;

import info.magnolia.beancoder.MgnlNode;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.module.workflow.WorkflowModule;
import info.magnolia.module.workflow.beancoder.OwfeJcrBeanCoder;
import openwfe.org.engine.expressions.FlowExpressionId;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.StringAttribute;
import openwfe.org.util.beancoder.BeanCoderException;
import openwfe.org.worklist.store.StoreException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * The Magnolia-specific workflow participant.
 *
 * @author Jackie Ju
 * @author Philipp Bracher
 * @author Nicolas Modrzyk
 * @author John Mettraux
 */
public class JCRWorkItemStore {
    private final static Logger log = LoggerFactory.getLogger(JCRWorkItemStore.class.getName());

    private static final String BACKUP_REL = "backup";
    private static final String BACKUP = "/" + BACKUP_REL;

    private final HierarchyManagerWrapper hm;
    private final boolean shouldBackupWorkItems;

    public JCRWorkItemStore() throws Exception {
        this.hm = new HierarchyManagerWrapperDelegator(WorkflowConstants.WORKSPACE_STORE);

        shouldBackupWorkItems = WorkflowModule.backupWorkitems();
        if (shouldBackupWorkItems) {
            // ensure the backup directory is there.
            if (!hm.isExist(BACKUP)) {
                hm.createPath(BACKUP, ItemType.CONTENT);
                hm.save();
                log.info("Created " + BACKUP + " in workflow store.");
            }
        }
    }

    /**
     * Deletes or moves a workItem to the backup folder.
     */
    public void removeWorkItem(FlowExpressionId fei) throws StoreException {
        synchronized (this.hm) {
            try {
                Content ct = getWorkItemById(fei);
                if (ct != null) {
                    // TODO : this behaviour could be hidden/wrapped in a special HierarchyManager
                    if (!shouldBackupWorkItems) {
                        ct.delete();
                    } else {
                        final ValueFactory vf = ct.getJCRNode().getSession().getValueFactory();
                        ct.setNodeData("isBackup", vf.createValue(true));
                        final Content parent = ct.getParent();
                        final String pathInBackup = BACKUP + parent.getHandle();
                        hm.createPath(pathInBackup, ItemType.WORKITEM);
                        hm.save();
                        hm.moveTo(ct.getHandle(), BACKUP + ct.getHandle());
                        // TODO : MAGNOLIA-1225 : we should only save here, once move uses session instead of workspace
                    }
                    hm.save();
                    log.debug("work item removed or moved to /backup");
                }

            } catch (Exception e) {
                log.error("exception when unstoring workitem:" + e, e);
            }
        }
    }

    /**
     * retrieve work item by
     * @param storeName TODO : this parameter is not used ...
     * @param fei
     * @return
     * @throws StoreException
     */
    public InFlowWorkItem retrieveWorkItem(final String storeName, final FlowExpressionId fei) throws StoreException {
        if (log.isDebugEnabled()) {
            log.debug("starting retrieve work item. this = " + this);
            log.debug("retrieve work item for ID = " + fei.toParseableString());
        }

        Content ct = getWorkItemById(fei);

        if (ct == null) {
            throw new StoreException("cannot find workitem " + fei);
        }

        try {
            return loadWorkItem(ct);
        }
        catch (Exception e) {
            throw new StoreException("load work item form xml failed", e);
        }
    }

    /**
     * load a work item from a JCR content
     * @param ct the content node
     * @return
     * @throws Exception
     */
    public InFlowWorkItem loadWorkItem(Content ct) throws Exception {
        OwfeJcrBeanCoder coder = new OwfeJcrBeanCoder(null, new MgnlNode(ct.getContent(WorkflowConstants.NODEDATA_VALUE)));
        return (InFlowWorkItem) coder.decode();
    }

    /**
     * retrieve a work item by participant name
     * @param participant the full participant name (for example, user-superuser)
     */
    public Content getWorkItemByParticipant(String participant) {
        String queryString = "//*[@participant=\"" + participant + "\"]";
        if (log.isDebugEnabled()) {
            log.debug("xpath query string = " + queryString);
        }
        List list = doQuery(queryString);
        if (list != null && list.size() > 0) {
            return (Content) list.get(0);
        }

        return null;
    }

    /**
     * get work item by id
     * @param fei
     */
    public Content getWorkItemById(FlowExpressionId fei) {
        String path = createPathFromId(fei);
        try {
            return this.hm.getContent(path);
        }
        catch (Exception e) {
            log.error("get work item by id failed, path = " + path, e);
        }
        return null;
    }

    /**
     * check whether the specified work item exists
     * @param fei expression id of work item
     * @return true if exist, false if not
     */
    public boolean hasWorkItem(FlowExpressionId fei) throws AccessDeniedException, RepositoryException {
        String path = createPathFromId(fei);
        if (StringUtils.isNotEmpty(path) && StringUtils.indexOf(path, "/") != 0) {
            path = "/" + path;
        }
        return this.hm.isExist(path);
    }

    /**
     * check if the content contains the right work Item with same id
     * @param ct JCR content
     * @param eid id of work item
     */
    public boolean checkContentWithEID(Content ct, FlowExpressionId eid) {
        String cid = ct.getNodeData(WorkflowConstants.NODEDATA_ID).getString();
        if (log.isDebugEnabled()) {
            log.debug("checkContentWithEID: ID = " + cid);
        }
        FlowExpressionId id = FlowExpressionId.fromParseableString(cid);
        return id.equals(eid);
    }

    /**
     * convert the name to valid path
     * @param id
     */
    public final String convertPath(String id) {
        return StringUtils.replace(
                StringUtils.replace(id, WorkflowConstants.BAR, StringUtils.EMPTY),
                WorkflowConstants.COLON,
                WorkflowConstants.DOT);
    }

    /**
     * create the jcr node path for work Item by its id
     * @param eid
     */
    public String createPathFromId(FlowExpressionId eid) {
        String wlInstId = eid.getWorkflowInstanceId();
        // TODO someone who knows the code better should have a look
        String groupString = StringUtils.right(StringUtils.substringBefore(wlInstId, "."), 3);
        int groupNumber = Integer.parseInt(groupString) % 100;
        StringBuffer buffer = new StringBuffer(eid.getWorkflowDefinitionName());
        buffer.append(WorkflowConstants.SLASH);
        buffer.append(eid.getWorkflowDefinitionRevision());
        buffer.append(WorkflowConstants.SLASH);
        buffer.append(groupNumber);
        buffer.append(WorkflowConstants.SLASH);
        buffer.append(eid.getWorkflowInstanceId());
        buffer.append(WorkflowConstants.SLASH);
        buffer.append(eid.getExpressionName());
        buffer.append(WorkflowConstants.SLASH);
        buffer.append(eid.getExpressionId());

        return convertPath(buffer.toString());
    }

    /**
     * @param arg0 TODO : this parameter is not used ...
     * @param wi   the work item to be stored
     */
    public void storeWorkItem(String arg0, InFlowWorkItem wi) throws StoreException {
        synchronized (this.hm) {
            try {

                // delete it if already exist
                if (hasWorkItem(wi.getId())) {
                    // do not use removeWorkItem() since it persist changes immedietely
                    this.hm.delete(createPathFromId(wi.getId()));
                }

                // create path from work item id
                String path = createPathFromId(wi.getId());
                if (log.isDebugEnabled()) {
                    log.debug("storing workitem with path = " + path);
                }

                Content newc = hm.createPath(path, ItemType.WORKITEM);

                ValueFactory vf = newc.getJCRNode().getSession().getValueFactory();
                String sId = wi.getLastExpressionId().toParseableString();

                newc.createNodeData(WorkflowConstants.NODEDATA_ID, vf.createValue(sId));
                newc.createNodeData(WorkflowConstants.NODEDATA_PARTICIPANT, vf.createValue(wi.getParticipantName()));

                StringAttribute assignTo = (StringAttribute) wi.getAttribute(WorkflowConstants.ATTRIBUTE_ASSIGN_TO);
                if (assignTo != null) {
                    String s = assignTo.toString();
                    if (s.length() > 0) {
                        newc.createNodeData(WorkflowConstants.ATTRIBUTE_ASSIGN_TO, vf.createValue(s));
                    }
                }

                // convert to xml string
                encodeWorkItemToNode(wi, newc);
                hm.save();

                if (log.isDebugEnabled()) {
                    log.debug("store work item ok. ");
                }
            }
            catch (Exception e) {
                log.error("store work item failed", e);
                throw new StoreException(e.toString());
            }
        }
    }

    protected void encodeWorkItemToNode(InFlowWorkItem wi, Content newc) throws BeanCoderException {
        OwfeJcrBeanCoder coder = new OwfeJcrBeanCoder(null, new MgnlNode(newc), WorkflowConstants.NODEDATA_VALUE);
        coder.encode(wi);
    }

    /**
     * execute the xPath Query
     */
    public List doQuery(String queryString) {
        return doQuery(queryString, Query.XPATH);
    }

    public List doQuery(String queryString, String language) {
        ArrayList list = new ArrayList();
        if (log.isDebugEnabled()) {
            log.debug("xpath query string: " + queryString);
        }
        try {
            final QueryManager queryManager = MgnlContext.getSystemContext().getQueryManager(
                    WorkflowConstants.WORKSPACE_STORE);
            final Query q = queryManager.createQuery(queryString, language);

            QueryResult result = q.execute();
            if (result == null) {
                log.info("query result was null");
                return null;
            }

            Iterator it = result.getContent(WorkflowConstants.NODENAME_WORKITEM).iterator();
            while (it.hasNext()) {
                Content ct = (Content) it.next();

                // check for stale data.
                try {
                    if (!hm.isExist(ct.getHandle())) {
                        if (log.isDebugEnabled()) {
                            log.debug(ct.getHandle() + " does not exist anymore.");
                        }
                        continue;
                    }
                }
                catch (Exception e) {
                    log.error("SKipping strange node");
                }

                String title = ct.getTitle();
                String sname = ct.getName();

                if (log.isDebugEnabled()) {
                    log.debug("title=" + title);
                    log.debug("name=" + sname);
                }

                InFlowWorkItem wi = loadWorkItem(ct);
                if (wi == null) {
                    log.error("can not load found workitem");
                    continue;
                }
                if (log.isDebugEnabled()) {
                    log.debug("added workitem to return list ok");
                }
                list.add(wi);
            }
        }
        catch (Exception e) {
            log.error("query flow failed", e);
            return null;
        }
        return list;

    }

}
