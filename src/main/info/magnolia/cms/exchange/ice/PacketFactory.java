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

import info.magnolia.exchange.Packet;
import info.magnolia.exchange.PacketBody;
import info.magnolia.exchange.PacketHeader;
import info.magnolia.exchange.PacketIOException;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.jdom.Document;


/**
 * @author Sameer Charles
 */
public class PacketFactory {

    private static Logger log = Logger.getLogger(PacketFactory.class);

    /**
     * <p>
     * create Packet using JDOM Document
     * </p>
     * @param document
     */
    public static Packet getPacket(Document document) {
        Packet packet = new PacketImpl();
        PacketBody body = new PacketBodyImpl();
        try {
            body.setBody(document.toString());
        }
        catch (PacketIOException e) {
            log.error("Failed to set packet body");
            log.error(e.getMessage());
        }
        packet.setBody(body);
        packet.assignID();
        return packet;
    }

    /**
     * <p>
     * create Packet with the provided string body
     * </p>
     * @param bodyString
     */
    public static Packet getPacket(String bodyString) {
        Packet packet = new PacketImpl();
        PacketBody body = new PacketBodyImpl();
        try {
            body.setBody(bodyString);
        }
        catch (PacketIOException e) {
            log.error("Failed to set packet body");
            log.error(e.getMessage());
        }
        packet.setBody(body);
        packet.assignID();
        return packet;
    }

    /**
     * <p>
     * creates a packet using JDOM document as body and specified headers <br>
     * it will keep the existing header defined by the creation of packet itself
     * </p>
     * @param document
     * @param header
     */
    public static Packet getPacket(Document document, PacketHeader header) {
        Packet packet = getPacket(document);
        packet.setHeaders(header);
        return packet;
    }

    /**
     * <p>
     * creates a packet with Input steam as data without any headers
     * </p>
     * @param dataStream
     * @return newly created packet
     */
    public static Packet getPacket(InputStream dataStream) {
        Packet packet = new PacketImpl();
        PacketBody body = new PacketBodyImpl();
        try {
            body.setBody(dataStream);
        }
        catch (PacketIOException e) {
            log.error("Failed to set packet body");
            log.error(e.getMessage());
        }
        packet.setBody(body);
        packet.assignID();
        return packet;
    }

    /**
     * <p>
     * creates a packet with Input steam as data and headers as specified <br>
     * it will keep the existing header defined by the creation of packet itself
     * </p>
     * @param dataStream
     * @param header
     * @return newly created packet
     */
    public static Packet getPacket(InputStream dataStream, PacketHeader header) {
        Packet packet = getPacket(dataStream);
        packet.setHeaders(header);
        return packet;
    }
}
