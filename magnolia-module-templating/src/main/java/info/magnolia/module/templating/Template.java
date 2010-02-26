/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.templating;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.DeprecationUtil;
import info.magnolia.context.MgnlContext;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class Template extends AbstractRenderable {
    private Content content;

    private boolean visible = true;

    private Map<String, Template> subTemplates = new HashMap<String, Template>();

    /**
     * Used internally for SubTemplates.
     */
    public Template() {

    }

    /**
     * Getter for <code>path</code>.
     * @return Returns the path.
     * @deprecated since 4.0. use getTemplatePath() instead
     */
    public String getPath() {
        DeprecationUtil.isDeprecated("The 'path' property is deprecated: use the templatePath property instead. (current value: " + getTemplatePath() + ")");
        return getTemplatePath();
    }


    public String getI18NTitle() {
        Messages msgs = MessagesManager.getMessages(getI18nBasename());

        return msgs.getWithDefault(getTitle(), getTitle());
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
        return this.subTemplates.get(extension);
    }

    public void addSubTemplate(String extension, Template subTemplate) {
        this.subTemplates.put(extension, subTemplate);
    }

    public Map<String, Template> getSubTemplates() {
        return this.subTemplates;
    }

    public void setSubTemplates(Map<String, Template> subTemplates) {
        this.subTemplates = subTemplates;
    }

    /**
     * @deprecated since 4.0 use {@link #setTemplatePath(String)}
     */
    public void setPath(String path) {
        // log message can only output the templatePath, as there is not guarantee the name or content name have been set already
        DeprecationUtil.isDeprecated("The 'path' property is deprecated: use the templatePath property instead. (setting to value: " + path + ")");
        setTemplatePath(path);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isAvailable(Content node) {
        // TODO is called quite often and should be faster
        AccessManager am = MgnlContext.getAccessManager(getContent().getHierarchyManager().getName());
        return am.isGranted(getContent().getHandle(), Permission.READ);
    }

    public Content getContent() {
        return this.content;
    }

    // this is set by content2bean
    public void setContent(Content content) {
        this.content = content;
    }

}