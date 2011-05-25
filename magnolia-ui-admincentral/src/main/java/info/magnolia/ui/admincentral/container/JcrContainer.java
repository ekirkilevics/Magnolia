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
package info.magnolia.ui.admincentral.container;

import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.ui.model.column.definition.AbstractColumnDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;


/**
 * Vaadin container that reads its items from a JCR repository. Implements a simple mechanism for lazy loading items from a JCR repository and a cache for items and item ids.
 * Inspired by http://vaadin.com/directory#addon/vaadin-sqlcontainer.
 *
 * @author tmattsson
 */
public abstract class JcrContainer extends AbstractContainer implements Container.Sortable, Container.Indexed, Container.ItemSetChangeNotifier, Container.PropertySetChangeNotifier {

    private static final Logger log = LoggerFactory.getLogger(JcrContainer.class);

    private Set<ItemSetChangeListener> itemSetChangeListeners;

    private Set<PropertySetChangeListener> propertySetChangeListeners;

    private final JcrContainerSource jcrContainerSource;

    private int size = Integer.MIN_VALUE;

    /** Page length = number of items contained in one page. Defaults to 100.*/
    private int pageLength = DEFAULT_PAGE_LENGTH;
    public static final int DEFAULT_PAGE_LENGTH = 100;

    /** Number of items to cache = cacheRatio x pageLength. Default cache ratio value is 2.*/
    private int cacheRatio = DEFAULT_CACHE_RATIO;
    public static final int DEFAULT_CACHE_RATIO = 2;

    /** Item and index caches. */
    private final Map<Long, ContainerItemId> itemIndexes = new HashMap<Long, ContainerItemId>();
    private final LinkedHashMap<ContainerItemId, ContainerItem> cachedItems = new LinkedHashMap<ContainerItemId, ContainerItem>();

    private Map<String, String> sortableProperties = new HashMap<String,String>();


    /** Filters (WHERE) and sorters (ORDER BY). */
    //private final List<Filter> filters = new ArrayList<Filter>();
    private final List<OrderBy> sorters = new ArrayList<OrderBy>();

    private String workspace;

    /** Starting row number of the currently fetched page. */
    private int currentOffset;

    private static final Long LONG_ZERO = Long.valueOf(0);

    private static final String CONTENT_SELECTOR_NAME = "content";

    private static final String METADATA_SELECTOR_NAME = "metaData";

    private static final String SELECT_CONTENT = "select * from [mgnl:content] as content ";

    private static final String JOIN_METADATA_ORDER_BY = "inner join [mgnl:metaData] as metaData on ischildnode(metaData,content) order by ";

    private static final String NAME_PROPERTY = "name";

    private static final String JCR_NAME_FUNCTION = "name("+CONTENT_SELECTOR_NAME+")";


    public JcrContainer(JcrContainerSource jcrContainerSource, WorkbenchDefinition workbenchDefinition) {
        this.jcrContainerSource = jcrContainerSource;
        this.workspace = workbenchDefinition.getWorkspace();

        for(AbstractColumnDefinition columnDefinition: workbenchDefinition.getColumns()){
            if(columnDefinition.isSortable()){
                log.debug("Configuring column [{}] as sortable", columnDefinition.getName());

                String propertyName = columnDefinition.getPropertyName();
                log.debug("propertyName is {}", propertyName);

                if(StringUtils.isBlank(propertyName)){
                    propertyName = columnDefinition.getName();
                    log.warn("Column {} is sortable but no propertyName has been defined. Defaulting to column name (sorting may not work as expected).", columnDefinition.getName());
                }

                sortableProperties.put(columnDefinition.getName(), propertyName);
            }
        }
    }

    /**
     * Updates the container with the items pointed to by the {@link NodeIterator} passed as argument.
     * @param iterator
     * @throws RepositoryException
     */
    public abstract void update(NodeIterator iterator) throws RepositoryException;

    @Override
    public void addListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners == null) {
            itemSetChangeListeners = new LinkedHashSet<ItemSetChangeListener>();
        }
        itemSetChangeListeners.add(listener);
    }

    @Override
    public void removeListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners != null) {
            itemSetChangeListeners.remove(listener);
            if (itemSetChangeListeners.isEmpty()) {
                itemSetChangeListeners = null;
            }
        }
    }

    @Override
    public void addListener(PropertySetChangeListener listener) {
        if (propertySetChangeListeners == null) {
            propertySetChangeListeners = new LinkedHashSet<PropertySetChangeListener>();
        }
        propertySetChangeListeners.add(listener);
    }

    @Override
    public void removeListener(PropertySetChangeListener listener) {
        if (propertySetChangeListeners != null) {
            propertySetChangeListeners.remove(listener);
            if (propertySetChangeListeners.isEmpty()) {
                propertySetChangeListeners = null;
            }
        }
    }

    public void fireItemSetChange() {

        log.debug("Firing item set changed");
        if (itemSetChangeListeners != null && !itemSetChangeListeners.isEmpty()) {
            final Container.ItemSetChangeEvent event = new AbstractContainer.ItemSetChangeEvent();
            Object[] array = itemSetChangeListeners.toArray();
            for (Object anArray : array) {
                ItemSetChangeListener listener = (ItemSetChangeListener) anArray;
                listener.containerItemSetChange(event);
            }
        }
    }

    public void firePropertySetChange() {

        log.debug("Firing property set changed");
        if (propertySetChangeListeners != null && !propertySetChangeListeners.isEmpty()) {
            final Container.PropertySetChangeEvent event = new AbstractContainer.PropertySetChangeEvent();
            Object[] array = propertySetChangeListeners.toArray();
            for (Object anArray : array) {
                PropertySetChangeListener listener = (PropertySetChangeListener) anArray;
                listener.containerPropertySetChange(event);
            }
        }
    }

    protected Map<Long, ContainerItemId> getItemIndexes() {
        return itemIndexes;
    }

    protected LinkedHashMap<ContainerItemId, ContainerItem> getCachedItems() {
        return cachedItems;
    }

    public int getPageLength() {
        return pageLength;
    }

    public void setPageLength(int pageLength) {
        this.pageLength = pageLength;
    }

    public int getCacheRatio() {
        return cacheRatio;
    }

    public void setCacheRatio(int cacheRatio) {
        this.cacheRatio = cacheRatio;
    }

    /**************************************/
    /** Methods from interface Container **/
    /**************************************/

    @Override
    public Item getItem(Object itemId) {
        if (!cachedItems.containsKey(itemId)) {
            int index = indexOfId(itemId);
            // load the item into cache
            updateOffsetAndCache(index);
            return new ContainerItem((ContainerItemId) itemId, this);
        }
        return cachedItems.get(itemId);
    }

    @Override
    public Collection<ContainerItemId> getItemIds() {
        throw new UnsupportedOperationException(getClass().getName() +" does not support this method.");
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        return new JcrContainerProperty((String) propertyId, (ContainerItemId) itemId, this);
    }

    @Override
    public int size() {
        updateCount();
        //log.debug("size is {}", size);
        return size;
    }

    @Override
    public boolean containsId(Object itemId) {
        if (itemId == null) {
            return false;
        }

        if (cachedItems.containsKey(itemId)) {
            return true;
        }

        return false;
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        fireItemSetChange();
        return getItem(itemId);
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**********************************************/
    /** Methods from interface Container.Indexed **/
    /**********************************************/

    @Override
    public int indexOfId(Object itemId) {

        if (!containsId(itemId)) {
            return -1;
        }
        if (cachedItems.isEmpty()) {
            getPage();
        }
        int size = size();
        boolean wrappedAround = false;
        while (!wrappedAround) {
            for (Long i : itemIndexes.keySet()) {
                if (itemIndexes.get(i).equals(itemId)) {
                    return i.intValue();
                }
            }
            // load in the next page.
            int nextIndex = (currentOffset / (pageLength * cacheRatio) + 1) * (pageLength * cacheRatio);
            if (nextIndex >= size) {
                // Container wrapped around, start from index 0.
                wrappedAround = true;
                nextIndex = 0;
            }
            updateOffsetAndCache(nextIndex);
        }
        return -1;
    }

    @Override
    public Object getIdByIndex(int index) {
        if (index < 0 || index > size() - 1) {
            return null;
        }
        final Long idx = Long.valueOf(index);
        if (itemIndexes.containsKey(idx)) {
            return itemIndexes.get(idx);
        }
        log.debug("item id {} not found in cache. Need to update offset, fetch new item ids from jcr repo and put them in cache.", index);
        updateOffsetAndCache(index);
        return itemIndexes.get(idx);

    }

    /**********************************************/
    /** Methods from interface Container.Ordered **/
    /**********************************************/

    @Override
    public Object nextItemId(Object itemId) {
        return getIdByIndex(indexOfId(itemId) + 1);
    }

    @Override
    public Object prevItemId(Object itemId) {
        return getIdByIndex(indexOfId(itemId) - 1);
    }

    @Override
    public Object firstItemId() {
        updateCount();
        if (size == 0) {
            return null;
        }
        if (!itemIndexes.containsKey(LONG_ZERO)) {
            updateOffsetAndCache(0);
        }
        return itemIndexes.get(LONG_ZERO);
    }

    @Override
    public Object lastItemId() {
        final Long lastIx = Long.valueOf(size() - 1);
        if (!itemIndexes.containsKey(lastIx)) {
            updateOffsetAndCache(size - 1);
        }
        return itemIndexes.get(lastIx);
    }

    @Override
    public boolean isFirstId(Object itemId) {
        return firstItemId().equals(itemId);
    }

    @Override
    public boolean isLastId(Object itemId) {
        return lastItemId().equals(itemId);
    }

    /***********************************************/
    /** Methods from interface Container.Sortable **/
    /***********************************************/
    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        sorters.clear();
        for (int i = 0; i < propertyId.length; i++) {
            if (sortableProperties.keySet().contains(propertyId[i])) {
                OrderBy orderBy = new OrderBy(sortableProperties.get(propertyId[i]), ascending[i]);
                sorters.add(orderBy);
            }
        }
        refresh();
    }

    public List<String> getSortableContainerPropertyIds() {
        return Collections.unmodifiableList(new ArrayList<String>(sortableProperties.keySet()));
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        // throw new UnsupportedOperationException();
        fireItemSetChange();
        return true;
    }

    /***********************************************/
    /** Used by JcrContainerProperty              **/
    /***********************************************/

    public Object getColumnValue(String propertyId, Object itemId) {
        try {
            return jcrContainerSource.getColumnComponent(propertyId, getJcrItem(((ContainerItemId) itemId)));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void setColumnValue(String propertyId, Object itemId, Object newValue) {
        try {
            jcrContainerSource.setColumnComponent(propertyId, getJcrItem(((ContainerItemId) itemId)), (Component) newValue);
            firePropertySetChange();
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public javax.jcr.Item getJcrItem(ContainerItemId containerItemId) throws RepositoryException {
        if (containerItemId == null) {
            return null;
        }
        Node node = jcrContainerSource.getNodeByIdentifier(containerItemId.getNodeIdentifier());
        if (containerItemId.isProperty()) {
            return node.getProperty(containerItemId.getPropertyName());
        }
        return node;
    }

    public ContainerItemId getItemByPath(String path) {
        try {
            return createContainerId(jcrContainerSource.getItemByPath(path));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    protected ContainerItemId createContainerId(javax.jcr.Item item) throws RepositoryException {
        return new ContainerItemId(item);
    }

    protected JcrContainerSource getJcrContainerSource() {
        return jcrContainerSource;
    }

    /************************************/
    /** UNSUPPORTED CONTAINER FEATURES **/
    /************************************/

    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Determines a new offset for updating the row cache. The offset is
     * calculated from the given index, and will be fixed to match the start of
     * a page, based on the value of pageLength.
     *
     * @param index
     *            Index of the item that was requested, but not found in cache
     */
    private void updateOffsetAndCache(int index) {
        if (itemIndexes.containsKey(Long.valueOf(index))) {
            return;
        }
        currentOffset = (index / (pageLength * cacheRatio)) * (pageLength * cacheRatio);
        if (currentOffset < 0) {
            currentOffset = 0;
        }
        getPage();
    }


    /**
     * Triggers a refresh if the current row count has changed.
     */
    private void updateCount() {
        int newSize = getRowCount();
        if (newSize != size) {
            size = newSize;
            refresh();
        }

    }

    /**
     * Fetches a page from the data source based on the values of pageLenght and
     * currentOffset.
     */
    protected void getPage() {
        updateCount();
        cachedItems.clear();
        itemIndexes.clear();

        try {
            final StringBuilder stmt = new StringBuilder(SELECT_CONTENT);
            if(!sorters.isEmpty()) {
                stmt.append(JOIN_METADATA_ORDER_BY);
                for(OrderBy orderBy: sorters){
                    if(NAME_PROPERTY.equals(orderBy.getProperty())){
                        //need to use name(..) function here as name or jcr:name is not supported by JCR2.
                        stmt.append(JCR_NAME_FUNCTION)
                        .append(orderBy.isAscending() ? " asc":" desc")
                        .append(", ");
                        continue;
                    }
                    stmt.append(CONTENT_SELECTOR_NAME+".["+orderBy.getProperty()+"]")
                    .append(orderBy.isAscending() ? " asc":" desc")
                    .append(", ");
                    //TODO here we don't know to which primary type this prop belongs to. I would tend not to clutter the column definition with yet another info about primary type.
                    stmt.append(METADATA_SELECTOR_NAME+".["+orderBy.getProperty()+"]")
                    .append(orderBy.isAscending() ? " asc":" desc")
                    .append(", ");
                }
                stmt.delete(stmt.lastIndexOf(","), stmt.length()-1);
            }

            //FIXME sql2 query is much slower than its xpath/sql1 counterpart (on average 80 times slower) see https://issues.apache.org/jira/browse/JCR-2830.
            //However xpath is deprecated and, although query execution is faster, it takes much longer to iterate over the results (for an explanation see comment here
            //https://issues.apache.org/jira/browse/JCR-2715?focusedCommentId=12965273&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-12965273
            //"The SQL2/QOM implementation loads all matching rows into memory during the execute() call, so you'll see an expensive query.execute() but can then very quickly iterate over the query results.")
            // to the point that any benefit gained from faster query execution is lost and overall performance gets worse.
            final QueryResult queryResult = executeQuery(stmt.toString(), Query.JCR_SQL2, pageLength * cacheRatio, currentOffset);

            final RowIterator iterator = queryResult.getRows();
            long rowCount = currentOffset;
            while(iterator.hasNext()){

                final ContainerItemId id = createContainerId(iterator.nextRow().getNode(CONTENT_SELECTOR_NAME));
                /* Cache item */
                itemIndexes.put(rowCount++, id);
                cachedItems.put(id, new ContainerItem(id, this));

            }
        } catch (RepositoryException re){
            throw new RuntimeRepositoryException(re);
        }

    }

    public String getWorkspace() {
        return workspace;
    }

    /**
     * Refreshes the container - clears all caches and resets size and offset.
     * Does NOT remove sorting or filtering rules!
     */
    public void refresh() {
        currentOffset = 0;
        cachedItems.clear();
        itemIndexes.clear();
        fireItemSetChange();
    }

    protected int getRowCount()  {
        //cache the size cause at present the query to count rows is extremely slow.
        if(size >= 0){
            return size;
        }
        QueryResult result = executeQuery(SELECT_CONTENT, Query.JCR_SQL2, 0, 0);
        try {
            return Long.valueOf(result.getRows().getSize()).intValue();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

    }

    protected void setSize(int size) {
        this.size = size;
    }

    protected QueryResult executeQuery(String statement, String language, long limit, long offset){
        try {
            final Session jcrSession = MgnlContext.getJCRSession(workspace);
            final QueryManager jcrQueryManager = jcrSession.getWorkspace().getQueryManager();
            final Query query = jcrQueryManager.createQuery(statement , language);
            if(limit > 0) {
                query.setLimit(limit);
            }
            if(offset >= 0){
                query.setOffset(offset);
            }
            log.debug("Executing query against workspace [{}] with statement [{}] and limit {} and offset {}...", new Object[]{getWorkspace(), statement, limit, offset});
            long start = System.currentTimeMillis();
            final QueryResult result = query.execute();
            log.debug("Query execution took {} ms", System.currentTimeMillis() - start);

            return result;

        } catch (LoginException e) {
           throw new RuntimeRepositoryException(e);
        } catch (InvalidQueryException e) {
            throw new RuntimeRepositoryException(e);
        } catch (RepositoryException e) {
           throw new RuntimeRepositoryException(e);
        }
    }
}
