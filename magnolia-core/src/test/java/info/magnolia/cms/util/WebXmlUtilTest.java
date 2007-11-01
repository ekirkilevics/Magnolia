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

    public void testIsFilterDispatcherConfiguredNoFilter() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_nofilter.xml"));
        assertTrue(util.isFilterDispatcherConfigured(MgnlMainFilter.class.getName()));
    }
}
