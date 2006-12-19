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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.module.ModuleDefinition;
import junit.framework.TestCase;
import org.apache.commons.betwixt.io.BeanReader;
import org.xml.sax.SAXException;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.StringReader;

/**
 * Validates our betwixt config and integration.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleRegistrationBetwixtTest extends TestCase {

    public void testDisplayNameCanBeWrittenWithDashEventhoughThisIsDeprecated() throws Exception {
        String xml = "<module>\n" +
                "  <name>the name</name>\n" +
                "  <display-name>The Display Name</display-name>" +
                "</module>";
        ModuleDefinition mod = readModuleDefinition(xml);
        assertEquals("The Display Name", mod.getDisplayName());
    }

    public void testDisplayNameShouldBeWrittenWithCapitalN() throws Exception {
        String xml = "<module>\n" +
                "  <name>the name</name>\n" +
                "  <displayName>The Display Name</displayName>" +
                "</module>";
        ModuleDefinition mod = readModuleDefinition(xml);
        assertEquals("The Display Name", mod.getDisplayName());
    }

    private ModuleDefinition readModuleDefinition(String xml) throws IntrospectionException, IOException, SAXException {
        BeanReader beanReader = new BeanReader();
        beanReader.registerBeanClass(ModuleDefinition.class);
        ModuleDefinition mod = (ModuleDefinition) beanReader.parse(new StringReader(xml));
        return mod;
    }
}
