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
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.TemplateMessagesUtil;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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

    private Content content;

    private String name;

    private String path;

    private String type;

    private String title;

    //private String location;

    private String description;

    private String i18nBasename;

    private boolean visible;

    private String image;

    private Map parameters = new HashMap();

    private Map subTemplates = new HashMap();

    /**
     * Used internally for SubTemplates.
     */
    public Template() {

    }

    /**
     * Getter for <code>description</code>.
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for <code>name</code>.
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for <code>path</code>.
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Getter for <code>title</code>.
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    public String getI18NTitle() {
        Messages msgs;

        if (StringUtils.isNotEmpty(i18nBasename)) {
            msgs = MessagesManager.getMessages(i18nBasename);
        }
        else {
            msgs = TemplateMessagesUtil.getMessages();
        }

        return msgs.getWithDefault(getTitle(), getTitle());
    }

    /**
     * Getter for <code>image</code>.
     * @return Returns the image.
     */
    public String getImage() {
        return image;
    }

    /**
     * Getter for <code>type</code>.
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    public String getParameter(String key) {
        return (String) getParameters().get(key);
    }

    /**
     * Getter for <code>visible</code>.
     * @return Returns the visible.
     */
    public boolean isVisible() {
        return this.visible;
    }

    public Template getSubTemplate(String extension) {
        return (Template) this.subTemplates.get(extension);
    }

    public void addSubTemplate(String extension, Template subTemplate) {
        this.subTemplates.put(extension, subTemplate);
    }

    public String getI18nBasename() {
        return this.i18nBasename;
    }

    public void setI18nBasename(String basename) {
        this.i18nBasename = basename;
    }

    public Map getParameters() {
        return this.parameters;
    }

    public void setParameters(Map params) {
        this.parameters = params;
    }

    public Map getSubTemplates() {
        return this.subTemplates;
    }

    public void setSubTemplates(Map subTemplates) {
        this.subTemplates = subTemplates;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isAvailable(Content node) {
        // TODO is called quite often and should be faster
        AccessManager am = MgnlContext.getAccessManager(getContent().getHierarchyManager().getName());
        return am.isGranted(getContent().getHandle(), Permission.READ);
    }

    public Content getContent() {
        return this.content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

}
