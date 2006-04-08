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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;


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

    /**
     * Template type (e.g. <code>jsp</code>).
     */
    private String type;

    /**
     * Mandatory.
     */
    private boolean visible;

    /**
     * Optional fields.
     */
    private String description;

    private String image;

    private String title;

    private String location;

    private Map alternativeTemplates;

    /**
     * Used internally for SubTemplates.
     */
    private Template() {

    }

    public Template(Content c) {
        this.name = c.getNodeData("name").getString(); //$NON-NLS-1$
        this.path = c.getNodeData("path").getString(); //$NON-NLS-1$

        this.type = c.getNodeData("type").getString(); //$NON-NLS-1$
        this.visible = c.getNodeData("visible").getBoolean(); //$NON-NLS-1$
        this.title = c.getNodeData("title").getString(); //$NON-NLS-1$
        this.description = c.getNodeData("description").getString(); //$NON-NLS-1$
        this.image = c.getNodeData("image").getString(); //$NON-NLS-1$
        this.location = c.getHandle();

        addAlternativePaths(c);
    }

    /**
     * Getter for <code>description</code>.
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Getter for <code>name</code>.
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for <code>path</code>.
     * @return Returns the path.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Getter for <code>title</code>.
     * @return Returns the title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Getter for <code>image</code>.
     * @return Returns the image.
     */
    public String getImage() {
        return this.image;
    }

    /**
     * Getter for <code>type</code>.
     * @return Returns the type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Getter for <code>location</code>.
     * @return Returns the location.
     */
    public String getLocation() {
        return this.location;
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

        return path;
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

        Content cl;
        try {
            cl = node.getContent("SubTemplates");//$NON-NLS-1$
        }
        catch (RepositoryException e) {
            // no subtemplates available, that's ok
            return;
        }

        Iterator it = cl.getChildren().iterator();

        this.alternativeTemplates = new HashMap();

        while (it.hasNext()) {
            Content c = (Content) it.next();

            Template template = new Template();
            template.path = c.getNodeData("path").getString(); //$NON-NLS-1$

            template.type = c.getNodeData("type").getString(); //$NON-NLS-1$
            if (template.type == null) {
                template.type = type;
            }

            template.name = name;
            template.visible = visible;
            template.location = location;

            synchronized (alternativeTemplates) {
                this.alternativeTemplates.put(c.getNodeData("extension").getString(), template); //$NON-NLS-1$ 
            }
        }

    }

}
