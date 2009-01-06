/**
 * This file Copyright (c) 2007-2009 Magnolia International
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
package info.magnolia.setup.for3_5;

import info.magnolia.importexport.PropertiesImportExport;
import info.magnolia.module.InstallContext;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.voting.voters.URIStartsWithVoter;
import static org.easymock.EasyMock.*;

import java.util.Properties;


/**
 * @author vsteller
 * @version $Id$
 *
 */
public class CheckAndUpdateExistingFiltersTest extends MgnlTestCase {
    
    public void testWarnIfFilterDidNotExistIn30() throws Exception {
        final String testContent = "" +
                "/server/filters/customFilter.class=any.filter.Clazz\n" +
                "/server/filters/customFilter.priority=1\n";

        final MockHierarchyManager hm = MockUtil.createHierarchyManager(testContent);
        final InstallContext ctx = createStrictMock(InstallContext.class);
        expect(ctx.getHierarchyManager("config")).andReturn(hm);
        ctx.warn(contains("not existing"));
        
        replay(ctx);
        final CheckAndUpdateExistingFilters task = new CheckAndUpdateExistingFilters("/server/filters");
        task.execute(ctx);
        verify(ctx);
    }
    
    public void testWarnIfFilterClassHasBeenModified() throws Exception {
        final String testContent = "" +
            "/server/filters/contentType.class=another.filter.Clazz\n" +
            "/server/filters/contentType.priority=long:100\n";

        final MockHierarchyManager hm = MockUtil.createHierarchyManager(testContent);
        final InstallContext ctx = createStrictMock(InstallContext.class);
        expect(ctx.getHierarchyManager("config")).andReturn(hm);
        ctx.warn(contains("modified"));
        
        replay(ctx);
        final CheckAndUpdateExistingFilters task = new CheckAndUpdateExistingFilters("/server/filters");
        task.execute(ctx);
        verify(ctx);
    }
    
    public void testWarnIfFilterPriorityHasBeenModified() throws Exception {
        final String testContent = "" +
            "/server/filters/contentType.class=info.magnolia.cms.filters.ContentTypeFilter\n" +
            "/server/filters/contentType.priority=long:200\n";

        final MockHierarchyManager hm = MockUtil.createHierarchyManager(testContent);
        final InstallContext ctx = createStrictMock(InstallContext.class);
        expect(ctx.getHierarchyManager("config")).andReturn(hm);
        ctx.warn(contains("modified"));
        
        replay(ctx);
        final CheckAndUpdateExistingFilters task = new CheckAndUpdateExistingFilters("/server/filters");
        task.execute(ctx);
        verify(ctx);
    }
    
    public void testWarnIfFilterParamsHaveBeenModified() throws Exception {
        final String testContent = "" +
            "/server/filters/intercept.class=info.magnolia.cms.filters.MgnlInterceptFilter\n" +
            "/server/filters/intercept.priority=long:600\n" + 
            "/server/filters/intercept/params/test=myCustomization";
    
        final MockHierarchyManager hm = MockUtil.createHierarchyManager(testContent);
        final InstallContext ctx = createStrictMock(InstallContext.class);
        expect(ctx.getHierarchyManager("config")).andReturn(hm);
        ctx.warn(contains("modified"));
        
        replay(ctx);
        final CheckAndUpdateExistingFilters task = new CheckAndUpdateExistingFilters("/server/filters");
        task.execute(ctx);
        verify(ctx);
    }

    public void testProperTransformationIfFilterBypassHasBeenModified() throws Exception {
        final String testContent = "" +
            "/server/filters/cms.class=info.magnolia.cms.filters.MgnlCmsFilter\n" +
            "/server/install/backup/filters/cms.class=info.magnolia.cms.filters.MgnlCmsFilter\n" +
            "/server/install/backup/filters/cms.priority=long:800\n" + 
            "/server/install/backup/filters/cms/config.bypass=string:/.,/myCustomization";

        final MockHierarchyManager hm = MockUtil.createHierarchyManager(testContent);
        final InstallContext ctx = createStrictMock(InstallContext.class);
        expect(ctx.getHierarchyManager("config")).andReturn(hm);
        ctx.info(contains("different bypass"));
        expect(ctx.getConfigHierarchyManager()).andReturn(hm).times(2);

        replay(ctx);
        final CheckAndUpdateExistingFilters task = new CheckAndUpdateExistingFilters("/server/install/backup/filters");
        task.execute(ctx);
        verify(ctx);
        
        final Properties hmProps = PropertiesImportExport.toProperties(hm);
        assertEquals(hmProps.get("/server/filters/cms/bypasses/dot.class"), URIStartsWithVoter.class.getName());
        assertEquals(hmProps.get("/server/filters/cms/bypasses/dot.pattern"), "/.");
        assertEquals(hmProps.get("/server/filters/cms/bypasses/myCustomization.class"), URIStartsWithVoter.class.getName());
        assertEquals(hmProps.get("/server/filters/cms/bypasses/myCustomization.pattern"), "/myCustomization");
        
    }
    
    public void testDoNotWarnIfFilterHasDefault30Configuration() throws Exception {
        final String testContent = "" +
                "/server/filters/contentType.class=info.magnolia.cms.filters.ContentTypeFilter\n" +
                "/server/filters/contentType.priority=long:100\n";

        final MockHierarchyManager hm = MockUtil.createHierarchyManager(testContent);
        final InstallContext ctx = createStrictMock(InstallContext.class);
        expect(ctx.getHierarchyManager("config")).andReturn(hm);
        
        replay(ctx);
        final CheckAndUpdateExistingFilters task = new CheckAndUpdateExistingFilters("/server/filters");
        task.execute(ctx);
        verify(ctx);
    }
    
}
