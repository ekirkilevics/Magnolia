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
package info.magnolia.util;

/**
 *    <p>Utilities to escaping characters for preventing XSS attack.</p>
 *    <p>This class escapes only & (&amp;), "(&quot), <(&lt;), >(&gt;)  characters, but doesn't escape others characters.</p>
 *    <p>Use when StringEscapeUtils cannot be used because of escaping more or less character entities.</p>
 */
public class EscapeUtil {

    public static String escapeXss(String str) {
        return str == null ? null : str.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String[] escapeXss(String[] str) {
        if (str == null) {
            return null;
        }
        String[] retValue = new String[str.length];
        for(int i = 0; i < retValue.length; i++) {
            retValue[i] = escapeXss(unescapeXss(str[i]));
        }
        return retValue;
    }

    public static String unescapeXss(String str) {
        return str == null ? null : str.replace("&amp;", "&").replace("&quot;", "\"").replace("&lt;", "<").replace("&gt;", ">");
    }

    public static String[] unescapeXss(String[] str) {
          if (str == null) {
              return null;
          }
          String[] retValue = new String[str.length];
          for(int i = 0; i < retValue.length; i++) {
              retValue[i] = unescapeXss(str[i]);
          }
          return retValue;
    }
}
