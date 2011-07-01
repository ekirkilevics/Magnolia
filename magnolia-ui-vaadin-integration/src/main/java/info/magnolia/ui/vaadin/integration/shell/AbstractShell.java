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
package info.magnolia.ui.vaadin.integration.shell;

import info.magnolia.ui.framework.event.HandlerRegistration;
import info.magnolia.ui.framework.shell.FragmentChangedEvent;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;
import info.magnolia.ui.framework.shell.Fragmenter;
import info.magnolia.ui.framework.shell.Shell;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.UriFragmentUtility;


/**
 * Implements the methods to handle URI fragment changes.
 *
 * @version $Id$
 */
@SuppressWarnings("serial")
public abstract class AbstractShell implements Shell, com.vaadin.ui.UriFragmentUtility.FragmentChangedListener{

    private static final Logger log = LoggerFactory.getLogger(AbstractShell.class);

    private Collection<info.magnolia.ui.framework.shell.FragmentChangedHandler> handlers = new ArrayList<FragmentChangedHandler>();

    protected String id;

    public AbstractShell(String id) {
        this.id = id;
    }

    @Override
    public String getFragment() {
        final String fragment = getUriFragmentUtility().getFragment();
        log.debug("complete uri fragment is {}", fragment);

        final Fragmenter fragmenter = new Fragmenter(fragment);
        return fragmenter.getSubFragment(id);
    }

    @Override
    public void setFragment(String fragment) {
        final String currentCompleteFragment = getUriFragmentUtility().getFragment();
        final Fragmenter fragmenter = new Fragmenter(currentCompleteFragment);
        log.debug("current uri fragment is {}", currentCompleteFragment);

        // only change the uri if the value has changed, other don't bother Vaadin
        String currentFragment = fragmenter.getSubFragment(id);
        if(currentFragment == null || !currentFragment.equals(fragment)){
            fragmenter.setSubFragment(id, fragment);
            final String newFragment = fragmenter.toString();
            log.debug("setting uri fragment to {}", newFragment);
            getUriFragmentUtility().setFragment(newFragment, false);
        }
    }

    @Override
    public HandlerRegistration addFragmentChangedHandler(final FragmentChangedHandler handler) {
        log.debug("adding listener {}", handler);

        if(handlers.size()==0){
            log.debug("adding listener {}", this);
            getUriFragmentUtility().addListener(this);
        }
        handlers.add(handler);
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                removeFragmentChangedHandler(handler);
            }
        };
    }

    private void removeFragmentChangedHandler(FragmentChangedHandler handler) {
        log.debug("removing listener {}", handler);

        handlers.remove(handler);
        if(handlers.size()==0){
            log.debug("removing listener {}", this);
            getUriFragmentUtility().removeListener(this);
        }
    }

    @Override
    public void fragmentChanged(com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent event) {
        final Fragmenter fragmenter = new Fragmenter(event.getUriFragmentUtility().getFragment());
        final String subFragment = fragmenter.getSubFragment(id);
        if(subFragment != null){
            for (info.magnolia.ui.framework.shell.FragmentChangedHandler listener : handlers) {
                log.debug("firing info.magnolia.ui.framework.shell.FragmentChangedEvent with sub fragment {}", subFragment);
                listener.onFragmentChanged(new FragmentChangedEvent(subFragment));
            }
        }
    }

    protected abstract UriFragmentUtility getUriFragmentUtility();

    @Override
    public Shell createSubShell(String id) {
        return new SubShell(id, this);
    }

}
