/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.dialog.field;

import junit.framework.TestCase;

/**
 * Tests for {@link ImageSize}.
 *
 * @version $Id$
 */
public class ImageSizeTest extends TestCase {

    public void testScaleToFitIfLarger() throws Exception {

        // If it's equal it shouldn't change
        assertEquals(150, 150, new ImageSize(150, 150).scaleToFitIfLarger(150, 150));

        // If it's the same proportions it should equal the bounding rectangle
        assertEquals(200, 300, new ImageSize(400, 600).scaleToFitIfLarger(200, 300));

        // If it's smaller it shouldn't change
        assertEquals(50, 50, new ImageSize(50, 50).scaleToFitIfLarger(150, 150));

        // If it's taller than the height should match and the width be scaled to keep aspect ratio
        assertEquals(30, 150, new ImageSize(100, 500).scaleToFitIfLarger(150, 150));
        assertEquals(30, 150, new ImageSize(1000, 5000).scaleToFitIfLarger(150, 150));

        // If it's wider than the width should match and the height be scaled to keep aspect ratio
        assertEquals(150, 30, new ImageSize(500, 100).scaleToFitIfLarger(150, 150));
        assertEquals(150, 30, new ImageSize(5000, 1000).scaleToFitIfLarger(150, 150));
    }

    private void assertEquals(long expectedWidth, long expectedHeight, ImageSize imageSize) {
        assertEquals(expectedWidth, imageSize.getWidth());
        assertEquals(expectedHeight, imageSize.getHeight());
    }
}
