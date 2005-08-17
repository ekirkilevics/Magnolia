package info.magnolia.jaas.sp.ldap;

import java.util.*;

/**
 * Date: Aug 16, 2005
 * Time: 11:58:27 AM
 *
 * @author Sameer Charles
 * $Id :$
 */
class AttributeMap {

    /**
     * config file parameter
     * */
    static final String ORGANIZATION = "Organization";

    /**
     * config file parameter
     * */
    static final String ORGANIZATION_UNIT = "OrganizationUnit";

    /**
     * config file parameter
     * */
    static final String COMMON_NAME = "CommonName";

    /**
     * config file parameter
     * */
    static final String SURNAME = "Surname";

    /**
     * config file parameter
     * */
    static final String GIVEN_NAME = "GivenName";

    /**
     * config file parameter
     * */
    static final String USER_ID = "uid";

    /**
     * config file parameter
     * */
    static final String DISTINGUISHED_NAME = "dn";

    /**
     * config file parameter
     * */
    static final String MAIL = "mail";

    /**
     * config file parameter
     * */
    static final String GROUP_ID = "GroupId";

    /**
     * config file parameter
     * */
    static final String PASSWORD = "Password";

    /**
     * config file parameter
     * */
    static final String LANGUAGE = "Language";

    /**
     * name map
     * */
    private Map nameMap;

    /**
     * store
     * */
    private Map store;

    /**
     * Package private constructor
     * @param props all defined properties
     * */
    AttributeMap(Properties props) {
        this.store = new Hashtable();
        this.nameMap = props; // simply a reference
    }

    /**
     * Set property
     * @param key
     * @param value
     * */
    void setProperty(String key, String value) {
        List values;
        if (this.store.get(key) == null) {
            // create a new value pair
            values = new ArrayList();
            this.store.put(key, values);
        } else {
            values = (ArrayList) this.store.get(key);
        }
        values.add(value);
    }

    /**
     * Get property value(s)
     * @param key
     * @return list of values
     * */
    List getProperty(String key) {
        // check the corresponding LDAP key
        String ldapKey = (String) this.nameMap.get(key);
        return (List) this.store.get(ldapKey);
    }

    /**
     * Get single value property
     * @param key
     * @return string value
     * */
    String getSingleValueProperty(String key) {
        List valueList = this.getProperty(key);
        if (valueList != null) {
            return (String) valueList.get(0);
        }
        return null;
    }

}
