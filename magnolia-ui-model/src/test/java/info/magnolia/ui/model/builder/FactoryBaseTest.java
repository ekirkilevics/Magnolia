/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.model.builder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import info.magnolia.test.mock.MockSimpleComponentProvider;

import org.junit.Test;

public class FactoryBaseTest {

    public static class Def extends SuperclassOfDef {
    }

    public static class Impl extends SuperclassOfImpl {

        public Impl(Def ignored) {
            super(ignored);
        }

        // TODO: check whether impl in FactoryBase is tolerant enough - when removing the following constructor, it'll
        // not be able to create an Impl from a OtherSubclassOfDef although the constructor above is compile-wise satisfying - new Impl(otherSubclassOfDef);
        public Impl(OtherSubclassOfDef ignored) {
            super(ignored);
        }
    }

    public static class SubclassOfDef extends Def {
    }

    public static class SubclassOfImpl extends Impl {
        public SubclassOfImpl(SubclassOfDef ignored) {
            super(ignored);
        }
    }

    public class OtherSubclassOfDef extends Def {
    }

    public static class SuperclassOfDef {
    }

    public static class SuperclassOfImpl {
        public SuperclassOfImpl(SuperclassOfDef ignored) {
        }
    }

    @Test
    public void testCreate() {
        DummyFactory factoryBase = new DummyFactory(new MockSimpleComponentProvider());
        Def def = new Def();
        SuperclassOfImpl impl = factoryBase.create(def);
        assertNotNull(impl);
        assertTrue(impl instanceof Impl);

        SubclassOfDef subclassOfDef = new SubclassOfDef();
        SuperclassOfImpl anotherImpl = factoryBase.create(subclassOfDef);
        assertNotNull(anotherImpl);
        assertTrue(anotherImpl instanceof SubclassOfImpl);

        // SubclassOfSubclassOfDef is actually not registered - so registration from Hierarchy should be used
        OtherSubclassOfDef otherSubclassOfDef = new OtherSubclassOfDef();

        SuperclassOfImpl yetAnotherImpl = factoryBase.create(otherSubclassOfDef);
        assertNotNull(yetAnotherImpl);
    }
}
