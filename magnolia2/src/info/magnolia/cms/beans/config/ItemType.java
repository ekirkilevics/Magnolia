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



package info.magnolia.cms.beans.config;

import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.apache.log4j.Logger;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.io.File;

import info.magnolia.cms.util.Path;

/**
 * Date: Jun 2, 2004
 * Time: 12:34:22 PM
 *
 * @author Sameer Charles
 */


public class ItemType {


    private static Logger log = Logger.getLogger(ItemType.class);





    /**
     * Basic node types
     * */
    public static final String NT_BASE = "NT_Base";
    public static final String NT_UNSTRUCTRUED = "NT_Unstructured";
    public static final String NT_HIERARCHY = "NT_HierarchyItem";
    public static final String NT_FOLDER = "NT_Folder";
    public static final String NT_FILE = "NT_File";



    /**
     * Basic mixin types
     * */
    public static final String MIX_AccessControllable = "MIX_AccessControllable";
    public static final String MIX_Referenceable = "MIX_Referenceable";
    public static final String MIX_Versionable = "MIX_Versionable";




    /**
     * magnolia specific basic node types
     * */
    public static final String NT_CONTENT = "NT_Content";
    public static final String NT_CONTENTNODE = "NT_ContentNode";
    public static final String NT_NODEDATA = "NT_NodeData";



    /**
     * internal magnolia item type values
     * */
    public static final int MAGNOLIA_NODE_DATA = 0;
    public static final int MAGNOLIA_CONTENT_NODE = 1;
    public static final int MAGNOLIA_PAGE = 2;


    /**
     * magnolia item types
     * */
    public static final String MAGNOLIA_SYSTEM_DETAILS = "magnoliaSystem";
    public static final String MAGNOLIA_INTERNAL_TYPE = "internalType";



    /**
     * Basic child node types
     * */
    public static String JCR_CONTENT = "JCR_Content";


    /**
     * Basic property types
     * */
    public static String JCR_CREATED = "JCR_Created";
    public static String JCR_LASTMODIFIED = "JCR_LastModified";



    /**
     * Config elements
     * */
    private static final String ELEMENT_NODE_TYPE = "nodeType";
    private static final String ELEMENT_PROPERTY = "property";



    private static Hashtable map = new Hashtable();





    public static void init() {
        ItemType.map.clear();
        try {
            ItemType.cacheDocument();
            log.info("JCR Item type definition loaded");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
    }



    public static void reload() {
        log.info("Reload Item types - "+Path.getJCRItemTypesFile());
        ItemType.init();
    }



    /**
     * <p>
     * builds JDOM document
     * </p>
     * */
    private static Document buildDocument() throws Exception {
        File source = new File(Path.getJCRItemTypesFile());
        if (!source.exists())
            throw new Exception("Failed to locate JCR Item-types definition file");
        SAXBuilder builder = new SAXBuilder();
        return builder.build(source);
    }



    /**
     * <p>
     * cache ItemTypes in a flat hierarchy
     * </p>
     * */
    private static void cacheDocument() throws Exception {
        Document document = ItemType.buildDocument();
        Element root = document.getRootElement();
        Iterator nodeTypes = root.getChildren(ItemType.ELEMENT_NODE_TYPE).iterator();
        while (nodeTypes.hasNext()) {
            Element nodeType = (Element) nodeTypes.next();
            ItemType.map.put(nodeType.getAttributeValue("id"),nodeType.getAttributeValue("name"));
            List propertyDefList = nodeType.getChildren(ItemType.ELEMENT_PROPERTY);
            if ((propertyDefList==null) || propertyDefList.isEmpty())
                continue;
            Iterator propertyDefIterator = propertyDefList.iterator();
            while (propertyDefIterator.hasNext()) {
                Element property = (Element) propertyDefIterator.next();
                ItemType.map.put(property.getAttributeValue("id"),property.getAttributeValue("name"));
            }
        }
    }


    /**
     * <p>
     * gets internal name string of Item type as defined in itemtype.xml
     * </p>
     *
     * @param id (id is a use defined name for the JCR Item type)
     * */
    public static String getSystemName(String id) {
        return (String) ItemType.map.get(id);
    }




}
