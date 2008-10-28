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
package info.magnolia.module.templating;

import freemarker.core.Environment;
import info.magnolia.cms.beans.config.ParagraphRenderingFacade;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.I18nContentWrapper;
import info.magnolia.cms.util.InheritanceContentWrapper;
import info.magnolia.cms.util.Resource;
import info.magnolia.cms.util.SiblingsHelper;

import java.io.IOException;
import java.io.Writer;

import javax.jcr.RepositoryException;

/**
 * This is an object exposing a couple of methods useful for templates; it's exposed in
 * templates as "mgnl".
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MagnoliaTemplatingUtilities {

    /**
     * Returns an instance of SiblingsHelper for the given node.
     */
    public SiblingsHelper siblings(Content node) throws RepositoryException {
        return SiblingsHelper.of(node);
    }

    public void renderParagraph(Content paragraphNode) throws IOException {
        final Environment env = Environment.getCurrentEnvironment();
        final Writer out = env.getOut();
        ParagraphRenderingFacade.getInstance().render(paragraphNode, out);
    }

    public Content inherit(Content node) {
        return new InheritanceContentWrapper(node);
    }

    public Content i18n(Content node) {
        return new I18nContentWrapper(node);
    }

    public boolean isEditMode(){
        return isAuthorInstance() && !isPreviewMode();
    }

    public boolean isPreviewMode(){
        return Resource.showPreview();
    }

    public boolean isAuthorInstance(){
        return ServerConfiguration.getInstance().isAdmin();
    }

    public boolean isPublicInstance(){
        return !isAuthorInstance();
    }

}
