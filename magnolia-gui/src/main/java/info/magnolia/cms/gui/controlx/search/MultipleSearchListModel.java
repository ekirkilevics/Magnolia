package info.magnolia.cms.gui.controlx.search;

import info.magnolia.cms.gui.controlx.list.ListModelIterator;
import info.magnolia.cms.gui.query.SearchQuery;

import java.util.Collection;
import java.util.NoSuchElementException;

public class MultipleSearchListModel extends AbstractSearchableListModel {

    protected final AbstractSearchableListModel[] models;
    protected final String[] modelNames;

    public MultipleSearchListModel(AbstractSearchableListModel[] models, String[] modelNames) {
        this.models = models;
        this.modelNames = modelNames;
    }

    public ListModelIterator getListModel() {
        return new MultipleListIterator();
    }

    private class MultipleListIterator implements ListModelIterator {

        private ListModelIterator current, previous;
        private int pos;

        public Object getValue(String name) {
            if(previous != null){
                return previous.getValue(name);
            }
            return current.getValue(name);
        }

        public Object getValueObject() {
            if(previous != null){
                return previous.getValueObject();
            }
            return current.getValueObject();
        }

        public String getId() {
            if(previous != null){
                return previous.getId();
            }
            return current.getId();
        }

        public String getGroupName() {
            if(previous != null){
                return previous.getGroupName();
            }
            return current.getGroupName();
        }

        public Object nextGroup() {
            if(previous != null){
                return previous.nextGroup();
            }
            return current.nextGroup();
        }

        public boolean hasNextInGroup() {
            if(previous != null){
                return previous.hasNext();
            }
            return current.hasNextInGroup();
        }

        public void remove() {
            current.remove();
        }

        public boolean hasNext() {
            if(current == null && models.length > 0){
                current = models[pos].getListModelIterator();
            }
            while(!current.hasNext()){
                if(++pos < models.length){
                    previous = current;
                    current = models[pos].getListModelIterator();
                }else{
                    return false;
                }
            }
            return true;
        }

        public Object next() {
            previous = null;
            if(current == null) {
                throw new NoSuchElementException();
            }
            current.next();
            return getName();
        }

        public String getName() {
            return modelNames[pos];
        }

    }

    public void setGroupBy(String name, String order) {
        super.setGroupBy(name, order);
        for(int i = 0; i < models.length; i++) {
            models[i].setGroupBy(name, order);
        }
    }

    public void setGroupBy(String name) {
        super.setGroupBy(name);
        for(int i = 0; i < models.length; i++) {
            models[i].setGroupBy(name);
        }
    }

    public void setSortBy(String name, String order) {
        super.setSortBy(name, order);
        for(int i = 0; i < models.length; i++) {
            models[i].setSortBy(name, order);
        }
    }

    public void setSortBy(String name) {
        super.setSortBy(name);
        for(int i = 0; i < models.length; i++) {
            models[i].setSortBy(name);
        }
    }

    public void setQuery(SearchQuery query) {
        super.setQuery(query);
        for(int i = 0; i < models.length; i++) {
            models[i].setQuery(query);
        }
    }

    protected Collection getResult() throws Exception {
        throw new RuntimeException("this method should never be called since the iterator creation was overwritten");
    }
}
