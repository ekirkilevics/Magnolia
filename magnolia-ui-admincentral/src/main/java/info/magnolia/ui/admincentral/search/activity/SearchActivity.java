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

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.core.query.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.search.action.SearchActionFactory;
import info.magnolia.ui.admincentral.search.view.SearchParameters;
import info.magnolia.ui.admincentral.search.view.SearchResult;
import info.magnolia.ui.admincentral.search.view.SearchView;
import info.magnolia.ui.admincentral.workbench.place.ItemSelectedPlace;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;

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
    private ItemSelectedPlace place;

    public SearchActivity(SearchView view, SearchActionFactory actionFactory, ItemSelectedPlace place, Shell shell) {
        this.view = view;
        this.actionFactory = actionFactory;
        this.shell = shell;
        this.place = place;
        this.view.setPresenter(this);
    }

    public void start(ViewPort viewPort, EventBus eventBus) {
        viewPort.setView(view);
    }

    public SearchResult onSearch(SearchParameters params) {
        //FIXME do it right.
        long foundItems = 0;
        try {
            final Session jcrSession = MgnlContext.getJCRSession(place.getWorkspace());
            final QueryManager jcrQueryManager = jcrSession.getWorkspace().getQueryManager();

            final String stmt = "//*[jcr:contains(@*,'*"+params.getQuery()+"*') and @jcr:primaryType='mgnl:content']";
            final QueryImpl query = (QueryImpl) jcrQueryManager.createQuery(stmt , Query.XPATH);

            log.debug("executing query against searching workspace [{}] with statement [{}] ", place.getWorkspace(), stmt);
            final QueryResult queryResult = query.execute();
            foundItems = queryResult.getRows().getSize();

            log.debug("query returned {} rows", foundItems);
        } catch (LoginException e) {
            log.error(e.getMessage());
            shell.showError(e.getMessage(), e);
        } catch (RepositoryException e) {
            shell.showError(e.getMessage(), e);
            log.error(e.getMessage());
        }
        return new SearchResult(params.getQuery(), foundItems);
    }

    public void onAddFilter() {
        shell.showNotification("Hi, one fine day you will see a search filter added to this UI");
    }

}
