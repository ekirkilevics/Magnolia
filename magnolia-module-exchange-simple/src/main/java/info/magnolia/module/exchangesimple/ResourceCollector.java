/**
 * This file Copyright (c) 2011 Magnolia International
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

import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.ACTION;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.ACTIVATE;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.CONTENT_FILTER_RULE;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.NODE_UUID;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.PARENT_PATH;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.REPOSITORY_NAME;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.RESOURCE_MAPPING_FILE;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.RESOURCE_MAPPING_FILE_ELEMENT;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.RESOURCE_MAPPING_ID_ATTRIBUTE;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.RESOURCE_MAPPING_MD_ATTRIBUTE;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.RESOURCE_MAPPING_NAME_ATTRIBUTE;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.RESOURCE_MAPPING_ROOT_ELEMENT;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.RESOURCE_MAPPING_UUID_ATTRIBUTE;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.SIBLINGS_ELEMENT;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.SIBLINGS_ROOT_ELEMENT;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.SIBLING_UUID;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.UTF8_STATUS;
import static info.magnolia.module.exchangesimple.BaseSyndicatorImpl.WORKSPACE_NAME;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.security.SecurityUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.cms.util.RuleBasedContentFilter;
import info.magnolia.repository.RepositoryConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Class responsible for collecting all the resources for activation of content.
 * 
 * @version $Id$
 * 
 */
public class ResourceCollector {

    private final MessageDigest md;

    public ResourceCollector() throws ExchangeException {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ExchangeException("In order to proceed with activation please run Magnolia CMS using Java version with MD5 support.", e);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ResourceCollector.class);

    /**
     * Collects all information about activated content and its children (those that are set to be activated with the parent by filter rules).
     * 
     * @param contentFilterRule
     * @param repositoryName
     * @param workspaceName
     * @param parent
     * 
     * @throws Exception
     */
    protected ActivationContent collect(Content node, List<String> orderBefore, String parent, String workspaceName, String repositoryName, Rule contentFilterRule) throws Exception {

        // just to be sure, there should be no reason for md to not be reset (reset is called automatically after digest call)
        md.reset();

        Content.ContentFilter contentFilter = new RuleBasedContentFilter(contentFilterRule);

        // make sure resource file is unique
        File resourceFile = File.createTempFile("resources", ".xml", Path.getTempDirectory());

        ActivationContent activationContent = new ActivationContent();
        // add global properties true for this path/hierarchy
        activationContent.addProperty(PARENT_PATH, parent);
        activationContent.addProperty(WORKSPACE_NAME, workspaceName);
        activationContent.addProperty(REPOSITORY_NAME, repositoryName);
        activationContent.addProperty(RESOURCE_MAPPING_FILE, resourceFile.getName());// "resources.xml");
        activationContent.addProperty(ACTION, ACTIVATE);
        activationContent.addProperty(CONTENT_FILTER_RULE, contentFilterRule.toString());
        activationContent.addProperty(NODE_UUID, node.getUUID());
        activationContent.addProperty(UTF8_STATUS, SystemProperty.getProperty(SystemProperty.MAGNOLIA_UTF8_ENABLED));

        Document document = new Document();
        Element root = new Element(RESOURCE_MAPPING_ROOT_ELEMENT);
        document.setRootElement(root);
        // collect exact order of this node within its same nodeType siblings
        addOrderingInfo(root, orderBefore);

        this.addResources(root, node.getWorkspace().getSession(), node, contentFilter, activationContent);
        XMLOutputter outputter = new XMLOutputter();

        outputter.output(document, new DigestOutputStream(new FileOutputStream(resourceFile), md));
        // add resource file to the list
        activationContent.addFile(resourceFile.getName(), resourceFile);
        // add signature of the resource file itself
        activationContent.addProperty(RESOURCE_MAPPING_MD_ATTRIBUTE, SecurityUtil.byteArrayToHex(md.digest()));

        // add deletion info
        activationContent.addProperty(ItemType.DELETED_NODE_MIXIN, "" + node.hasMixin(ItemType.DELETED_NODE_MIXIN));

        return activationContent;
    }

    /**
     * Adds ordering information to the resource mapping file.
     * 
     * @param root
     *            element of the resource file under which ordering info must be added
     * @param orderBefore
     */
    protected void addOrderingInfo(Element root, List<String> orderBefore) {
        // do not use magnolia Content class since these objects are only meant for a single use to read UUID
        Element siblingRoot = new Element(SIBLINGS_ROOT_ELEMENT);
        root.addContent(siblingRoot);
        if (orderBefore == null) {
            return;
        }
        Iterator<String> siblings = orderBefore.iterator();
        while (siblings.hasNext()) {
            String uuid = siblings.next();
            Element e = new Element(SIBLINGS_ELEMENT);
            e.setAttribute(SIBLING_UUID, uuid);
            siblingRoot.addContent(e);
        }
    }

    protected void addResources(Element resourceElement, Session session, final Content content, Content.ContentFilter filter, ActivationContent activationContent) throws IOException, RepositoryException, SAXException, Exception {
        final String workspaceName = content.getWorkspace().getName();
        log.debug("Preparing content {}:{} for publishing.", new String[] { workspaceName, content.getHandle() });
        final String uuid = content.getUUID();

        File file = File.createTempFile("exchange_" + uuid, ".xml.gz", Path.getTempDirectory());
        OutputStream outputStream = new DigestOutputStream(new GZIPOutputStream(new FileOutputStream(file)), md);

        // TODO: remove the second check. It should not be necessary. The only safe way to identify the versioned node is by looking at its type since the type is mandated by spec. and the frozen nodes is what the filter below removes anyway
        if (content.isNodeType("nt:frozenNode") || workspaceName.equals(RepositoryConstants.VERSION_STORE)) {
            XMLReader elementfilter = new FrozenElementFilter(XMLReaderFactory
                    .createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName()));
            ((FrozenElementFilter) elementfilter).setNodeName(content.getName());
            /**
             * nt:file node type has mandatory sub nodes
             */
            boolean noRecurse = !content.isNodeType(ItemType.NT_FILE);
            exportAndParse(session, content, elementfilter, outputStream, noRecurse);
        } else {
            /**
             * nt:file node type has mandatory sub nodes
             */
            if (content.isNodeType(ItemType.NT_FILE)) {
                session.exportSystemView(content.getJCRNode().getPath(), outputStream, false, false);
            } else {
                session.exportSystemView(content.getJCRNode().getPath(), outputStream, false, true);
            }
        }

        IOUtils.closeQuietly(outputStream);
        // add file entry in mapping.xml
        Element element = new Element(RESOURCE_MAPPING_FILE_ELEMENT);
        element.setAttribute(RESOURCE_MAPPING_NAME_ATTRIBUTE, content.getName());
        element.setAttribute(RESOURCE_MAPPING_UUID_ATTRIBUTE, uuid);
        element.setAttribute(RESOURCE_MAPPING_ID_ATTRIBUTE, file.getName());
        element.setAttribute(RESOURCE_MAPPING_MD_ATTRIBUTE, SecurityUtil.byteArrayToHex(md.digest()));
        resourceElement.addContent(element);
        // add this file element as resource in activation content
        activationContent.addFile(file.getName(), file);

        Iterator<Content> children = content.getChildren(filter).iterator();
        while (children.hasNext()) {
            Content child = children.next();
            this.addResources(element, session, child, filter, activationContent);
        }
    }

    /**
     * Exports frozen resource. We can't export such resource directly, but need to filter out versioning information from the export.
     */
    protected void exportAndParse(Session session, Content content, XMLReader elementfilter, OutputStream os, boolean noRecurse) throws Exception {
        File tempFile = File.createTempFile("Frozen_" + content.getName(), ".xml");
        OutputStream tmpFileOutStream = null;
        FileInputStream tmpFileInStream = null;
        try {
            tmpFileOutStream = new FileOutputStream(tempFile);
            // has to get path via JCR node since if "content" is of type ContentVersion, getHandle() call would have returned path to the base
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
            log.error("Failed to parse XML using FrozenElementFilter", t);
            throw new Exception(t);
        } finally {
            IOUtils.closeQuietly(tmpFileInStream);
            IOUtils.closeQuietly(tmpFileOutStream);
            tempFile.delete();
        }
    }

    // @Inject
    // public void setProperties(MagnoliaConfigurationProperties props) {
    // this.properties = props;
    // }
}
