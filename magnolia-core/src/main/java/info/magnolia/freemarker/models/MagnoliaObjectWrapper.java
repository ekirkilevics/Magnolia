/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.freemarker.models;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.MapModel;
import freemarker.ext.util.ModelFactory;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleDate;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import info.magnolia.cms.beans.config.RenderableDefinition;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.User;
import info.magnolia.context.Context;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A Freemarker ObjectWrapper that knows about Magnolia specific objects.
 *
 * @author Chris Miner
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MagnoliaObjectWrapper extends DefaultObjectWrapper {
    private final ModelFactory contextModelFactory = new ModelFactory() {
        public TemplateModel create(Object object, ObjectWrapper wrapper) {
            // SimpleMapModel would prevent us from using Context's specific methods
            // SimpleHash (which seems to be the default in 2.3.14) also prevents using specific methods
            return new MapModel((Map) object, (BeansWrapper) wrapper);
        }
    };

    private final ModelFactory calendarFactory = new ModelFactory() {
        public TemplateModel create(Object object, ObjectWrapper wrapper) {
            return handleCalendar((Calendar) object);
        }
    };

    /**
     * The ModelFactory implementations explicitely registered by modules, as well as the default ones.
     */
    private final Map registeredModelFactories;

    public MagnoliaObjectWrapper() {
        super();
        this.registeredModelFactories = new HashMap();
        // default ModelFactories
        registeredModelFactories.put(NodeData.class, NodeDataModelFactory.INSTANCE);
        registeredModelFactories.put(Content.class, ContentModel.FACTORY);
        registeredModelFactories.put(Calendar.class, calendarFactory);
        registeredModelFactories.put(User.class, UserModel.FACTORY);
        registeredModelFactories.put(Context.class, contextModelFactory);
        registeredModelFactories.put(RenderableDefinition.class, RenderableDefinitionModel.FACTORY);
    }

    /**
     * Unwraps our custom wrappers, let the default wrapper do the rest.
     */
    public Object unwrap(TemplateModel model, Class hint) throws TemplateModelException {
        if (model instanceof ContentModel) {
            return ((ContentModel) model).asContent();
        }
        if (model instanceof BinaryNodeDataModel) {
            return ((BinaryNodeDataModel) model).asNodeData();
        }
        if (model instanceof UserModel) {
            return ((UserModel) model).asUser();
        }
        return super.unwrap(model, hint);
    }

    public TemplateModel wrap(Object obj) throws TemplateModelException {
        if (obj instanceof Context) {
            // bypass the default SimpleHash wrapping, we need a MapModel, see contextModelFactory
            return handleUnknownType(obj);
        }
        return super.wrap(obj);
    }

    // TODO let modules plug in their own ModelFactories
    protected ModelFactory getModelFactory(Class clazz) {
        final Set classes = registeredModelFactories.keySet();
        final Iterator it = classes.iterator();
        while (it.hasNext()) {
            final Class classHandledByFactory = (Class) it.next();
            if (classHandledByFactory.isAssignableFrom(clazz)) {
                return (ModelFactory) registeredModelFactories.get(classHandledByFactory);
            }
        }

        return super.getModelFactory(clazz);
    }

    protected SimpleDate handleCalendar(Calendar cal) {
        return new SimpleDate(cal.getTime(), TemplateDateModel.DATETIME);
    }

}
