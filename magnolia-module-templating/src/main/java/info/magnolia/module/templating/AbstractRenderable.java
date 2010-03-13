/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.module.templating;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.MgnlInstantiationException;


/**
 * Base implementation for paragraph and template definitions. Provides the
 * {@link #modelClass} property which is used in the method
 * {@link #newModel(Content, RenderableDefinition , RenderingModel)}
 * @author pbracher
 * @version $Id$
 */
public class AbstractRenderable implements RenderableDefinition {
    private String name;
    private String title;
    private String templatePath;
    private String dialog;
    private String type;
    private String description;
    private String i18nBasename;
    private Class<? extends RenderingModel> modelClass = RenderingModelImpl.class;
    private Map parameters = new HashMap();

    /**
     * Return always the {@link #templatePath} property.
     */
    public String determineTemplatePath(String actionResult, RenderingModel model ) {
        return this.getTemplatePath();
    }

    /**
     * Instantiates the model based on the class defined by the {@link #modelClass} property. The class must provide a
     * constructor similar to {@link RenderingModelImpl#RenderingModelImpl(Content, RenderableDefinition, RenderingModel)}.
     * All the request parameters are then mapped to the model's properties.
     */
    public RenderingModel newModel(Content content, RenderableDefinition definition, RenderingModel parentModel) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        try {
            final RenderingModel model = Classes.getClassFactory().newInstance(getModelClass(), new Class[]{Content.class, definition.getClass(), RenderingModel.class}, content, definition, parentModel);
            final Map<String, String> params = MgnlContext.getParameters();
            if (params != null) {
                BeanUtils.populate(model, params);
            }
            return model;
        } catch (MgnlInstantiationException e) {
            throw new IllegalArgumentException(MISSING_CONSTRUCTOR_MESSAGE + "Can't instantiate " + getModelClass(), e);
        }
    }

    public String getName() {
        return this.name;
    }

    public String getTitle() {
        return this.title;
    }

    public String getTemplatePath() {
        return this.templatePath;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDialog() {
        return this.dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    public String getI18nBasename() {
        return this.i18nBasename;
    }

    public void setI18nBasename(String basename) {
        this.i18nBasename = basename;
    }

    public Map getParameters() {
        return this.parameters;
    }

    public void setParameters(Map params) {
        this.parameters = params;
    }

    public Class<? extends RenderingModel> getModelClass() {
        return this.modelClass;
    }

    public void setModelClass(Class<? extends RenderingModel> modelClass) {
        this.modelClass = modelClass;
    }

    public String toString() {
        return new ToStringBuilder(this)
        .append("name", this.name) //$NON-NLS-1$
        .append("type", this.type) //$NON-NLS-1$
        .append("description", this.description) //$NON-NLS-1$
        .append("dialog", this.dialog) //$NON-NLS-1$
        .append("title", this.title) //$NON-NLS-1$
        .append("templatePath", this.templatePath) //$NON-NLS-1$
        .toString();
    }

    private static final Class<?>[] MODEL_CONSTRUCTOR_TYPES = new Class[]{Content.class, RenderableDefinition.class, RenderingModel.class};
    private static final String MISSING_CONSTRUCTOR_MESSAGE;
    static {
        MISSING_CONSTRUCTOR_MESSAGE = "A model class must define a constructor with types " + ArrayUtils.toString(MODEL_CONSTRUCTOR_TYPES) + ". ";
    }
}
