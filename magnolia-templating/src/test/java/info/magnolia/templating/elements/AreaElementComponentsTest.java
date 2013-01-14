/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.templating.elements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.ComponentAvailability;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.template.configured.ConfiguredAreaDefinition;
import info.magnolia.rendering.template.configured.ConfiguredComponentAvailability;
import info.magnolia.test.ComponentsTestUtil;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

public class AreaElementComponentsTest extends AbstractElementTestCase {


    private AreaElement element;
    private ConfiguredAreaDefinition areaDefinition;
    private ServerConfiguration serverCfg;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Init ServerConfiguration
        serverCfg = new ServerConfiguration();
        serverCfg.setAdmin(true);
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverCfg);

        element = new AreaElement(serverCfg, getContext(), getEngine());

        areaDefinition = new ConfiguredAreaDefinition();
        getTemplateDefinition().addArea("stage", areaDefinition);
        areaDefinition.setDialog("dialog_testCnfTemplate");
        areaDefinition.setType("type_testCnfTemplate");
        areaDefinition.setTitle("title_testCnfTemplate");
        Map<String, ComponentAvailability> availableComponents = new HashMap<String, ComponentAvailability>();
        ConfiguredComponentAvailability componentAvailability = new ConfiguredComponentAvailability();
        componentAvailability.setEnabled(true);
        componentAvailability.setId("componentAvailability_Id");
        availableComponents.put("componentAvailability", componentAvailability);
        areaDefinition.setAvailableComponents(availableComponents);

        TemplateDefinitionAssignment assignment = mock(TemplateDefinitionAssignment.class);
        TemplateDefinition definition = mock(TemplateDefinition.class);

        ComponentsTestUtil.setInstance(RenderingContext.class, mock(RenderingContext.class));
        ComponentsTestUtil.setInstance(TemplateDefinitionAssignment.class, assignment);

        when(assignment.getAssignedTemplateDefinition((Node)anyObject())).thenReturn(definition);
    }

    @Test
    public void testMaxComponentsNotReached() throws Exception {
        // GIVEN
        Node component = this.getComponentNode();
        component.addNode("00", MgnlNodeType.NT_COMPONENT);
        component.addNode("01", MgnlNodeType.NT_COMPONENT);
        areaDefinition.setMaxComponents(3);
        element.setName("stage");
        element.setContent(getComponentNode());

        // WHEN
        element.begin(out);
        element.end(out);
        String output = out.toString();
        // THEN
        assertTrue(output.contains("showAddButton=\"true\""));
        assertTrue(output.contains("cms:component content=\"website:/foo/bar/paragraphs/0/00\""));
        assertTrue(output.contains("cms:component content=\"website:/foo/bar/paragraphs/0/01\""));
//        printOutput();
    }

    @Test
    public void testMaxComponentsReached() throws Exception {
        // GIVEN
        Node component = this.getComponentNode();
        component.addNode("00", MgnlNodeType.NT_COMPONENT);
        Node component2 = component.addNode("01", MgnlNodeType.NT_COMPONENT);
        component2.setProperty("enabled", "true");
        Node component3 = component.addNode("02", MgnlNodeType.NT_COMPONENT);
        component3.setProperty("enabled", "false");
        areaDefinition.setMaxComponents(2);
        element.setName("stage");
        element.setContent(getComponentNode());

        // WHEN
        element.begin(out);
        element.end(out);
        String output = out.toString();

        // THEN
        assertTrue(output.contains("showAddButton=\"false\""));
        assertTrue(output.contains("cms:component content=\"website:/foo/bar/paragraphs/0/00\""));
        assertTrue(output.contains("cms:component content=\"website:/foo/bar/paragraphs/0/01\""));
        assertFalse(output.contains("cms:component content=\"website:/foo/bar/paragraphs/0/02\""));
//        printOutput();
    }

    @Test
    public void testMaxComponentsNotSet() throws Exception {
        // GIVEN
        Node component = this.getComponentNode();
        component.addNode("00", MgnlNodeType.NT_COMPONENT);
        Node component2 = component.addNode("01", MgnlNodeType.NT_COMPONENT);
        component2.setProperty("enabled", "true");
        Node component3 = component.addNode("02", MgnlNodeType.NT_COMPONENT);
        component3.setProperty("enabled", "false");

        element.setName("stage");
        element.setContent(getComponentNode());
        // WHEN
        element.begin(out);
        element.end(out);
        String output = out.toString();

        // THEN
        assertTrue(output.contains("showAddButton=\"true\""));
        assertTrue(output.contains("cms:component content=\"website:/foo/bar/paragraphs/0/00\""));
        assertTrue(output.contains("cms:component content=\"website:/foo/bar/paragraphs/0/01\""));
        assertTrue(output.contains("cms:component content=\"website:/foo/bar/paragraphs/0/02\""));
//        printOutput();
    }

    @Test
    public void testShouldShowAddButtonTypeNoComponent() throws Exception {
        // GIVEN
        element.setName("stage");
        element.setType(AreaDefinition.TYPE_NO_COMPONENT);
        // WHEN
        element.begin(out);
        String output = out.toString();
        // THEN
        assertTrue(output.contains("showAddButton=\"false\""));
//        printOutput();
    }

    @Test
    public void testShouldShowAddButtonTypeSingle() throws Exception {
        // GIVEN
        element.setName("stage");
        element.setType(AreaDefinition.TYPE_SINGLE);
        // WHEN
        element.begin(out);
        String output = out.toString();
        // THEN
        assertTrue(output.contains("showAddButton=\"true\""));

        // GIVEN
        element.setType(AreaDefinition.TYPE_SINGLE);
        Node component = this.getComponentNode();
        component.addNode("00", MgnlNodeType.NT_COMPONENT);
        element.setContent(getComponentNode());
        // WHEN
        element.begin(out);
        output = out.toString();
        // THEN
        assertTrue(output.contains("showAddButton=\"false\""));

//        printOutput();
    }



    private void printOutput() {
        System.out.println(out);
    }
}
