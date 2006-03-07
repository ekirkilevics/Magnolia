package info.magnolia.cms.module;

import info.magnolia.cms.core.Content;

import java.util.jar.JarFile;

/**
 * Default implementation. Imports bootstrap files, registers dialogs ,...
 * @author philipp
 */
public abstract class ModuleImpl implements Module {

	/**
	 * The configuration passed by the initializer (read from the repository)
	 */
	private ModuleConfig config;
	
	/**
	 * Initialize the module. Registers the dialogs, paragraphs and templates of this modules.
	 * Calls the abstract onInit method.
	 */
	public void init(ModuleConfig moduleConfig) throws InvalidConfigException {
		setConfig(config);
		// register dialogs
		// register paragraphs
		// register templates
		onInit();
	}


	/**
	 * Calles onRegister if not yet installed after it loaded the bootstrapfiles
	 * of this module
	 */
	public void register(String moduleName, String version, Content moduleNode,
			JarFile jar, int registerState) throws RegisterException {
		if(registerState == REGISTER_STATE_INSTALLATION || registerState == REGISTER_STATE_NEW_VERSION){
			// bootsrap the module files
			
			// copy the content of mgnl-content to the webapp
			
			onRegister(moduleName, version, moduleNode, jar, registerState);
		}
	}

	public void destroy() {
	}

	protected void setConfig(ModuleConfig config) {
		this.config = config;
	}

	protected ModuleConfig getConfig() {
		return config;
	}

	/**
	 * Template pattern. Implement to perfome some module specific stuff
	 * @param moduleName
	 * @param version
	 * @param moduleNode
	 * @param jar
	 * @param registerState
	 */
	abstract void onRegister(String moduleName, String version, Content moduleNode, JarFile jar, int registerState);

	/**
	 * Template pattern. Implement to perfome somem module specific stuff
	 *
	 */
	abstract void onInit();
}
