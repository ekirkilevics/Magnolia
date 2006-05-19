package info.magnolia.commands;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import org.apache.commons.chain.config.ConfigParser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * Date: Mar 27, 2006
 * Time: 10:54:32 AM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlXmlCatalog extends MgnlBaseCatalog {
    static final String DEFAULT_PATH = SystemProperty.getProperty("magnolia.commands.dir");
    private static final String CHAINS_COMMAND = "//chains/command";
    private static final String CLASS_NAME = "className";
    private static final String NAME = "name";
    private static final String CHAINS_CHAIN = "//chains/chain";
    private static final String COMMAND = "command";

    public void initCatalog(String string) {
        File f = new File(Path.getAbsoluteFileSystemPath(DEFAULT_PATH + "/" + string + ".xml"));
        parseUsingCustomXmlParser(f);
        log.info("Parsing catalog:" + string);
    }

    /**
     * This method is needed since, the Chain object have to be constrained to MgnlChain object.
     * All the commands and chains will be added in this catalog.
     *
     * @param f the chain definition file to parse
     */
    private void parseUsingCustomXmlParser(File f) {
        SAXBuilder builder = new SAXBuilder();
        try {
            Document doc = builder.build(f);

            // retrieve single commands
            XPath commands = XPath.newInstance(CHAINS_COMMAND);
            List commandList = commands.selectNodes(doc);
            Iterator iter1 = commandList.iterator();
            while (iter1.hasNext()) {
                Element elem = (Element) iter1.next();
                String className = elem.getAttribute(CLASS_NAME).getValue();
                String name = elem.getAttribute(NAME).getValue();
                log.info("Found action [" + name + "]:" + className);
                this.addCommand(name, (MgnlCommand) Class.forName(className).newInstance());
            }

            // retrieve chains
            XPath chains = XPath.newInstance(CHAINS_CHAIN);
            List chainList = chains.selectNodes(doc);
            Iterator iter2 = chainList.iterator();
            // loop through the chains
            while (iter2.hasNext()) {
                Element elem = (Element) iter2.next();
                MgnlChain chain = new MgnlChain();

                // loop through the command element under that chain
                Iterator iter3 = (elem.getChildren(COMMAND)).iterator();
                while (iter3.hasNext()) {
                    Element comm = (Element) iter3.next();
                    String className = comm.getAttribute(CLASS_NAME).getValue();

                    // add the command to the chain
                    chain.addCommand((MgnlCommand) Class.forName(className).newInstance());
                }

                // get the name of the chain
                String name = elem.getAttribute(NAME).getValue();
                log.info("Found chain [" + name + "]: with [" + chain.countCommands() + "] commands");

                // add the chain with the above name, and all the commands found in this catalog
                this.addCommand(name, chain);

                // go to next chain definition

            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void parseUsingChainXmlParser(File f) {
        ConfigParser parser = new ConfigParser();
        URL url = null;
        try {
            url = new URL("file://" + f);
        } catch (MalformedURLException e) {
            log.error("Could not find the command file.Commands will be unusable", e);
        }
        try {
            parser.parse(this, url);
        }
        catch (Exception e) {
            log.error("Could not parse the command file.Commands will be unusable", e);
        }
    }
}
