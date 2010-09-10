/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.module.wcm.pageeditor.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Stopwatch used for performance test. It supports single time stamps and start/stop. Multiple
 * start/stop operation on the same key are supported.
 * 
 * @version $Id: PerformanceTestStatus.java 32667 2010-03-13 00:37:06Z gjoseph $
 * 
 */
public class StopWatch {
    
    /**
     * Multiple measurements per key are possible.
     */
    private static Map<String, List<Measurement>> states = new LinkedHashMap<String, List<Measurement>>();

    public static void start(String key) {
        getTestStatus(key, true).start = System.currentTimeMillis();
    }

    public static void sample(String key) {
        getTestStatus(key, true).start = System.currentTimeMillis();
    }

    public static void sample(String key, long time) {
        getTestStatus(key, true).start = time;
    }

    private static Measurement getTestStatus(String key, boolean create) {
        if (!states.containsKey(key)) {
            states.put(key, new ArrayList<Measurement>());
        }
        List<Measurement> list = states.get(key);
        Measurement status;
        if (create) {
            status = new Measurement(key);
            list.add(status);
        }
        else {
            status = list.get(list.size() - 1);
        }
        return status;

    }

    public static void stop(String key) {
        Measurement status = getTestStatus(key, false);
        if (status == null) {
            throw new RuntimeException("tried to stop while never started: " + key);
        }
        status.stop += System.currentTimeMillis();
    }

    public static String getReport(long base) {
        ArrayList<ReportItem> reportItems = new ArrayList<ReportItem>();
        for (List<Measurement> list : states.values()) {
            int count = 1;
            for (Measurement testStatus : list) {
                String label = testStatus.key;
                if (list.size() > 1) {
                    label = testStatus.key + " (" + count + ")";
                }

                long startTime = testStatus.start - base;
                long duration = testStatus.stop - testStatus.start;
                long stopTime = testStatus.stop - base;

                reportItems.add(new ReportItem(label + (stopTime > 0 ? " - start" : "") + ": " + startTime + "ms", testStatus.start));

                if (testStatus.stop > 0) {
                    reportItems.add(new ReportItem(
                        label + (stopTime > 0 ? " - stop" : "") + ": " + stopTime + "ms (" + duration + "ms)",
                        testStatus.stop));
                }
                count++;
            }
        }

        Collections.sort(reportItems);

        StringBuffer str = new StringBuffer();
        for (ReportItem reportItem : reportItems) {
            str.append(reportItem.label).append("\n");
        }
        return str.toString();
    }
    
    /**
     * A measurement with a start and optional stop time.
     */
    static class Measurement {

        String key;

        long start = -1;

        long stop = -1;

        public Measurement(String name) {
            this.key = name;
        }
    }

    /**
     * A single item in the report. Sorted by time.
     */
    static class ReportItem implements Comparable<ReportItem> {

        String label;

        long time;

        public ReportItem(String label, long time) {
            this.label = label;
            this.time = time;
        }

        public int compareTo(ReportItem o) {
            return new Long(this.time).compareTo(new Long(o.time));
        }
    }

}
