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
package info.magnolia.cms.exchange.ice;

import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.Path;
import info.magnolia.cms.util.ReverseFileReader;
import info.magnolia.cms.util.regex.RegexWildcardPattern;
import info.magnolia.exchange.Packet;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;


/**
 * @author Sameer Charles
 */
public class PacketCollector {

    public static final String MAIN_PACKET = "main";

    private static Logger log = Logger.getLogger(PacketFactory.class);

    private static final String ROOT_ELEMENT = "Root";

    private static final String CONTENT_ELEMENT = "Content";

    private static final String CONTENT_NODE_ELEMENT = "ContentNode";

    private static final String NODE_DATA_ELEMENT = "NodeData";

    private Map packets;

    private Subscriber subscriber;

    private Document masterPacketData;

    private Session contextSession;

    private String contextNameString;

    /**
     * <p>
     * Creates a packet collector for the specified subscriber.
     * </p>
     * @param subscriber
     */
    public PacketCollector(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    /**
     * <p>
     * Collect data needed to be sent to the current subscriber.
     * </p>
     * @param contextSession
     * @param path
     * @param depth
     */
    public void collect(Session contextSession, String path, int depth) throws RepositoryException {
        this.contextSession = contextSession;
        this.contextNameString = this.contextSession.getRootNode().getName();
        this.packets = new Hashtable();
        HierarchyManager hm = new HierarchyManager();
        hm.init(this.contextSession.getRootNode());
        if (hm.isPage(path)) {
            this.masterPacketData = new Document();
            this.createXMLPacket(hm.getPage(path));
        }
        else if (hm.isContentNode(path)) {
        }
        else if (hm.isNodeData(path)) {
            this.createBinaryPacket(hm.getNodeData(path));
        }
        else {
            throw new RepositoryException("Invalid path (does not match to any JCR type)");
        }
    }

    /**
     * @param content
     */
    private void createXMLPacket(Content content) {
        Element root = new Element(PacketCollector.ROOT_ELEMENT);
        this.masterPacketData.setRootElement(root);
        Element page = new Element(PacketCollector.CONTENT_ELEMENT);
        page.setAttribute(Header.PATH, content.getHandle());
        root.addContent(page);
        this.addNodeDataList(root, content);
        this.addContentNodeList(root, content);
        Packet packet = PacketFactory.getPacket(this.masterPacketData);
        packet.getHeaders().addHeader(Header.PATH, content.getHandle());
        this.setContext(packet);
        this.packets.put(PacketCollector.MAIN_PACKET, packet);
    }

    /**
     * @param root , root element of the mail packet
     * @param content
     */
    private void addContentNodeList(Element root, Content content) {
        Collection children = content.getChildren(ItemType.NT_CONTENTNODE);
        if (children == null || (children.isEmpty())) {
            return;
        }
        Iterator childIterator = children.iterator();
        while (childIterator.hasNext()) {
            Content contentNode = (Content) childIterator.next();
            /* add only if it has been changed since last activation */
            if (this.isActivated(contentNode)) {
                continue;
            }
            Element contentNodeElement = new Element(PacketCollector.CONTENT_NODE_ELEMENT);
            root.addContent(contentNodeElement);
            contentNodeElement.setAttribute(Header.PATH, contentNode.getHandle());
            /* add all non-binary properties to xml */
            this.addNodeDataList(root, contentNode);
            if (contentNode.hasChildren(ItemType.NT_CONTENTNODE)) {
                this.addContentNodeList(root, contentNode);
            }
        }
    }

    /**
     * @param root , root element of the mail packet
     * @param content
     */
    private void addNodeDataList(Element root, Content content) {
        Collection properties = content.getChildren(ItemType.MAGNOLIA_NODE_DATA);
        if (properties == null) {
            return;
        }
        Iterator propertyListIterator = properties.iterator();
        while (propertyListIterator.hasNext()) {
            NodeData nodeData = (NodeData) propertyListIterator.next();
            int nodeDataType = nodeData.getType();
            if (nodeDataType == PropertyType.BINARY) {
                this.createBinaryPacket(nodeData);
                continue;
            }
            Element nodeDataElement = new Element(PacketCollector.NODE_DATA_ELEMENT);
            root.addContent(nodeDataElement);
            nodeDataElement.setAttribute(Header.PATH, nodeData.getHandle());
            nodeDataElement.setAttribute(Header.TYPE, (new Integer(nodeDataType).toString()));
            Element data = new Element(Header.DATA);
            nodeDataElement.addContent(data);
            String value = null;
            switch (nodeDataType) {
                case PropertyType.STRING:
                    value = nodeData.getString();
                    break;
                case PropertyType.LONG:
                    value = (new Long(nodeData.getLong())).toString();
                    break;
                case PropertyType.DOUBLE:
                    value = (new Double(nodeData.getDouble())).toString();
                    break;
                case PropertyType.BOOLEAN:
                    value = BooleanUtils.toStringTrueFalse(nodeData.getBoolean());
                    break;
                case PropertyType.DATE:
                    value = nodeData.getDate().getTime().toString();
                    break;
                default:
                    value = StringUtils.EMPTY;
            }
            data.setText(value);
        }
    }

    /**
     * <p>
     * only works for binary files, wraps nodeData to info.magnolia.exchange.Packet
     * </p>
     * @see info.magnolia.exchange.Packet
     * @param nodeData
     */
    private void createBinaryPacket(NodeData nodeData) {
        try {
            Packet packet = PacketFactory.getPacket(nodeData.getValue().getStream());
            packet.getHeaders().addHeader(Header.PATH, nodeData.getHandle());
            this.setContext(packet);
            this.packets.put(packet.getID(), packet);
        }
        catch (RepositoryException e) {
            log.error("Failed to create exchange packet for BINARY file - " + nodeData.getHandle());
            log.error(e.getMessage());
        }
    }

    /**
     * @return packets collected (XML / Binary)
     */
    protected Map getPackets() {
        return this.packets;
    }

    /**
     * @param packet
     */
    private void setContext(Packet packet) {
        try {
            packet.getHeaders().addHeader(Header.CONTEXT, this.contextNameString);
        }
        catch (Exception e) {
            log.error("Failed to set context for packet - " + packet.getID());
            log.error(e.getMessage());
        }
    }

    /**
     * <p>
     * Compares last modification time with the last activation or deactivation time in history logs. <br>
     * reads history file backward to check for any trace for deactivation/activation for "this" subscriber
     * </p>
     * @param contentNode
     */
    private boolean isActivated(Content contentNode) {
        String exchangeHistoryFilePath = Path.getHistoryFilePath();
        SimpleDateFormat sdf = new SimpleDateFormat("d.M.y H:m:s");
        try {
            long lastModification = contentNode.getMetaData().getModificationDate().getTime().getTime();
            ReverseFileReader rfr = new ReverseFileReader(exchangeHistoryFilePath, "r");
            String record = StringUtils.EMPTY;
            while ((record = rfr.getRecord()) != null) {

                String pattern = RegexWildcardPattern.getMultipleCharPattern()
                    + " --- "
                    + this.subscriber.getName()
                    + " --- "
                    + this.contextNameString
                    + " --- "
                    + contentNode.getHandle()
                    + " --- "
                    + RegexWildcardPattern.getMultipleCharPattern();
                if (record.matches(pattern)) {
                    if (record.indexOf(" --- REMOVED --- ") > -1) {
                        return false; /* URI deactivated */
                    }
                    /**
                     * check if the time of last SENT event is after the node modification
                     */
                    long lastActivation = sdf.parse(record.substring(0, 19)).getTime();
                    if (lastActivation > lastModification) {
                        return true;
                    }
                    break;
                }
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        log.info("URI - " + contentNode.getHandle() + " needs to activated for [ " + this.subscriber.getName() + " ]");
        return false;
    }
}
