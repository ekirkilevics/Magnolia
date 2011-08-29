/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.module.samples.model;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.RenderableDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
/**
 * This model class is defined in the component definition in the configuration
 * <code>(modules/samples/components/name-of-component/modelClass)</code>.
 * To be accessed in the template file by <code>def.method-name</code>.
 *
 * Component models can also be defined in the component definitions.
 * @author tmiyar
 *
 */
public class SampleComponentModel extends RenderingModelImpl<RenderableDefinition> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SampleComponentModel.class);

    private String query;

    public SampleComponentModel(Node content, RenderableDefinition definition, RenderingModel<?> parent) {
        super(content, definition, parent);
        log.info("Running sample component model");

    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<ContentMap> getSearchResult(){
        List<ContentMap> results = new ArrayList<ContentMap>();

        String sql = "SELECT * from nt:base WHERE jcr:path like '/%' AND contains(*, '"+query+"') AND (jcr:primaryType = 'mgnl:content' OR jcr:primaryType = 'mgnl:contentNode') order by jcr:path";

        //TODO cringele: QueryUtil should return Node and not Content. See SCRUM-293
        Collection<Content> contentList = QueryUtil.query(ContentRepository.WEBSITE, sql);
        for(Iterator<Content> it=contentList.iterator(); it.hasNext();){
            results.add(new ContentMap(it.next().getJCRNode()));
        }

        return results;
    }

}
