package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.SystemProperty;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class PropertiesInitializerTest extends TestCase {

    public void testSimpleProperty() throws Exception {
        PropertiesInitializer pi = PropertiesInitializer.getInstance();

        URL propertyUrl = this.getClass().getResource("/test-magnolia.properties");
        String propertyDir = new File(propertyUrl.toURI().getRawPath()).getParent();

        pi.loadAllProperties("test-magnolia.properties", propertyDir);

        assertEquals("property", SystemProperty.getProperty("test.one"));
    }

    public void testNestedProperty() throws Exception {
        PropertiesInitializer pi = PropertiesInitializer.getInstance();

        URL propertyUrl = this.getClass().getResource("/test-magnolia.properties");
        String propertyDir = new File(propertyUrl.toURI().getRawPath()).getParent();

        pi.loadAllProperties("test-magnolia.properties", propertyDir);

        assertEquals("nested property", SystemProperty.getProperty("test.two"));
    }

    public void testNestedPropertyMoreLevels() throws Exception {
        PropertiesInitializer pi = PropertiesInitializer.getInstance();

        URL propertyUrl = this.getClass().getResource("/test-magnolia.properties");
        String propertyDir = new File(propertyUrl.toURI().getRawPath()).getParent();

        pi.loadAllProperties("test-magnolia.properties", propertyDir);

        assertEquals("another nested property", SystemProperty.getProperty("test.three"));
    }

    public void testCircularProperty() throws Exception {
        PropertiesInitializer pi = PropertiesInitializer.getInstance();

        URL propertyUrl = this.getClass().getResource("/test-magnolia.properties");
        String propertyDir = new File(propertyUrl.toURI().getRawPath()).getParent();

        pi.loadAllProperties("test-magnolia.properties", propertyDir);

        assertEquals("${test.circular2}", SystemProperty.getProperty("test.circular1"));
        assertEquals("${test.circular1}", SystemProperty.getProperty("test.circular2"));
    }

    public void testSelfReferencingProperty() throws Exception {
        PropertiesInitializer pi = PropertiesInitializer.getInstance();

        URL propertyUrl = this.getClass().getResource("/test-magnolia.properties");
        String propertyDir = new File(propertyUrl.toURI().getRawPath()).getParent();

        pi.loadAllProperties("test-magnolia.properties", propertyDir);

        assertEquals("${test.circular3}", SystemProperty.getProperty("test.circular3"));
    }
}
