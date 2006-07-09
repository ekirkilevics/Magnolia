/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.SelectOption;
import info.magnolia.cms.gui.dialog.DialogSelect;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * Select one of the supported lanuge
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class LanguageSelect extends DialogSelect {


    /**
     * @see info.magnolia.cms.gui.dialog.DialogSelect#init(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, info.magnolia.cms.core.Content, info.magnolia.cms.core.Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode) throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
    
        List options = new ArrayList();

        Collection col = MessagesManager.getAvailableLocales();

        for (Iterator iter = col.iterator(); iter.hasNext();) {
            Locale locale = (Locale) iter.next();
            String code = locale.getLanguage();
            if (StringUtils.isNotEmpty(locale.getCountry())) {
                code += "_" + locale.getCountry(); //$NON-NLS-1$
            }
            String name = locale.getDisplayName(MgnlContext.getLocale());
            SelectOption option = new SelectOption(name, code);
            options.add(option);
        }

        // sort them
        Collections.sort(options, new Comparator() {

            public int compare(Object arg0, Object arg1) {
                try {
                    String name0 = ((SelectOption) arg0).getLabel();
                    String name1 = ((SelectOption) arg1).getLabel();
                    return name0.compareTo(name1);
                }
                catch (Exception e) {
                    return 0;
                }
            }
        });

        this.setOptions(options);
    }
}
