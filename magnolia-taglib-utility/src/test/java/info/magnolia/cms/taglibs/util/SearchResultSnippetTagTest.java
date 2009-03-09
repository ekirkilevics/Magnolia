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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.test.mock.MockContent;
import junit.framework.TestCase;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang.exception.ExceptionUtils;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class SearchResultSnippetTagTest extends TestCase {

    public void testStripHtmlSimple() {
        String html = "<div>uh!</div>";
        String text = "uh!";
        String result = new SearchResultSnippetTag().stripHtmlTags(html);
        assertEquals(text, result);
    }

    public void testStripHtmlEmptyTag() {
        String html = "<div>uh!<br/></div>";
        String text = "uh!";
        String result = new SearchResultSnippetTag().stripHtmlTags(html);
        assertEquals(text, result);
    }

    public void testStripHtmlNewLines() {
        String html = "<div\n class=\"abc\">uh!</div>";
        String text = "uh!";
        String result = new SearchResultSnippetTag().stripHtmlTags(html);
        assertEquals(text, result);
    }

    public void testStripHtmlMultipleNewLines() {
        String html = "<div\n class=\"abc\"\n style=\"abc\">uh!</div>";
        String text = "uh!";
        String result = new SearchResultSnippetTag().stripHtmlTags(html);
        assertEquals(text, result);
    }

    public void testStripHtmlNewLineAsLast() {
        String html = "<div\n class=\"abc\"\n style=\"abc\"\n>uh!</div>";
        String text = "uh!";
        String result = new SearchResultSnippetTag().stripHtmlTags(html);
        assertEquals(text, result);
    }

    public void testLongTestShouldNotProduceIssues() throws JspException, RepositoryException, AccessDeniedException {
        // this is taken from a sample documentation page.
        final String longText = "{toc:maxLevel=1}\n" +
                "\n" +
                "h1. Rationale\n" +
                "\n" +
                "With **Magnolia 4.0**, we decided to add support for Freemarker for several reasons: not only for its flexibility, cleaner syntax, and better error reporting, but also for the fact that it does not depend on the file system: templates don't have to be extracted to the file system, so they can easily be stored in the repository, versioned, and so on. Since the AdminCentral was already built using Freemarker, and we had experimental support for Freemarker paragraphs, it was really the next logical move.\n" +
                "However, this doesn't mean we're dropping support for JSP-based templates. As a matter of fact, you can mix and match templates and paragraphs using both templating mechanisms at will.\n" +
                "\n" +
                "Freemarker is easy to learn, and you can even use it with your regular html editing tools. Most IDEs also provide a good level of integration.\n" +
                "\n" +
                "h1. References\n" +
                "\n" +
                "Here a few useful links to get started with Freemarker:\n" +
                "* http://freemarker.sourceforge.net/docs/index.html\n" +
                "* http://freemarker.sourceforge.net/docs/dgui_quickstart_template.html - A quickstart guide for templating.\n" +
                "* http://freemarker.sourceforge.net/docs/ref_builtins.html - Built-ins are the {{?bits}} following expressions and providing useful functionality.\n" +
                "* http://freemarker.sourceforge.net/docs/ref_directives.html - Directives are {{\\[#blocks]}} of code which can be assimilated to jsp tags.\n" +
                "\n" +
                "\n" +
                "h1. Introduction\n" +
                "\n" +
                "h2. Directives\n" +
                "Freemarker has a number of {link:directives|http://freemarker.org/docs/dgui_template_directives.html} to help structure your templates: {{if}}, {{else}}, {{list}}, etc.. Directives are {{\\[#someDirective]...\\[/#directive]}} blocks or {{\\[#otherDirective]}} tags. Note that FreeMarker supports two syntaxes, using either {{\\[}} or {{<}} delimiters. We favor the former for clarity and because it makes HTML editors happier, but you will notice that the FreeMarker documentation uses the latter as a default. Both are interchangeable but one template has to stick to one syntax.\n" +
                "\n" +
                "The {link:complete list|http://freemarker.org/docs/ref_directives.html} is available in the FreeMarker documentation. The following few directives should get you a long way: {{\\[#assign]}}, {{\\[#if]}} {{\\[#else]}}, {{\\[#list]}} & {{\\[#include]}}.\n" +
                "\n" +
                "Be sure to check the documentation, you might find nice little surprises, such as the fact that the {{list}} directive expose 2 useful extra variables in the loop: {{item_index}} & {{item_has_next}}.\n" +
                "\n" +
                "h2. Expressions\n" +
                "Very similar to the JSP expressions {{$\\{somevar\\}}} you can access the available objects/variables. You can access all properties by the simple dot notation  {{$\\{bean.prop\\}}}. In addition you are able to call methods {{$\\{util.method(param1, param2)\\}}}\n" +
                "\n" +
                "h2. Default values\n" +
                "FreeMarker offers a nice little syntactic sugar to handle default values: you can use the {{!}} operator: {{$\\{content.title!\"no title\"\\}}}. They can be chained and the following example would either output the title, the name of the page or a default string: {{$\\{content.title!content.@name!\"no title\"\\}}}.\n" +
                "\n" +
                "h2. Null\n" +
                "Freemarker does not allow you to render null values (will throw an exception). If you expect a value to be null you can also use the ! operator: {{$\\{content.title!\\}}}. This is the equivalent to {{$\\{content.title!''\\}}}.\n" +
                "\n" +
                "h2. Built-ins\n" +
                "FreeMarker is in contrast to JSP optimized for writing templates (especially HTML). There are many so called built ins you can call on a variable by using the {{?}} notation.\n" +
                "\n" +
                "Built-ins are specific to the type of object they're applied to. Here's an example applied to a string and to a boolean, respectively:\n" +
                "{code}\n" +
                "${\"some text\"?cap_first}\n" +
                "${aBoolean?string(\"yes\", \"no\")}\n" +
                "{code}\n" +
                "\n" +
                "FreeMarker's documentation offers a {link:comprehensive list of the available built-ins|http://freemarker.org/docs/ref_builtins.html}.\n" +
                "\n" +
                "h2. JSP Tags\n" +
                "FreeMarker optionally supports JSP tag libraries, and we have enabled this feature in Magnolia too. You can use any tag library in your FreeMarker, and that means [you can use the Magnolia tags|#UsingMagnoliaTags] too.\n" +
                "\n" +
                "h1. Working with Magnolia content \n" +
                "To help using Magnolia content in templates, we've implemented a few specificities: besides the regular Java objects available in the template ({{def}}, {{model}}, {{ctx}}, ...), the content nodes are treated a little more specifically:\n" +
                "\n" +
                "h2. Accessing content\n" +
                "You can access any nodeData or child node with the dot notation: {{$\\{content.title\\}}}, {{$\\{content.metaData.creationDate\\}}}, {{$\\{content.childNode\\}}}.\n" +
                "\n" +
                "h2. Built-ins for content\n" +
                "Content nodes are wrapped such that you can use the {link:built-ins for nodes|http://freemarker.org/docs/ref_builtins_node.html}. A few examples:\n" +
                "\n" +
                "* {{content?children}} to get all children of a node.\n" +
                "* {{content?parent}} to get its parent.\n" +
                "* {{content?node_type}} to get the node's type. (can be used to distinguish between pages and paragraphs, for instance)\n" +
                "\n" +
                "h2. Special content properties\n" +
                "To distinguish the nodeData values and the bean properties (node name, node type) we have introduced the {{@}} notation:\n" +
                "* {{content.@name}}\n" +
                "* {{content.@handle}}\n" +
                "* {{content.@uuid}}\n" +
                "\n" +
                "h2. Using Magnolia Tags\n" +
                "You can use all JSP tags and therefore you can use all the common Magnolia JSP tags.\n" +
                "{code}\n" +
                "[#assign cms=JspTaglibs[\"cms-taglib\"]]\n" +
                "\n" +
                "[@cms.contentNodeIterator contentNodeCollectionName=\"main\"]\n" +
                "    [@cms.includeTemplate /]\n" +
                "[/@cms.contentNodeIterator]\n" +
                "[@cms.newBar contentNodeCollectionName=\"main\" newLabel=\"New Content\" paragraph=\"${stk.asStringList(def.mainArea.paragraphs)}\" /]\n" +
                "{code}\n" +
                "\n" +
                "h1. Examples\n" +
                "\n" +
                "* To output a simple property of the current node, called \"myProperty\": {{$\\{content.myProperty\\}}}.\n" +
                "* You can navigate sub nodes: {{$\\{content.someSubnode.someOtherProperty\\}}}.\n" +
                "* Or loop through them like a regular Freemarker sequence:\n" +
                "{code}\n" +
                "[#list content.mynodes?children as n]\n" +
                "${n.someProperty}\n" +
                "[#/list]\n" +
                "{code}\n" +
                "\n" +
                "In a Freemarker template, you can render paragraphs using a syntax very similar to that of JSPs, using the Magnolia CMS taglib:\n" +
                "{snippet:url=it-templates/templating_test.ftl|id=para-loop-jsp|lang=html}\n" +
                "\n" +
                "But you can also use a more Freemarker-friendly syntax, using a regular Freemarker {{list}} directive, and the {{renderParagraph}} method of the {{mgnl}} tool:\n" +
                "{snippet:url=it-templates/templating_test.ftl|id=para-loop-ftl|lang=html}\n" +
                "\n" +
                "And here's another example of using the Magnolia CMS(U) taglibs:\n" +
                "{snippet:url=it-templates/para_dynamic.ftl|id=ftl-taglib-usage|lang=html}\n" +
                "\n" +
                "Here are some of the implicitely available objects and how to use them:\n" +
                "{snippet:url=it-templates/para_dynamic.ftl|id=ftl-available-objects|lang=html}\n" +
                "\n" +
                "The syntax is very similar in JSP, with limitations (no concept of \"built-ins\", etc. and no method calls are possible), you have to resort to scriplets to achieve the same results:\n" +
                "{snippet:url=it-templates/templating_test_newschool.jsp|id=jsp-mgnl|lang=html}\n" +
                "\n" +
                "h1. Template loaders\n" +
                "\n" +
                "The template loaders are configurable, under {{/server/rendering/freemarker}}. By default, Freemarker templates are loaded from the webapp folder, using the {javadoc:info.magnolia.freemarker.loaders.LazyWebappTemplateLoader}. If no matching template is found, then it will be loaded from the classpath. We also provide a {javadoc:info.magnolia.freemarker.loaders.LazyFileTemplateLoader} implementation, which allows your templates to be loaded from an arbitrary location on the file system. Custom template loaders are of course also useable. Our [inplace-templating|/modules/inplace-templating] module offers a {{JcrRepoTemplateLoader}} which allows, as its name suggests, to load templates from the repository.\n" +
                "\n" +
                "h1. Technical notes\n" +
                "\n" +
                "In Freemarker, objects can be wrapped in \"models\". This is what we do with our Content, NodeData and someother objects, through our custom {javadoc:info.magnolia.freemarker.models.MagnoliaObjectWrapper}. We have a {javadoc:info.magnolia.module.templating.renderers.FreemarkerTemplateRenderer} and a {javadoc:info.magnolia.module.templating.paragraphs.FreemarkerParagraphRenderer}.\n" +
                "\n" +
                "In general, all Freemarker rendering in Magnolia happens through the {javadoc:info.magnolia.freemarker.FreemarkerHelper} class.\n" +
                "";
        final String longLongText = longText + longText + longText + longText + longText + longText + longText + longText;
        try {
            new SearchResultSnippetTag().stripHtmlTags(longLongText);
        } catch (Throwable e) {
            fail("Failed when stripping html: " + ExceptionUtils.getMessage(e));
        }

        final SearchResultSnippetTag tag = new SearchResultSnippetTag();
        tag.setQuery("module");
        final MockContent page = new MockContent("page");
        final MockContent paragraphsColl1 = (MockContent) page.createContent("paragraphs", ItemType.CONTENTNODE);
        for (int i = 0; i < 10; i++) {
            final String paraName = "para" + i;
            final MockContent para = (MockContent) paragraphsColl1.createContent(paraName, ItemType.CONTENTNODE);
            para.createNodeData("text", longLongText);
            para.createNodeData("text2", longLongText);
            para.createNodeData("text3", longLongText);
        }
        tag.setPage(page);

        try {
            tag.getSnippets();
        } catch (Throwable e) {
            fail("Failed when collecting snippets: " + ExceptionUtils.getMessage(e));
        }
    }
}
