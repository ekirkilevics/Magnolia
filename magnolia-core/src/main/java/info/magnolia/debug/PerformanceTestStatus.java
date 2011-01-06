/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.debug;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.LinkedMap;

import info.magnolia.context.MgnlContext;

/**
 * Key based time measuring.
 *
 * @author philipp
 * @version $Id$
 *
 */
public class PerformanceTestStatus {

    private static final String ATTRIBUTE_NAME = PerformanceTestStatus.class.getName();

    /**
     * Simple place holder for the start and total execution times.
     */
    class TestStatus{
       long start = -1;
       long total = -1;
    }

    Map states = MapUtils.lazyMap(new LinkedMap(), new Factory() {
        public Object create() {
            return new TestStatus();
        }
    });

    public void start(String key){
       getTestStatus(key).start = System.currentTimeMillis();
    }

    private TestStatus getTestStatus(String key) {
        return ((TestStatus)states.get(key));
    }

    public void stop(String key){
        TestStatus status = getTestStatus(key);
        status.total += System.currentTimeMillis() - status.start;
    }

    public static PerformanceTestStatus getInstance() {
        if(!MgnlContext.hasAttribute(ATTRIBUTE_NAME)) {
            MgnlContext.setAttribute(ATTRIBUTE_NAME, new PerformanceTestStatus());
        }
        return (PerformanceTestStatus) MgnlContext.getAttribute(ATTRIBUTE_NAME);
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        final Iterator it = states.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            final TestStatus testStatus = (TestStatus) entry.getValue();
            final String value = testStatus.total >= 0 ? String.valueOf(testStatus.total) : "(not stopped yet)";
            str.append(entry.getKey());
            str.append(": ");
            str.append(value);
            if (it.hasNext()) {
                str.append(", ");
            }
        }
        return str.toString();
    }

}
