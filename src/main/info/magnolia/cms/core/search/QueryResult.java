package info.magnolia.cms.core.search;

import info.magnolia.cms.security.AccessManager;

import java.util.Iterator;

/**
 * Date: Apr 4, 2005
 * Time: 11:14:57 AM
 *
 * @author Sameer Charles
 */

public interface QueryResult {

    /**
     * Gets iterator of resultant NodeData objects
     * */
    public Iterator getNodeDataIterator();

    /**
     * Gets iterator of Content objects
     * */
    public Iterator getContentIterator();

    /**
     * Gets iterator of ContentNode objects
     * */
    public Iterator getContentNodeIterator();

}
