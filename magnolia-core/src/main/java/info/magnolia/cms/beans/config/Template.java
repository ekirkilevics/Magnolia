/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class Template implements Serializable {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Contains all the Template parameters.
     */
    private Map parameters;

    /**
     * Mandatory.
     */
    private boolean visible;

    private Map alternativeTemplates;

    /**
     * Used internally for SubTemplates.
     */
    private Template() {

    }

    public Template(Content c) {

        parameters = nodeDataCollectionToStringMap(c.getNodeDataCollection());
        if (getName() == null) {
            parameters.put("name", c.getName());
        }

        this.visible = c.getNodeData("visible").getBoolean(); //$NON-NLS-1$

        addAlternativePaths(c);
    }

    /**
     * Getter for <code>description</code>.
     * @return Returns the description.
     */
    public String getDescription() {
        return getParameter("description");
    }

    /**
     * Getter for <code>name</code>.
     * @return Returns the name.
     */
    public String getName() {
        return getParameter("name");
    }

    /**
     * Getter for <code>path</code>.
     * @return Returns the path.
     */
    public String getPath() {
        return getParameter("path");
    }

    /**
     * Getter for <code>title</code>.
     * @return Returns the title.
     */
    public String getTitle() {
        return getParameter("title");
    }

    /**
     * Getter for <code>image</code>.
     * @return Returns the image.
     */
    public String getImage() {
        return getParameter("image");
    }

    /**
     * Getter for <code>type</code>.
     * @return Returns the type.
     */
    public String getType() {
        return getParameter("type");
    }

    /**
     * Getter for <code>location</code>.
     * @return Returns the location.
     */
    public String getLocation() {
        return getParameter("location");
    }

    public String getParameter(String key) {
        return (String) parameters.get(key);
    }

    /**
     * Getter for <code>visible</code>.
     * @return Returns the visible.
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * @param extension
     * @deprecated obtain the template using {@link TemplateManager#getInfo(String, String)} and then use
     * {@link Template#getPath()}.
     * @return template path for the specified extension
     */
    public String getPath(String extension) {
        Template template = getSubTemplate(extension);

        if (template != null) {
            return template.getPath();
        }

        return getParameter("path");
    }

    /**
     * @param extension
     * @return template path for the specified extension
     */
    public Template getSubTemplate(String extension) {
        if (alternativeTemplates != null) {
            return (Template) this.alternativeTemplates.get(extension);
        }
        return null;
    }

    /**
     * Add alternative extention paths to templates cache.
     * @param node
     * @param ti TemplateInfo
     */
    public void addAlternativePaths(Content node) {

        Content cl = node.getChildByName("SubTemplates");//$NON-NLS-1$

        if (cl == null) {
            return;
        }

        Iterator it = cl.getChildren(ItemType.CONTENTNODE).iterator();

        this.alternativeTemplates = new HashMap();

        while (it.hasNext()) {
            Content c = (Content) it.next();

            Template template = new Template();

            template.parameters = new HashMap(this.parameters);
            template.parameters.putAll(nodeDataCollectionToStringMap(c.getNodeDataCollection()));

            nodeDataCollectionToStringMap(c.getNodeDataCollection());

            template.visible = visible;

            synchronized (alternativeTemplates) {
                this.alternativeTemplates.put(c.getNodeData("extension").getString(), template); //$NON-NLS-1$ 
            }
        }

    }

    private Map nodeDataCollectionToStringMap(Collection collection) {
        Map map = new HashMap();
        Iterator it = IteratorUtils.getIterator(collection);
        while (it.hasNext()) {
            NodeData data = (NodeData) it.next();
            map.put(data.getName(), data.getString());
        }
        return map;
    }

}
