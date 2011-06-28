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
package info.magnolia.ui.admincentral.search.activity;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.container.JcrContainer;
import info.magnolia.ui.admincentral.jcr.view.JcrView;
import info.magnolia.ui.admincentral.search.action.SearchActionFactory;
import info.magnolia.ui.admincentral.search.place.SearchPlace;
import info.magnolia.ui.admincentral.search.view.SearchParameters;
import info.magnolia.ui.admincentral.search.view.SearchResult;
import info.magnolia.ui.admincentral.search.view.SearchView;
import info.magnolia.ui.admincentral.toolbar.view.FunctionToolbarView;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The search activity.
 * @author fgrilli
 *
 */
public class SearchActivity extends AbstractActivity implements SearchView.Presenter {

    private static final Logger log = LoggerFactory.getLogger(SearchActivity.class);

    private FunctionToolbarView view;

    private SearchActionFactory actionFactory;

    private Shell shell;

    private SearchPlace initialSearchPlace;

    private PlaceController placeController;

    public SearchActivity(FunctionToolbarView view, SearchActionFactory actionFactory, PlaceController placeController, Shell shell) {
        this.view = view;
        this.actionFactory = actionFactory;
        this.shell = shell;
        this.placeController = placeController;
        this.view.setPresenter(this);
        Place place = placeController.getWhere();

        if(place instanceof SearchPlace) {
            initialSearchPlace = (SearchPlace)place;
        }
    }

    @Override
    public void start(ViewPort viewPort, EventBus eventBus) {
        viewPort.setView(view);
        if(initialSearchPlace != null){
            view.search(initialSearchPlace.getSearchParameters(), initialSearchPlace.getJcrView());
            view.update(initialSearchPlace);
        }
    }


    @Override
    public void onStartSearch(SearchParameters params) {
        placeController.goTo(new SearchPlace(params));
    }

    @Override
    public void onAddFilter() {
        shell.showNotification("Hi, one fine day you will see a search filter added to this UI");
    }

    @Override
    public void onPerformSearch(SearchParameters searchParameters, JcrView jcrView) {
        if (searchParameters == null || searchParameters.getQuery() == null) {
            return;
        }

        try {
            final Session jcrSession = MgnlContext.getJCRSession(searchParameters.getWorkspace());
            final QueryManager jcrQueryManager = jcrSession.getWorkspace().getQueryManager();
            final JcrContainer container = jcrView.getContainer();

            final String queryText = searchParameters.getQuery();
            // TODO attempting a join with metadata and then applying multiple or constraints seems not be working, i.e. all dataset is returned.
            // Try for instance with [select * from [mgnl:content] as content inner join [mgnl:metaData] as metaData on ischildnode (metaData, content)
            // where contains(content.title, '*foo*') or contains(metaData.*, '*foo*') or localname(content) = 'foo'
            String stmt = "select * from [mgnl:content] as content";
            if (!"".equals(queryText)) {
                stmt += " where contains(content.title, '*" + queryText + "*') or localname(content) like '%" + queryText + "%'";
            }

            final Query query = jcrQueryManager.createQuery(stmt, Query.JCR_SQL2);
            query.setLimit(container.getCacheRatio() * container.getPageLength());
            query.setOffset(0);

            log.debug("executing query against workspace [{}] with statement [{}] ", searchParameters.getWorkspace(), stmt);

            final QueryResult queryResult = query.execute();
            long itemsCount = container.update(queryResult.getRows());

            log.debug("search found {} item(s)", itemsCount);

            view.update(new SearchResult(queryText, itemsCount));
        }
        catch (LoginException e) {
            log.error(e.getMessage());
            shell.showError("An error occurred", e);
        }
        catch (RepositoryException e) {
            shell.showError("An error occurred", e);
            log.error(e.getMessage());
        }
        catch (RuntimeException e) {
            shell.showError("An error occurred", e);
            log.error(e.getMessage());
        }
    }

}
