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
package info.magnolia.templatinguicomponents.freemarker;

import freemarker.core.CollectionAndSequence;
import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.freemarker.models.ContentModel;
import info.magnolia.templatinguicomponents.AuthoringUiComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractDirective implements TemplateDirectiveModel {

    @SuppressWarnings("unchecked")
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        final AuthoringUiComponent uiComp = prepareUIComponent(serverConfiguration, aggregationState, env, params, loopVars, body);

        // prepareUIComponent should have removed the parameters it knows about.
        if (!params.isEmpty()) {
            throw new TemplateModelException("Unsupported parameter(s): " + params);
        }

        uiComp.render(env.getOut());
    }

    /**
     * Implementations of this method should return a AuthoringUiComponent, prepared with the known parameters.
     * If parameters have been grabbed using the methods provided by this class, they should be removed from
     * the map, thus leaving an empty map once the method returns. {@link #execute(freemarker.core.Environment, java.util.Map, freemarker.template.TemplateModel[], freemarker.template.TemplateDirectiveBody)}
     * will throw a TemplateModelException if there are leftover parameters.
     *
     * <strong>note:</strong> The current FreeMarker implementation passes a "new" Map which we can safely manipulate.
     * is thrown away after the execution of the directive. When no parameters are passed, the Map is readonly, but it
     * is otherwise a regular HashMap which has been instantiated shortly before the execution of the directive. However, since
     * this behavior is not mandated by their API, nor documented (at the time of writing, with FreeMarker 2.3.16), we
     * should exert caution. Unit tests hopefully cover this, so we'll be safe when updating to newer FreeMarker versions. 
     */
    protected abstract AuthoringUiComponent prepareUIComponent(ServerConfiguration serverCfg, AggregationState aggState, Environment env, Map<String, TemplateModel> params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateModelException, IOException;

    protected String mandatoryString(Map<String, TemplateModel> params, String key) throws TemplateModelException {
        return _param(params, key, TemplateScalarModel.class, true).getAsString();
    }

    protected String string(Map<String, TemplateModel> params, String key, String defaultValue) throws TemplateModelException {
        final TemplateScalarModel m = _param(params, key, TemplateScalarModel.class, false);
        if (m == null) { // we've already checked if the param is mandatory already
            return defaultValue;
        }
        return m.getAsString();
    }

    protected boolean mandatoryBool(Map<String, TemplateModel> params, String key) throws TemplateModelException {
        return _param(params, key, TemplateBooleanModel.class, true).getAsBoolean();
    }

    protected boolean bool(Map<String, TemplateModel> params, String key, boolean defaultValue) throws TemplateModelException {
        final TemplateBooleanModel m = _param(params, key, TemplateBooleanModel.class, false);
        if (m == null) {
            return defaultValue;
        }
        return m.getAsBoolean();
    }

    protected Content mandatoryContent(Map<String, TemplateModel> params, String key) throws TemplateModelException {
        return _param(params, key, ContentModel.class, true).asContent();
    }

    protected Content content(Map<String, TemplateModel> params, String key, Content defaultValue) throws TemplateModelException {
        final ContentModel m = _param(params, key, ContentModel.class, false);
        if (m == null) {
            return defaultValue;
        }
        return m.asContent();
    }

    protected List<String> mandatoryStringList(Map<String, TemplateModel> params, String key) throws TemplateModelException {
        final TemplateModel model = _param(params, key, TemplateModel.class, true);
        if (model instanceof TemplateScalarModel) {
            final String s = ((TemplateScalarModel) model).getAsString();
            return Collections.singletonList(s);
        } else if (model instanceof TemplateSequenceModel) {
            final CollectionAndSequence coll = new CollectionAndSequence((TemplateSequenceModel) model);
            return unwrapStringList(coll, key);
        } else if (model instanceof TemplateCollectionModel) {
            final CollectionAndSequence coll = new CollectionAndSequence((TemplateCollectionModel) model);
            return unwrapStringList(coll, key);
        } else {
            throw new TemplateModelException(key + " must be a String, a Collection of Strings. Found " + model.getClass().getSimpleName() + ".");
        }
    }

    private List<String> unwrapStringList(CollectionAndSequence collAndSeq, String key) throws TemplateModelException {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < collAndSeq.size(); i++) {
            final TemplateModel tm = collAndSeq.get(i);
            if (!(tm instanceof TemplateScalarModel)) {
                throw new TemplateModelException("The '" + key + "' attribute must be a String or a Collection of Strings. Found Collection of " + tm.getClass().getSimpleName() + ".");
            } else {
                list.add(((TemplateScalarModel) tm).getAsString());
            }
        }
        return list;
    }

    protected <MT extends TemplateModel> MT _param(Map<String, TemplateModel> params, String key, Class<MT> type, boolean isMandatory) throws TemplateModelException {
        final boolean containsKey = params.containsKey(key);
        if (isMandatory && !containsKey) {
            throw new TemplateModelException("The '" + key + "' parameter is mandatory.");

        }
        // can't remove here: in case of parameter-less directive call, FreeMarker passes a read-only Map.
        final TemplateModel m = params.get(key);
        if (m != null && !type.isAssignableFrom(m.getClass())) {
            throw new TemplateModelException("The '" + key + "' parameter must be a " + type.getSimpleName() + " and is a " + m.getClass().getSimpleName() + ".");
        }
        if (m == null && containsKey) {
            // parameter is passed but null value ... (happens with content.nonExistingSubNode apparently)
            throw new TemplateModelException("The '" + key + "' parameter was passed but not or wrongly specified.");
        }
        if (containsKey) {
            params.remove(key);
        }

        return (MT) m;
    }
}
