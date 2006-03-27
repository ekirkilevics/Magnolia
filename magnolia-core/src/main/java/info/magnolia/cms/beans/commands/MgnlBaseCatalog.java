package info.magnolia.cms.beans.commands;

import org.apache.commons.chain.Command;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Base class for a  catalog implementation for magnolia
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public abstract class MgnlBaseCatalog extends HashMap implements MgnlCatalog {

    static Logger log = Logger.getLogger(MgnlBaseCatalog.class);

    public void addCommand(String string, Command command) {
        this.put(string, command);
    }

    public Command getCommand(String string) {
        return (Command) this.get(string);
    }

    public Iterator getNames() {
        return this.keySet().iterator();
    }

    public abstract void initCatalog(String defaultCatalog);

    public void initCatalog() {
        initCatalog(MgnlBaseCatalogFactory.DEFAULT_CATALOG);
    }

}
