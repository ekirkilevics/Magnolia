/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */




package info.magnolia.cms.exchange.simple;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Content;
import info.magnolia.exchange.Packet;
import info.magnolia.exchange.PacketIOException;

import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;


/**
 * Date: Jun 21, 2004
 * Time: 11:43:10 AM
 *
 * @author Sameer Charles
 * @version 2.0
 */



public class PacketCollector {




    private static Logger log = Logger.getLogger(PacketCollector.class);


    


    public static Packet getPacket(HierarchyManager hm, String path, boolean recurse) {
        Packet packet = new PacketImpl();
        try {
            Content page = hm.getPage(path);
            SerializableContent content = new SerializableContent(page, recurse);
            packet.getBody().setBody(content);
        } catch(RepositoryException re) {
            log.error(re.getMessage(), re);
        } catch (PacketIOException pe) {
            log.error(pe.getMessage(), pe);
        }


        return packet;
    }


}
