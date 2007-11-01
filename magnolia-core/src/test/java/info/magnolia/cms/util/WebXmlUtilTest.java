package info.magnolia.cms.util;

import info.magnolia.cms.filters.MgnlMainFilter;
import junit.framework.TestCase;

/**
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class WebXmlUtilTest extends TestCase {

    public void testIsFilterDispatcherConfiguredOk() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterok.xml"));
        assertTrue(util.isFilterDispatcherConfigured(MgnlMainFilter.class.getName()));
    }

    public void testIsFilterDispatcherConfiguredMissing() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filternodispatcher.xml"));
        assertFalse(util.isFilterDispatcherConfigured(MgnlMainFilter.class.getName()));
    }

    public void testFilterDispatcherChecksShouldNotFailIfFilterNotRegistered() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_nofilter.xml"));
        assertTrue(util.isFilterDispatcherConfigured(MgnlMainFilter.class.getName()));
    }

//    public void testFilterDispatcherChecksShouldFailIfWrongDispatchersAreUsed() {
//        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterwrongdispatchers.xml"));
//        assertFalse(util.isFilterDispatcherConfigured(MgnlMainFilter.class.getName()));
//    }

    public void testCanDetectServletRegistration() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterok.xml"));
        assertEquals(true, util.isServletRegistered("test"));
        assertEquals(false, util.isServletRegistered("foobar"));
        assertEquals(true, util.isServletMappingRegistered("test"));
        assertEquals(false, util.isServletMappingRegistered("foobar"));
        assertEquals(true, util.isServletMappingRegistered("test", "/test/*"));
        assertEquals(false, util.isServletMappingRegistered("test", "/bleh/*"));
        assertEquals(false, util.isServletMappingRegistered("foobar", "/bleh/*"));
        assertEquals(true, util.isServletOrMappingRegistered("test"));
        assertEquals(false, util.isServletOrMappingRegistered("foobar"));
    }
}
