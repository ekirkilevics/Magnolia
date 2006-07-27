/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.module.DependencyDefinition;
import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.ModuleDefinition;
import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.cms.util.NodeDataUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.PathNotFoundException;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Executes the registration of the modules. It searches the META-INF/magnolia/*.xml module descriptors, instantiate the
 * engine object and calls the register method on it.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ModuleRegistration {

    /**
     * The instance of the registration
     */
    private static ModuleRegistration instance = new ModuleRegistration();

    // (ModuleRegistration) FactoryUtil .getSingleton(ModuleRegistration.class);

    /**
     * @return Returns the instance.
     */
    public static ModuleRegistration getInstance() {
        return instance;
    }

    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(ModuleRegistration.class);

    /**
     * All the module definitions.
     */
    private OrderedMap moduleDefinitions = new LinkedMap();

    /**
     * If this flag is set a restart of the system is needed. The reason could be a servlet registration for example.
     */
    private boolean restartNeeded = false;

    /**
     * Don't instantiate! Warum dann nicht private?????
     */
    public ModuleRegistration() {
    }

    /**
     * Get's the META-INF/magnolia/*.xml module descriptors and registers the modules.
     */
    public void init() throws MissingDependencyException {
        // read the definitions from the xml files in the classpath
        readModuleDefinitions();

        // check the dependecies: before odering!
        checkDependencies();

        // order them by dependency level
        sortByDependencyLevel();

        // register the modules
        registerModules();
    }

    protected File getModuleRoot(String magnoliaModuleXml) {
        URL xmlUrl = getClass().getResource(magnoliaModuleXml);

        return getModuleRoot(xmlUrl);
    }

    /**
     * @param xmlUrl
     * @return
     */
    protected File getModuleRoot(URL xmlUrl) {
        String xmlString = xmlUrl.getFile();
        String protocol = xmlUrl.getProtocol();

        File moduleRoot = null;

        if ("jar".equals(protocol)) {
            xmlString = StringUtils.substringBefore(xmlString, ".jar!") + ".jar";
            try {
                moduleRoot = new File(new URI(xmlString));
            }
            catch (URISyntaxException e) {
                // should never happen
                log.error(e.getMessage(), e);
            }
        }
        else {
            try {
                File xmlFile = new File(new URI(xmlUrl.toString()));
                moduleRoot = xmlFile.getParentFile().getParentFile().getParentFile();
            }
            catch (URISyntaxException e) {
                // should never happen
                log.error(e.getMessage(), e);
            }
        }
        return moduleRoot;
    }

    /**
     * Read the xml files and make the objects
     */
    protected void readModuleDefinitions() {
        try {
            BeanReader beanReader = new BeanReader();
            beanReader.registerBeanClass(ModuleDefinition.class);

            log.info("Reading module definition");

            // get the xml files
            String[] defResources = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {

                public boolean accept(String name) {
                    return name.startsWith("/META-INF/magnolia") && name.endsWith(".xml");
                }
            });

            // parse the xml files
            for (int j = 0; j < defResources.length; j++) {
                String name = defResources[j];

                File moduleRoot = getModuleRoot(name);

                log.info("Parsing module file {} for module @ {}", name, moduleRoot.getAbsolutePath());

                try {
                    ModuleDefinition def = (ModuleDefinition) beanReader.parse(new StringReader(getXML(name)));
                    def.setModuleRoot(moduleRoot);
                    this.moduleDefinitions.put(def.getName(), def);
                }
                catch (Exception e) {
                    throw new ConfigurationException("can't read the module definition file [" + name + "].", e);
                }
            }

            // add adhoc definitions for already registered modules without an xml file (pseudo modules)
            Content modulesNode = ModuleLoader.getInstance().getModulesNode();

            for (Iterator iter = modulesNode.getChildren().iterator(); iter.hasNext();) {
                Content moduleNode = (Content) iter.next();

                String name = moduleNode.getName();
                String version = NodeDataUtil.getString(moduleNode, "version", "");
                String className = NodeDataUtil.getString(ContentRepository.CONFIG, moduleNode.getHandle()
                    + "/Register/class", "");

                if (!this.moduleDefinitions.containsKey(name)) {
                    log.warn("no proper module definition file found for [{}]: will add an adhoc definition", name);
                    ModuleDefinition def = new ModuleDefinition(name, version, className);
                    this.moduleDefinitions.put(def.getName(), def);
                }
            }
        }
        catch (Exception e) {
            throw new ConfigurationException("can't read the module definition files.", e);
        }
    }

    /**
     * Check if the dependencies are ok
     * @return true if so
     * @throws MissingDependencyException
     */
    private void checkDependencies() throws MissingDependencyException {
        for (MapIterator iter = this.moduleDefinitions.orderedMapIterator(); iter.hasNext();) {
            iter.next();
            ModuleDefinition def = (ModuleDefinition) iter.getValue();

            for (Iterator iterator = def.getDependencies().iterator(); iterator.hasNext();) {
                DependencyDefinition dep = (DependencyDefinition) iterator.next();
                if (!dep.isOptional()) {
                    if (!this.moduleDefinitions.containsKey(dep.getName())
                        || !dep.getVersion().equals(this.getModuleDefinition(dep.getName()).getVersion())) {
                        throw new MissingDependencyException("missing dependency: module ["
                            + def.getName()
                            + "] needs ["
                            + dep.getName()
                            + "]");
                    }
                }
            }
        }
    }

    /**
     * Sort all the definitions by the dependency level
     */
    private void sortByDependencyLevel() {
        // order by dependencies
        List modules = new ArrayList();

        // make a list for sorting
        for (MapIterator iter = this.moduleDefinitions.mapIterator(); iter.hasNext();) {
            iter.next();
            modules.add(iter.getValue());
        }

        Collections.sort(modules, new Comparator() {

            public int compare(Object arg1, Object arg2) {
                ModuleDefinition def1 = (ModuleDefinition) arg1;
                ModuleDefinition def2 = (ModuleDefinition) arg2;
                int level1 = calcDependencyLevel(def1);
                int level2 = calcDependencyLevel(def2);

                // lower level first
                int dif = level1 - level2;
                if (dif != 0) {
                    return dif;
                }
                // rest is ordered alphabetically

                return def1.getName().compareTo(def2.getName());

            }
        });

        // clear the not yet ordered entries
        this.moduleDefinitions.clear();

        // register the sorted defs
        for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
            ModuleDefinition def = (ModuleDefinition) iterator.next();
            if (log.isDebugEnabled()) {
                log.debug("add module definition [{}]", def.getName());
            }
            this.moduleDefinitions.put(def.getName(), def);
        }
    }

    /**
     * Register the modules
     */
    protected void registerModules() {
        try {
            Content modulesNode = ModuleLoader.getInstance().getModulesNode();

            for (MapIterator iter = this.moduleDefinitions.orderedMapIterator(); iter.hasNext();) {
                iter.next();
                ModuleDefinition def = (ModuleDefinition) iter.getValue();
                registerModule(modulesNode, def);
            }

        }
        catch (Exception e) {
            log.error("can't register modules", e); //$NON-NLS-1$
        }
    }

    /**
     * Regsiter a module
     * @param modulesNode the module is or wil get placed under this node
     * @param def the definition of the module to register
     */
    protected void registerModule(Content modulesNode, ModuleDefinition def) {
        try {
            Module module = (Module) Class.forName(def.getClassName()).newInstance();
            int registerState = Module.REGISTER_STATE_NONE;
            ModuleLoader.getInstance().addModuleInstance(def.getName(), module);

            Content moduleNode;

            try {
                moduleNode = modulesNode.getContent(def.getName());
                // node exists: is it a new version ?
                if (!def.getVersion().equals(moduleNode.getNodeData("version").getString())) { //$NON-NLS-1$
                    registerState = Module.REGISTER_STATE_NEW_VERSION;
                }
            }
            // first installation
            catch (PathNotFoundException e1) {
                moduleNode = modulesNode.createContent(def.getName());
                ModuleUtil.createMinimalConfiguration(moduleNode, def.getName(), def.getDisplayName(), def
                    .getClassName(), def.getVersion());
                registerState = Module.REGISTER_STATE_INSTALLATION;
            }

            try {

                long startTime = System.currentTimeMillis();
                // do only log if the register state is not none
                if (registerState != Module.REGISTER_STATE_NONE) {
                    log.info("start registration of module {}", def.getName());
                }

                // call register: this is always done not only during the first startup
                module.register(def, moduleNode, registerState);
                if (module.isRestartNeeded()) {
                    this.restartNeeded = true;
                }
                

                if (registerState == Module.REGISTER_STATE_NEW_VERSION) {
                    moduleNode.createNodeData("version").setValue(def.getVersion()); //$NON-NLS-1$
                }
                modulesNode.save();

                // execute now the post bootstrap if module specific configuration files found
                if(registerState == Module.REGISTER_STATE_INSTALLATION){
                    postBootstrapModule(def.getName());
                }
                
                log.info("Registration of module {} completed in {} second(s)", def.getName(), Long.toString((System
                    .currentTimeMillis() - startTime) / 1000));

            }
            catch (RegisterException e) {
                switch (registerState) {
                    case Module.REGISTER_STATE_INSTALLATION:
                        log.error("can't install module [" + def.getName() + "]" + def.getVersion(), e); //$NON-NLS-1$ //$NON-NLS-2$
                        break;
                    case Module.REGISTER_STATE_NEW_VERSION:
                        log.error("can't update module [" + def.getName() + "] to version " + def.getVersion(), //$NON-NLS-1$ //$NON-NLS-2$
                            e);
                        break;
                    default:
                        log.error("error during registering an already installed module [" //$NON-NLS-1$
                            + def.getName()
                            + "]", e); //$NON-NLS-1$
                        break;
                }
            }
        }

        catch (Exception e) {
            log.error("can't register module [" //$NON-NLS-1$
                + def.getName()
                + "] due to a " //$NON-NLS-1$
                + e.getClass().getName()
                + " exception: " //$NON-NLS-1$
                + e.getMessage(), e);
        }
    }

	/**
     * Calculates the level of dependency. 0 means no dependency. If no of the dependencies has itself dependencies is
     * this level 1. If one or more of the dependencies has a dependencies has a dependency it would return 2. And so on
     * ...
     * @param def module definition
     * @return the level
     */
    protected int calcDependencyLevel(ModuleDefinition def) {
        if (def.getDependencies() == null || def.getDependencies().size() == 0) {
            return 0;
        }
        List dependencyLevels = new ArrayList();
        for (Iterator iter = def.getDependencies().iterator(); iter.hasNext();) {
            DependencyDefinition dep = (DependencyDefinition) iter.next();
            ModuleDefinition depDef = this.getModuleDefinition(dep.getName());
            if (depDef == null) {
                throw new RuntimeException("Missing definition for module:" + dep.getName());
            }
            dependencyLevels.add(new Integer(calcDependencyLevel(depDef)));
        }
        return ((Integer) Collections.max(dependencyLevels)).intValue() + 1;
    }

    /**
     * Returns the definition of this module
     * @param moduleName
     */
    public ModuleDefinition getModuleDefinition(String moduleName) {
        return (ModuleDefinition) this.moduleDefinitions.get(moduleName);
    }

    /**
     * @return Returns the moduleDefinitions.
     */
    public OrderedMap getModuleDefinitions() {
        return this.moduleDefinitions;
    }

    /**
     * Changes the doctype to the correct dtd path (in the classpath)
     * @param name name of the xml resource
     * @return the reader for passing to the beanReader
     * @throws IOException
     * @throws JDOMException
     */
    private String getXML(String name) throws IOException, JDOMException {
        URL dtdUrl = getClass().getResource("/info/magnolia/cms/module/module.dtd");

        String content = IOUtils.toString(getClass().getResourceAsStream(name));

        // remove doctype
        Pattern pattern = Pattern.compile("<!DOCTYPE .*>");
        Matcher matcher = pattern.matcher(content);
        content = matcher.replaceFirst("");

        // set doctype to the dtd
        Document doc = new SAXBuilder().build(new StringReader(content));
        doc.setDocType(new DocType("module", dtdUrl.toString()));
        // write the xml to the string
        XMLOutputter outputter = new XMLOutputter();
        StringWriter writer = new StringWriter();
        outputter.output(doc, writer);
        return writer.toString();
    }

    /**
     * Bootstrap module specifig bootstrap file after the registration to load custom settings
     * @param moduleName
     */
    protected void postBootstrapModule(final String moduleName) {
    	Bootstrapper.bootstrapRepository(ContentRepository.CONFIG, new Bootstrapper.BootstrapFilter(){
    		public boolean accept(String filename) {
    			return filename.startsWith("config.modules." + moduleName);
    		}
    	});
	}

    /**
     * @return Returns the restartNeeded.
     */
    public boolean isRestartNeeded() {
        return this.restartNeeded;
    }

    /**
     * @param b
     */
    public void setRestartNeeded(boolean b) {
        this.restartNeeded = b;
    }
}