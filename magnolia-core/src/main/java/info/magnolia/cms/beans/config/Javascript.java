/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.beans.config;

import java.util.List;
import java.util.ArrayList;

/**
 * @author olli
 */
public class Javascript {

    private String name;
    private String path;
    private String requires;
    private String contextPath;

    public static final String TYPE = "text/javascript";

    public Javascript() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setRequires(String requires) {
        this.requires = requires;
    }

    public String getRequires() {
        return requires;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getType() {
        return Javascript.TYPE;
    }

    protected String getSrc() {
        return contextPath + path + name + ".js";
    }

    public String getHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(getSrc());
        html.append("\">");
        html.append("</script>");
        return html.toString();
    }

    public List getDependencies() {
        List list = new ArrayList();
        if (requires != null) {
            list.add(requires);
        }
        return list;
    }

}
