/**
 * This file Copyright (c) 2008 Magnolia International
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
package info.magnolia.debug;

import info.magnolia.cms.filters.AbstractMgnlFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Filter which dumps the headers for the request and the response. The response is therefore wrapped.
 * @author philipp
 * @version $Id$
 */
public class DumpHeadersFilter extends AbstractMgnlFilter {
    private long count = 0;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger("info.magnolia.debug");


    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        long nr = count++;
        LoggingResponse wrappedResponse = new LoggingResponse(response);
        log.info("{} uri: {}", Long.valueOf(nr), request.getRequestURI());
        log.info("{} session: {}", Long.valueOf(nr), Boolean.valueOf(request.getSession(false)!=null));

        StringBuffer params = new StringBuffer();
        for(Enumeration paramEnum =request.getParameterNames(); paramEnum.hasMoreElements();){
            String name = (String) paramEnum.nextElement();
            params.append(name).append(" = ").append(StringUtils.join(request.getParameterValues(name), ","));
            if(paramEnum.hasMoreElements()){
                params.append("; ");
            }
        }
        log.info("{} parameters: {}", Long.valueOf(nr),  params);
        for (Enumeration en = request.getHeaderNames(); en.hasMoreElements();) {
            String name = (String) en.nextElement();
            log.info("{} header: {}={}", new String[]{String.valueOf(nr), name, request.getHeader(name)});
        }
        chain.doFilter(request, wrappedResponse);
        log.info("{} response status: {}", Long.valueOf(nr),Integer.valueOf(wrappedResponse.getStatus()));
        log.info("{} response length: {}", Long.valueOf(nr),Long.valueOf(wrappedResponse.getLength()));
        for (Iterator iter = wrappedResponse.getHeaders().keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            log.info(request.getRequestURI()
                + " response: "
                + name
                + " = "
                + wrappedResponse.getHeaders().get(name));
        }
    }
}
