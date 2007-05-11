/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.i18n;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.filters.AbstractMagnoliaFilter;

/**
 * Rewrites the i18n uris and sets the current language.
 * @author philipp
 * @version $Id$
 *
 */
public class I18NSupportFilter extends AbstractMagnoliaFilter {

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        I18NSupport i18nSupport = I18NSupportFactory.getI18nSupport();
        String lang = i18nSupport.languageFromURI(Path.getURI());
        if(StringUtils.isNotEmpty(lang)){
            i18nSupport.setCurrentLanguage(lang);
            if(request.getSession(false)!=null){
                i18nSupport.setSessionLanguage(lang);
            }
            Path.setURI(i18nSupport.toURI(Path.getURI()));
        }
        chain.doFilter(request, response);
    }
}
