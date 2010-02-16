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
package info.magnolia.authoringui.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;
import info.magnolia.authoringui.AuthoringUiComponent;
import info.magnolia.authoringui.components.NewParagraphBar;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $) 
 */
public class NewParagraphBarDirective extends AbstractDirective {

    @Override
    protected AuthoringUiComponent doExecute(ServerConfiguration serverCfg, AggregationState aggState, Environment env, Map<String, TemplateModel> params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateModelException, IOException {
//        final String label = string(params, "label", null);
//        final String description = string(params, "description", null);

        final String newButtonLabel = string(params, "newLabel", null);
        final Content target = mandatoryContent(params, "target");
        final List<String> allowedParagraphs;
        final TemplateModel model = params.get("paragraphs");
        if (model == null) {
            throw new TemplateModelException("The 'paragraphs' parameter is mandatory.");
        }
        if (model instanceof TemplateScalarModel) {
            final String p = ((TemplateScalarModel) model).getAsString();
            // TODO we could still support the string list here too ... 
            allowedParagraphs = Collections.singletonList(p);
        } else if (model instanceof TemplateSequenceModel) {
            // TODO - also support TemplateCollectionModel with new CollectionAndSequence(model)

            allowedParagraphs = new ArrayList<String>();

            final TemplateSequenceModel seqModel = (TemplateSequenceModel) model;
            for (int i = 0; i < seqModel.size(); i++) {
                final TemplateModel tm = seqModel.get(i);
                if (!(tm instanceof TemplateScalarModel)) {
                    throw new TemplateModelException("The 'paragraphs' attribute must be a String or a Collection of Strings. Found Collection of " + tm.getClass().getSimpleName() + ".");
                } else {
                    allowedParagraphs.add(((TemplateScalarModel)tm).getAsString());
                }
            }
        } else {
            throw new TemplateModelException("paragraphs must be a String, a Collection of Strings. Found " + model.getClass().getSimpleName() + ".");
        }

        final NewParagraphBar bar = new NewParagraphBar(serverCfg, aggState);
//        bar.setLabel(label);
//        bar.setDescription(description);
        if (target != null) {
            bar.setTarget(target);
        }

        if (newButtonLabel != null) {
            bar.setNewButtonLabel(newButtonLabel);
        }

        bar.setAllowedParagraphs(allowedParagraphs);

        return bar;
    }
}
