/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module.templatingcomponents.components;

import java.io.IOException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.module.templating.Area;

/**
 * Outputs an area.
 *
 * @version $Id: $
 */
public class AreaMarker extends AbstractContentComponent {

    private String name;
    private Area area;
    private String paragraphs;
    private Boolean singleton;
    private String dialog;

    // TODO implement support for script and placeholderScript
    //    private String script;
    //    private String placeholderScript;

    public AreaMarker(ServerConfiguration server, AggregationState aggregationState) {
        super(server, aggregationState);
    }

    @Override
    protected void doRender(Appendable out) throws IOException, RepositoryException {
        Node content = getTargetContent();
        out.append("<!-- cms:begin cms:content=\"" + getNodePath(content) + "\" -->").append(LINEBREAK);
        out.append("<cms:area");
        param(out, "content", getNodePath(content));
        param(out, "name", name != null ? name : area.getName());
        if (StringUtils.isNotEmpty(paragraphs)) {
            param(out, "paragraphs", paragraphs);
        }
        if (singleton != null) {
            param(out, "singleton", String.valueOf(singleton));
        }
        if (StringUtils.isNotEmpty(dialog)) {
            param(out, "dialog", dialog);
        }
        out.append(">").append(LINEBREAK);
    }

    @Override
    public void postRender(Appendable out) throws IOException, RepositoryException {
        Node content = getTargetContent();

        // TODO call RenderingEngine and render the area

        out.append("<!-- cms:end cms:content=\"" + getNodePath(content) + "\" -->").append(LINEBREAK);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getParagraphs() {
        return paragraphs;
    }

    public void setParagraphs(String paragraphs) {
        this.paragraphs = paragraphs;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(Boolean singleton) {
        this.singleton = singleton;
    }

    public String getDialog() {
        return dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }
}
