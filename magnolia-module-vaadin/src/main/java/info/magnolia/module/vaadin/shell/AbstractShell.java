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

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.UriFragmentUtility;


/**
 * Implements the methods to handle URI fragment changes.
 */
public abstract class AbstractShell implements Shell, com.vaadin.ui.UriFragmentUtility.FragmentChangedListener{
    private static final Logger log = LoggerFactory.getLogger(AbstractShell.class);

    private Collection<info.magnolia.module.vaadin.shell.FragmentChangedListener> listeners = new ArrayList<FragmentChangedListener>();

    protected String id;

    public AbstractShell(String id) {
        this.id = id;
    }

    public String getFragment() {
        final String fragment = getUriFragmentUtility().getFragment();
        log.debug("complete uri fragment is {}", fragment);

        final Fragmenter fragmenter = new Fragmenter(fragment);
        return fragmenter.getSubFragment(id);
    }

    public void setFragment(String fragment, boolean fireEvent) {
        final String currentCompleteFragment = getUriFragmentUtility().getFragment();
        final Fragmenter fragmenter = new Fragmenter(currentCompleteFragment);
        log.debug("current uri fragment is {}", currentCompleteFragment);

        // only change the uri if the value has changed, other don't bother Vaadin
        String currentFragment = fragmenter.getSubFragment(id);
        if(currentFragment == null || !currentFragment.equals(fragment)){
            fragmenter.setSubFragment(id, fragment);
            final String newFragment = fragmenter.toString();
            log.debug("setting uri fragment to {}", newFragment);
            getUriFragmentUtility().setFragment(newFragment, fireEvent);
        }
    }

    public void addListener(FragmentChangedListener listener) {
        log.debug("adding listener {}", listener);

        if(listeners.size()==0){
            log.debug("adding listener {}", this);
            getUriFragmentUtility().addListener(this);
        }
        listeners.add(listener);
    }

    public void removeListener(FragmentChangedListener listener) {
        log.debug("removing listener {}", listener);

        listeners.remove(listener);
        if(listeners.size()==0){
            log.debug("removing listener {}", this);
            getUriFragmentUtility().removeListener(this);
        }
    }

    public void fragmentChanged(com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent event) {
        final Fragmenter fragmenter = new Fragmenter(event.getUriFragmentUtility().getFragment());
        final String subFragment = fragmenter.getSubFragment(id);
        if(subFragment != null){
            for (info.magnolia.module.vaadin.shell.FragmentChangedListener listener : listeners) {
                log.debug("firing info.magnolia.module.vaadin.shell.FragmentChangedEvent with sub fragment {}", subFragment);
                listener.onFragmentChanged(new FragmentChangedEvent(subFragment));
            }
        }
    }

    protected abstract UriFragmentUtility getUriFragmentUtility();

}
