/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.TemplateMessagesUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;


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

        if (getLocation() == null) {
            parameters.put("location", c.getHandle());
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

    public String getI18NTitle() {
        Messages msgs;
        final String i18nBasename = getParameter("i18nBasename");

        if(StringUtils.isNotEmpty(i18nBasename)){
            msgs = MessagesManager.getMessages(i18nBasename);
        }
        else{
            msgs = TemplateMessagesUtil.getMessages();
        }

        return msgs.getWithDefault(getTitle(), getTitle());
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
