/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.DeprecationUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * Represents a paragraph definition. Following are most of the properties you can use
 * to configure your paragraphs. Of course, if you're using specific subclasses,
 * other properties could be available.
 * <br/>
 * <br/>
 * <table border="1">
 * <tbody>
 * <tr>
 * <th>Property</th>
 * <th>Default</th>
 * <th>Values</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>class</td>
 * <td> {@link Paragraph}</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>type</td>
 * <td><code>jsp</code>,<code>freemarker</code>, ...</td>
 * <td>Determines which <code>ParagraphRenderer</code> to use. Out of the box,
 * Magnolia provides support for JSP and FreeMarker.</td>
 * </tr>
 * <tr>
 * <td>templatePath</td>
 * <td>&nbsp;</td>
 * <td>This property follows conventional syntax for path definitions.</td>
 * <td>This property defines the path to the template to be used for this
 * paragraph.</td>
 * </tr>
 * <tr>
 * <td>modelClass</td>
 * <td>&nbsp;</td>
 * <td>The fully qualified name of a class implementing
 * {@link RenderingModel}</td>
 * <td>The bean created by the renderer based on the modelClass defined on the
 * paragraph or template definition. The current content, definition and the
 * parent model are passed to the constructor. This object is instantiated for
 * each rendering of a template or a paragraph.</td>
 * </tr>
 * <tr>
 * <td>i18nBasename</td>
 * <td>info.magnolia.module. admininterface.messages or whatever the
 * i18nBasename is set to in the dialog for this paragraph.</td>
 * <td>This can be any properly defined Magnolia message bundle.</td>
 * <td>This property defines the message bundle to use for this paragraph.</td>
 * </tr>
 * <tr>
 * <td>title</td>
 * <td>&nbsp;</td>
 * <td>The title or a message bundle key to be used with the bundle defined by
 * <code>i18nBasename</code>.</td>
 * <td>This property defines the title of the paragraph.</td>
 * </tr>
 * <tr>
 * <td>description</td>
 * <td>&nbsp;</td>
 * <td>The description or a message bundle key to be used with the bundle
 * defined by <code>i18nBasename</code>.</td>
 * <td>This property is used to describe the paragraph.</td>
 * </tr>
 * <tr>
 * <td>dialog</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * <td>This property is used to specify the name of the dialog associated with
 * this paragraph.</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * @author Sameer Charles
 */
public class Paragraph extends AbstractRenderable {

    private final static Logger log = LoggerFactory.getLogger(Paragraph.class);

    /**
     * @deprecated since 4.0 use the type property
     */
    public String getTemplateType(){
        DeprecationUtil.isDeprecated("The property templateType in paragraph definitions has changed to type" );
        return getType();
    }

    /**
     * @deprecated since 4.0 use the type property
     */
    public void setTemplateType(String type){
        DeprecationUtil.isDeprecated("The property templateType in paragraph definitions has changed to type" );
        setType(type);
    }

    /**
     * @deprecated since 4.0 use the dialog property
     */
    public String getDialogPath(String path){
        String msg = "The property dialogPath in paragraph definitions has been deprecated, use the dialog property instead";
        DeprecationUtil.isDeprecated(msg);
        throw new UnsupportedOperationException(msg);
    }

    /**
     * @deprecated since 4.0 use the dialog property
     */
    public void setDialogPath(String path){
        DeprecationUtil.isDeprecated("The property dialogPath in paragraph definitions has been deprecated, use the dialog property instead");
        Content node;
        try {
            node = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG).getContent(path);
            String name = NodeDataUtil.getString(node, "name", node.getName());
            setDialog(name);
        }
        catch (RepositoryException e) {
            log.error("Can't determine dialog name using the path [" + path + "]", e);
        }
    }

}
