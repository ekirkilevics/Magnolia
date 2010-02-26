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
package info.magnolia.templatinguicomponents.components;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.Permission;
import info.magnolia.templatinguicomponents.AuthoringUiComponent;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.List;

/**
 * Common subclass for ui components, provides utility methods and defaults.
 * Implementations should expose setter methods for their specific parameters (so that template-specific wrappers
 * can set parameters). (no need to clutter things up with getters). Implementation might also expose static factory
 * methods, which can take care of default values, i.e for labels.
 *
 * If no target node is set explicitly, it is deduced using {@link #defaultTarget()}.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractAuthoringUiComponent implements AuthoringUiComponent {
    private final ServerConfiguration server;
    private final AggregationState aggregationState;

    private Content target;

    protected AbstractAuthoringUiComponent(final ServerConfiguration server, final AggregationState aggregationState) {
        this.server = server;
        this.aggregationState = aggregationState;
        this.target = defaultTarget();
    }

    protected ServerConfiguration getServer() {
        return server;
    }

    protected AggregationState getAggregationState() {
        return aggregationState;
    }

    protected Content getTarget() {
        return target;
    }

    public void setTarget(Content target) {
        this.target = target;
    }

    public void render(Appendable out) throws IOException {
        if (!shouldRender()) {
            return;
        }
        try {
            doRender(out);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void doRender(Appendable out) throws IOException, RepositoryException;

    /**
     * Returns the "current content" from the aggregation state.
     * Override this method if your component needs a different target node.
     */
    protected Content defaultTarget() {
        final Content currentContent = aggregationState.getCurrentContent();
        if (currentContent == null) {
            throw new IllegalStateException("Could not determine defaultTarget from AggregationState, currentContent is null");
        }
        return currentContent;
    }

    /**
     * Override this method if the component needs to be rendered under different conditions.
     */
    protected boolean shouldRender() {
        return (server.isAdmin() && aggregationState.getMainContent().isGranted(Permission.SET));
    }

    /**
     * Utility method - our current gui components (magnolia-gui) expect comma separated strings.
     */
    protected String asString(List<String> strings) {
        return StringUtils.join(strings, ',');
    }

}
