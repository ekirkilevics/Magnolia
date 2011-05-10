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
package info.magnolia.objectfactory.pico;

import info.magnolia.objectfactory.ComponentConfigurationPath;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.ObservedComponentFactory;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.adapters.AbstractAdapter;

import java.lang.reflect.Type;

/**
 * A PicoContainer {@link org.picocontainer.ComponentAdapter} wrapping our {@link ObservedComponentFactory}
 *
 * TODO : there is very likely room for improvement, cleanup, and removal of unneeded code here.
 * TODO : check if these are ComponentMonitor'ed.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ObservedComponentAdapter extends AbstractAdapter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ObservedComponentAdapter.class);

    private final ComponentConfigurationPath path;
    private ComponentProvider componentProvider;
    private ObservedComponentFactory factory;
    private Object o;

    public ObservedComponentAdapter(ComponentConfigurationPath path, Class type, ComponentProvider componentProvider) {
        this(path, type, type, componentProvider);
    }

    public ObservedComponentAdapter(ComponentConfigurationPath path, Object componentKey, Class type, ComponentProvider componentProvider) {
        super(componentKey, type);
        this.path = path;
        this.componentProvider = componentProvider;
    }

    @Override
    public Object getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
        if (factory == null) {
            factory = new ObservedComponentFactory(path.getRepository(), path.getPath(), getComponentImplementation(), componentProvider);
        }
        if (o == null) {
            o = factory.newInstance();
        }
        return o;
    }

    @Override
    public void verify(PicoContainer container) throws PicoCompositionException {
        // anything to do here ?
    }

    @Override
    public String getDescriptor() {
        return "ObservedComponentAdapter";
    }
}
