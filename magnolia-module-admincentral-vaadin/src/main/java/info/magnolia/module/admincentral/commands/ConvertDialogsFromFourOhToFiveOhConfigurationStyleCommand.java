/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;


/**
 * Converts all existing dialogs already registered in the system into new 50 dialogs and marks them as converted. Can be executed safely multiple times.
 * This command makes permanent changes to the repository and will not rollback the changes done up to date when failing during execution.
 * Due to dependency on ControlsManager, can't be executed at startup of the modules, but only after observers are in place.
 * @author had
 * @version $Id:$
 *
 */
public class ConvertDialogsFromFourOhToFiveOhConfigurationStyleCommand extends MgnlCommand {

    private static final Logger log = LoggerFactory.getLogger(ConvertDialogsFromFourOhToFiveOhConfigurationStyleCommand.class);

    @Override
    public boolean execute(Context context) throws Exception {
        try {
            convertDialogs(context);
            return true;
        } catch (RepositoryException e) {
            log.error("Failed to convert dialogs for vaadin.", e);
        }
        return false;
    }

    private void convertDialogs(Context context) throws RepositoryException {
        HierarchyManager hm = context.getHierarchyManager(ContentRepository.CONFIG);
        QueryManager qm = hm.getQueryManager();
        // this query will ignore DMS dialogs (and any other that are still of the mgnl:content node type)
        Collection<Content> dialogs = qm.createQuery("select * from mgnl:contentNode where jcr:path like '/modules/%/dialogs/%' and controlType is null and importedTo50 is null", Query.SQL).execute().getContent(ItemType.CONTENTNODE.getSystemName());
        Map<String, String> controlsMap = getControls(qm);
        for (Content dialog : dialogs) {
            String dialogHandle = dialog.getHandle();
            String modulePath =  StringUtils.substringBefore(dialogHandle, "/dialogs/");
            String newDialogHandle = modulePath + "/mgnl50dialogs/" + StringUtils.substringAfter(dialogHandle, "/dialogs/");
            if (hm.isExist(newDialogHandle)) {
                throw new RuntimeException("Failed to convert " + dialogHandle + " to " + newDialogHandle + " due to path conflict.");
            } else {
                // _copy_ dialog
                String parentPath = StringUtils.substringBeforeLast(newDialogHandle, "/");
                // have to save immediately or the copyTo will not see the path (session vs. workspace ops as usually)
                ContentUtil.createPath(hm, parentPath);
                hm.save();
                hm.copyTo(dialogHandle, newDialogHandle);
                Content newDialog = hm.getContent(newDialogHandle);
                // move tabs
                Collection<Content> tabs = getTabs(newDialog);
                if (!tabs.isEmpty()) {
                    // no tabs no moving anything ...
                    moveTabs(hm, controlsMap, newDialog, tabs);
                }
            }
            // make sure we don't try to process this dialog again
            dialog.setNodeData("importedTo50", true);
            dialog.save();
            // and save to keep session small and to persist in case of errors later.
            hm.save();
        }
    }

    private Map<String, String> getControls(QueryManager qm) throws InvalidQueryException, RepositoryException {
        Map<String, String> controlMap = new HashMap<String, String>();
        Collection<Content> controlNodes = qm.createQuery("select * from mgnl:contentNode where jcr:path like '/modules/%/controls/%'", Query.SQL).execute().getContent(ItemType.CONTENTNODE.getSystemName());
        for (Content control : controlNodes) {
            String controlClass = control.getNodeData("class").getString();
            if (controlClass == null) {
                log.warn("Failed to find class for control {} registered at {}.", control.getName(), control.getHandle());
                continue;
            }
            // map old controls to new ones
            String controlTypeName = StringUtils.substringAfterLast(controlClass, ".Dialog");
            String newControlClassName = "info.magnolia.module.admincentral.control." + controlTypeName + "Control";
            try {
                Class<?> clazz = Class.forName(newControlClassName);
                controlClass = clazz.getName();
            } catch (ClassNotFoundException e) {
                log.warn("Failed to find equivalent of field control class {}. Expected name {}", controlClass, newControlClassName);
            } catch (NoClassDefFoundError e) {
                // silly jvm decides to throw and error instead of exception when class name doesn't match only on letter cases ... e.g. UUIDLink vs. UuidLink
                log.warn("Failed to find equivalent of field control class {}. Expected name {}", controlClass, newControlClassName);
            }
            // TODO: lookup list for new controls might be necessary ... or we sweep/convert all controls upfront and blindly refere to them here
            controlMap.put(control.getName(), controlClass);
        }
        return controlMap;
    }

    private void moveTabs(HierarchyManager hm, Map<String, String> controlsMap, Content newDialog, Collection<Content> tabs) throws PathNotFoundException,
            RepositoryException, AccessDeniedException {
        Content tabsCollection = newDialog.createContent("tabs", ItemType.CONTENTNODE);
        // and again the famous clash of session vs. workspace ops (move down the line would fail w/o save here)
        newDialog.save();
        for (Content tab : tabs) {
            // move controls
            Collection<Content> controls = getControls(tab);
            if (!controls.isEmpty()) {
                moveControls(hm, controlsMap, tab, controls);
            }
            // and the last time
            hm.save();
            // move the tab as the last thing we do, doing so earlier ends with IISE (modified externally)
            hm.moveTo(tab.getHandle(), tabsCollection.getHandle() + "/" + tab.getName());
        }
    }

    private void moveControls(HierarchyManager hm, Map<String, String> controlsMap, Content tab, Collection<Content> controls) throws PathNotFoundException,
            RepositoryException, AccessDeniedException {
        Content fieldsCollection = tab.createContent("fields", ItemType.CONTENTNODE);
        // 3rd time stage appearance for the famous clash of session vs. workspace ops (move down the line would fail w/o save here)
        hm.save();
        for (Content control : controls) {
            String controlName = control.getNodeData("controlType").getString();
            control.setNodeData("class", controlsMap.get(controlName));
            control.save();
            // and again
            hm.save();
            hm.moveTo(control.getHandle(), fieldsCollection.getHandle() + "/" + control.getName());
        }
    }

    private Collection<Content> getTabs(Content newDialog) {
        Collection<Content> tabs = newDialog.getChildren(new Content.ContentFilter() {
            public boolean accept(Content content) {
                try {
                    return content.hasNodeData("controlType") && content.getNodeData("controlType").getString().equals("tab");
                } catch (RepositoryException e) {
                    // not a tab
                    return false;
                }
            }
        });
        return tabs;
    }

    private Collection<Content> getControls(Content tab) {
        Collection<Content> controls = tab.getChildren(new Content.ContentFilter() {
            public boolean accept(Content content) {
                try {
                    return content.hasNodeData("controlType");
                } catch (RepositoryException e) {
                    // not a control
                    return false;
                }
            }
        });
        return controls;
    }
}
