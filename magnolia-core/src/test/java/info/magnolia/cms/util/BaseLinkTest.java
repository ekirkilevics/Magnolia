/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.BinaryMockNodeData;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.context.WebContext;
import info.magnolia.context.MgnlContext;
import org.easymock.IAnswer;
import static org.easymock.classextension.EasyMock.*;

import java.text.MessageFormat;

/**
 * @author philipp
 * @version $Id$
 *
 */
public abstract class BaseLinkTest extends MgnlTestCase {

    protected static final String UUID_PATTNER_OLD_FORMAT = "$'{'link:'{'uuid:'{'{0}'}',repository:'{'{1}'}',workspace:'{'default'}',path:'{'{2}'}}}'";
    protected static final String UUID_PATTNER_NEW_FORMAT = "$'{'link:'{'uuid:'{'{0}'}',repository:'{'{1}'}',handle:'{'{2}'}',nodeData:'{'{3}'}',extension:'{'{4}'}}}'";
    protected static final String UUID_PATTNER_SIMPLE = MessageFormat.format(UUID_PATTNER_NEW_FORMAT, new String[]{"2", ContentRepository.WEBSITE, "/parent/sub", "", "html"});
    protected static final String UUID_PATTNER_SIMPLE_OLD_FORMAT = MessageFormat.format(UUID_PATTNER_OLD_FORMAT, new String[]{"2", ContentRepository.WEBSITE, "/parent/sub"});

    protected static final String HREF_SIMPLE = "/parent/sub.html";
    private WebContext webContext;

    protected void setUp() throws Exception {
        super.setUp();

        String website =
            "/parent@uuid=1\n" +
            "/parent/sub@uuid=2\n" +
            "/parent/sub2@uuid=3";

        MockHierarchyManager hm = MockUtil.createHierarchyManager(website);
        webContext = createMock(WebContext.class);
        expect(webContext.getHierarchyManager(ContentRepository.WEBSITE)).andReturn(hm).anyTimes();
        expect(webContext.getContextPath()).andReturn("some-context").anyTimes();

        // add a binary
        MockContent page = (MockContent) hm.getContent("/parent/sub");
        page.addNodeData(new BinaryMockNodeData("file", null, "test.jpg", "image/jpeg", 5000));

        replay(webContext);
        MgnlContext.setInstance(webContext);

        URI2RepositoryManager uri2repo = createMock(URI2RepositoryManager.class);
        expect(uri2repo.getURI((String) anyObject(), (String) anyObject())).andStubAnswer(new IAnswer<String>() {
            public String answer() throws Throwable {
                return (String) getCurrentArguments()[1];
            }
        });
        expect(uri2repo.getHandle((String) anyObject())).andStubAnswer(new IAnswer<String>() {
            public String answer() throws Throwable {
                return (String) getCurrentArguments()[0];
            }
        });

        expect(uri2repo.getRepository((String) anyObject())).andStubReturn(ContentRepository.WEBSITE);

        replay(uri2repo);
        FactoryUtil.setInstance(URI2RepositoryManager.class, uri2repo);

        FactoryUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());

        final Server.ServerConfiguration serverConfiguration = new Server.ServerConfiguration();
        serverConfiguration.setDefaultBaseUrl("http://myTests:1234/yay");
        FactoryUtil.setInstance(Server.ServerConfiguration.class, serverConfiguration);
    }

    protected void tearDown() throws Exception {
        verify(webContext);
        super.tearDown();
    }
}