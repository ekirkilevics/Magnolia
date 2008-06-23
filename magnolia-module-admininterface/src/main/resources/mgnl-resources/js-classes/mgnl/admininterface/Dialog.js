/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 2008 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
classDef("mgnl.admininterface.Dialog",
    {
        open: function(dialogName, repository, path, nodeName){
            mgnlOpenDialog(path, null , nodeName, null, repository, ".magnolia/dialogs/" + dialogName + ".html");
        }
    }
);