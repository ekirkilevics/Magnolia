package info.magnolia.cms.beans.config;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Basic catalog implementation for magnolia
 * User: niko
 */
public class MgnlCatalog extends HashMap implements Catalog {
    public void addCommand(String string, Command command) {
        this.put(string, command);
    }

    public Command getCommand(String string) {
        return (Command) this.get(string);
    }

    public Iterator getNames() {
        return this.keySet().iterator();
    }
}
