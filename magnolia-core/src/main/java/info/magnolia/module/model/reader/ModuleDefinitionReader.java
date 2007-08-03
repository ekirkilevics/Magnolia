/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.model.reader;

import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.model.ModuleDefinition;

import java.io.Reader;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface ModuleDefinitionReader {

    /**
     * Reads all found module definitions.
     * @return a Map<String, ModuleDefinition> where the key is the module name.
     */
    Map readAll() throws ModuleManagementException;

    ModuleDefinition read(Reader in) throws ModuleManagementException;

    ModuleDefinition readFromResource(String resourcePath) throws ModuleManagementException;

}
