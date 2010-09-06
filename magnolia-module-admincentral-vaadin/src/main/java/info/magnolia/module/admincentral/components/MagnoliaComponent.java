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

import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;

/**
 * The interface for all Magnolia UI components that want to manage their own state: e.g. history management (browser back button) and application state bookmarking.
 * For example, a tree component might want to provide such features as being able to bookmark a page containing it and (re)open it showing the tree collapsed at the same
 * node where it was, when it was bookmarked. <br/>
 * Concrete classes need to implement the {@link FragmentChangedListener#fragmentChanged(com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent)} method to
 * handle what to do when the URI fragment portion changes (that is the part after the <code>#</code> sign, e.g. http://mysite.com/mypage<strong>#myproduct</strong>).
 * <br/>
 * <br/>
 *<strong>This way of managing application state is experimental and likely to change in the future.</strong>
 * @author fgrilli
 *
 */
public interface MagnoliaComponent extends FragmentChangedListener {

    UriFragmentUtility getUriFragmentUtility();
    void addListener(FragmentChangedListener fragmentChangedListener);
}
