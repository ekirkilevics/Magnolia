/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.cms.beans.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * An extension of {@link RegexpVirtualURIMapping} that allows a rotation between different destination urls. In order
 * to rotate <code>toURI</code> must contain the <code>*</code> that will be replaced by a random number between
 * <code>start</code> (default is <code>1</code>) and <code>end</code> (defaults is <code>3</code>).
 * </p>
 * <p>
 * An additional property <code>padding</code> can specify the left 0 padding for numbers (defaults is <code>2</code>).
 * So for example a destination url like <code>forward:/banner/image_*.jpg</code> will randomly forward the request to
 * <code>/banner/image_01.jpg</code>, <code>/banner/image_02.jpg</code> or <code>/banner/image_03.jpg</code>
 * </p>
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class RotatingVirtualURIMapping extends RegexpVirtualURIMapping {

    private static final Logger log = LoggerFactory.getLogger(RotatingVirtualURIMapping.class);
    
    /**
     * Placeholder that will be replaced by a random number.
     */
    private static final String RANDOM_PLACEHOLDER = "*";

    /**
     * Lower bound for the generated random number.
     */
    private int start = 1;

    /**
     * Upper bound for the generated random number.
     */
    private int end = 3;

    /**
     * Left padding (using 0s).
     */
    private int padding = 2;

    /**
     * Sets the start.
     * @param start the start to set
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Sets the end.
     * @param end the end to set
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * Sets the padding.
     * @param padding the padding to set
     */
    public void setPadding(int padding) {
        this.padding = padding;
    }

    /**
     * {@inheritDoc}
     */
    public MappingResult mapURI(String uri) {
        // delegate the initial processing to RegexpVirtualURIMapping
        MappingResult mr = super.mapURI(uri);

        if (mr != null) {
            if (end > start) {
                int randomNumber = RandomUtils.nextInt(end - (start - 1)) + start;
                String randomAsString = StringUtils.leftPad(Integer.toString(randomNumber), padding, '0');

                mr.setToURI(StringUtils.replace(mr.getToURI(), RANDOM_PLACEHOLDER, randomAsString));
            }else{
               log.warn("End value must be greater than start value.");
            }
        }
        return mr;
    }
}
