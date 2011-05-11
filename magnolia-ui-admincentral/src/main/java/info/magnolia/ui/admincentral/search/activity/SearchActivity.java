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
import info.magnolia.ui.admincentral.jcr.view.JcrView;
import info.magnolia.ui.admincentral.search.action.SearchActionFactory;
import info.magnolia.ui.admincentral.search.place.SearchPlace;
import info.magnolia.ui.admincentral.search.view.SearchParameters;
import info.magnolia.ui.admincentral.search.view.SearchResult;
import info.magnolia.ui.admincentral.search.view.SearchView;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.core.query.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The search activity.
 * @author fgrilli
 *
 */
public class SearchActivity extends AbstractActivity implements SearchView.Presenter{
    private static final Logger log = LoggerFactory.getLogger(SearchActivity.class);
    private SearchView view;
    private SearchActionFactory actionFactory;
    private Shell shell;
    private SearchPlace place;
    private PlaceController placeController;
    private JcrView jcrView;

    public SearchActivity(SearchView view, SearchActionFactory actionFactory, SearchPlace place, PlaceController placeController, Shell shell) {
        this.view = view;
        this.actionFactory = actionFactory;
        this.shell = shell;
        this.place = place;
        this.placeController = placeController;
        this.view.setPresenter(this);
        this.jcrView = place.getJcrView();
    }

    @Override
    public void start(ViewPort viewPort, EventBus eventBus) {
        viewPort.setView(view);
        onPerformSearch();
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
    public void onPerformSearch() {
        if(place.getSearchParameters() == null || place.getSearchParameters().getQuery() == null){
            return;
        }
        //FIXME do it right.
        long foundItems = 0;
        try {
            final Session jcrSession = MgnlContext.getJCRSession(place.getWorkspace());
            final QueryManager jcrQueryManager = jcrSession.getWorkspace().getQueryManager();

            final String stmt = "//*[jcr:contains(@*,'*"+place.getSearchParameters().getQuery()+"*') and @jcr:primaryType='mgnl:content']";
            final QueryImpl query = (QueryImpl) jcrQueryManager.createQuery(stmt , Query.XPATH);

            log.debug("executing query against workspace [{}] with statement [{}] ", place.getWorkspace(), stmt);
            final QueryResult queryResult = query.execute();
            foundItems = queryResult.getRows().getSize();

            log.debug("query returned {} rows", foundItems);
            //shell.showNotification("query returned "+ foundItems + " rows");
            jcrView.getContainer().updateContainerIds(queryResult.getNodes());
            view.getSearchForm().updateUI(true, new SearchResult(place.getSearchParameters().getQuery(), queryResult.getNodes().getSize()));

        } catch (LoginException e) {
            log.error(e.getMessage());
            shell.showError("An error occurred", e);
        } catch (RepositoryException e) {
            shell.showError("An error occurred", e);
            log.error(e.getMessage());
        } catch (RuntimeException e ){
            shell.showError("An error occurred", e);
            log.error(e.getMessage());
        }
    }
}
