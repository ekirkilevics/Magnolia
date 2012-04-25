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
package info.magnolia.cms.i18n;

import static org.mockito.Mockito.mock;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @version $Id$
 */
public class AbstractI18NContentSupportTest {

    @Test
    public void testGetLocaleWorksWhenNotInWebContext() {
        // GIVEN
        final Context ctx = mock(Context.class);
        MgnlContext.setInstance(ctx);

        final AbstractI18nContentSupport contentSupport = new DefaultI18nContentSupport();
        // WHEN
        final Locale result = contentSupport.getLocale();

        // THEN
        assertEquals(contentSupport.getFallbackLocale(), result);
        
        MgnlContext.setInstance(null);
    }
}