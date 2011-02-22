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
package info.magnolia.module.vaadin.shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog.Listener;

import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;


/**
 * A shell working only with a sub fragment of the URL fragment. Used to build sub containers by using {@link info.magnolia.module.vaadin.activity.MVPSubContainerActivity}.
 */
public class SubShell implements Shell {
    private Logger log = LoggerFactory.getLogger(SubShell.class);
    private String id;
    private Shell parent;

    public SubShell(String id, Shell parent) {
        this.id = id;
        this.parent = parent;
    }

    public void confirm(String message, Listener listener) {
        parent.confirm(message, listener);
    }

    public void notify(String message) {
        parent.notify(message);
    }

    public String getFragment() {
        log.warn("SubShell not implemented");
        return null;
    }

    public void setFragment(String fragment, boolean fireEvent) {
        log.warn("SubShell not implemented");
    }

    public void addListener(FragmentChangedListener listener) {
        log.warn("SubShell not implemented");
    }

    public void removeListener(FragmentChangedListener listener) {
        log.warn("SubShell not implemented");
    }
}
