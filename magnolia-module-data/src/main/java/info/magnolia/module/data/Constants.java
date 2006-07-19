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

package info.magnolia.module.data;

/**
 * 
 *
 * @author Enrico Kufahl (BeagleSoft GmbH)
 * @version $Revision$ ($Author$)
 * 
 */
public class Constants {

	public static final String DATA_COMMAND_CONTEXT_ITEM_TYPE 				= "itemType"; 
	public static final String DATA_COMMAND_CONTEXT_ACTIVATION_SYNDICATOR 	= "activationSyndicator"; 
	
	public static final String TYPE_COMMAND_PARAM						= "typeAction"; 
	public static final String TYPE_COMMAND_CONTEXT_DEFAULT_IMPORTER	= "defaultImporter"; 
	public static final String TYPE_COMMAND_IMPORT						= "importData"; 

	
	public static final String DATA_COMMAND_PARAM				= "dataAction"; 
	public static final String DATA_COMMAND_DELETE_ALL 			= "deleteAll"; 
	public static final String DATA_COMMAND_ACTIVATE_ALL 		= "activateAll"; 
	public static final String DATA_COMMAND_DEACTIVATE_ALL 		= "deactivateAll"; 
	
	public static final String DATA_DIALOG_PRE				= "moduleDataData";
	public static final String DATA_TREE_PRE				= "moduleDataData";
	
	public static final String TYPES_NODE 					= "types";
	public static final String TYPES_NODE_PATH 				= "/modules/data/config/" + TYPES_NODE;

	public static final String TYPE_NAME 					= "name";
	public static final String TYPE_TITLE 					= "title";
	
	public static final String TYPE_DIALOGS_NODE 			= "dialogs";
	public static final String TYPE_DIALOG_NAME 			= "name";
	public static final String TYPE_DIALOG_TYPE 			= "itemType";
	public static final String TYPE_DIALOG_CLASS 			= "class";
	public static final String TYPE_DIALOG_SAVE_HANDLER 	= "saveHandler";
	public static final String TYPE_DIALOG_I18N_BASE 		= "i18nBasename";
	public static final String TYPE_DIALOG_ICON 			= "icon";

	public static final String TYPE_DIALOG_FIELD_VISIBILITY 	= "visible";
	public static final String TYPE_DIALOG_FIELD_NAME 			= "name";
	public static final String TYPE_DIALOG_FIELD_TYPE 			= "controlType";

	public static final String TYPE_TREES_NODE 			= "trees";
	public static final String TYPE_TREE_CLASS 			= "class";
	public static final String TYPE_TREE_NAME 			= "name";
	public static final String TYPE_TREE_REPOSITORY 	= "repository";
	public static final String TYPE_TREE_DIALOG 		= "dialogName";

	

	public static final String FIELD_TYPE_REFERENCE_MULTI_SELECT 		= "dataReference";

	
	public static final String IMPORT_NODE 										= "importer";
	// activation of the new content after import
	public static final String IMPORT_ACTIVATE									= "activateImport"; 
	// delete (deactivating) old content
	public static final String IMPORT_DELETE_EXISTING							= "deleteExisting";
	// java handler class for the importer 
	public static final String IMPORT_HANDLER 									= "handler";
	// indicates the default importer accessible via menu 
	public static final String IMPORT_DEFAULT 									= "default";
	// repository for the importer 
	public static final String IMPORT_REPOSITORY 								= "repository";

	// configuration of the automatic import
	public static final String IMPORT_AUTOMATIC_EXECUTION_NODE					= "automaticExecution";
	// enable automatic import
	public static final String IMPORT_AUTOMATIC_EXECUTION_ENABLE				= "enabled";
	// start time day of week 1 - 7 (1 = Sunday)
	public static final String IMPORT_AUTOMATIC_EXECUTION_START_DAY				= "startDayOfWeek"; 
	// start time hour of day (0-23)
	public static final String IMPORT_AUTOMATIC_EXECUTION_START_HOUR			= "startHourOfDay"; 
	// period for import repetition
	public static final String IMPORT_AUTOMATIC_EXECUTION_PERIOD				= "period";	
	// import types node (define the configuration for the dataTypes)
	public static final String IMPORT_TYPES_NODE								= "types";
	// import file for the special dataType
	public static final String IMPORT_TYPE_FILE									= "file";

	
	
}
