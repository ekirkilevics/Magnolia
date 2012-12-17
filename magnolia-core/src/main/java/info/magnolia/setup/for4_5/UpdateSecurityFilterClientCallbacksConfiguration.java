/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.setup.for4_5;

import static java.lang.String.format;
import static org.apache.commons.lang.ArrayUtils.contains;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.filters.FilterManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.Collection;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Updates the given security filter's client callback configuration to reflect the changes introduced in 4.5.
 */
public class UpdateSecurityFilterClientCallbacksConfiguration extends AbstractRepositoryTask {

    private static final Logger log = LoggerFactory.getLogger(UpdateSecurityFilterClientCallbacksConfiguration.class);

    private static final String FORM_CLASS = "info.magnolia.cms.security.auth.callback.FormClientCallback";
    private static final String COMPOSITE_CLASS = "info.magnolia.cms.security.auth.callback.CompositeCallback";
    private static final String BASIC_CLASS = "info.magnolia.cms.security.auth.callback.BasicClientCallback";
    private static final String REDIRECT_CLASS = "info.magnolia.cms.security.auth.callback.RedirectClientCallback";
    private static final String[] SIMPLE_CALLBACK_CLASSES = new String[]{FORM_CLASS, BASIC_CLASS, REDIRECT_CLASS};
    private final String fromFilterName;
    private final String targetFilterName;
    private boolean wereWeAbleToUpdateEverything = true;

    public UpdateSecurityFilterClientCallbacksConfiguration(String fromFilterName, String targetFilterName) {
        super("Security filter configuration", "Moves the client callback configuration from the " + fromFilterName + " to the new " + targetFilterName + " filter to enable multiple client callbacks.");
        this.fromFilterName = fromFilterName;
        this.targetFilterName = targetFilterName;
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getConfigHierarchyManager();
        final Content fromFilterNode = hm.getContent(FilterManager.SERVER_FILTERS + "/" + fromFilterName);
        final Content targetFilterNode = hm.getContent(FilterManager.SERVER_FILTERS + "/" + targetFilterName);

        final Content newCallbacksNode = targetFilterNode.createContent("clientCallbacks", NodeTypes.ContentNode.NAME);
        final Content currentCallbackNode = fromFilterNode.getContent("clientCallback");
        final String currentClass = currentCallbackNode.getNodeData("class").getString();
        if (contains(SIMPLE_CALLBACK_CLASSES, currentClass)) {
            addCallback(ctx, newCallbacksNode, null, currentCallbackNode, null);
        } else if (!currentCallbackNode.hasChildren()) {
            // we can assume it's a simple custom callback which we can simply move
            addCallback(ctx, newCallbacksNode, "custom", currentCallbackNode, null);
        } else if (COMPOSITE_CLASS.equals(currentClass)) {
            final Collection<Content> existingCallbacks = currentCallbackNode.getContent("patterns").getChildren();
            for (Content existingCallback : existingCallbacks) {
                final String clazz = existingCallback.getNodeData("class").getString();
                if ("info.magnolia.cms.util.UrlPatternDelegate".equals(clazz)) {
                    final String url = existingCallback.getNodeData("url").getString();
                    addCallback(ctx, newCallbacksNode, existingCallback.getName(), existingCallback.getContent("delegate"), url);
                } else {
                    ctx.warn("Unknown callback class at " + existingCallback.getHandle() + ":" + clazz);
                    wereWeAbleToUpdateEverything = false;
                }
            }
        } else {
            ctx.warn("Unknown callback class:" + currentClass);
            wereWeAbleToUpdateEverything = false;
        }

        // only rename if unsuccessful ?
        if (wereWeAbleToUpdateEverything) {
            currentCallbackNode.delete();
        } else {
            ContentUtil.moveInSession(currentCallbackNode, fromFilterNode.getHandle() + "/_clientCallback_backup_config");
            ctx.warn(format(
                            "Client callback configuration for %s was not standard: an untouched copy of %s has been kept at %s. Please check, validate and correct the new configuration at %s.",
                            fromFilterName, fromFilterNode.getHandle() + "/clientCallback", fromFilterNode.getHandle() + "/_clientCallback_backup_config", newCallbacksNode.getHandle()
                    ));

        }
    }

    private void addCallback(InstallContext ctx, Content target, String givenCallbackName, Content source, String urlPattern) throws RepositoryException {
        final String clazz = source.getNodeData("class").getString();
        final String newName;
        if (givenCallbackName == null && contains(SIMPLE_CALLBACK_CLASSES, clazz)) {
            newName = simplifyClassName(clazz);
        } else if (givenCallbackName != null) {
            newName = givenCallbackName;
        } else {
            log.warn("Can not determine name for callback at " + source.getHandle());
            wereWeAbleToUpdateEverything = false;
            return;
        }

        final Content newCallback = target.createContent(newName, NodeTypes.ContentNode.NAME);
        copyStringProperty(source, newCallback, "class");
        if (FORM_CLASS.equals(clazz)) {
            copyStringProperty(source, newCallback, "loginForm");
            logAndIgnore(ctx, source, "realmName");
            checkRemainingProperties(ctx, source, "class", "loginForm", "realmName");
        } else if (REDIRECT_CLASS.equals(clazz)) {
            copyStringProperty(source, newCallback, "location");
            logAndIgnore(ctx, source, "realmName");
            checkRemainingProperties(ctx, source, "class", "location", "realmName");
        } else if (BASIC_CLASS.equals(clazz)) {
            copyStringProperty(source, newCallback, "realmName");
            checkRemainingProperties(ctx, source, "class", "realmName");
        } else {
            log.warn("Unknown callback class: " + clazz + "; copying all properties.");
            wereWeAbleToUpdateEverything = false;
            copyRemainingProperties(ctx, source, newCallback, "class");
        }

        if (urlPattern != null) {
            Content urlPatternContent = newCallback.createContent("urlPattern", NodeTypes.ContentNode.NAME);
            urlPatternContent.setNodeData("class", "info.magnolia.cms.util.SimpleUrlPattern");
            urlPatternContent.setNodeData("patternString", urlPattern);
        }

    }

    private String simplifyClassName(String clazz) {
        return StringUtils.removeEnd(StringUtils.substringAfterLast(clazz, "."), "ClientCallback").toLowerCase();
    }

    private void copyStringProperty(Content source, Content target, String propertyName) throws RepositoryException {
        target.setNodeData(propertyName, source.getNodeData(propertyName).getString());
    }

    /**
     * Checks if the given node has a given property; logs it and continues if so.
     */
    private void logAndIgnore(InstallContext ctx, Content source, String propertyName) throws RepositoryException {
        if (source.hasNodeData(propertyName)) {
            ctx.warn(source.getHandle() + " has a '" + propertyName + "' property; it is ignored and has been removed.");
        }
    }

    /**
     * Checks if the given node has other properties than those specified by the ignoredProperties parameter.
     */
    private void checkRemainingProperties(InstallContext ctx, Content source, String... ignoredProperties) {
        final Set<String> ignoredPropsSet = Sets.newHashSet(ignoredProperties);
        final Collection<NodeData> allProps = source.getNodeDataCollection();
        final Iterable<String> allPropsNames = Iterables.transform(allProps, new Function<NodeData, String>() {
            @Override
            public String apply(NodeData from) {
                return from.getName();
            }
        });
        final Iterable<String> remaining = Iterables.filter(allPropsNames, Predicates.not(Predicates.in(ignoredPropsSet)));
        if (!Iterables.isEmpty(remaining)) {
            log.warn(source.getHandle() + " has the following unknown properties which were not copied: " + remaining);
            wereWeAbleToUpdateEverything = false;
        }
    }

    private void copyRemainingProperties(InstallContext ctx, Content source, Content target, String... ignoredProperties) throws RepositoryException {
        final Collection<NodeData> existingProps = source.getNodeDataCollection();
        for (NodeData prop : existingProps) {
            if (ArrayUtils.contains(ignoredProperties, prop.getName())) {
                continue;
            }
            copyStringProperty(source, target, prop.getName());
        }
    }

}
