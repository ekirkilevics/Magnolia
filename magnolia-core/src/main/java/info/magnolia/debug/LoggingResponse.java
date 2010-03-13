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
package info.magnolia.debug;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Wrapps a response and records the set headers and http status
 * @author philipp
 * @version $Id$
 */
class LoggingResponse extends HttpServletResponseWrapper {

    private Map headers = new HashMap();

    private int length;

    private int status = HttpServletResponse.SC_OK;

    LoggingResponse(HttpServletResponse response) {
        super(response);
    }

    public Map getHeaders() {
        return this.headers;
    }


    public int getLength() {
        return this.length;
    }


    public int getStatus() {
        return this.status;
    }


    public void setDateHeader(String name, long date) {
        super.setDateHeader(name, date);
        headers.put(name, String.valueOf(date));
    }

    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        headers.put(name, String.valueOf(value));
    }

    public void setContentLength(int len) {
        this.length = len;
        super.setContentLength(len);
    }

    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        headers.put(name, value);
    }

    public void sendRedirect(String location) throws IOException {
        this.status = HttpServletResponse.SC_MOVED_TEMPORARILY;
        super.sendRedirect(location);
    }

    public void sendError(int sc) throws IOException {
        this.status = sc;
        super.sendError(sc);
    }

    public void sendError(int sc, String msg) throws IOException {
        this.status = sc;
        super.sendError(sc, msg);
    }

    public void setStatus(int sc) {
        this.status = sc;
        super.setStatus(sc);
    }

    public void setStatus(int sc, String sm) {
        this.status = sc;
        super.setStatus(sc, sm);
    }

}
