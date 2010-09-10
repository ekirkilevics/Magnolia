/**
 * This file Copyright (c) 2010 Magnolia International
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


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;


/**
 * Currently exists test wise. So that we see when GWT is launched.
 * @author pbaerfuss
 * @version $Id$
 *
 */
public class PageEditorEntryPoint implements EntryPoint {

    // can get set by a patched ApplicationConfigugration to make performance tests
    public static long startTime = -1;

    public void onModuleLoad() {

        if(Window.Location.getParameter("performanceTest") != null){
            // if not yet set
            if(startTime == -1){
                // window.startTime
                startTime = (long)getStartTime();
                if(startTime == -1){
                    startTime = System.currentTimeMillis();
                }
            }
            performanceTest();
        }
    }

    private void performanceTest() {
        StopWatch.sample("entry point");
        new Timer() {

            @Override
            public void run() {
                StopWatch.sample("start", (long) getStartTime());
                StopWatch.sample("onload", (long)getOnLoadEvent());

                String str = StopWatch.getReport(startTime);
                String[] lines = str.split("\n");
                StringBuffer table = new StringBuffer();
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    table.append("<tr><td>").append(line.replace(":","</td><td>")).append("</td></tr>");
                }
                HTML report = new HTML("<table>" + table + "</table>");
                DialogBox popup = new DialogBox();
                popup.setModal(false);
                popup.setText("Report");
                popup.add(report);
                popup.setPopupPosition(300, 300);
                popup.show();
            }
        }.schedule(5000);
    }

    native double getStartTime() /*-{
        return $wnd.startTime?$wnd.stopWatchBase:-1;
    }-*/;

    native double getOnLoadEvent() /*-{
        return $wnd.onLoadEventTime?$wnd.onLoadEvent:-1;
    }-*/;


}
