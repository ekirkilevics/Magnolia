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
package info.magnolia.module.exchangesimple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;

import EDU.oswego.cs.dl.util.concurrent.Sync;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.cms.util.RuleBasedContentFilter;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.logging.AuditLoggingUtil;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.context.MgnlContext;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.List;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;
import java.net.URLConnection;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileInputStream;

/**
 * @author Sameer Charles
 * $Id$
 */
public abstract class BaseSyndicatorImpl implements Syndicator {
     private static final Logger log = LoggerFactory.getLogger(BaseSyndicatorImpl.class);

    /**
      * URI used for activation
      */
     public static final String DEFAULT_HANDLER = ".magnolia/activation"; //$NON-NLS-1$

     public static final String PARENT_PATH = "mgnlExchangeParentPath";

     public static final String MAPPED_PARENT_PATH = "mgnlExchangeMappedParent";

     /**
      * activated/deactivated path
      */
     public static final String PATH = "mgnlExchangePath";

     public static final String NODE_UUID = "mgnlExchangeNodeUUID";

     public static final String REPOSITORY_NAME = "mgnlExchangeRepositoryName";

     public static final String WORKSPACE_NAME = "mgnlExchangeWorkspaceName";

     public static final String VERSION_NAME = "mgnlExchangeVersionName";

     /**
      * resource reading sequence
      */
     public static final String RESOURCE_MAPPING_FILE = "mgnlExchangeResourceMappingFile";

     /**
      * resource file, Siblings element
      * siblings element will contain all siglings of the same node type which are "before"
      * this node
      */
     public static final String SIBLINGS_ROOT_ELEMENT = "NodeSiblings";

     public static final String SIBLINGS_ELEMENT = "sibling";

     public static final String SIBLING_UUID = "siblingUUID";

     public static final String RESOURCE_MAPPING_FILE_ELEMENT = "File";

     public static final String RESOURCE_MAPPING_NAME_ATTRIBUTE = "name";

     public static final String RESOURCE_MAPPING_UUID_ATTRIBUTE = "contentUUID";

     public static final String RESOURCE_MAPPING_ID_ATTRIBUTE = "resourceId";

     public static final String RESOURCE_MAPPING_ROOT_ELEMENT = "Resources";

     public static final String ACTION = "mgnlExchangeAction";

     public static final String ACTIVATE = "activate"; //$NON-NLS-1$

     public static final String DEACTIVATE = "deactivate"; //$NON-NLS-1$

     public static final String COMMIT = "commit";

     public static final String ROLLBACK = "rollback";

     public static final String AUTHORIZATION = "Authorization";

     public static final String AUTH_CREDENTIALS= "mgnlUserPSWD";

     public static final String AUTH_USER = "mgnlUserId";

     public static final String CONTENT_FILTER_RULE = "mgnlExchangeFilterRule";

     public static final String ACTIVATION_SUCCESSFUL = "sa_success"; //$NON-NLS-1$

     public static final String ACTIVATION_FAILED = "sa_failed"; //$NON-NLS-1$

     public static final String ACTIVATION_ATTRIBUTE_STATUS = "sa_attribute_status"; //$NON-NLS-1$

     public static final String ACTIVATION_ATTRIBUTE_MESSAGE = "sa_attribute_message"; //$NON-NLS-1$

     public static final String ACTIVATION_ATTRIBUTE_VERSION = "sa_attribute_version"; //$NON-NLS-1$

     /**
     * Runs a given job in the thread pool.
     * 
     * @param job the job to run
     * @throws ExchangeException if the job could not be put in the pool
     */
    protected static void executeInPool(Runnable job) throws ExchangeException {
        try {
            ThreadPool.getInstance().execute(job);
        } catch (InterruptedException e) {
            // this is kind of a problem, we could not add the job to the pool
            // retrying might or might not work now that the interruption
            // status is cleared but there is not much we can do so throwing
            // an ExchangeException seems like the least bad choice
            String message = "could not execute job in pool";
            log.error(message, e);
            throw new ExchangeException(message, e);
        }
    }

    /**
     * Acquires a {@link Sync} ignoring any interruptions. Should any
     * interruption occur the interruption status will be set. Might
     * potentially block/wait forever.
     * 
     * @see Sync#acquire()
     * 
     * @param latch the latch on which to wait
     */
    protected static void acquireIgnoringInterruption(Sync latch) {
        try {
            latch.acquire();
        } catch (InterruptedException e) {
            // waked up externally - ignore try again
            acquireIgnoringInterruption(latch);
            // be a good citizen and set back the interruption status
            Thread.currentThread().interrupt();
        }
    }

    protected String repositoryName;

     protected String workspaceName;

     protected String parent;

     protected String path;

     protected String nodeUUID;

     protected Content.ContentFilter contentFilter;

     protected Rule contentFilterRule;

     protected User user;

     protected String basicCredentials;

     /**
      * @param user
      * @param repositoryName repository ID
      * @param workspaceName workspace ID
      * @param rule content filter rule
      * @see info.magnolia.cms.exchange.Syndicator#init(info.magnolia.cms.security.User, String, String,
      * info.magnolia.cms.util.Rule)
      */
     public void init(User user, String repositoryName, String workspaceName, Rule rule) {
         this.user = user;
         this.basicCredentials = "Basic "
             + new String(Base64.encodeBase64((this.user.getName() + ":" + this.user.getPassword()).getBytes()));
         this.contentFilter = new RuleBasedContentFilter(rule);
         this.contentFilterRule = rule;
         this.repositoryName = repositoryName;
         this.workspaceName = workspaceName;
     }

    /**
      * This will activate specifies page (sub pages) to all configured subscribers.
     *
      * @param parent parent under which this page will be activated
      * @param content to be activated
      * @throws javax.jcr.RepositoryException
      * @throws info.magnolia.cms.exchange.ExchangeException
      */
     public void activate(String parent, Content content) throws ExchangeException, RepositoryException {
         this.activate(parent, content, null);
     }

     /**
      * This will activate specified node to all configured subscribers.
      *
      * @param parent parent under which this page will be activated
      * @param content to be activated
      * @param orderBefore List of UUID to be used by the implementation to order this node after activation
      * @throws javax.jcr.RepositoryException
      * @throws info.magnolia.cms.exchange.ExchangeException
      *
      */
     public void activate(String parent, Content content, List orderBefore) throws ExchangeException, RepositoryException {
         this.activate(null, parent, content, orderBefore);
     }

     /**
      * This will activate specifies page (sub pages) to the specified subscriber.
      *
      * @param subscriber
      * @param parent parent under which this page will be activated
      * @param content to be activated
      * @throws javax.jcr.RepositoryException
      * @throws info.magnolia.cms.exchange.ExchangeException
      */
     public void activate(Subscriber subscriber, String parent, Content content) throws ExchangeException, RepositoryException {
         this.activate(subscriber, parent, content, null);
     }

     /**
      * This will activate specifies node to the specified subscriber.
      *
      * @param subscriber
      * @param parent      parent under which this page will be activated
      * @param content     to be activated
      * @param orderBefore List of UUID to be used by the subscriber to order this node after activation
      * @throws javax.jcr.RepositoryException
      * @throws info.magnolia.cms.exchange.ExchangeException
      */
     public void activate(Subscriber subscriber, String parent, Content content, List orderBefore) throws ExchangeException, RepositoryException {
         this.parent = parent;
         this.path = content.getHandle();
         ActivationContent activationContent = null;
         try {
             activationContent = this.collect(content, orderBefore);
             if (null == subscriber) {
                 this.activate(activationContent);
             } else {
                 this.activate(subscriber, activationContent);
             }
             this.updateActivationDetails();
             log.info("Exchange: activation succeeded [{}]", content.getHandle());
             AuditLoggingUtil.log(AuditLoggingUtil.ACTION_ACTIVATE, this.workspaceName, this.path );
         }
         catch (Exception e) {
             if (log.isDebugEnabled()) {
                 log.error("Exchange: activation failed for path:" + ((path != null) ? path : "[null]"), e);
                 long timestamp = System.currentTimeMillis();
                 log.warn("moving files from failed activation to *.failed" + timestamp );
                 Iterator keys = activationContent.getFiles().values().iterator();
                 while (keys.hasNext()) {
                     File f = (File) keys.next();
                     f.renameTo(new File(f.getAbsolutePath()+".failed" + timestamp));
                 }
                 activationContent.getFiles().clear();

             }
             throw new ExchangeException(e);
         }
         finally {
             log.debug("Cleaning temporary files");
             cleanTemporaryStore(activationContent);
         }
     }

     /**
      * @throws ExchangeException
      */
     public abstract void activate(ActivationContent activationContent) throws ExchangeException;


     /**
      * Send activation request if subscribed to the activated URI
      * @param subscriber
      * @param activationContent
      * @throws ExchangeException
      */
     public abstract String activate(Subscriber subscriber, ActivationContent activationContent) throws ExchangeException;

     /**
      * cleans temporary store
      * @param activationContent
      */
     protected void cleanTemporaryStore(ActivationContent activationContent) {
         if (activationContent == null) {
             log.debug("Clean temporary store - nothing to do");
             return;
         }
         Iterator keys = activationContent.getFiles().keySet().iterator();
         while (keys.hasNext()) {
             String key = (String) keys.next();
             log.debug("Removing temporary file {}", key);
             activationContent.getFile(key).delete();
         }
     }

     public synchronized void deactivate(String path) throws ExchangeException, RepositoryException {
         Content node = getHierarchyManager().getContent(path);
         this.nodeUUID = node.getUUID();
         this.path = node.getHandle();
         this.doDeactivate();
         updateDeactivationDetails();
     }

    /**
     * @param node , to deactivate
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public synchronized void deactivate(Content node) throws ExchangeException, RepositoryException {
        this.nodeUUID = node.getUUID();
        this.path = node.getHandle();
        this.doDeactivate();
        updateDeactivationDetails();
    }

    /**
     * @param node , to deactivate
     * @param subscriber
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public synchronized void deactivate(Subscriber subscriber, Content node) throws ExchangeException, RepositoryException {
        this.nodeUUID = node.getUUID();
        this.path = node.getHandle();
        this.doDeactivate(subscriber);
        updateDeactivationDetails();
    }

     /**
      * @throws ExchangeException
      */
     public abstract void doDeactivate() throws ExchangeException;

     /**
      * deactivate from a specified subscriber
      * @param subscriber
      * @throws ExchangeException
      */
     public abstract String doDeactivate(Subscriber subscriber) throws ExchangeException;

     /**
      * get deactivation URL
      * @param subscriberInfo
      */
     protected String getDeactivationURL(Subscriber subscriberInfo) {
         return getActivationURL(subscriberInfo);
     }

     /**
      * add deactivation request header fields
      * @param connection
      */
     protected void addDeactivationHeaders(URLConnection connection) {
         connection.addRequestProperty(REPOSITORY_NAME, this.repositoryName);
         connection.addRequestProperty(WORKSPACE_NAME, this.workspaceName);
         connection.addRequestProperty(NODE_UUID, this.nodeUUID);
         connection.addRequestProperty(ACTION, DEACTIVATE);
     }

     /**
      * Get activation URL
      * @param subscriberInfo
      * @return activation handle
      */
     protected String getActivationURL(Subscriber subscriberInfo) {
         if (!subscriberInfo.getURL().endsWith("/")) {
             return subscriberInfo.getURL() + "/" + DEFAULT_HANDLER;
         }
         return subscriberInfo.getURL() + DEFAULT_HANDLER;
     }

     /**
      * add request headers needed for this activation
      * @param connection
      * @param activationContent
      */
     protected void addActivationHeaders(URLConnection connection, ActivationContent activationContent) {
         Iterator headerKeys = activationContent.getProperties().keySet().iterator();
         while (headerKeys.hasNext()) {
             String key = (String) headerKeys.next();
             String value = activationContent.getproperty(key);
             connection.setRequestProperty(key, value);
         }
     }

     /**
      * Update activation meta data
      * @throws RepositoryException
      */
     protected void updateActivationDetails() throws RepositoryException {
         Content page = getHierarchyManager().getContent(this.path);
         updateMetaData(page, ACTIVATE);
         page.save();
     }

     /**
      * Update de-activation meta data
      * @throws RepositoryException
      */
     protected void updateDeactivationDetails() throws RepositoryException {
         Content page = getHierarchyManager().getContentByUUID(this.nodeUUID);
         updateMetaData(page, DEACTIVATE);
         page.save();
         AuditLoggingUtil.log(AuditLoggingUtil.ACTION_DEACTIVATE, this.workspaceName, page.getHandle() );
     }


     private HierarchyManager getHierarchyManager() {
         return MgnlContext.getHierarchyManager(this.repositoryName, this.workspaceName);
     }

     /**
      * @param node
      * @param type (activate / deactivate)
      */
     protected void updateMetaData(Content node, String type) throws AccessDeniedException {
         // update the passed node
         MetaData md = node.getMetaData();
         if (type.equals(ACTIVATE)) {
             md.setActivated();
         }
         else {
             md.setUnActivated();
         }
         md.setActivatorId(this.user.getName());
         md.setLastActivationActionDate();

         Iterator children;
         if (type.equals(ACTIVATE)) {
             // use syndicator rule based filter
             children = node.getChildren(this.contentFilter).iterator();
         }
         else {
             // all children
             children = node.getChildren(ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER).iterator();
         }

         while (children.hasNext()) {
             Content child = (Content) children.next();
             this.updateMetaData(child, type);
         }


     }

     /**
      * Collect Activation content
      * @throws Exception
      */
     protected ActivationContent collect(Content node, List orderBefore) throws Exception {
         File resourceFile = File.createTempFile("resources", ".xml", Path.getTempDirectory());

         ActivationContent activationContent = new ActivationContent();
         // add global properties true for this path/hierarchy
         activationContent.addProperty(PARENT_PATH, this.parent);
         activationContent.addProperty(WORKSPACE_NAME, this.workspaceName);
         activationContent.addProperty(REPOSITORY_NAME, this.repositoryName);
         activationContent.addProperty(RESOURCE_MAPPING_FILE, resourceFile.getName());//"resources.xml");
         activationContent.addProperty(ACTION, ACTIVATE);
         activationContent.addProperty(CONTENT_FILTER_RULE, this.contentFilterRule.toString());
         activationContent.addProperty(NODE_UUID, node.getUUID());


         Document document = new Document();
         Element root = new Element(RESOURCE_MAPPING_ROOT_ELEMENT);
         document.setRootElement(root);
         // collect exact order of this node within its same nodeType siblings
         addOrderingInfo(root, orderBefore);

         this.addResources(root, node.getWorkspace().getSession(), node, this.contentFilter, activationContent);
         XMLOutputter outputter = new XMLOutputter();
         outputter.output(document, new FileOutputStream(resourceFile));
         // add resource file to the list
         //activationContent.addFile("resources.xml", resourceFile);
         activationContent.addFile(resourceFile.getName(), resourceFile);

         return activationContent;
     }

     /**
      * add ordering info to the resource file mapping
      * @param root element of the resource file under which ordering info must be added
      * @param orderBefore
      * */
     protected void addOrderingInfo(Element root, List orderBefore) {
         //do not use magnolia Content class since these objects are only meant for a single use to read UUID
         Element siblingRoot = new Element(SIBLINGS_ROOT_ELEMENT);
         root.addContent(siblingRoot);
         if (orderBefore == null) return;
         Iterator siblings = orderBefore.iterator();
         while (siblings.hasNext()) {
             String uuid = (String) siblings.next();
             Element e = new Element(SIBLINGS_ELEMENT);
             e.setAttribute(SIBLING_UUID, uuid);
             siblingRoot.addContent(e);
         }
     }

     /**
      * @param resourceElement
      * @param session
      * @param content
      * @param filter
      * @param activationContent
      * @throws IOException
      * @throws RepositoryException
      */
     protected void addResources(Element resourceElement, Session session, Content content, Content.ContentFilter filter, ActivationContent activationContent) throws IOException, RepositoryException, SAXException, Exception {
         File file = File.createTempFile("exchange_" + content.getUUID(), ".xml.gz", Path.getTempDirectory());
         GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(file));

         if (content.getWorkspace().getName().equalsIgnoreCase(ContentRepository.VERSION_STORE)) {
             XMLReader elementfilter = new FrozenElementFilter(XMLReaderFactory
                 .createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName()));
             ((FrozenElementFilter) elementfilter).setNodeName(content.getName());
             /**
              * nt:file node type has mandatory sub nodes
              */
             boolean noRecurse = !content.isNodeType(ItemType.NT_FILE);
             exportAndParse(session, content, elementfilter, gzipOutputStream, noRecurse);
         } else {
             /**
              * nt:file node type has mandatory sub nodes
              */
             if (content.isNodeType(ItemType.NT_FILE)) {
                 session.exportSystemView(content.getJCRNode().getPath(), gzipOutputStream, false, false);
             }
             else {
                 session.exportSystemView(content.getJCRNode().getPath(), gzipOutputStream, false, true);
             }
         }

         IOUtils.closeQuietly(gzipOutputStream);
         // add file entry in mapping.xml
         Element element = new Element(RESOURCE_MAPPING_FILE_ELEMENT);
         element.setAttribute(RESOURCE_MAPPING_NAME_ATTRIBUTE, content.getName());
         element.setAttribute(RESOURCE_MAPPING_UUID_ATTRIBUTE, content.getUUID());
         element.setAttribute(RESOURCE_MAPPING_ID_ATTRIBUTE, file.getName());
         resourceElement.addContent(element);
         // add this file element as resource in activation content
         activationContent.addFile(file.getName(), file);

         Iterator children = content.getChildren(filter).iterator();
         while (children.hasNext()) {
             Content child = (Content) children.next();
             this.addResources(element, session, child, filter, activationContent);
         }
     }

     /**
      * @param session
      * @param content
      * @param elementfilter
      * @param os
      * @param noRecurse
      * */
     protected void exportAndParse(Session session, Content content, XMLReader elementfilter, OutputStream os, boolean noRecurse) throws Exception {
         File tempFile = File.createTempFile("Frozen_"+content.getName(), ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
         OutputStream tmpFileOutStream = null;
         FileInputStream tmpFileInStream = null;
         try {
             tmpFileOutStream = new FileOutputStream(tempFile);
             session.exportSystemView(content.getJCRNode().getPath(), tmpFileOutStream, false, noRecurse);
             tmpFileOutStream.flush();
             tmpFileOutStream.close();

             OutputFormat outputFormat = new OutputFormat();
             outputFormat.setPreserveSpace(false);

             tmpFileInStream = new FileInputStream(tempFile);
             elementfilter.setContentHandler(new XMLSerializer(os, outputFormat));
             elementfilter.parse(new InputSource(tmpFileInStream));
             tmpFileInStream.close();
         } catch (Throwable t) {
             log.error("Failed to parse XML using FrozenElementFilter",t);
             throw new Exception(t);
         } finally {
             IOUtils.closeQuietly(tmpFileInStream);
             IOUtils.closeQuietly(tmpFileOutStream);
             tempFile.delete();
         }
     }


}
