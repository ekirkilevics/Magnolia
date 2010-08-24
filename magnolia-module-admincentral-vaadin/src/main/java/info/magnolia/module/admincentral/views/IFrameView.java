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
package info.magnolia.module.admincentral.views;

import org.vaadin.navigator.Navigator;
import org.vaadin.navigator.Navigator.View;

import com.vaadin.Application;
import com.vaadin.ui.Embedded;

/**
 * A {@link View} component which creates an iframe. Default type is {@link Embedded#TYPE_BROWSER}.
 * Before being usable (that is fully initialized), this object must explicitly set the source. E.g. in your code you need to call something like
 * <pre>
 *  IFrameView iframe = new IFrameView();
 *  iframe.setSource(new ExternalResource("http://www.magnolia-cms.com"));
 * </pre>
 * @author fgrilli
 *
 */
public class IFrameView extends Embedded implements View {
    private static final long serialVersionUID = 1L;

    public IFrameView() {
        setType(Embedded.TYPE_BROWSER);
        setSizeFull();
    }
    public void init(Navigator navigator, Application application) {
        // TODO Auto-generated method stub

    }

    public void navigateTo(String requestedDataId) {
        System.out.println("IFrameView requested data is " + requestedDataId );

    }

    public String getWarningForNavigatingFrom() {
        // TODO Auto-generated method stub
        return null;
    }

}
