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
package info.magnolia.templating.editor.client.widget.button;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;


/**
 * Locale selector widget. It will output a select box with all locales passed in. Changing selection will trigger page reloading by default. You can change this behavior, by overriding
 * {@link #onChangeCallback(ChangeEvent)}.
 *
 * @version $Id$
 *
 */
public class LocaleSelector extends ListBox {
    /**
     * @param availableLocales a map whose key is the locale string in a human readable form (i.e. English, Deutsch) and whose value is a string representing
     * the URI associated to that locale.
     * @param currentURI a string used to select the current locale in the selector.
     */
    public LocaleSelector(final Map<String, String> availableLocales, final String currentURI) {
        if(availableLocales != null && !availableLocales.isEmpty()) {
            int index = 0;

            for(Entry<String, String> locale : availableLocales.entrySet()) {
                this.addItem(locale.getKey(), locale.getValue());
                if(locale.getValue().equals(currentURI)) {
                    this.setSelectedIndex(index);
                }
                index++;
            }

            this.addChangeHandler(new ChangeHandler() {

                @Override
                public void onChange(ChangeEvent event) {
                   onChangeCallback(event);
                }
            });
        }
    }

    protected void onChangeCallback(ChangeEvent event) {
        ListBox languageSelector = (ListBox)event.getSource();
        Window.Location.replace(languageSelector.getValue(languageSelector.getSelectedIndex()));
    }
}
