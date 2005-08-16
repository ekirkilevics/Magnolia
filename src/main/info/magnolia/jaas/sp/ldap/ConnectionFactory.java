package info.magnolia.jaas.sp.ldap;

import org.apache.log4j.Logger;

import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.DirContext;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;

/**
 * Date: Aug 11, 2005
 * Time: 4:39:01 PM
 *
 * @author Sameer Charles
 * $Id :$
 */
public abstract class ConnectionFactory {

    /**
     * Logger
     * */
    private static Logger log = Logger.getLogger(ConnectionFactory.class);

    /**
     * caches all contexts
     * */
    private static Map contextStore = new Hashtable();

    /**
     * caches all connection properties
     * */
    private static Map connectionPropertyList = new Hashtable();

    /**
     * get context with the specified id ir create using the specified file if context does not exist
     * @param id
     * @param file
     * */
    public static DirContext getContext(String id, String file) {
        if (getContext(id) == null) {
            createContext(id, file);
        }
        return getContext(id);
    }

    /**
     * get context
     * @param id
     * */
    public static DirContext getContext(String id) {
        return (DirContext) contextStore.get(id);
    }

    /**
     * Get previously created connection property list
     * @param id
     * */
    public static Properties getProperties(String id) {
        return (Properties) connectionPropertyList.get(id);
    }

    /**
     * Create property list if does not exist
     * @param id
     * @param propertyFile
     * */
    public static Properties getProperties(String id, String propertyFile) {
        if (connectionPropertyList.get(id) == null) {
            loadProperties(id, propertyFile);
        }
        return getProperties(id);
    }

    /**
     * Load properties from the file
     * @param id
     * @param propertyFile
     * */
    private static void loadProperties(String id, String propertyFile) {
        File file = new File(propertyFile);
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(file));
            connectionPropertyList.put(id, props);
        } catch (Exception e) {
            log.error("JNDI/LDAP Properties file cannot be found " + propertyFile);
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Create a new JNDI context based on supplied config
     * @param id to be assigned to this context
     * @param propertyFile
     * */
    private static void createContext(String id, String propertyFile) {
        Properties props = getProperties(id, propertyFile);
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, props.getProperty(Context.INITIAL_CONTEXT_FACTORY));
        env.put(Context.PROVIDER_URL, props.getProperty(Context.PROVIDER_URL));
        //env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        //env.put(Context.PROVIDER_URL, "ldap://ldap.bath.ac.uk/" + "dirRoot");
        try {
            DirContext context = new InitialDirContext(env);
            contextStore.put(id, context);
        } catch (NamingException ne) {
            log.error(ne.getMessage(), ne);
        }
    }

}
