/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.*;
import info.magnolia.context.MgnlContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.template.ComponentAvailability;
import info.magnolia.rendering.template.configured.ConfiguredAreaDefinition;
import info.magnolia.rendering.template.configured.ConfiguredComponentAvailability;
import info.magnolia.rendering.template.configured.ConfiguredInheritance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for AreaMarker.
 *
 * @version $Id$
 */
public class AreaElementTest extends AbstractElementTestCase {


    private AreaElement element;
    private ConfiguredAreaDefinition areaDefinition;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        element = new AreaElement(getServerCfg(), getContext(), getEngine());


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
    }



    @Test
    public void testBeginSimple() throws Exception {
        // GIVEN
        ConfiguredAreaDefinition areaDefinition = new ConfiguredAreaDefinition();
        element.setName("name_testSimple");
        element.setArea(areaDefinition);
        element.setDialog("dialog_testSimple");
        element.setType("type_testSimple");
        element.setLabel("label_testSimple");
        element.setAvailableComponents("availableComponent_testSimple");
        element.setContent(getComponentNode());

        // WHEN
        element.begin(out);
        // THEN
        assertThat(out.toString(),containsString("<!-- cms:area content=\"website:/foo/bar/paragraphs/0\" name=\"name_testSimple\" availableComponents=\"availableComponent_testSimple\" type=\"type_testSimple\" dialog=\"dialog_testSimple\" label=\"label_testSimple\" inherit=\"false\" optional=\"false\" showAddButton=\"true\" description=\"Description\" -->"));
    }


    @Test
    public void testBeginWithConfFromTemplateDefinition() throws Exception {
        // GIVEN
        element.setName("stage");
        element.setContent(getComponentNode());

        // WHEN
        element.begin(out);

        // THEN
        assertThat(out.toString(),containsString("<!-- cms:area content=\"website:/foo/bar/paragraphs/0\" name=\"stage\" availableComponents=\"componentAvailability_Id\" type=\"type_testCnfTemplate\" dialog=\"dialog_testCnfTemplate\" label=\"title_testCnfTemplate\" inherit=\"false\" optional=\"false\" showAddButton=\"true\" description=\"Description\" -->"));
    }

    @Test
    public void testBeginWithInheritence() throws Exception {
        // GIVEN
        ConfiguredInheritance inheritanceConfiguration = new ConfiguredInheritance();
        inheritanceConfiguration.setEnabled(true);
        areaDefinition.setInheritance(inheritanceConfiguration);
        element.setName("stage");
        element.setContent(getComponentNode());

        // WHEN
        element.begin(out);

        // THEN
        assertThat(out.toString(),containsString("<!-- cms:area content=\"website:/foo/bar/paragraphs/0\" name=\"stage\" availableComponents=\"componentAvailability_Id\" type=\"type_testCnfTemplate\" dialog=\"dialog_testCnfTemplate\" label=\"title_testCnfTemplate\" inherit=\"true\" optional=\"false\" showAddButton=\"true\" description=\"Description\" -->"));
    }

    @Test
    public void testBeginWithEditable() throws Exception {
        // GIVEN
        element.setName("stage");
        element.setEditable(false);

        // WHEN
        element.begin(out);

        // THEN
        assertThat(out.toString(),containsString("<!-- cms:area content=\"website:/foo/bar/paragraphs/stage\" name=\"stage\" availableComponents=\"componentAvailability_Id\" type=\"type_testCnfTemplate\" dialog=\"dialog_testCnfTemplate\" label=\"title_testCnfTemplate\" inherit=\"false\" editable=\"false\" optional=\"false\" showAddButton=\"true\" description=\"Description\" -->"));
    }

    @Test
    public void testBeginGetContentFromParent() throws Exception {
        // GIVEN
        element.setName("stage");

        // WHEN
        element.begin(out);

        // THEN
        assertThat(out.toString(),containsString("<!-- cms:area content=\"website:/foo/bar/paragraphs/stage\" name=\"stage\" availableComponents=\"componentAvailability_Id\" type=\"type_testCnfTemplate\" dialog=\"dialog_testCnfTemplate\" label=\"title_testCnfTemplate\" inherit=\"false\" optional=\"false\" showAddButton=\"true\" description=\"Description\" -->"));
    }


    @Test
    public void testBeginCreateNode() throws Exception {
        // GIVEN
        element.setName("stage");
        getComponentNode().getParent().getNode("/stage").remove();
        assertTrue("Should no more exist", !getComponentNode().getParent().hasNode("/stage"));

        // WHEN
        element.begin(out);

        // THEN
        assertTrue("Should be created", getComponentNode().getParent().hasNode("/stage"));
        assertThat(out.toString(),containsString("<!-- cms:area content=\"website:/foo/bar/paragraphs/stage\" name=\"stage\" availableComponents=\"componentAvailability_Id\" type=\"type_testCnfTemplate\" dialog=\"dialog_testCnfTemplate\" label=\"title_testCnfTemplate\" inherit=\"false\" optional=\"false\" showAddButton=\"true\" description=\"Description\" -->"));
    }


    @Test
    public void testEndWithConfFromTemplateDefinition() throws Exception {
        // GIVEN
        element.setName("stage");
        element.setContent(getComponentNode());
        assertEquals("Should be null ", null, areaDefinition.getRenderType());
        assertEquals("Should be null ", null, areaDefinition.getI18nBasename());

        element.begin(out);

        // WHEN
        element.end(out);

        // THEN
        assertThat(out.toString(),containsString("<!-- /cms:area -->"));
        assertEquals("Should get the RenderType value from RemplateDefinition ", getTemplateDefinition().getRenderType(), areaDefinition.getRenderType());
        assertEquals("Should get the I18N value from RemplateDefinition ", getTemplateDefinition().getI18nBasename(), areaDefinition.getI18nBasename());
    }

    @Test
    public void testAvailableComponentsForEditor() throws IOException, RenderException {
        // GIVEN
        prepareAreaDefinition();
        element.setName("stage");

        // user role is editor
        ArrayList<String> userRoles = new ArrayList<String>();
        userRoles.add("editor");

        when(MgnlContext.getUser().getAllRoles()).thenReturn(userRoles);

        // WHEN
        element.begin(out);

        // THEN
        assertEquals("Components available to editor should be:", "component_2", element.getAvailableComponents());
    }

         @Test
    public void testAvailableComponentsForSuperuser() throws IOException, RenderException {
        // GIVEN
        prepareAreaDefinition();
        element.setName("stage");

        // user role is superuser
        ArrayList<String> userRoles = new ArrayList<String>();
        userRoles.add("superuser");

        when(MgnlContext.getUser().getAllRoles()).thenReturn(userRoles);

        // WHEN
        element.begin(out);

        // THEN
        assertEquals("Components available to superuser should be:", "component_2,component_1", element.getAvailableComponents());
    }

    private void prepareAreaDefinition() {
        Map<String, ComponentAvailability> availableComponents = new HashMap<String, ComponentAvailability>();

        // component available only to superuser
        ConfiguredComponentAvailability firstComponentAvailability = new ConfiguredComponentAvailability();
        firstComponentAvailability.addRole("superuser");
        firstComponentAvailability.setEnabled(true);
        firstComponentAvailability.setId("component_1");

        // component available to superuser and editor
        ConfiguredComponentAvailability secondComponentAvailability = new ConfiguredComponentAvailability();
        secondComponentAvailability.addRole("superuser");
        secondComponentAvailability.addRole("editor");
        secondComponentAvailability.setEnabled(true);
        secondComponentAvailability.setId("component_2");

        availableComponents.put("component_1", firstComponentAvailability);
        availableComponents.put("component_2", secondComponentAvailability);

        areaDefinition.setAvailableComponents(availableComponents);
    }

}
