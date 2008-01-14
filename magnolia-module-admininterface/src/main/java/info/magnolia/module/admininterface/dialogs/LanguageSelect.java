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
 */
public class LanguageSelect extends DialogSelect {

    /**
     * @see info.magnolia.cms.gui.dialog.DialogSelect#init(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse, info.magnolia.cms.core.Content, info.magnolia.cms.core.Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
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

        this.setConfig(DEFAULT_VALUE_PROPERTY, MessagesManager.getDefaultLocale().toString());
    }
}
