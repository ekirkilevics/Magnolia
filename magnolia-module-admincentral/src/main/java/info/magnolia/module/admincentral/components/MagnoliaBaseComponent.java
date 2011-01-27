/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.components;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;
/**
 * A base implementation for {@link MagnoliaComponent}. Subclasses need only to implement {@link MagnoliaComponent#fragmentChanged(com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent)}
 * @author fgrilli
 *
 */
public abstract class MagnoliaBaseComponent extends CustomComponent implements MagnoliaComponent {

    private static final long serialVersionUID = 1L;

    private UriFragmentUtility uriFragmentUtility = new UriFragmentUtility();

    public MagnoliaBaseComponent() {
        setCompositionRoot(uriFragmentUtility);
        addListener(this);
    }

    public final UriFragmentUtility getUriFragmentUtility() {
        return uriFragmentUtility;
    }

    public final void addListener(FragmentChangedListener fragmentChangedListener) {
        uriFragmentUtility.addListener(fragmentChangedListener);
    }

    public final void removeListener(FragmentChangedListener fragmentChangedListener) {
        uriFragmentUtility.removeListener(fragmentChangedListener);
    }

    @Override
    public void detach() {
        super.detach();
        removeListener(this);
    }

    @Override
    public void attach() {
        super.attach();
        //In order to work, UriFragmentUtility MUST be attached to the application main window
        getApplication().getMainWindow().addComponent(uriFragmentUtility);
    }
}
