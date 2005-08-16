package info.magnolia.jaas.principal;

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.security.Principal;
import java.io.Serializable;

/**
 * Date: Jun 29, 2005
 * @author Sameer Charles
 * $Id :$
 */
public class PrincipalCollection implements Principal, Serializable  {

    private static final String NAME = "PrincipalCollection";

    /**
     * collection on principal objects
     * */
    private Collection collection = new ArrayList();

    private String name;

    /**
     * Get name given to this principal
     * @return name
     * */
    public String getName() {
        if (StringUtils.isEmpty(this.name)) {
            return NAME;
        }
        return this.name;
    }

    /**
     * Set this principal name
     * */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set collection
     * @param collection
     * */
    public void set(Collection collection) {
        this.collection = collection;
    }

    /**
     * Add to collection
     * @param principal to be added to the collection
     * */
    public void add(Principal principal) {
        this.collection.add(principal);
    }

    /**
     * Remove from the collection
     * @param principal to be removed from the collection
     * */
    public void remove(Principal principal) {
        this.collection.remove(principal);
    }

    /**
     * Clear collection
     * */
    public void clearAll() {
        this.collection.clear();
    }

    /**
     * Check if this collection contains specified object
     * @param principal
     * @return true if the specified object exist in the collection
     * */
    public boolean contains(Principal principal) {
        return this.collection.contains(principal);
    }

    /**
     * Checks if this collection contains object with the specified name
     * @param name
     * @return true if the collection contains the principal by the specified name
     * */
    public boolean contains(String name) {
        if (this.get(name) == null) {
            return false;
        }
        return true;
    }

    /**
     * Get principal associated to the specified name from the collection
     * @param name
     * @return principal object associated to the specified name
     * */
    public Principal get(String name) {
        Iterator principalIterator = this.collection.iterator();
        while (principalIterator.hasNext()) {
            Principal principal = (Principal) principalIterator.next();
            if (StringUtils.equalsIgnoreCase(name, principal.getName())) {
                return principal;
            }
        }
        return null;
    }
}
