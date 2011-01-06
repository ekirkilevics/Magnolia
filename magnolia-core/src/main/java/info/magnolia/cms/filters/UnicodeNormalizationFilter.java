/**
 * This file Copyright (c) 2009-2011 Magnolia International
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
package info.magnolia.cms.filters;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.UnicodeNormalizer;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Normalizes the current URI to the NFC form which is used internally.
 * @see UnicodeNormalizer
 *
 * @author Luca Boati
 * @version $Id: $
 */
public class UnicodeNormalizationFilter extends AbstractMgnlFilter {

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        final AggregationState aggregationState = MgnlContext.getAggregationState();
        String originalBrowserURI = MgnlContext.getContextPath() + aggregationState.getOriginalBrowserURI();
        String originalBrowserURL = aggregationState.getOriginalBrowserURL();
        String originalURINormalized = MgnlContext.getContextPath() + UnicodeNormalizer.normalizeNFC(aggregationState.getOriginalURI());
        String originalURLNormalized = UnicodeNormalizer.normalizeNFC(aggregationState.getOriginalURL());
        String currentURI = MgnlContext.getContextPath() + aggregationState.getCurrentURI();

        // reset uri of the aggregationState in order to set new values for decoded uris
        MgnlContext.resetAggregationState();

        // restore some values
        MgnlContext.getAggregationState().setOriginalBrowserURI(originalBrowserURI);
        MgnlContext.getAggregationState().setOriginalBrowserURL(originalBrowserURL);
        MgnlContext.getAggregationState().setCurrentURI(currentURI);

        // set new values for original uri and url
        MgnlContext.getAggregationState().setOriginalURI(originalURINormalized);
        MgnlContext.getAggregationState().setOriginalURL(originalURLNormalized);

        HttpServletRequest unicodeRequest = new UnicodeNormalizationRequestWrapper(request);
        MgnlContext.push(unicodeRequest, response);
        try {
            if (MgnlContext.getPostedForm() != null) {
                // if it is a multipart form, request is already wrapped and parameters are read from multipartform object;
                // parameters are sometimes read by form.getParameter (deprecated) so we have to convert values in
                // multipartform.parameters map
                normalizeParameterMap(MgnlContext.getPostedForm().getParameters());
            }

            chain.doFilter(unicodeRequest, response);
        } finally {
            MgnlContext.pop();
        }
    }

    private void normalizeParameterMap(Map<String, String[]> parameterMap) {
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            entry.setValue(UnicodeNormalizer.normalizeNFC(entry.getValue()));
        }
    }

    @Override
    public boolean isEnabled() {
        // @todo this filter is enabled only if utf8 support is enabled. remove it when the check of this property is not needed anymore.
        return super.isEnabled() && SystemProperty.getBooleanProperty(SystemProperty.MAGNOLIA_UTF8_ENABLED);
    }

}
