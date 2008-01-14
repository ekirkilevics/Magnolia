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
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.FactoryUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Manages the templates of the system
 * @author philipp
 */
public class TemplateManager extends ObservedManager {

    /**
     * The cached templates
     */
    private Map cachedContent = new Hashtable();

    /**
     * The templates visible in the templates selection
     */
    private List visibleTemplates = new ArrayList();

    /**
     * Called by the ObervedManager
     */
    protected void onRegister(Content node) {
        try {
            log.info("Config : loading Template info - " + node.getHandle()); //$NON-NLS-1$

            // It makes possibly to use templates defined within subfolders of /module/templating/Templates
            Collection children = collectChildren(node);

            if ((children != null) && !(children.isEmpty())) {
                Iterator templates = children.iterator();
                cacheContent(templates);
            }

            log.info("Config : Template info loaded - " + node.getHandle()); //$NON-NLS-1$
        }
        catch (Exception re) {
            log.error("Config : Failed to load Template info - " + node.getHandle()); //$NON-NLS-1$
            log.error(re.getMessage(), re);
        }

    }

    protected void onClear() {
        this.cachedContent.clear();
        this.visibleTemplates.clear();
    }

    /**
     * Returns the cached content of the requested template. TemplateInfo properties:
     * <ol>
     * <li> title - title describing template</li>
     * <li> type - jsp / servlet</li>
     * <li> path - jsp / servlet path</li>
     * <li> description - description of a template</li>
     * </ol>
     * @return TemplateInfo
     */
    public Template getInfo(String key) {
        return (Template) cachedContent.get(key);
    }

    /**
     * Returns the cached content of the requested template. TemplateInfo properties:
     * <ol>
     * <li> title - title describing template</li>
     * <li> type - jsp / servlet</li>
     * <li> path - jsp / servlet path</li>
     * <li> description - description of a template</li>
     * </ol>
     * @return TemplateInfo
     */
    public Template getInfo(String key, String extension) {
        Template template = (Template) cachedContent.get(key);

        if (template == null) {
            return null;
        }
        Template subtemplate = template.getSubTemplate(extension);
        if (subtemplate != null) {
            return subtemplate;
        }

        return template;
    }

    /**
     * Adds templates definition to TemplatesInfo cache.
     * @param templates iterator as read from the repository
     * @param visibleTemplates List in with all visible templates will be added
     */
    private void addTemplatesToCache(Iterator templates, List visibleTemplates) {
        while (templates.hasNext()) {
            Content c = (Content) templates.next();

            Template ti = new Template(c);
            cachedContent.put(ti.getName(), ti);
            if (ti.isVisible()) {
                visibleTemplates.add(ti);
            }

            if (log.isDebugEnabled()) {
                log.debug(MessageFormat.format("Registering template [{0}]", new Object[]{ti.getName()})); //$NON-NLS-1$
            }

        }
    }

    /**
     * Load content of this template info page in a hash table caching at the system load, this will save lot of time on
     * every request while matching template info.
     */
    private void cacheContent(Iterator templates) {
        if (templates != null) {
            addTemplatesToCache(templates, visibleTemplates);
        }
    }

    /**
     * Recursive search for content nodes contains template data (looks up subfolders)
     * @author <a href="mailto:tm@touk.pl">Tomasz Mazan</a>
     * @param cnt current folder to look for template's nodes
     * @return collection of template's content nodes from current folder and descendants
     */
    private Collection collectChildren(Content cnt) {
        // Collect template's content node - children of current node
        Collection children = cnt.getChildren(ItemType.CONTENTNODE);

        // Look into subfolders
        Collection subFolders = cnt.getChildren(ItemType.CONTENT);
        if ((subFolders != null) && !(subFolders.isEmpty())) {

            Iterator it = subFolders.iterator();
            while (it.hasNext()) {
                Content subCnt = (Content) it.next();
                Collection grandChildren = collectChildren(subCnt);

                if ((grandChildren != null) && !(grandChildren.isEmpty())) {
                    children.addAll(grandChildren);
                }
            }

        }

        return children;
    }

    /**
     * Get templates collection after access control filter applied using specified AccessManager
     * @return Collection list containing templates as Template objects
     */
    public Iterator getAvailableTemplates(AccessManager accessManager) {
        List templateList = new ArrayList();
        Iterator it = visibleTemplates.iterator();
        while (it.hasNext()) {
            Template template = (Template) it.next();
            if (accessManager.isGranted(template.getLocation(), Permission.READ)) {
                templateList.add(template);
            }
        }
        return templateList.iterator();
    }

    /**
     * Get templates collection.
     * @return Collection list containing templates as Template objects
     */
    public Iterator getAvailableTemplates() {
        return visibleTemplates.iterator();
    }

    /**
     * @return Returns the instance.
     */
    public static TemplateManager getInstance() {
        return (TemplateManager) FactoryUtil.getSingleton(TemplateManager.class);
    }

}
