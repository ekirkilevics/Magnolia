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

import com.vaadin.ui.UriFragmentUtility;


/**
 * @author pbaerfuss
 * @version $Id$
 *
 */
public abstract class AbstractShell implements Shell, com.vaadin.ui.UriFragmentUtility.FragmentChangedListener{

    protected String id;


    private Collection<info.magnolia.module.vaadin.shell.FragmentChangedListener> listeners = new ArrayList<FragmentChangedListener>();


    public AbstractShell(String id) {
        this.id = id;
    }


    public String getFragment() {
        Fragmenter fragmenter = new Fragmenter(getUriFragmentUtility().getFragment());
        return fragmenter.getSubFragment(id);
    }

    public void setFragment(String fragment, boolean fireEvent) {
        Fragmenter fragmenter = new Fragmenter(getUriFragmentUtility().getFragment());
        fragmenter.setSubFragment(id, fragment);
        getUriFragmentUtility().setFragment(fragmenter.toString(), fireEvent);
    }

    public void addListener(FragmentChangedListener listener) {
        if(listeners.size()==0){
            getUriFragmentUtility().addListener(this);
        }
        listeners.add(listener);
    }

    public void removeListener(FragmentChangedListener listener) {
        listeners.remove(listener);
        if(listeners.size()==0){
            getUriFragmentUtility().removeListener(this);
        }
    }

    public void fragmentChanged(com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent event) {
        Fragmenter fragmenter = new Fragmenter(event.getUriFragmentUtility().getFragment());
        if(fragmenter.getSubFragment(id) != null){
            for (info.magnolia.module.vaadin.shell.FragmentChangedListener listener : listeners) {
                listener.onFragmentChanged(new FragmentChangedEvent(fragmenter.getSubFragment(id)));
            }
        }
    }

    protected abstract UriFragmentUtility getUriFragmentUtility();

}
