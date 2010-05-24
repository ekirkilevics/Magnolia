/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.I18nContentWrapper;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.InheritanceContentWrapper;
import info.magnolia.cms.util.SiblingsHelper;
import info.magnolia.context.MgnlContext;
import info.magnolia.link.LinkUtil;
import info.magnolia.link.LinkException;
import info.magnolia.module.templating.engine.RenderingEngine;
import info.magnolia.objectfactory.Components;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
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

    protected RenderingEngine renderingEngine = Components.getSingleton(RenderingEngine.class);

    public static MagnoliaTemplatingUtilities getInstance(){
        return Components.getSingleton(MagnoliaTemplatingUtilities.class);
    }

    /**
     * Returns an instance of SiblingsHelper for the given node.
     */
    public SiblingsHelper siblings(Content node) throws RepositoryException {
        return SiblingsHelper.of(node);
    }

    public void renderTemplate(Content content) throws RenderException, IOException {
        renderTemplate(content, getWriter());
    }

    public void renderTemplate(Content content, Writer out) throws RenderException, IOException {
        renderingEngine.render(content, out);
    }

    public void renderTemplate(Content content, Writer out, String templateName) throws RenderException, IOException {
        renderingEngine.render(content, templateName, out);
    }

    public void renderParagraph(Content paragraphNode) throws RenderException, IOException {
        renderParagraph(paragraphNode, getWriter());
    }

    public void renderParagraph(Content paragraphNode, Writer out) throws RenderException, IOException {
        renderingEngine.render(paragraphNode, out);
    }

    public void renderParagraph(Content paragraphNode, Writer out, String paragraphName) throws RenderException, IOException {
        renderingEngine.render(paragraphNode, paragraphName, out);
    }

    /**
     * TODO each renderer should provide its own subclass
     */
    protected Writer getWriter() {
        final Environment env = Environment.getCurrentEnvironment();
        return env.getOut();
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

    public String createLink(Content node) {
        return LinkUtil.createLink(node);
    }

    public String createLink(NodeData nd) {
        try {
            return LinkUtil.createLink(nd);
        } catch (LinkException e) {
            log.error("Can't resolve link defined in node {} because of {}.", nd.getHandle(), ExceptionUtils.getRootCauseMessage(e));
            return null;
        }
    }

    public String createLink(String repositoryId, String uuid) {
        try {
            return LinkUtil.createLink(repositoryId, uuid);
        } catch (RepositoryException e) {
            log.error("Can't resolve link with UUID {} because of {}.", uuid , ExceptionUtils.getRootCauseMessage(e));
            return null;
        }
    }

    /**
     * Util method to create html attributes <code>name="value"</code>. If the value is empty an empty string will be returned.
     * This is mainlly helpful to avoid empty attributes.
     */
    public String createAttribute(String name, String value){
        value = StringUtils.trim(value);
        if(StringUtils.isNotEmpty(value)){
            return new StringBuffer().append(name).append("=\"").append(value).append("\"").toString();
        }
        return StringUtils.EMPTY;
    }

    public Content getContent(String path){
      return getContent(ContentRepository.WEBSITE, path);
    }

    public Content getContent(String repository, String path){
        return ContentUtil.getContent(repository, path);
    }

    public Content getContentByUUID(String uuid){
        return getContentByUUID(ContentRepository.WEBSITE, uuid);
    }

    public Content getContentByUUID(String repository, String uuid){
        return ContentUtil.getContentByUUID(repository, uuid);
    }

}
