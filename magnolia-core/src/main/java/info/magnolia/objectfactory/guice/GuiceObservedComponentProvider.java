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
package info.magnolia.objectfactory.guice;

import javax.inject.Inject;

import com.google.inject.Provider;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.ObservedComponentFactory;


/**
 * Guice provider that creates an object by reading it from the repository and by using observation updates the
 * object if the repository changes.
 *
 * @param <T> type of the instance the provider provides
 * @version $Id$
 */
public class GuiceObservedComponentProvider<T> implements Provider<T> {

    @Inject
    private ComponentProvider componentProvider;
    private ObservedComponentFactory<T> observedComponentFactory;
    private String repository;
    private String path;
    private Class<T> type;

    public GuiceObservedComponentProvider(String repository, String path, Class<T> type) {
        this.repository = repository;
        this.path = path;
        this.type = type;
    }

    @Override
    public synchronized T get() {
        if (observedComponentFactory == null) {
            observedComponentFactory = new ObservedComponentFactory<T>(repository, path, type, componentProvider);
        }
        // FIXME ObservedComponentFactory seems to return a new cglib proxy for each call
        return observedComponentFactory.newInstance();
    }
}
