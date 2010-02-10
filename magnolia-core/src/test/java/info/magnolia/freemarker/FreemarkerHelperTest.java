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
package info.magnolia.freemarker;

import freemarker.core.InvalidReferenceException;
import freemarker.template.TemplateException;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockAggregationState;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockMetaData;
import info.magnolia.test.mock.MockNodeData;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.model.Color;
import info.magnolia.test.model.Pair;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FreemarkerHelperTest extends AbstractFreemarkerTestCase {

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
        final Pair pair = new Pair(Color.ORANGE, foo);
        final Map map = createSingleValueMap("pair", pair);

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
        final Object pair = new Pair(Color.ORANGE, foo);
        final String key = "pair";
        Map map = createSingleValueMap(key, pair);

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
        assertRendereredContent("MetaData,pouet,", createSingleValueMap("c", f), "test.ftl");
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
        final MockHierarchyManager hm = new MockHierarchyManager();
        hm.getRoot().addContent(foo);
        hm.getRoot().addContent(bar);

        tplLoader.putTemplate("test.ftl", "${foo['some-ref']} ${foo['some-ref'].baz}");
        assertRendereredContent("bar gazonk", createSingleValueMap("foo", foo), "test.ftl");
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
        final Map m = createSingleValueMap("content", c);
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

        expect(context.getContextPath()).andReturn("/tralala");
        expect(context.getAggregationState()).andReturn(new MockAggregationState());
        expect(context.getServletContext()).andReturn(null);
        expect(context.getRequest()).andReturn(null);
        replay(context);
        MgnlContext.setInstance(context);
        assertRendereredContentWithoutCheckingContext(":/tralala:", new HashMap(), "pouet");
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

    public void testContextPathIsAlsoAvailableThroughMagnoliaContext() throws IOException, TemplateException {
        tplLoader.putTemplate("pouet", ":${ctx.contextPath}:");
        final WebContext context = createStrictMock(WebContext.class);
        expect(context.getLocale()).andReturn(Locale.US);

        expect(context.getContextPath()).andReturn("/tralala"); // called when preparing the freemarker data model
        expect(context.getAggregationState()).andReturn(new MockAggregationState());
        expect(context.getServletContext()).andReturn(null);
        expect(context.getRequest()).andReturn(null);
        expect(context.getContextPath()).andReturn("/tralala"); // actual call from the template
        replay(context);
        MgnlContext.setInstance(context);
        assertRendereredContentWithoutCheckingContext(":/tralala:", new HashMap(), "pouet");
        verify(context);
    }

    public void testMagnoliaContextIsExposed() throws IOException, TemplateException {
        tplLoader.putTemplate("pouet", ":${ctx.user.name}:");
        final Context context = createStrictMock(Context.class);
        final User user = createStrictMock(User.class);
        expect(context.getLocale()).andReturn(Locale.US);

        expect(context.getUser()).andReturn(user);
        expect(user.getName()).andReturn("Hiro Nakamura");
        replay(context, user);
        MgnlContext.setInstance(context);
        assertRendereredContentWithoutCheckingContext(":Hiro Nakamura:", new HashMap(), "pouet");
        verify(context, user);
    }

    public void testMagnoliaContextAttributesAreAvailableWithMapSyntax() throws IOException, TemplateException {
        tplLoader.putTemplate("pouet", ":${ctx.foo}:${ctx['baz']}:");
        final Context context = createStrictMock(Context.class);
        expect(context.getLocale()).andReturn(Locale.US);

        expect(context.get("foo")).andReturn("bar");
        expect(context.get("baz")).andReturn("buzz");

        replay(context);

        MgnlContext.setInstance(context);
        assertRendereredContentWithoutCheckingContext(":bar:buzz:", new HashMap(), "pouet");
        verify(context);
    }

    public void testEvalCanEvaluateDynamicNodeProperties() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "evaluated result: ${'content.title'?eval}");

        final MockContent c = new MockContent("content");
        c.setUUID("123");
        c.addNodeData(new MockNodeData("title", "This is my title"));
        c.addNodeData(new MockNodeData("other", "other-value"));
        Map m = new HashMap();
        m.put("content", c);

        assertRendereredContent("evaluated result: This is my title", m, "test.ftl");
    }

    public void testInterpretCanBeUsedForDynamicNodeProperties() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "[#assign dynTpl='${content.title}'?interpret]\n" +
                "evaluated result: [@dynTpl/]");

        final MockContent c = new MockContent("content");
        c.setUUID("123");
        c.addNodeData(new MockNodeData("title", "This is my ${content.other} title"));
        c.addNodeData(new MockNodeData("other", "other-value"));
        Map m = new HashMap();
        m.put("content", c);

        assertRendereredContent("evaluated result: This is my other-value title", m, "test.ftl");
    }

    public void testEvalCanAlsoBeUsedForNestedExpressions() throws IOException, TemplateException {
        // except we need lots of quotes
        tplLoader.putTemplate("test.ftl", "evaluated result: ${'\"${content.title}\"'?eval}");

        final MockContent c = new MockContent("content");
        c.setUUID("123");
        c.addNodeData(new MockNodeData("title", "This is my ${content.other}"));
        c.addNodeData(new MockNodeData("other", "other-value"));
        Map m = new HashMap();
        m.put("content", c);

        assertRendereredContent("evaluated result: This is my other-value", m, "test.ftl");
    }

    public void testInterpretCanBeUsedEvenIfPropertyHasNoFreemarkerStuff() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "[#assign dynTpl='${content.title}'?interpret]evaluated title: [@dynTpl/]");

        final MockContent c = new MockContent("content");
        c.setUUID("123");
        c.addNodeData(new MockNodeData("title", "This is my plain title"));
        c.addNodeData(new MockNodeData("other", "other-value"));
        Map m = new HashMap();
        m.put("content", c);

        assertRendereredContent("evaluated title: This is my plain title", m, "test.ftl");
    }

    private final static String SOME_UUID = "deb0c7d0-402f-4e04-9db3-cb308695733e";

    public void testUuidLinksAreTransformedToRelativeLinksInWebContext() throws IOException, TemplateException, RepositoryException {
        final MockContent page = new MockContent("baz");
        final MockHierarchyManager hm = prepareHM(page);

        final AggregationState agg = new AggregationState();
        agg.setMainContent(page);
        final WebContext context = createStrictMock(WebContext.class);
        expect(context.getLocale()).andReturn(Locale.CANADA);
        expect(context.getAggregationState()).andReturn(agg);
        expect(context.getHierarchyManager("website")).andReturn(hm);

        replay(context);
        doTestUuidLinksAreTransformed(context, "== Some text... blah blah... <a href=\"baz.html\">Bleh</a> ! ==");
        verify(context);
    }

    public void testUuidLinksAreTransformedToAbsoluteLinksInWebContextWithoutAggregationState() throws IOException, TemplateException, RepositoryException {
        final MockContent page = new MockContent("baz");
        final MockHierarchyManager hm = prepareHM(page);

        final WebContext context = createStrictMock(WebContext.class);
        expect(context.getLocale()).andReturn(Locale.CANADA);
        expect(context.getAggregationState()).andReturn(new AggregationState());
        expect(context.getHierarchyManager("website")).andReturn(hm);
        expect(context.getContextPath()).andReturn("/some-context");

        replay(context);
        doTestUuidLinksAreTransformed(context, "== Some text... blah blah... <a href=\"/some-context/foo/bar/baz.html\">Bleh</a> ! ==");
        verify(context);
    }

    public void testUuidLinksAreTransformedToFullUrlLinksInNonWebContext() throws IOException, TemplateException, RepositoryException {
        doTestUuidLinksAreTransformed(null, "== Some text... blah blah... <a href=\"http://myTests:1234/yay/foo/bar/baz.html\">Bleh</a> ! ==");
    }

    private void doTestUuidLinksAreTransformed(Context webCtx, String expectedOutput) throws IOException, TemplateException, RepositoryException {
        MockHierarchyManager cfgHM = MockUtil.createHierarchyManager("fakeemptyrepo");
        MockUtil.mockObservation(cfgHM);

        final SystemContext sysMockCtx = createStrictMock(SystemContext.class);

        if (webCtx == null) {
            expect(sysMockCtx.getLocale()).andReturn(Locale.KOREA);
            final MockHierarchyManager hm = prepareHM(new MockContent("baz"));
            expect(sysMockCtx.getHierarchyManager("website")).andReturn(hm);
        }
        ComponentsTestUtil.setInstance(SystemContext.class, sysMockCtx);
        replay(sysMockCtx);

        ComponentsTestUtil.setImplementation(URI2RepositoryManager.class, URI2RepositoryManager.class);
        final I18nContentSupport i18NSupportMock = createStrictMock(I18nContentSupport.class);
        ComponentsTestUtil.setInstance(I18nContentSupport.class, i18NSupportMock);

        expect(i18NSupportMock.toI18NURI("/foo/bar/baz.html")).andReturn("/foo/bar/baz.html").times(1, 2);

        final String text = "Some text... blah blah... <a href=\"${link:{uuid:{" + SOME_UUID + "},repository:{website},handle:{/foo/bar},nodeData:{},extension:{html}}}\">Bleh</a> !";
        final MockContent c = new MockContent("content");
        c.addNodeData(new MockNodeData("text", text));
        tplLoader.putTemplate("test", "== ${text} ==");

        replay(i18NSupportMock);
        MgnlContext.setInstance(webCtx == null ? sysMockCtx : webCtx);
        assertRendereredContentWithoutCheckingContext(expectedOutput, c, "test");
        verify(i18NSupportMock);
        verify(sysMockCtx);
    }

    public void testUserPropertiesAreAvailable() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "${user.name} is my name, is speak ${user.language}, I'm ${user.enabled?string('', 'not ')}enabled, and testProp has a value of ${user.testProp} !");

        final User user = createStrictMock(User.class);
        expect(user.getName()).andReturn("myName");
        expect(user.getLanguage()).andReturn("fr");
        expect(user.isEnabled()).andReturn(false);
        expect(user.getProperty("testProp")).andReturn("testValue");

        replay(user);
        assertRendereredContent("myName is my name, is speak fr, I'm not enabled, and testProp has a value of testValue !", createSingleValueMap("user", user), "test.ftl");
        verify(user);
    }

    public void testUserUnsupportedExceptionFallback() throws Exception {
        tplLoader.putTemplate("test.ftl", "${user.name} is my name, fullName: ${user.fullName!user.name}, testProp: ${user.testProp!'default'} !");
        final User user = createStrictMock(User.class);
        expect(user.getName()).andReturn("myName");
        expect(user.getProperty("fullName")).andThrow(new UnsupportedOperationException("getProperty:fullName"));
        expect(user.getName()).andReturn("myName");
        expect(user.getProperty("testProp")).andThrow(new UnsupportedOperationException("getProperty:testValue"));

        replay(user);
        assertRendereredContent("myName is my name, fullName: myName, testProp: default !", createSingleValueMap("user", user), "test.ftl");
        verify(user);
    }

    public void testNodeNameCanBeRenderedImplicitely() throws Exception {
        tplLoader.putTemplate("test.ftl", "This should output the node's name: ${content}");
        final Map root = createSingleValueMap("content", new MockContent("myNode"));
        assertRendereredContent("This should output the node's name: myNode", root, "test.ftl");
    }

    public void testNodeNameCanBeRenderedExplicitely() throws Exception {
        tplLoader.putTemplate("test.ftl", "This should also output the node's name: ${content.@name}");
        final Map root = createSingleValueMap("content", new MockContent("myOtherNode"));
        assertRendereredContent("This should also output the node's name: myOtherNode", root, "test.ftl");
    }

    public void testGivenLocaleTakesOverAnyContextLocale() throws IOException, TemplateException {
        tplLoader.putTemplate("test_en.ftl", "in english");
        tplLoader.putTemplate("test_de.ftl", "in deutscher Sprache");
        tplLoader.putTemplate("test_fr.ftl", "en francais");
        tplLoader.putTemplate("test.ftl", "fallback template - no specific language");

        assertRendereredContentWithSpecifiedLocale("en francais", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    public void testSimpleI18NMessageCanBeUsedInTemplates() throws Exception {
        tplLoader.putTemplate("test.ftl", "ouais: ${i18n.get('testMessage')}");
        assertRendereredContentWithSpecifiedLocale("ouais: mon message en francais", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    public void testSimpleI18NMessageFallsBackToEnglishIfNotSpecifiedGivenLanguage() throws Exception {
        tplLoader.putTemplate("test.ftl", "hop: ${i18n.get('testMessage')}");
        assertRendereredContentWithSpecifiedLocale("hop: my message in english", Locale.GERMAN, new HashMap(), "test.ftl");
    }

    // TODO this test can't work at the moment since we're in core and the default bundle is in the admininterface module.
//    public void testI18NFallsBackToDefaultBundle() throws Exception {
//        tplLoader.putTemplate("test.ftl", "ouais: ${i18n['buttons.admincentral']}");
//        assertRendereredContentWithSpecifiedLocale("ouais: Console d'administration", Locale.FRENCH, new HashMap(), "test.ftl");
//    }

    public void testCanUseDotSyntaxToGetASimpleI18NMessage() throws Exception {
        tplLoader.putTemplate("test.ftl", "ouais: ${i18n.testMessage}");
        assertRendereredContentWithSpecifiedLocale("ouais: mon message en francais", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    public void testCanUseBracketSyntaxToGetASimpleI18NMessage() throws Exception {
        tplLoader.putTemplate("test.ftl", "ouais: ${i18n['testMessage']}");
        assertRendereredContentWithSpecifiedLocale("ouais: mon message en francais", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    public void testMustUseMethodCallSyntaxToGetAParameterizedI18NMessage() throws Exception {
        tplLoader.putTemplate("test.ftl", "result: ${i18n.get('withOneParam', ['bar'])}");
        assertRendereredContentWithSpecifiedLocale("result: foo:bar", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    public void testSupportsI18NMessagesWithMultipleParameters() throws Exception {
        tplLoader.putTemplate("test.ftl", "result: ${i18n.get('withMoreParams', ['one', 'two', 'three'])}");
        assertRendereredContentWithSpecifiedLocale("result: 1:one, 2:two, 3:three", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    public void testOutputsInterrogationMarksAroundI18NKeyIfUnknown() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "ouais: ${i18n['bleh.blah']}");
        assertRendereredContentWithSpecifiedLocale("ouais: ???bleh.blah???", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    public void testI18NMessageParametersCanComeFromData() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "result: ${i18n.get('withOneParam', [value])}");
        assertRendereredContentWithSpecifiedLocale("result: foo:wesh t'as vu", Locale.FRENCH, createSingleValueMap("value", "wesh t'as vu"), "test.ftl");
    }

    public void testCanPassBundleNameFromTemplateWithMethodCallSyntaxToGetSimple18NMessage() throws Exception {
        tplLoader.putTemplate("test.ftl", "result: ${i18n.get('testMessage', 'info.magnolia.freemarker.other')}");
        assertRendereredContentWithSpecifiedLocale("result: this is the other bundle", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    public void testCanPassBundleNameFromTemplateWithMethodCallSyntaxToGetAParameterizedI18NMessage() throws Exception {
        tplLoader.putTemplate("test.ftl", "result: ${i18n.get('withOneParam', ['bar'], 'info.magnolia.freemarker.other')}");
        assertRendereredContentWithSpecifiedLocale("result: bling:bar", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    public void testCanPassBundleNameFromTemplateAndSupportsI18NMessagesWithMultipleParameters() throws Exception {
        tplLoader.putTemplate("test.ftl", "result: ${i18n.get('withMoreParams', ['one', 'two', 'three'], 'info.magnolia.freemarker.other')}");
        assertRendereredContentWithSpecifiedLocale("result: bling:one, bling:two, bling:three", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    private MockHierarchyManager prepareHM(MockContent page) {
        final MockContent root = new MockContent("foo");
        final MockContent bar = new MockContent("bar");
        page.setUUID(SOME_UUID);
        root.addContent(bar);
        bar.addContent(page);
        final MockHierarchyManager hm = new MockHierarchyManager();
        hm.getRoot().addContent(root);
        return hm;
    }

}
