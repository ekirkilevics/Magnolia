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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;


/**
 * @author Sameer Charles
 * @version 1.1
 */
public class Template {

    /**
     * Template name.
     */
    private String name;

    /**
     * Template path.
     */
    private String path;

    private Map alternativePaths;

    /**
     * Mandatatory.
     */
    private String type;

    /**
     * Mandatatory.
     */
    private boolean visible;

    /**
     * Optional fields.
     */
    private String description;

    private String image;

    private String title;

    private String location;

    public Template(Content c) throws ValueFormatException, IllegalStateException, RepositoryException {
        this.name = c.getNodeData("name").getValue().getString(); //$NON-NLS-1$
        // ti.name = getTemplatePath(c);
        this.path = c.getNodeData("path").getValue().getString(); //$NON-NLS-1$
        addAlternativePaths(c);
        this.type = c.getNodeData("type").getValue().getString(); //$NON-NLS-1$
        this.visible = c.getNodeData("visible").getBoolean(); //$NON-NLS-1$
        this.title = c.getNodeData("title").getString(); //$NON-NLS-1$
        this.description = c.getNodeData("description").getString(); //$NON-NLS-1$
        this.image = c.getNodeData("image").getString(); //$NON-NLS-1$
        this.setLocation(c.getHandle());
	}

	/**
     *
     */
    public String getName() {
        return this.name;
    }

    /**
     *
     */
    public String getTitle() {
        return this.title;
    }

    /**
     *
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return default template path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @param extension
     * @return template path for the specified extension
     */
    public String getPath(String extension) {
        try {
            String path = (String) this.alternativePaths.get(extension);
            if (path == null) {
                return this.getPath();
            }
            return path;
        }
        catch (Exception e) {
            return this.getPath();
        }
    }
    
	/**
	 * Add alternative extention paths to templates cache.
	 * @param node
	 * @param ti TemplateInfo
	 */
	public void addAlternativePaths(Content node) {
	    try {
	        Content cl = node.getContent("SubTemplates"); //$NON-NLS-1$
	        Iterator it = cl.getChildren().iterator();
	        this.alternativePaths = new Hashtable();
	        while (it.hasNext()) {
	            Content c = (Content) it.next();
	            this.alternativePaths.put(c.getNodeData("extension").getString(), c.getNodeData("path").getString()); //$NON-NLS-1$ //$NON-NLS-2$
	        }
	    }
	    catch (RepositoryException re) {
	    }
	}

    /**
     *
     */
    public String getType() {
        return this.type;
    }

    /**
     *
     */
    public String getImage() {
        return this.image;
    }

    /**
     *
     */
    public boolean isVisible() {
        return this.visible;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
