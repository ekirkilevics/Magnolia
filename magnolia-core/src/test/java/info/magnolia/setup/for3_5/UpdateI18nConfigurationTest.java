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
package info.magnolia.setup.for3_5;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.content2bean.Content2BeanProcessor;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeMapping;
import info.magnolia.content2bean.impl.Content2BeanProcessorImpl;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;
import info.magnolia.content2bean.impl.TransformationStateImpl;
import info.magnolia.content2bean.impl.TypeMappingImpl;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.PropertiesImportExport;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class UpdateI18nConfigurationTest extends TestCase {
    private static final String CONTENT_AS_IN_30 = "" +
            "server.i18n@type=mgnl:contentNode\n" +
            "server.i18n.language=zz\n" +
            "server.i18n.availableLanguages@type=mgnl:contentNode\n" +
            "server.i18n.availableLanguages.en=en\n" +
            "server.i18n.availableLanguages.es=es\n" +
            "server.i18n.availableLanguages.fr_BE=fr_BE\n";

    private static final String BOOTSTRAPPED_CONTENT_FOR_35 = "" +
            "server.i18n@type=mgnl:content\n" +
            "server.i18n.system@type=mgnl:contentNode\n" +
            "server.i18n.system.fallbackLanguage=en\n" +
            "server.i18n.system.languages@type=mgnl:contentNode\n" +
            "server.i18n.system.languages.zh_CN.country=CN\n" +
            "server.i18n.system.languages.zh_CN.enabled=true\n" +
            "server.i18n.system.languages.zh_CN.langage=zh\n" +
            "server.i18n.system.languages.ru.country=\n" +
            "server.i18n.system.languages.ru.enabled=true\n" +
            "server.i18n.system.languages.ru.langage=ru\n" +
            "server.i18n.system.languages.en.country=\n" +
            "server.i18n.system.languages.en.enabled=true\n" +
            "server.i18n.system.languages.en.langage=en\n" +
            "server.i18n.system.languages.es.country=\n" +
            "server.i18n.system.languages.es.enabled=true\n" +
            "server.i18n.system.languages.es.langage=es\n" +
            "server.i18n.system.languages.fr_BE.country=BE\n" +
            "server.i18n.system.languages.fr_BE.enabled=true\n" +
            "server.i18n.system.languages.fr_BE.langage=fr\n" +

            "server.i18n.content@type=mgnl:contentNode\n" +
            "server.i18n.content.class=this.is.irrelevant.for.this.test\n" +
            "server.i18n.content.enabled=false\n" +
            "server.i18n.content.fallbackLocale=fr\n" +
            "server.i18n.content.locales.en.country=\n" +
            "server.i18n.content.locales.en.enabled=true\n" +
            "server.i18n.content.locales.en.langage=en\n" +
            "server.i18n.content.locales.es.country=\n" +
            "server.i18n.content.locales.es.enabled=true\n" +
            "server.i18n.content.locales.es.langage=es\n";

    protected void setUp() throws Exception {
        super.setUp();
        FactoryUtil.setDefaultImplementation(Content2BeanTransformer.class, Content2BeanTransformerImpl.class);
        FactoryUtil.setDefaultImplementation(Content2BeanProcessor.class, Content2BeanProcessorImpl.class);
        FactoryUtil.setDefaultImplementation(TransformationState.class, TransformationStateImpl.class);
        FactoryUtil.setDefaultImplementation(TypeMapping.class, TypeMappingImpl.class);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        MgnlContext.setInstance(null);
        FactoryUtil.clear();
    }

    public void testUpdatesProperlyWhenAlreadyExisting() throws Exception {
        final MockHierarchyManager hm = MockUtil.createHierarchyManager(CONTENT_AS_IN_30);
        final InstallContext ctx = createStrictMock(InstallContext.class);
        expect(ctx.getConfigHierarchyManager()).andReturn(hm);
        final Context mgnlCtx = createStrictMock(Context.class);

        replay(mgnlCtx, ctx);

        MgnlContext.setInstance(mgnlCtx);
        final UpdateI18nConfiguration.UpdateFrom30 task = new UpdateI18nConfiguration.UpdateFrom30() {
            protected void doBootstrap(InstallContext ctx) throws TaskExecutionException {
                final ByteArrayInputStream propertiesStream = new ByteArrayInputStream(BOOTSTRAPPED_CONTENT_FOR_35.getBytes());
                try {
                    MockUtil.createContent(hm.getRoot(), propertiesStream);
                } catch (IOException e) {
                    throw new RuntimeException(e); // TODO
                } catch (RepositoryException e) {
                    throw new RuntimeException(e); // TODO
                }
            }
        };
        task.execute(ctx);
        verify(mgnlCtx, ctx);

        final Content i18nNode = hm.getContent("/server/i18n");
        assertEquals("mgnl:content", i18nNode.getNodeTypeName());
        assertFalse(i18nNode.hasNodeData("language"));
        assertFalse(i18nNode.hasContent("availableLanguages"));
        final Content systemNode = i18nNode.getContent("system");
        assertEquals("zz", systemNode.getNodeData("fallbackLanguage").getString());
        final Content languagesNode = systemNode.getContent("languages");
        assertEquals("false", languagesNode.getContent("ru").getNodeData("enabled").getString());
        assertEquals("false", languagesNode.getContent("zh_CN").getNodeData("enabled").getString());
        assertEquals("true", languagesNode.getContent("fr_BE").getNodeData("enabled").getString());
        assertEquals("true", languagesNode.getContent("en").getNodeData("enabled").getString());
        assertEquals("true", languagesNode.getContent("es").getNodeData("enabled").getString());

        // just a lazy assertion on the total number of properties after the task was executed
        final Properties hmAsProps = PropertiesImportExport.toProperties(hm);
        assertEquals(25, hmAsProps.size());
        assertEquals(null, hmAsProps.get("/server/i18n.language"));
        assertEquals(null, hmAsProps.get("/server/i18n/availableLanguages"));

    }
}
