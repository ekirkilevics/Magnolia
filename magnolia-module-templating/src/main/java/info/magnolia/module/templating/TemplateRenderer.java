/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.templating;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A TemplateRenderer implementation is responsible for generating the actual response from request data and a
 * template. A typical jsp implementation will simply forward the request to the jsp through request dispatcher, but
 * anybody is free to bind a specific implementation to a template type.
 *
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public interface TemplateRenderer {

    /**
     * Generates the actual output using the selected template.
     * 
     * @param template template to be rendered
     * @param response HttpServletResponse
     * @throws java.io.IOException exception occurred while writing to the output stream
     * @throws javax.servlet.ServletException generic servlet exception
     */
    void renderTemplate(Template template, HttpServletResponse response) throws IOException, ServletException;

    /**
     * @deprecated since 4.0
     * @see #renderTemplate(Template, javax.servlet.http.HttpServletResponse)
     */
    void renderTemplate(Template template, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;
}