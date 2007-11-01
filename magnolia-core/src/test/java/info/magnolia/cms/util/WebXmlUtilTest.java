package info.magnolia.cms.util;

import info.magnolia.cms.filters.MgnlMainFilter;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class WebXmlUtilTest extends TestCase {

    public void testFilterDispatcherChecksShouldNotFailWithCorrectConfiguration() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterok.xml"));
        assertTrue(util.areFilterDispatchersConfiguredProperly(MgnlMainFilter.class.getName(), Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
    }

    public void testFilterDispatcherChecksShouldFailIfDispatcherNotSet() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filternodispatcher.xml"));
        assertFalse(util.areFilterDispatchersConfiguredProperly(MgnlMainFilter.class.getName(), Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
    }

    public void testFilterDispatcherChecksShouldNotFailIfFilterNotRegistered() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_nofilter.xml"));
        assertTrue(util.areFilterDispatchersConfiguredProperly(MgnlMainFilter.class.getName(), Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
    }

    public void testFilterDispatcherChecksShouldFailIfWrongDispatchersAreUsed() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterwrongdispatchers.xml"));
        assertEquals(false, util.areFilterDispatchersConfiguredProperly("webxmltest.WithMissingForward", Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
        assertEquals(false, util.areFilterDispatchersConfiguredProperly("webxmltest.WithInclude", Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
    }

    public void testFilterDispatcherErrorIsNotMandatory() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterwrongdispatchers.xml"));
        assertEquals(true, util.areFilterDispatchersConfiguredProperly("webxmltest.ErrorIsNotMandatory", Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
    }

    public void testFilterDispatcherOrderIsIrrelevant() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterwrongdispatchers.xml"));
        assertEquals(true, util.areFilterDispatchersConfiguredProperly("webxmltest.OrderIsIrrelevant", Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
    }

    public void testCanDetectFilterRegistration() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterwrongdispatchers.xml"));
        assertEquals(true, util.isFilterRegistered("webxmltest.OrderIsIrrelevant"));
        assertEquals(false, util.isFilterRegistered("nonregistered.BlehFilter"));
    }

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
