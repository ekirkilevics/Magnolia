/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.freemarker;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.InvalidReferenceException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockMetaData;
import info.magnolia.test.mock.MockNodeData;
import info.magnolia.test.model.Color;
import info.magnolia.test.model.Pair;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FreemarkerHelperTest extends TestCase {
    private StringTemplateLoader tplLoader;
    private FreemarkerHelper fmHelper;

    protected void setUp() throws Exception {
        super.setUp();
        tplLoader = new StringTemplateLoader();
        fmHelper = new FreemarkerHelper();
        fmHelper.getConfiguration().setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        fmHelper.getConfiguration().setTemplateLoader(tplLoader);

        // seems useless when running tests from maven (?), so we'll shunt log4j as well
        freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE);
        final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("freemarker.runtime");
        logger.setLevel(org.apache.log4j.Level.OFF);
    }

    private void assertRendereredContent(String expectedOutput, Object o, String templateName) throws TemplateException, IOException {
        assertRendereredContent(expectedOutput, Locale.US, o, templateName);
    }

    private void assertRendereredContent(String expectedOutput, Locale l, Object o, String templateName) throws TemplateException, IOException {
        final Context context = createStrictMock(Context.class);
        expect(context.getLocale()).andReturn(l);
//        if (o instanceof Map) {
//            expect(context.getAttribute("msg", 1)).andReturn(null);
//        }
        replay(context);
        MgnlContext.setInstance(context);

        final StringWriter out = new StringWriter();
        fmHelper.render(templateName, o, out);

        assertEquals(expectedOutput, out.toString());
        verify(context);
    }

    public void testWeCanUseAnyObjectTypeAsOurRoot() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "${left} ${right.left.blue - left} ${right.right.green} ${right.right.name}");
        final Pair root = new Pair(Integer.valueOf(100), new Pair(Color.PINK, Color.ORANGE));
        assertRendereredContent("100 75 200 orange", root, "test.ftl");
    }

    public void testSimpleNodeReferenceOutputsItsName() throws TemplateException, IOException {
        final MockContent foo = new MockContent("foo");
        foo.addContent(new MockContent("bar"));
        foo.addContent(new MockContent("baz"));
        foo.addContent(new MockContent("gazonk"));
        Pair pair = new Pair(Color.ORANGE, foo);
        Map<String, Pair> map = Collections.singletonMap("pair", pair);

        tplLoader.putTemplate("test.ftl", "${pair.right} ${pair.right.gazonk} ${pair.left?string} ${pair.right?string} ${pair.right.gazonk?string}");

        assertRendereredContent("foo gazonk color:orange foo gazonk", map, "test.ftl");
    }

    public void testSubNodesAreReachable() throws TemplateException, IOException {
        tplLoader.putTemplate("test_sub.ftl", "The child node's bli'bla property is ${bli['bla']}");

        final MockContent c = new MockContent("plop");
        c.setUUID("123");
        c.addNodeData(new MockNodeData("foo", "bar"));
        final MockContent bli = new MockContent("bli");
        c.addContent(bli);
        bli.addNodeData(new MockNodeData("bla", "bloup"));

        assertRendereredContent("The child node's bli'bla property is bloup", c, "test_sub.ftl");
    }

    public void testSubSubNode() throws TemplateException, IOException {
        final MockContent baz = new MockContent("baz");
        baz.addNodeData(new MockNodeData("prop", "wassup"));
        final MockContent bar = new MockContent("bar");
        final MockContent foo = new MockContent("foo");
        final MockContent c = new MockContent("root");
        bar.addContent(baz);
        foo.addContent(bar);
        c.addContent(foo);

        tplLoader.putTemplate("test.ftl", "yo, ${foo.bar.baz['prop']} ?");
        assertRendereredContent("yo, wassup ?", c, "test.ftl");
    }

    public void testCanLoopThroughNodes() throws TemplateException, IOException {
        final MockContent foo = new MockContent("foo");
        final MockContent c = new MockContent("root");
        foo.addContent(new MockContent("bar"));
        foo.addContent(new MockContent("baz"));
        foo.addContent(new MockContent("gazonk"));
        c.addContent(foo);

        tplLoader.putTemplate("test.ftl", "${foo?children?size}: <#list foo?children as n>${n.@handle} </#list>");

        assertRendereredContent("3: /root/foo/bar /root/foo/baz /root/foo/gazonk ", c, "test.ftl");
    }

    public void testCanLoopThroughNodesNestedInBean() throws TemplateException, IOException {
        final MockContent foo = new MockContent("foo");
        foo.addContent(new MockContent("bar"));
        foo.addContent(new MockContent("baz"));
        foo.addContent(new MockContent("gazonk"));
        Pair pair = new Pair(Color.ORANGE, foo);
        Map<String, Pair> map = Collections.singletonMap("pair", pair);

        tplLoader.putTemplate("test.ftl", "${pair.right?children?size}: <#list pair.right?children as n>${n.@handle} </#list>");

        assertRendereredContent("3: /foo/bar /foo/baz /foo/gazonk ", map, "test.ftl");
    }

    public void testCanLoopThroughPropertiesUsingTheKeysBuiltIn() throws TemplateException, IOException {
        final MockContent f = new MockContent("flebele");
        f.addNodeData(new MockNodeData("foo", "bar"));
        f.addNodeData(new MockNodeData("bar", "baz"));
        f.addNodeData(new MockNodeData("baz", "gazonk"));
        final MockContent c = new MockContent("root");
        c.addContent(f);
        tplLoader.putTemplate("test.ftl", "${flebele?keys?size}:<#list flebele?keys as n> ${n}=${flebele[n]}</#list>");

        assertRendereredContent("3: foo=bar bar=baz baz=gazonk", c, "test.ftl");
    }

    public void testCanLoopThroughPropertiesUsingTheValuesBuiltIn() throws TemplateException, IOException {
        final MockContent f = new MockContent("flebele");
        f.addNodeData(new MockNodeData("foo", "bar"));
        f.addNodeData(new MockNodeData("bar", "baz"));
        f.addNodeData(new MockNodeData("baz", "gazonk"));
        final MockContent c = new MockContent("root");
        c.addContent(f);
        tplLoader.putTemplate("test.ftl", "${flebele?values?size}:<#list flebele?values as v> ${v}</#list>");

        assertRendereredContent("3: bar baz gazonk", c, "test.ftl");
    }

//    public void testCanGetPropertyHandle() throws TemplateException, IOException {
//        final MockContent f = new MockContent("flebele");
//        f.addNodeData(new MockNodeData("foo", "bar"));
//        final MockContent c = new MockContent("root");
//        c.addContent(f);
//        tplLoader.putTemplate("test2.ftl", "${flebele['foo'].@handle} ${flebele['foo'].name}");// ${flebele.foo.@handle}");
//        assertRendereredContent("/root/flebele/foo", c, "test2.ftl");
//    }

    public void testCanRenderMetaData() throws TemplateException, IOException, AccessDeniedException {
        final MockContent f = new MockContent("foo");
        final MockMetaData md = f.createMetaData();
        md.setAuthorId("greg");
        md.setActivated();
        md.setTitle("my test page");
        md.setCreationDate();
        final MockContent c = new MockContent("root");
        c.addContent(f);
        // skipping the time in the datetime format, cause i'm lazy: MetaData currently does not let me set the activation datetime as i want it
        String expectedDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        tplLoader.putTemplate("test.ftl", "${foo.MetaData.authorId}:${foo.MetaData.isActivated?string('yes','no')}:${foo.MetaData.title}:${foo.MetaData.creationDate?string('yyyy-MM-dd')}");
        assertRendereredContent("greg:yes:my test page:" + expectedDate, c, "test.ftl");
    }

    public void testMetaDataIsOneOfTheChildrenRetrievedByTheChildrenBuiltIn() throws TemplateException, IOException, AccessDeniedException {
        final MockContent f = new MockContent("foo");
        final MockMetaData md = f.createMetaData();
        md.setAuthorId("greg");
        md.setActivated();
        md.setTitle("my test page");
        md.setCreationDate();
        final MockContent c = new MockContent("pouet");
        f.addContent(c);
        tplLoader.putTemplate("test.ftl", "[#list c?children as n]${n},[/#list]");
        assertRendereredContent("MetaData,pouet," , Collections.singletonMap("c", f), "test.ftl");
    }

    public void testBooleanPropertiesAreHandledProperly() throws TemplateException, IOException {
        final MockContent c = new MockContent("root");
        final MockContent foo = new MockContent("foo");
        foo.addNodeData(new MockNodeData("hop", Boolean.TRUE));
        foo.addNodeData(new MockNodeData("hip", Boolean.FALSE));
        c.addContent(foo);
        tplLoader.putTemplate("test.ftl", "${foo['hop']?string(\"yes\", \"no\")}" +
                " ${foo.hop?string(\"yes\", \"no\")}" +
                " ${foo['hip']?string(\"yes\", \"no\")}" +
                " ${foo.hip?string(\"yes\", \"no\")}");

        assertRendereredContent("yes yes no no", c, "test.ftl");
    }

    public void testDatePropertiesAreHandledProperly() throws TemplateException, IOException {
        final MockContent c = new MockContent("root");
        final MockContent foo = new MockContent("foo");
        foo.addNodeData(new MockNodeData("date", new GregorianCalendar(2007, 5, 3, 15, 39, 46)));
        c.addContent(foo);

        tplLoader.putTemplate("test.ftl", "${foo['date']?string('yyyy-MM-dd HH:mm:ss')}");

        assertRendereredContent("2007-06-03 15:39:46", c, "test.ftl");
    }

    public void testNumberProperties() throws TemplateException, IOException {
        final MockContent c = new MockContent("root");
        final MockContent foo = new MockContent("foo");
        foo.addNodeData(new MockNodeData("along", new Long(1234567890123456789l)));
        foo.addNodeData(new MockNodeData("adouble", new Double(12345678.901234567890d)));
        c.addContent(foo);
        tplLoader.putTemplate("test.ftl", "${foo['along']} , ${foo.adouble}");
        // ! TODO ! this is locale dependent
        assertRendereredContent("1,234,567,890,123,456,789 , 12,345,678.901", c, "test.ftl");
    }

    public void testReferenceProperties() throws TemplateException, IOException {
        final MockContent foo = new MockContent("foo");
        final MockContent bar = new MockContent("bar");
        foo.addNodeData(new MockNodeData("some-ref", bar));
        bar.addNodeData(new MockNodeData("baz", "gazonk"));
        tplLoader.putTemplate("test.ftl", "${foo['some-ref']} ${foo['some-ref'].baz}");
        assertRendereredContent("bar gazonk", Collections.singletonMap("foo", foo), "test.ftl");
    }

    public void testRendereredWithCurrentLocale() throws TemplateException, IOException {
        tplLoader.putTemplate("test.ftl", "this is a test template.");
        tplLoader.putTemplate("test_en.ftl", "this is a test template in english.");
        tplLoader.putTemplate("test_fr_BE.ftl", "Ceci est une template belge hein une fois.");
        tplLoader.putTemplate("test_fr.ftl", "Ceci est une template de test en français.");

        final MockContent c = new MockContent("pouet");
        c.setUUID("123");
        c.addNodeData(new MockNodeData("foo", "bar"));

        assertRendereredContent("Ceci est une template belge hein une fois.", new Locale("fr", "BE"), c, "test.ftl");
    }

    public void testMissingAndDefaultValueOperatorsActsAsIExceptThemTo() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "[#if content.title?has_content]<h2>${content.title}</h2>[/#if]");
        final MockContent c = new MockContent("pouet");
        final Map m = Collections.singletonMap("content", c);
        assertRendereredContent("", m, "test.ftl");

        c.addNodeData(new MockNodeData("title", ""));
        assertRendereredContent("", m, "test.ftl");

        c.addNodeData(new MockNodeData("title", "pouet"));
        assertRendereredContent("<h2>pouet</h2>", m, "test.ftl");
    }

    public void testContextPathIsAddedWithWebContext() throws IOException, TemplateException {
        tplLoader.putTemplate("pouet", ":${contextPath}:");
        final WebContext context = createStrictMock(WebContext.class);
        expect(context.getLocale()).andReturn(Locale.US);
        expect(context.getContextPath()).andReturn("tralala");

        replay(context);
        MgnlContext.setInstance(context);

        final StringWriter out = new StringWriter();
        fmHelper.render("pouet", new HashMap(), out);

        assertEquals(":tralala:", out.toString());
        verify(context);
    }

    public void testContextPathIsNotAddedWithNotWebContext() throws IOException, TemplateException {
        tplLoader.putTemplate("pouet", ":${contextPath}:");
        final Context context = createStrictMock(Context.class);
        expect(context.getLocale()).andReturn(Locale.US);

        replay(context);
        MgnlContext.setInstance(context);

        final StringWriter out = new StringWriter();
        try {
            fmHelper.render("pouet", new HashMap(), out);
            fail("should have failed");
        } catch (InvalidReferenceException e) {
            assertEquals("Expression contextPath is undefined on line 1, column 4 in pouet.", e.getMessage());
        }

        verify(context);
    }

    public void testEvalCanEvaluateDynamicNodeProperties() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "evaluated title: ${'content.title'?eval}");

        final MockContent c = new MockContent("content");
        c.setUUID("123");
        c.addNodeData(new MockNodeData("title", "This is my title"));
        c.addNodeData(new MockNodeData("other", "other-value"));
        Map m=new HashMap();
        m.put("content", c);

        assertRendereredContent("evaluated title: This is my title", m, "test.ftl");
    }

    public void testInterpretCanBeUsedForDynamicNodeProperties() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "[#assign dynTpl='${content.title}'?interpret]evaluated title: [@dynTpl/]");

        final MockContent c = new MockContent("content");
        c.setUUID("123");
        c.addNodeData(new MockNodeData("title", "This is my ${content.other} title"));
        c.addNodeData(new MockNodeData("other", "other-value"));
        Map m=new HashMap();
        m.put("content", c);

        assertRendereredContent("evaluated title: This is my other-value title", m, "test.ftl");
    }

    public void testInterpretCanBeUsedEvenIfPropertyHasNoFreemarkerStuff() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "[#assign dynTpl='${content.title}'?interpret]evaluated title: [@dynTpl/]");

        final MockContent c = new MockContent("content");
        c.setUUID("123");
        c.addNodeData(new MockNodeData("title", "This is my plain title"));
        c.addNodeData(new MockNodeData("other", "other-value"));
        Map m=new HashMap();
        m.put("content", c);

        assertRendereredContent("evaluated title: This is my plain title", m, "test.ftl");
    }
}
