/**
 * This file Copyright (c) 2013 Magnolia International
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

import info.magnolia.context.MgnlContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Invocation aggregator.
 */
public class TrackingStatus {

    private static final String ATTRIBUTE_NAME = TrackingStatus.class.getName();

    private final Map<String, Long>[] traces = new Map[10];

    private final boolean trace;
    private final Object testString;

    /**
     * Gets instance of the status configured for current request or new one if none is configured
     * yet.
     */
    public static TrackingStatus getInstance() {
        return getInstance(true);
    }

    /**
     * Gets instance of the status configured for current request or new one if none is configured
     * yet.
     * 
     * @param trace used only for the very first invocation to enable/disable this status instance.
     */
    public static TrackingStatus getInstance(boolean trace) {
        return getInstance(true, null);
    }

    /**
     * Gets instance of the status configured for current request or new one if none is configured
     * yet.
     * 
     * @param trace used only for the very first invocation to enable/disable this status instance.
     * @param testString used to further filter the calls to trace. Only calls to
     *            {@link #track(String)} matching the {@link #testString} will be logged.
     */
    public static TrackingStatus getInstance(boolean trace, String testString) {
        if(!MgnlContext.hasAttribute(ATTRIBUTE_NAME)) {
            MgnlContext.setAttribute(ATTRIBUTE_NAME, new TrackingStatus(trace, testString));
        }
        return (TrackingStatus) MgnlContext.getAttribute(ATTRIBUTE_NAME);
    }

    public TrackingStatus(boolean trace, String testString) {
        for (int i = 0; i < traces.length; i++) {
            traces[i] = new HashMap<String, Long>();
        }
        this.trace = trace;
        this.testString = testString;
    }

    /**
     * Track all the callers and update aggregated stacktrace.
     */
    public void track() {
        if (!trace) {
            return;
        }
        StackTraceElement[] trace = new Exception().getStackTrace();
        int count = 0;
        for (StackTraceElement el : trace) {
            String strTrace = el.toString();
            Map<String, Long> traceMap = traces[count];
            Long sum = traceMap.get(strTrace);
            if (sum != null) {
                sum += 1;
            } else {
                sum = 1L;
            }
            traceMap.put(strTrace, sum);
            count++;
            if (count >= traces.length) {
                break;
            }
        }
    }

    @Override
    /**
     * Generates aggregated tracktrace from all captured calls including count of invocations.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (totalInits > 0) {
            sb.append("inits: ").append(totalInits).append(",");
        }
        if (totalCalls > 0) {
            sb.append(", total calls: ").append(totalCalls).append(",");

        }
        if (totalTime > 0) {
            sb.append("total time: ").append(totalTime);

        }
        sb.append("\n");
        String ind = "  ";
        for (Map<String, Long> trace : traces) {
            for (Map.Entry<String, Long> entry : trace.entrySet()) {
                sb.append(ind).append(entry.getValue()).append(";").append(entry.getKey()).append("\n");
            }
            ind += "  ";
        }
        return sb.toString();
    }

    public long totalCalls;
    public long totalInits;
    public long totalTime;

    /**
     * Track only calls where {@link #testString} matches the argument.
     */
    public void track(String str) {
        if (testString == null || testString.equals(str)) {
            track();
        }
    }
}
