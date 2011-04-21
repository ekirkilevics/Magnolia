package info.magnolia.ui.admincentral.list.container;

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.data.Item;

import info.magnolia.ui.admincentral.tree.container.JcrContainer;
import info.magnolia.ui.admincentral.tree.container.JcrContainerSource;

public class SortableJcrContainer extends JcrContainer implements Container.Sortable {

    public SortableJcrContainer(JcrContainerSource jcrContainerSource) {
        super(jcrContainerSource);
    }

    public Object nextItemId(Object itemId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object prevItemId(Object itemId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object firstItemId() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object lastItemId() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isFirstId(Object itemId) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isLastId(Object itemId) {
        // TODO Auto-generated method stub
        return false;
    }

    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    public void sort(Object[] propertyId, boolean[] ascending) {
        // TODO Auto-generated method stub

    }

    public Collection<?> getSortableContainerPropertyIds() {
        // TODO Auto-generated method stub
        return null;
    }

}
