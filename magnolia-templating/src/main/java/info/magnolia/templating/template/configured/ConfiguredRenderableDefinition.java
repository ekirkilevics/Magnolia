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
package info.magnolia.templating.template.configured;

import info.magnolia.cms.core.Access;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.MgnlInstantiationException;
import info.magnolia.templating.model.RenderingModel;
import info.magnolia.templating.model.RenderingModelImpl;
import info.magnolia.templating.template.RenderableDefinition;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * A {@link RenderableDefinition} configured in the configuration workspace.
 */
public class ConfiguredRenderableDefinition implements RenderableDefinition {
    private String name;
    private String title;
    private String templateScript;
    private String renderType;
    private String description;
    private String i18nBasename;
    @SuppressWarnings("rawtypes")
    private Class<? extends RenderingModel> modelClass = RenderingModelImpl.class;

    protected Map<String, Object> parameters = new HashMap<String, Object>();

    /**
     * Instantiates the model based on the class defined by the {@link #modelClass} property. The class must provide a
     * constructor similar to {@link RenderingModelImpl#RenderingModelImpl(Content, RenderableDefinition, RenderingModel)}.
     * All the request parameters are then mapped to the model's properties.
     */
    @Override
    public RenderingModel<?> newModel(Node content, RenderableDefinition definition, RenderingModel<?> parentModel) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        try {
            final RenderingModel<?> model = Classes.getClassFactory().newInstance(getModelClass(), new Class[]{Node.class, definition.getClass(), RenderingModel.class}, content, definition, parentModel);
            // TODO pass the parameter map to the method
            final Map<String, String> params = MgnlContext.getParameters();
            if (params != null) {
                BeanUtils.populate(model, params);
            }
            return model;
        } catch (MgnlInstantiationException e) {
            throw new IllegalArgumentException(MISSING_CONSTRUCTOR_MESSAGE + "Can't instantiate " + getModelClass(), e);
        }
    }

    @Override
    public boolean isAvailable(Node content) {
        try {
            // should not fact that we are able to get path already show that we can read this node???
            // ... unless of course this "content" was created with system session ... so make sure we check using user session and not the node session
            return Access.isGranted(MgnlContext.getJCRSession(content.getSession().getWorkspace().getName()), content.getPath(), Session.ACTION_READ);
        } catch (RepositoryException e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getTemplateScript() {
        return this.templateScript;
    }

    @Override
    public String getRenderType() {
        return renderType;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTemplateScript(String templateScript) {
        this.templateScript = templateScript;
    }

    public void setRenderType(String renderType) {
        this.renderType = renderType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getI18nBasename() {
        return this.i18nBasename;
    }

    public void setI18nBasename(String basename) {
        this.i18nBasename = basename;
    }

    @SuppressWarnings("rawtypes")
    public Class<? extends RenderingModel> getModelClass() {
        return this.modelClass;
    }

    public void setModelClass(Class<? extends RenderingModel<?>> modelClass) {
        this.modelClass = modelClass;
    }

    @Override
    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    public void setParameters(Map<String, Object> params) {
        this.parameters = params;
    }

    public void addParameter(String name, Object parmeter) {
        this.parameters.put(name, parmeter);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("name", this.name)
        .append("renderType", this.renderType)
        .append("description", this.description)
        .append("title", this.title)
        .append("templateScript", this.templateScript)
        .toString();
    }

    private static final Class<?>[] MODEL_CONSTRUCTOR_TYPES = new Class[]{Node.class, RenderableDefinition.class, RenderingModel.class};
    private static final String MISSING_CONSTRUCTOR_MESSAGE;
    static {
        MISSING_CONSTRUCTOR_MESSAGE = "A model class must define a constructor with types " + ArrayUtils.toString(MODEL_CONSTRUCTOR_TYPES) + ". ";
    }
}
