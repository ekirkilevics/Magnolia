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

import freemarker.core.Environment;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.I18nContentWrapper;
import info.magnolia.cms.util.InheritanceContentWrapper;
import info.magnolia.cms.util.SiblingsHelper;
import info.magnolia.context.MgnlContext;
import info.magnolia.link.LinkFactory;
import info.magnolia.link.LinkUtil;
import info.magnolia.link.LinkException;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;

/**
 * This is an object exposing a couple of methods useful for templates; it's exposed in
 * templates as "mgnl".
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MagnoliaTemplatingUtilities {

    private static final Logger log = LoggerFactory.getLogger(MagnoliaTemplatingUtilities.class);

    /**
     * Returns an instance of SiblingsHelper for the given node.
     */
    public SiblingsHelper siblings(Content node) throws RepositoryException {
        return SiblingsHelper.of(node);
    }

    /**
     * FreeMarker only.
     */
    public void renderParagraph(Content paragraphNode) throws RenderException, IOException {
        final Environment env = Environment.getCurrentEnvironment();
        final Writer out = env.getOut();
        // TODO - set and unset AggState.currentContent
        ParagraphRenderingFacade.getInstance().render(paragraphNode, out);
    }

    public Content inherit(Content node) {
        return new InheritanceContentWrapper(node);
    }

    public Content i18n(Content node) {
        return new I18nContentWrapper(node);
    }

    public boolean isEditMode(){
        // TODO : see CmsFunctions.isEditMode, which checks a couple of other properties.
        return isAuthorInstance() && !isPreviewMode();
    }

    public boolean isPreviewMode(){
        return MgnlContext.getAggregationState().isPreviewMode();
    }

    public boolean isAuthorInstance(){
        return ServerConfiguration.getInstance().isAdmin();
    }

    public boolean isPublicInstance(){
        return !isAuthorInstance();
    }

    public String createLink(NodeData nd) {
        try {
            return LinkUtil.createAbsoluteLink(nd);
        } catch (LinkException e) {
            log.error("Can't resolve link defined in node {} because of {}.", nd.getHandle(), ExceptionUtils.getRootCauseMessage(e));
            return null;
        }
    }

    public String createLink(String repositoryId, String uuid) {
        try {
            return LinkUtil.createAbsoluteLink(repositoryId, uuid);
        } catch (RepositoryException e) {
            log.error("Can't resolve link with UUID {} because of {}.", uuid , ExceptionUtils.getRootCauseMessage(e));
            return null;
        }
    }

    public String createLink(Content node) {
        return LinkUtil.createAbsoluteLink(node);
    }

}
