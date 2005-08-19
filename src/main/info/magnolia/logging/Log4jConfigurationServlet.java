/*
 * Copyright 1999,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.magnolia.logging;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * A servlet used to dynamically adjust package logging levels while an application is running. NOTE: This servlet is
 * only aware of pre-configured packages and packages that contain objects that have logged at least one message since
 * application startup.
 * <p>
 * web.xml configuration:
 * </p>
 * 
 * <pre>
 * &lt;servlet>
 *   &lt;servlet-name>log4j&lt;/servlet-name>
 *   &lt;display-name>Log4j configuration Servlet&lt;/display-name>
 *   &lt;servlet-class>org.apache.log4j.servlet.ConfigurationServlet&lt;/servlet-class>
 * &lt;/servlet>
 * </pre>
 * 
 * <p>
 * The <code>fragment</code> parameter can be added if you don't want a full xhtml page in output, but only the
 * content of the body tag, so that it can be used in portlets or struts tiles.
 * </p>
 * 
 * <pre>
 * &lt;servlet>
 *   &lt;servlet-name>log4j&lt;/servlet-name>
 *   &lt;display-name>Log4j configuration Servlet&lt;/display-name>
 *   &lt;servlet-class>org.apache.log4j.servlet.ConfigurationServlet&lt;/servlet-class>
 *   &lt;init-param>
 *     &lt;param-name>fragment&lt;/param-name>
 *     &lt;param-value>true&lt;/param-value>
 *   &lt;/init-param>
 * &lt;/servlet>
 * </pre>
 * 
 * @author Luther E. Birdzell lebirdzell@yahoo.com
 * @author Yoav Shapira yoavs@apache.org
 * @author Fabrizio Giustina
 * @since 1.3
 * @version $Revision$ ($Author$)
 */
public class Log4jConfigurationServlet extends HttpServlet {

    /**
     * Stable <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 64182;

    /**
     * The response content type: text/html
     */
    private static final String CONTENT_TYPE = "text/html"; //$NON-NLS-1$

    /**
     * Should not print html head and body?
     */
    private static final String CONFIG_FRAGMENT = "fragment"; //$NON-NLS-1$

    /**
     * The root appender.
     */
    private static final String ROOT = "Root"; //$NON-NLS-1$

    /**
     * The name of the class / package.
     */
    private static final String PARAM_CLASS = "class"; //$NON-NLS-1$

    /**
     * The logging level.
     */
    private static final String PARAM_LEVEL = "level"; //$NON-NLS-1$

    /**
     * Sort by level?
     */
    private static final String PARAM_SORTBYLEVEL = "sortbylevel"; //$NON-NLS-1$

    /**
     * All the log levels.
     */
    private static final String[] LEVELS = new String[]{
        Level.OFF.toString(),
        Level.FATAL.toString(),
        Level.ERROR.toString(),
        Level.WARN.toString(),
        Level.INFO.toString(),
        Level.DEBUG.toString(),
        Level.ALL.toString()};

    /**
     * Don't include html head.
     */
    private boolean isFragment;

    /**
     * Print the status of all current <code>Logger</code> s and an option to change their respective logging levels.
     * @param request a <code>HttpServletRequest</code> value
     * @param response a <code>HttpServletResponse</code> value
     * @exception ServletException if an error occurs
     * @exception IOException if an error occurs
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String sortByLevelParam = request.getParameter(PARAM_SORTBYLEVEL);
        boolean sortByLevel = ("true".equalsIgnoreCase(sortByLevelParam) || "yes".equalsIgnoreCase(sortByLevelParam)); //$NON-NLS-1$ //$NON-NLS-2$

        List loggers = getSortedLoggers(sortByLevel);
        int loggerNum = 0;

        PrintWriter out = response.getWriter();
        if (!isFragment) {
            response.setContentType(CONTENT_TYPE);

            // print title and header
            printHeader(out);
        }

        // print scripts
        out.println("<a href=\"" + request.getRequestURI() + "\">Refresh</a>"); //$NON-NLS-1$ //$NON-NLS-2$

        out.println("<table class=\"log4jtable\">"); //$NON-NLS-1$
        out.println("<thead><tr>"); //$NON-NLS-1$

        out.println("<th title=\"Logger name\">"); //$NON-NLS-1$
        out.println("<a href=\"?" + PARAM_SORTBYLEVEL + "=false\">Class</a>"); //$NON-NLS-1$ //$NON-NLS-2$
        out.println("</th>"); //$NON-NLS-1$

        out.println("<th title=\"Is logging level inherited from parent?\" style=\"text-align:right\" >*</th>"); //$NON-NLS-1$
        out.println("<th title=\"Logger level\">"); //$NON-NLS-1$
        out.println("<a href=\"?" + PARAM_SORTBYLEVEL + "=true\">Level</a>"); //$NON-NLS-1$ //$NON-NLS-2$
        out.println("</th>"); //$NON-NLS-1$

        out.println("</tr></thead>"); //$NON-NLS-1$
        out.println("<tbody>"); //$NON-NLS-1$

        // print the root Logger
        displayLogger(out, Logger.getRootLogger(), loggerNum++);

        // print the rest of the loggers
        Iterator iterator = loggers.iterator();

        while (iterator.hasNext()) {
            displayLogger(out, (Logger) iterator.next(), loggerNum++);
        }

        out.println("</tbody>"); //$NON-NLS-1$
        out.println("</table>"); //$NON-NLS-1$
        out.println("<a href=\"\">Refresh</a>"); //$NON-NLS-1$

        if (!isFragment) {
            out.println("</body></html>"); //$NON-NLS-1$
            out.flush();
            out.close();
        }
    }

    /**
     * Change a <code>Logger</code>'s level, then call <code>doGet</code> to refresh the page.
     * @param request a <code>HttpServletRequest</code> value
     * @param response a <code>HttpServletResponse</code> value
     * @exception ServletException if an error occurs
     * @exception IOException if an error occurs
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String className = request.getParameter(PARAM_CLASS);
        String level = request.getParameter(PARAM_LEVEL);

        if (className != null) {
            setClass(className, level);
        }

        doGet(request, response);
    }

    /**
     * Print a Logger and its current level.
     * @param out the output writer.
     * @param logger the logger to output.
     * @param row the row number in the table this logger will appear in.
     * @param request the servlet request.
     */
    private void displayLogger(PrintWriter out, Logger logger, int row) {
        String color = null;
        String loggerName = (StringUtils.isEmpty(logger.getName()) ? ROOT : logger.getName());

        color = ((row % 2) == 1) ? "even" : "odd"; //$NON-NLS-1$ //$NON-NLS-2$

        out.println("<tr class=\"" + color + "\">"); //$NON-NLS-1$ //$NON-NLS-2$

        // logger
        out.println("<td>"); //$NON-NLS-1$
        out.println(loggerName);
        out.println("</td>"); //$NON-NLS-1$

        // level inherited?
        out.println("<td style=\"text-align:right\">"); //$NON-NLS-1$
        if ((logger.getLevel() == null)) {
            out.println("*"); //$NON-NLS-1$
        }
        out.println("</td>"); //$NON-NLS-1$

        // level and selection
        out.println("<td>"); //$NON-NLS-1$
        out.println("<form action=\"\" method=\"post\">"); //$NON-NLS-1$
        printLevelSelector(out, logger.getEffectiveLevel().toString());
        out.println("<input type=\"hidden\" name=\"" + PARAM_CLASS + "\" value=\"" + loggerName + "\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        out.print("<input type=\"submit\" name=\"Set\" value=\"Set \">"); //$NON-NLS-1$
        out.println("</form>"); //$NON-NLS-1$
        out.println("</td>"); //$NON-NLS-1$

        out.println("</tr>"); //$NON-NLS-1$
    }

    /**
     * Set a logger's level.
     * @param className class name of the logger to set.
     * @param level the level to set the logger to.
     * @return String return message for display.
     */
    private synchronized String setClass(String className, String level) {
        Logger logger = null;

        try {
            logger = (ROOT.equalsIgnoreCase(className) ? Logger.getRootLogger() : Logger.getLogger(className));
            logger.setLevel(Level.toLevel(level));
        }
        catch (Throwable e) {
            System // permetti system.out
            .out.println("ERROR Setting LOG4J Logger:" + e); //$NON-NLS-1$
        }

        return "Message Set For " + (StringUtils.isEmpty(logger.getName()) ? ROOT : logger.getName()); //$NON-NLS-1$
    }

    /**
     * Get a sorted list of all current loggers.
     * @param sortByLevel if <code>true</code> sort loggers by level instead of name.
     * @return List the list of sorted loggers.
     */
    private List getSortedLoggers(boolean sortByLevel) {
        Enumeration enm = LogManager.getCurrentLoggers();
        Comparator comp = new LoggerComparator(sortByLevel);
        List list = new ArrayList();

        // Add all current loggers to the list
        while (enm.hasMoreElements()) {
            list.add(enm.nextElement());
        }

        // sort the loggers
        Collections.sort(list, comp);

        return list;
    }

    /**
     * Prints the page header.
     * @param out The output writer
     * @param request The request
     */
    private void printHeader(PrintWriter out) {
        out.println("<html><head><title>Log4J Control Console</title>"); //$NON-NLS-1$

        out.println("<style type=\"text/css\">"); //$NON-NLS-1$
        out.println("body{ background-color:#fff; }"); //$NON-NLS-1$
        out.println("body, td, th, select, input{ font-family:Verdana, Geneva, Arial, sans-serif; font-size: 8pt;}"); //$NON-NLS-1$
        out.println("select, input{ border: 1px solid #ccc;}"); //$NON-NLS-1$
        out.println("table.log4jtable, table.log4jtable td { border-collapse:collapse; border: 1px solid #ccc; "); //$NON-NLS-1$
        out.println("white-space: nowrap; text-align: left; }"); //$NON-NLS-1$
        out.println("form { margin:0; padding:0; }"); //$NON-NLS-1$
        out.println("table.log4jtable thead tr th{ background-color: #5991A6; padding: 2px; }"); //$NON-NLS-1$
        out.println("table.log4jtable tr.even { background-color: #eee; }"); //$NON-NLS-1$
        out.println("table.log4jtable tr.odd { background-color: #fff; }"); //$NON-NLS-1$
        out.println("</style>"); //$NON-NLS-1$

        out.println("</head>"); //$NON-NLS-1$
        out.println("<body>"); //$NON-NLS-1$
        out.println("<h3>Log4J Control Console</h3>"); //$NON-NLS-1$
    }

    /**
     * Prints the Level select HTML.
     * @param out The output writer
     * @param currentLevel the current level for the log (the selected option).
     */
    private void printLevelSelector(PrintWriter out, String currentLevel) {
        out.println("<select id=\"" + PARAM_LEVEL + "\" name=\"" + PARAM_LEVEL + "\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        for (int j = 0; j < LEVELS.length; j++) {
            out.print("<option"); //$NON-NLS-1$
            if (LEVELS[j].equals(currentLevel)) {
                out.print(" selected=\"selected\""); //$NON-NLS-1$
            }
            out.print(">"); //$NON-NLS-1$
            out.print(LEVELS[j]);
            out.println("</option>"); //$NON-NLS-1$
        }
        out.println("</select>"); //$NON-NLS-1$
    }

    /**
     * Compare the names of two <code>Logger</code>s. Used for sorting.
     */
    private static class LoggerComparator implements Comparator {

        /**
         * Sort by level? (default is sort by class name)
         */
        private boolean sortByLevel;

        /**
         * instantiate a new LoggerComparator
         * @param sortByLevel if <code>true</code> sort loggers by level instead of name.
         */
        public LoggerComparator(boolean sortByLevel) {
            this.sortByLevel = sortByLevel;
        }

        /**
         * Compare the names of two <code>Logger</code>s.
         * @param object1 an <code>Object</code> value
         * @param object2 an <code>Object</code> value
         * @return an <code>int</code> value
         */
        public int compare(Object object1, Object object2) {
            Logger logger1 = (Logger) object1;
            Logger logger2 = (Logger) object2;

            if (!sortByLevel) {
                return logger1.getName().compareTo(logger2.getName());
            }
            return logger1.getEffectiveLevel().toInt() - logger2.getEffectiveLevel().toInt();
        }

        /**
         * Return <code>true</code> if the <code>Object</code> is a <code>LoggerComparator</code> instance.
         * @param object an <code>Object</code> value
         * @return a <code>boolean</code> value
         */
        public boolean equals(Object object) {
            if (!(object instanceof LoggerComparator)) {
                return false;
            }
            return this.sortByLevel == ((LoggerComparator) object).sortByLevel;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return super.hashCode();
        }
    }

    /**
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig config) throws ServletException {
        String fragmentParam = config.getInitParameter(CONFIG_FRAGMENT);
        isFragment = ("true".equalsIgnoreCase(fragmentParam) || "yes".equalsIgnoreCase(fragmentParam)); //$NON-NLS-1$ //$NON-NLS-2$
        super.init(config);
    }

}