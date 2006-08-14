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

import info.magnolia.cms.beans.config.FilterManager.FilterDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class FilterManagerTest extends TestCase {

    public void testSort() {

        List filterDefinitions = new ArrayList();

        FilterDefinition definition = new FilterDefinition();
        definition.setClassName("a");
        definition.setPriority(200);
        filterDefinitions.add(definition);

        definition = new FilterDefinition();
        definition.setClassName("b");
        definition.setPriority(100);
        filterDefinitions.add(definition);

        definition = new FilterDefinition();
        definition.setClassName("c");
        definition.setPriority(300);
        filterDefinitions.add(definition);

        Collections.sort(filterDefinitions);

        FilterDefinition[] defs = (FilterDefinition[]) filterDefinitions.toArray(new FilterDefinition[filterDefinitions
            .size()]);

        assertEquals("b", defs[0].getClassName());
        assertEquals("a", defs[1].getClassName());
        assertEquals("c", defs[2].getClassName());
    }
}
