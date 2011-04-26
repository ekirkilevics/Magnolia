/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.vaadin.integration.widget.historian.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.History;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ui.VUriFragmentUtility;

/**
 * TODO open a ticket at Vaadin and provide a patch.
 * Mostly a copy of {@link VUriFragmentUtility}
 */
public class VHistorian extends VUriFragmentUtility {

    // MAGNOLIA this is a copy because the variable are private
    private String fragment;
    private ApplicationConnection client;
    private String paintableId;
    private boolean immediate;

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (client.updateComponent(this, uidl, false)) {
            return;
        }
        String uidlFragment = uidl.getStringVariable("fragment");
        immediate = uidl.getBooleanAttribute("immediate");
        if (this.client == null) {
            // initial paint has some special logic
            this.client = client;
            paintableId = uidl.getId();
            fragment = History.getToken();

            // MAGNOLIA: if the fragment on startup is not empty we have to inform the server
            // otherwise we just add the received fragment.
            if(!fragment.equals("")){
                History.fireCurrentHistoryState();
            }
            else{
                History.newItem(uidlFragment, false);
            }
        } else {
            if (uidlFragment != null && !uidlFragment.equals(fragment)) {
                fragment = uidlFragment;
                // normal fragment change from server, add new history item
                History.newItem(uidlFragment, false);
            }
        }
    }

    // MAGNOLIA this is a copy because the variable are private
    public void onValueChange(ValueChangeEvent<String> event) {
        String historyToken = event.getValue();
        fragment = historyToken;
        if (client != null) {
            client.updateVariable(paintableId, "fragment", fragment, immediate);
        }
    }

}
