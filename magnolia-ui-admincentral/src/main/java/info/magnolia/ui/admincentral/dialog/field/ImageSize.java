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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.devlib.schmidt.imageinfo.ImageInfo;

/**
 * Represents the size of an image. This class is immutable.
 *
 * @version $Id$
 */
public final class ImageSize {

    private final long width;
    private final long height;

    public ImageSize(long width, long height) {
        this.width = width;
        this.height = height;
    }

    public long getWidth() {
        return width;
    }

    public long getHeight() {
        return height;
    }

    /**
     * Scales the size to fit inside a bounding rectangle keeping the aspect ratio as is. If this image is smaller than
     * the bounding rectangle no change occurs.
     */
    public ImageSize scaleToFitIfLarger(long boundingWidth, long boundingHeight) {

        if (this.width >= this.height && this.width > boundingWidth) {
            double scaleFactor = ((double) boundingWidth) / this.width;
            long scaledHeight = (long) (scaleFactor * this.height);
            return new ImageSize(boundingWidth, scaledHeight);
        }

        if (this.height > this.width && this.height > boundingHeight) {
            double scaleFactor = ((double) boundingHeight) / this.height;
            long scaledWidth = (long) (scaleFactor * this.width);
            return new ImageSize(scaledWidth, boundingHeight);
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImageSize)) {
            return false;
        }

        ImageSize imageSize = (ImageSize) o;

        if (height != imageSize.height) {
            return false;
        }
        if (width != imageSize.width) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (width ^ (width >>> 32));
        result = 31 * result + (int) (height ^ (height >>> 32));
        return result;
    }

    public static ImageSize valueOf(File file) throws FileNotFoundException {
        InputStream stream = null;
        try {
            ImageInfo imageInfo = new ImageInfo();
            stream = new FileInputStream(file);
            imageInfo.setInput(stream);
            return imageInfo.check() ? new ImageSize(imageInfo.getWidth(), imageInfo.getHeight()) : null;
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }
}
