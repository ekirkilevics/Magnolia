/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.module.DependencyDefinition;
import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.ModuleDefinition;
import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.model.reader.BetwixtModuleDefinitionReader;
import info.magnolia.module.model.reader.ModuleDefinitionReader;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

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
 *
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 *
 * @deprecated since 3.5, use ModuleManager and/or ModuleRegistry
 * @see info.magnolia.module.ModuleManager
 * @see info.magnolia.module.ModuleRegistry
 */
public class ModuleRegistration {
    private static final Logger log = LoggerFactory.getLogger(ModuleRegistration.class);
    
    /**
     * getInstance() used this flag to init the instance if not yet done.
     */
    private boolean initialized = false;

    /**
     * @return Returns the instance.
     */
    public synchronized static ModuleRegistration getInstance() {
        ModuleRegistration registration = (ModuleRegistration) FactoryUtil.getSingleton(ModuleRegistration.class);
        // TODO : couldn't this be in the ctor !?
        if(!registration.initialized) {
            registration.init();
        }
        return registration;
    }

    /**
     * All the module definitions.
     */
    private OrderedMap moduleDefinitions = new LinkedMap();

    /**
     * If this flag is set a restart of the system is needed. The reason could be a servlet registration for example.
     */
    private boolean restartNeeded = false;

    /**
     * Don't instantiate! (use getInstance)
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

        initialized = true;
    }

    protected File getModuleRoot(String magnoliaModuleXml) {
        URL xmlUrl = getClass().getResource(magnoliaModuleXml);

        return getModuleRoot(xmlUrl);
    }

    /**
     * @param xmlUrl
     * @return module root
     */
    protected File getModuleRoot(URL xmlUrl) {
        final String path = xmlUrl.getFile();

        final boolean withinJar = StringUtils.contains(path, ".jar!");
        if (withinJar) {
            final String jarPath = StringUtils.substringBefore(path, ".jar!") + ".jar";
            return new File(jarPath);
        } else {
            final File xmlFile = new File(path);
            // module.jar!:META-INF/magnolia/module-name.xml
            return xmlFile.getParentFile().getParentFile().getParentFile();
        }
    }

    /**
     * Read the xml files and make the objects
     *
     * @deprecated see info.magnolia.module.ModuleManager
     */
    protected void readModuleDefinitions() {
        try {
            final ModuleDefinitionReader moduleDefinitionReader = new BetwixtModuleDefinitionReader();

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
                    final String xml = getXML(name);
                    ModuleDefinition def = moduleDefinitionReader.read(new StringReader(xml));
                    def.setModuleRoot(moduleRoot);
                    this.moduleDefinitions.put(def.getName(), def);
                }
                catch (Exception e) {
                    throw new ConfigurationException("can't read the module definition file [" + name + "].", e);
                }
            }

        }
        catch (Exception e) {
            throw new ConfigurationException("can't read the module definition files.", e);
        }
    }

    protected void addAdHocDefinitions() {
        // add adhoc definitions for already registered modules without an xml file (pseudo modules)
        Content modulesNode;
        try {
            modulesNode = ModuleLoader.getInstance().getModulesNode();
        }
        catch (RepositoryException e) {
            log.error("can't add ad hoc module definitions", e);
            return;
        }

        for (Iterator iter = modulesNode.getChildren().iterator(); iter.hasNext();) {
            Content moduleNode = (Content) iter.next();

            String name = moduleNode.getName();
            String version = NodeDataUtil.getString(moduleNode, "version", "");
            String className = NodeDataUtil.getString(ContentRepository.CONFIG, moduleNode.getHandle()
                + "/Register/class", null); // TODO Register ??

            if (!this.moduleDefinitions.containsKey(name) && StringUtils.isNotEmpty(className)) {
                log.warn("no proper module definition file found for [{}]: will add an adhoc definition", name);
                ModuleDefinition def = new ModuleDefinition(name, version, className);
                this.moduleDefinitions.put(def.getName(), def);
            }
        }
    }

    /**
     * Check if the dependencies are ok
     * @throws MissingDependencyException
     * @deprecated see info.magnolia.module.reader.DependencyChecker
     */
    protected void checkDependencies() throws MissingDependencyException {
        for (MapIterator iter = this.moduleDefinitions.orderedMapIterator(); iter.hasNext();) {
            iter.next();
            ModuleDefinition def = (ModuleDefinition) iter.getValue();

            for (Iterator iterator = def.getDependencies().iterator(); iterator.hasNext();) {
                DependencyDefinition dep = (DependencyDefinition) iterator.next();
                if (!dep.isOptional()) {
                    checkDependency(def, dep);
                }
            }
        }
    }

    /**
     * @deprecated see info.magnolia.module.reader.DependencyChecker
     */
    protected void checkDependency(ModuleDefinition def, DependencyDefinition dep) throws MissingDependencyException {
        if (!this.moduleDefinitions.containsKey(dep.getName())){
            String msg = "missing dependency: module ["
                + def.getName()
                + "] needs ["
                + dep.getName()
                + "]";
            log.error(msg);
            throw new MissingDependencyException(msg);
        }

        ModuleDefinition instDef = this.getModuleDefinition(dep.getName());

        checkDependencyVersion(def, dep, instDef);
    }

    /**
     * @deprecated see info.magnolia.module.reader.DependencyChecker
     */
    protected void checkDependencyVersion(ModuleDefinition def, DependencyDefinition dep, ModuleDefinition instDef) throws MissingDependencyException {
        // check version
        String depVersion = dep.getVersion();
        String instVersion = instDef.getVersion();

        // ignore ${project.version}
        if(instVersion.equals("${project.version}")){
            log.info("module " + dep.getName() + " has a dynamic version [" + instVersion + "]. checks ignored" );
            return;
        }

        // check if only bugfix release is different
        // TODO better check
        int indexOfDifference = StringUtils.indexOfDifference(depVersion, instVersion);
        if( indexOfDifference != -1 && indexOfDifference < 3){
            String msg = "wrong version dependency: module ["
                + def.getName()
                + "] needs ["
                + dep.getName() + " " + depVersion
                + "] but [" + dep.getName() + " " + instVersion + "] is installed";
            log.error(msg);
            throw new MissingDependencyException(msg);
        }
        else if(indexOfDifference != -1){
            log.info("module ["
                + def.getName()
                + "] needs version ["
                + dep.getName() + " " + depVersion
                + "] and [" + dep.getName() + " " + instVersion + "] is installed. This version seems to be ok");
        }
    }

    /**
     * Sort all the definitions by the dependency level
     * @deprecated see info.magnolia.module.reader.DependencyChecker
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
     * Register the modules. This is separated of the init method to allow the usage of the descripors in advance.
     */
    public void registerModules() {
        
        addAdHocDefinitions();
        
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

            Module module = (Module) ClassUtil.newInstance(def.getClassName());
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
                if (registerState == Module.REGISTER_STATE_INSTALLATION) {
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
     * @deprecated see info.magnolia.module.reader.DependencyChecker
     */
    protected int calcDependencyLevel(ModuleDefinition def) {
        if (def.getDependencies() == null || def.getDependencies().size() == 0) {
            return 0;
        }
        List dependencyLevels = new ArrayList();
        for (Iterator iter = def.getDependencies().iterator(); iter.hasNext();) {
            DependencyDefinition dep = (DependencyDefinition) iter.next();
            ModuleDefinition depDef = this.getModuleDefinition(dep.getName());
            if (depDef == null && !dep.isOptional()) {
                throw new RuntimeException("Missing definition for module:" + dep.getName());
            } else if (depDef != null){
                dependencyLevels.add(new Integer(calcDependencyLevel(depDef)));
            }
        }
        return ((Integer) Collections.max(dependencyLevels)).intValue() + 1;
    }

    /**
     * Returns the definition of this module
     * @param moduleName
     *
     * @deprecated use ModuleRegistry
     */
    public ModuleDefinition getModuleDefinition(String moduleName) {
        return (ModuleDefinition) this.moduleDefinitions.get(moduleName);
    }

    /**
     * @return Returns the moduleDefinitions.
     * @deprecated use ModuleRegistry
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
        URL dtdUrl = getClass().getResource("/info/magnolia/module/model/module.dtd");

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
        Bootstrapper.bootstrapRepository(ContentRepository.CONFIG, new Bootstrapper.BootstrapFilter() {

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
        AlertUtil.setMessage("system.restart", MgnlContext.getSystemContext());
        this.restartNeeded = b;
    }
}
