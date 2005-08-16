package info.magnolia.jaas.principal;

import org.apache.commons.lang.StringUtils;

import java.util.Hashtable;
import java.util.Map;

/**
 * Date: Jun 30, 2005
 * Time: 12:01:35 AM
 * @author Sameer Charles
 *
 * $Id :$
 */
public class ACLFactory {

    /**
     * ACL object store
     * */
    private static Map store = new Hashtable();


    /**
     * Get ACL object with the specified name, it creates a new ACL if does not exist
     * @param name
     * */
    public static ACL get(String name) {
        ACL acl = (ACL) ACLFactory.store.get(name);
        if (acl == null) {
            acl = new ACL();
            acl.setName(name);
            /**
             * set repository and workspace ID
             * naming convention for acl name - REPOSITORY_[ WORKSPACE ]
             * */
            String[] tokens = StringUtils.split(name,"_");
            acl.setRepository(tokens[0]);
            /**
             * If workspace ID is not a part of name, "default" workspace is used
             * */
            if (tokens.length > 1) {
                acl.setWorkspace(tokens[1]);
            }
        }
        ACLFactory.store.put(name, acl);
        return acl;
    }

}
