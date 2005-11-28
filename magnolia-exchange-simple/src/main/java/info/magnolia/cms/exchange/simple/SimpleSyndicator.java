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
package info.magnolia.cms.exchange.simple;

import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.core.*;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.exchange.*;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;


/**
 * @author Sameer Charles
 * @version $Revision: 1633 $ ($Author: scharles $)
 */
public class SimpleSyndicator implements Syndicator {

    /**
     * activation handler servlet name as mapped in web descriptor
     * */
    public static final String DEFAULT_HANDLER = "ActivationHandler"; //$NON-NLS-1$

    /**
     * parent path
     * */
    public static final String PARENT_PATH = "mgnlExchangeParentPath";

    /**
     * activated/deactivated path
     * */
    public static final String PATH = "mgnlExchangePath";

    /**
     * repository name
     * */
    public static final String REPOSITORY_NAME = "mgnlExchangeRepositoryName";

    /**
     * workspace name
     * */
    public static final String WORKSPACE_NAME = "mgnlExchangeWorkspaceName";

    /**
     * resource reading sequence
     * */
    public static final String RESOURCE_MAPPING_FILE = "mgnlExchangeResourceMappingFile";

    /**
     * resource file, File element
     * */
    public static final String RESOURCE_MAPPING_FILE_ELEMENT = "File";

    /**
     * resource file, name attribute
     * */
    public static final String RESOURCE_MAPPING_NAME_ATTRIBUTE = "name";

    /**
     * resource file, resourceId attribute
     * */
    public static final String RESOURCE_MAPPING_ID_ATTRIBUTE = "resourceId";

    /**
     * resource file, root element
     * */
    public static final String RESOURCE_MAPPING_ROOT_ELEMENT = "Resources";

    /**
     * Action
     * */
    public static final String ACTION = "mgnlExchangeAction";

    /**
     * possible value for attribute "ACTION"
     * */
    public static final String ACTIVATE = "activate"; //$NON-NLS-1$

    /**
     * possible value for attribute "ACTION"
     * */
    public static final String DE_ACTIVATE = "deactivate"; //$NON-NLS-1$

    /**
     * request authorization exception
     * */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * attribute rule
     * */
    public static final String CONTENT_FILTER_RULE = "mgnlExchangeFilterRule";

    /**
     * return status values all simple activation headers start from sa_
     */
    public static final String ACTIVATION_SUCCESSFUL = "sa_success"; //$NON-NLS-1$

    public static final String ACTIVATION_FAILED = "sa_failed"; //$NON-NLS-1$

    public static final String ACTIVATION_ATTRIBUTE_STATUS = "sa_attribute_status"; //$NON-NLS-1$

    public static final String ACTIVATION_ATTRIBUTE_MESSAGE = "sa_attribute_message"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(SimpleSyndicator.class);

    private HttpServletRequest request;

    private String repositoryName;

    private String workspaceName;

    private String parent;

    private String path;

    private Content.ContentFilter contentFilter;

    private Rule contentFilterRule;

    /**
     * @param request
     * @param repositoryName repository ID
     * @param workspaceName workspace ID
     * @param rule content filter rule
     * */
    public SimpleSyndicator(HttpServletRequest request,
                      String repositoryName,
                      String workspaceName,
                      Rule rule) {
        this.request = request;
        this.contentFilter = new RuleBasedContentFilter(rule);
        this.contentFilterRule = rule;
        this.repositoryName = repositoryName;
        this.workspaceName = workspaceName;
    }

    /**
     * <p>
     * this will activate specifies page (sub pages) to all configured subscribers
     * </p>
     * @param parent parent under which this page will be activated
     * @param path page to be activated
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public synchronized void activate(String parent, String path)
            throws ExchangeException, RepositoryException  {
        this.parent = parent;
        this.path = path;
        ActivationContent activationContent = null;
        try {
            activationContent = this.collect();
            this.activate(activationContent);
            this.updateActivationDetails();
        } catch (Exception e) {
            throw new ExchangeException(e);
        } finally {
            log.debug("Cleaning temporary files");
            cleanTemporaryStore(activationContent);
        }
    }

    /**
     * <p>
     * this will activate specifies page (sub pages) to the specified subscribers
     * </p>
     * @param subscriber
     * @param parent parent under which this page will be activated
     * @param path page to be activated
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public synchronized void activate(Subscriber subscriber,
                                      String parent,
                                      String path)
            throws ExchangeException, RepositoryException {
        this.parent = parent;
        this.path = path;
        ActivationContent activationContent = null;
        try {
            activationContent = this.collect();
            this.activate(subscriber, activationContent);
            this.updateActivationDetails();
        } catch (Exception e) {
            throw new ExchangeException(e);
        } finally {
            log.debug("Cleaning temporary files");
            cleanTemporaryStore(activationContent);
        }
    }

    /**
     * @throws ExchangeException
     */
    private synchronized void activate(ActivationContent activationContent)
            throws ExchangeException {
        Enumeration en = Subscriber.getList();
        while (en.hasMoreElements()) {
            Subscriber si = (Subscriber) en.nextElement();
            if (si.isActive()) {
                activate(si, activationContent);
            }
        }
    }

    /**
     * Send activation request if subscribed to the activated URI
     * @param subscriber
     * @param activationContent
     * @throws ExchangeException
     */
    private synchronized void activate(Subscriber subscriber, ActivationContent activationContent)
            throws ExchangeException {
        if (!isSubscribed(subscriber)) {
            if (log.isDebugEnabled()) {
                log.debug("Exchange : subscriber [ " + subscriber.getName() + " ] is not subscribed to " + this.path);
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Exchange : sending activation request to " + subscriber.getName()); //$NON-NLS-1$
            log.debug("Exchange : user [ " + Authenticator.getUserId(this.request) + " ]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        String handle = getActivationURL(subscriber);
        try {
            URL url = new URL(handle);
            URLConnection urlConnection = url.openConnection();
            this.addActivationHeaders(urlConnection, activationContent);

            Transporter.transport(urlConnection, activationContent);

            String status = urlConnection.getHeaderField(SimpleSyndicator.ACTIVATION_ATTRIBUTE_STATUS);

            // check if the activation failed
            if (StringUtils.equals(status, SimpleSyndicator.ACTIVATION_FAILED)) {
                String message = urlConnection.getHeaderField(SimpleSyndicator.ACTIVATION_ATTRIBUTE_MESSAGE);
                throw new ExchangeException("Message received from subscriber: " + message);
            }
            urlConnection.getContent();
            log.info("Exchange : activation request received by " + subscriber.getName()); //$NON-NLS-1$
        } catch (ExchangeException e) {
            throw e;
        } catch (MalformedURLException e) {
            throw new ExchangeException("Incorrect URL for subscriber " + subscriber  + "[" + handle + "]");
        } catch (IOException e) {
            throw new ExchangeException("Not able to send the activation request [" +  handle + "]: " + e.getMessage());
        } catch (Exception e) {
            throw new ExchangeException(e);
        }
    }

    /**
     * cleans temporary store
     * @param activationContent
     * */
    private void cleanTemporaryStore(ActivationContent activationContent) {
        if (activationContent == null) {
            log.debug("Clean temporary store - nothing to do");
            return;
        }
        Iterator keys = activationContent.getFiles().keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (log.isDebugEnabled()) {
                log.debug("Removing temporary file - "+key);
            }
            activationContent.getFile(key).delete();
        }
    }

    private boolean isSubscribed(Subscriber subscriber) {
        boolean isSubscribed = false;
        List subscribedURIList = subscriber.getContext(this.repositoryName);
        for (int i = 0; i < subscribedURIList.size(); i++) {
            String uri = (String) subscribedURIList.get(i);
            if (this.path.equals(uri)) {
                isSubscribed = true;
            }
            else if (this.path.startsWith(uri + "/")) { //$NON-NLS-1$
                isSubscribed = true;
            }
            else if (uri.endsWith("/") && (this.path.startsWith(uri))) { //$NON-NLS-1$
                isSubscribed = true;
            }
        }
        return isSubscribed;
    }

    /**
     * @param path , to deactivate
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public synchronized void deActivate(String path)
            throws ExchangeException, RepositoryException {
        this.path = path;
        this.doDeActivate();
    }

    /**
     * @param path , to deactivate
     * @param subscriber
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public synchronized void deActivate(Subscriber subscriber, String path)
            throws ExchangeException, RepositoryException {
        this.path = path;
        this.doDeActivate(subscriber);
    }

    /**
     * @throws RepositoryException 
     * @throws ExchangeException
     */
    private synchronized void doDeActivate() throws ExchangeException, RepositoryException{
        Enumeration en = Subscriber.getList();
        while (en.hasMoreElements()) {
            Subscriber si = (Subscriber) en.nextElement();
            if (!si.isActive()) {
                continue;
            }
            if (log.isDebugEnabled()) {
                log.debug("Removing [ " + this.path + " ] from [ " + si.getAddress() + " ]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            doDeActivate(si);
        }
    }

    private synchronized void doDeActivate(Subscriber subscriber) throws ExchangeException, RepositoryException {
        if (!isSubscribed(subscriber)) {
            return;
        }
        String handle = getDeactivationURL(subscriber);
        try {
            URL url = new URL(handle);
            URLConnection urlConnection = url.openConnection();
            this.addDeactivationHeaders(urlConnection);
            urlConnection.getContent();
            updateDeActivationDetails();
        }
        catch (MalformedURLException e) {
            throw new ExchangeException("Incorrect URL for subscriber " + subscriber  + "[" + handle + "]");
        }
        catch (IOException e) {
            throw new ExchangeException("Not able to send the deactivation request [" +  handle + "]: " + e.getMessage());        }
        catch (RepositoryException e) {
            throw e;
        }
    }

    /**
     *
     */
    private String getDeactivationURL(Subscriber subscriberInfo) {
        String handle = subscriberInfo.getProtocol() + "://" + subscriberInfo.getAddress() + "/" + DEFAULT_HANDLER; //$NON-NLS-1$ //$NON-NLS-2$
        return handle;
    }

    private void addDeactivationHeaders(URLConnection connection) {
        connection.setRequestProperty(AUTHORIZATION, Authenticator.getCredentials(this.request));
        connection.addRequestProperty(REPOSITORY_NAME, this.repositoryName);
        connection.addRequestProperty(WORKSPACE_NAME, this.workspaceName);
        connection.addRequestProperty(PATH, this.path);
        connection.addRequestProperty(ACTION, DE_ACTIVATE);
    }

    /**
     * @return activation handle
     */
    private String getActivationURL(Subscriber subscriberInfo) {
        String handle = subscriberInfo.getProtocol() + "://" + subscriberInfo.getAddress() + "/" + DEFAULT_HANDLER; //$NON-NLS-1$ //$NON-NLS-2$
        return handle;
    }

    /**
     * add request headers needed for this activation
     * @param connection
     * @param activationContent
     * */
    private void addActivationHeaders(URLConnection connection, ActivationContent activationContent) {
        Iterator headerKeys = activationContent.getProperties().keySet().iterator();
        while (headerKeys.hasNext()) {
            String key = (String) headerKeys.next();
            String value = activationContent.getproperty(key);
            connection.setRequestProperty(key, value);
        }
    }

    /**
     *
     */
    private void updateActivationDetails() throws RepositoryException {
        HierarchyManager hm =
                SessionAccessControl.getHierarchyManager(this.request, this.repositoryName, this.workspaceName);
        Content page = hm.getContent(this.path);
        updateMetaData(page, SimpleSyndicator.ACTIVATE);
        page.save();
    }

    /**
     */
    private void updateDeActivationDetails() throws RepositoryException {
        HierarchyManager hm =
                SessionAccessControl.getHierarchyManager(this.request, this.repositoryName, this.workspaceName);
        Content page = hm.getContent(this.path);
        updateMetaData(page, SimpleSyndicator.DE_ACTIVATE);
        page.save();
    }

    /**
     * @param node
     * @param type (activate / deactivate)
     */
    private void updateMetaData(Content node, String type)
        throws AccessDeniedException {
        // update the passed node
        MetaData md = node.getMetaData(MetaData.ACTIVATION_INFO);
        if (type.equals(SimpleSyndicator.ACTIVATE)) {
            md.setActivated();
        }
        else {
            md.setUnActivated();
        }
        md.setActivatorId(Authenticator.getUserId(this.request));
        md.setLastActivationActionDate();

        Iterator children = node.getChildren(this.contentFilter).iterator();
        while (children.hasNext()) {
            Content child = (Content) children.next();
            this.updateMetaData(child, type);
        }

    }



    /**
     * Collect Activation content
     * @throws Exception
     * */
    private ActivationContent collect() throws Exception {
        ActivationContent activationContent = new ActivationContent();
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(request,
                this.repositoryName,
                this.workspaceName);
        // add global properties true for this path/hierarchy
        activationContent.addProperty(PARENT_PATH, this.parent);
        activationContent.addProperty(WORKSPACE_NAME, this.workspaceName);
        activationContent.addProperty(REPOSITORY_NAME, this.repositoryName);
        activationContent.addProperty(RESOURCE_MAPPING_FILE, "resources.xml");
        activationContent.addProperty(ACTION, ACTIVATE);
        activationContent.addProperty(CONTENT_FILTER_RULE, this.contentFilterRule.toString());
        activationContent.addProperty(AUTHORIZATION, Authenticator.getCredentials(this.request));


        Document document = new Document();
        Element root = new Element(RESOURCE_MAPPING_ROOT_ELEMENT);
        document.setRootElement(root);
        this.addResources(
                root,
                hm.getWorkspace().getSession(),
                hm.getContent(path),
                this.contentFilter,
                activationContent
                );
        File resourceFile = File.createTempFile("resources","",Path.getTempDirectory());
        XMLOutputter outputter = new XMLOutputter();
        outputter.output(document, new FileOutputStream(resourceFile));
        // add resource file to the list
        activationContent.addFile("resources.xml", resourceFile);

        return activationContent;
    }

    /**
     * @param resourceElement
     * @param session
     * @param content
     * @param filter
     * @param activationContent
     * @throws IOException
     * @throws RepositoryException
     * */
    private void addResources(Element resourceElement,
                              Session session,
                              Content content,
                              Content.ContentFilter filter,
                              ActivationContent activationContent)
            throws IOException, RepositoryException {

        File file = File.createTempFile("exchange"+content.getName(),"",Path.getTempDirectory());
        FileOutputStream outputStream = new FileOutputStream(file);

        /**
         * nt:file node type has mandatory sub nodes
         * */
        if (content.isNodeType(ItemType.NT_FILE)) {
            session.exportSystemView(content.getHandle(), outputStream, false, false);
        } else {
            session.exportSystemView(content.getHandle(), outputStream, false, true);
        }
        // add file entry in mapping.xml
        Element element = new Element(RESOURCE_MAPPING_FILE_ELEMENT);
        element.setAttribute(RESOURCE_MAPPING_NAME_ATTRIBUTE,content.getName());
        element.setAttribute(RESOURCE_MAPPING_ID_ATTRIBUTE,file.getName());
        resourceElement.addContent(element);
        // add this file element as resource in activation content
        activationContent.addFile(file.getName(), file);

        Iterator children = content.getChildren(filter).iterator();
        while (children.hasNext()) {
            Content child = (Content) children.next();
            this.addResources(element, session, child, filter, activationContent);
        }
    }

}
