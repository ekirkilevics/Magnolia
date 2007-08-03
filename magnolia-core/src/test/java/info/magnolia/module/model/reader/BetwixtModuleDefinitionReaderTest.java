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
package info.magnolia.module.model.reader;

import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.model.DependencyDefinition;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.VersionRange;
import info.magnolia.module.model.VersionTest;
import junit.framework.TestCase;

import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BetwixtModuleDefinitionReaderTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
        // shunt Digester logging ...
        org.apache.log4j.Logger.getLogger("org.apache.commons.digester.Digester").setLevel(org.apache.log4j.Level.OFF);
    }

    public void testDisplayNameCanBeWrittenWithDashEventhoughThisIsDeprecated() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<module>\n" +
                "  <name>the name</name>\n" +
                "  <display-name>The Display Name</display-name>" +
                "  <class>foo</class>\n" +
                "  <versionHandler>java.lang.String</versionHandler>\n" +
                "  <version>1.0</version>\n" +
                "</module>";
        ModuleDefinition mod = new BetwixtModuleDefinitionReader().read(new StringReader(xml));
        assertEquals("The Display Name", mod.getDisplayName());
    }

    public void testDisplayNameShouldBeWrittenWithCapitalN() throws Exception {
        String xml = "<module>\n" +
                "  <name>the name</name>\n" +
                "  <displayName>The Display Name</displayName>\n" +
                "  <class>foo</class>\n" +
                "  <versionHandler>java.lang.String</versionHandler>\n" +
                "  <version>1.0</version>\n" +
                "</module>";
        ModuleDefinition mod = new BetwixtModuleDefinitionReader().read(new StringReader(xml));
        assertEquals("The Display Name", mod.getDisplayName());
    }

    public void testClassIsResolvedToClassNameAsAString() throws Exception {
        String xml = "<module>\n" +
                "  <name>the name</name>\n" +
                "  <class>java.lang.Integer</class>\n" +
                "  <versionHandler>java.lang.String</versionHandler>\n" +
                "  <version>1.0</version>\n" +
                "</module>";
        ModuleDefinition mod = new BetwixtModuleDefinitionReader().read(new StringReader(xml));
        assertTrue(mod.getClassName() instanceof String);
        assertEquals("java.lang.Integer", mod.getClassName());
    }

    public void testVersionHandlerIsResolvedToAClass() throws Exception {
        String xml = "<module>\n" +
                "  <name>the name</name>\n" +
                "  <class>java.lang.Integer</class>\n" +
                "  <versionHandler>java.lang.String</versionHandler>" +
                "  <version>1.0</version>\n" +
                "</module>";
        ModuleDefinition mod = new BetwixtModuleDefinitionReader().read(new StringReader(xml));
        assertEquals(String.class, mod.getVersionHandler());
    }

    public void testModuleVersionIsProperlyRead() throws ModuleManagementException {
        String xml = "<module>\n" +
                "  <name>the name</name>\n" +
                "  <class>foo</class>\n" +
                "  <versionHandler>java.lang.String</versionHandler>\n" +
                "  <version>1.2.3</version>\n" +
                "</module>";
        final ModuleDefinition def = new BetwixtModuleDefinitionReader().read(new StringReader(xml));

        final Version version = def.getVersionDefinition();
        assertNotNull(version);
        assertEquals("1.2.3", version.toString());
        VersionTest.assertVersion(1, 2, 3, null, version);
        assertEquals("1.2.3", def.getVersion());
    }

    public void testDependenciesVersionAreProperlyRead() throws ModuleManagementException {
        //org.apache.log4j.Logger.getLogger("org.apache").setLevel(org.apache.log4j.Level.DEBUG);
        String xml = "<module>\n" +
                "  <name>myName</name>\n" +
                "  <class>foo</class>\n" +
                "  <versionHandler>java.lang.String</versionHandler>\n" +
                "  <version>1.2.3</version>\n" +
                "  <dependencies>\n" +
                "    <dependency>\n" +
                "      <name>foo</name>\n" +
                "      <version>2.3.4/*</version>\n" +
                "      <optional>true</optional>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <name>bar</name>\n" +
                "      <version>5.6.7/8.9.0</version>\n" +
                "    </dependency>\n" +
                "  </dependencies>\n" +
                "</module>";
        final ModuleDefinition def = new BetwixtModuleDefinitionReader().read(new StringReader(xml));
        final Collection deps = def.getDependencies();
        assertEquals(2, deps.size());
        final Iterator it = deps.iterator();

        final DependencyDefinition dep1 = (DependencyDefinition) it.next();
        final DependencyDefinition dep2 = (DependencyDefinition) it.next();
        assertEquals("foo", dep1.getName());
        assertEquals(true, dep1.isOptional());
        assertEquals("2.3.4/*", dep1.getVersion());
        final VersionRange dep1versionRange = dep1.getVersionRange();
        assertNotNull(dep1versionRange);
        assertEquals("2.3.4/*", dep1versionRange.toString());
        VersionTest.assertVersion(2, 3, 4, null, dep1versionRange.getFrom());
        assertEquals(Version.UNDEFINED_TO, dep1versionRange.getTo());

        assertEquals("bar", dep2.getName());
        assertEquals(false, dep2.isOptional());
        assertEquals("5.6.7/8.9.0", dep2.getVersion());
        final VersionRange dep2versionRange = dep2.getVersionRange();
        assertNotNull(dep2versionRange);
        assertEquals("5.6.7/8.9.0", dep2versionRange.toString());
        VersionTest.assertVersion(5, 6, 7, null, dep2versionRange.getFrom());
        VersionTest.assertVersion(8, 9, 0, null, dep2versionRange.getTo());
    }

    public void testInvalidXmlIsCheckedAgainstDTD() {
        String xmlWithVersionElementMisplaced = "<module>\n" +
                "  <version>2.3.4</version>\n" +
                "  <name>the name</name>\n" +
                "</module>";
        try {
            new BetwixtModuleDefinitionReader().read(new StringReader(xmlWithVersionElementMisplaced));
            fail("should have failed");
        } catch (Exception e) {
            assertEquals("Invalid module definition file, error at line 6 column 10: The content of element type \"module\" must match \"(name,(displayName|display-name)?,description?,class?,versionHandler?,version,properties?,dependencies?,servlets?,repositories?)\".", e.getMessage());
        }
    }

    public void testGivenDtdIsIgnoredAndCheckedAgainstOurs() {
        String xmlWithWrongDtd = "<!DOCTYPE log4j:configuration SYSTEM \"log4j.dtd\">\n" +
                "<module>\n" +
                "  <version>2.3.4</version>\n" +
                "  <name>the name</name>\n" +
                "</module>";
        try {
            new BetwixtModuleDefinitionReader().read(new StringReader(xmlWithWrongDtd));
            fail("should have failed");
        } catch (Exception e) {
            assertEquals("Invalid module definition file, error at line 6 column 10: The content of element type \"module\" must match \"(name,(displayName|display-name)?,description?,class?,versionHandler?,version,properties?,dependencies?,servlets?,repositories?)\".", e.getMessage());
        }

    }
}
