/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.link;

/**
 * Add the context path only to the binaries.
 * @author philipp
 * @version $Id$
 */
public class EditorLinkTransformer implements PathToLinkTransformer {

    protected PathToLinkTransformer binaryTransformer = new AbsolutePathTransformer(true,true,false);

    protected PathToLinkTransformer linkTransformer = new AbsolutePathTransformer(false,true,false);

    public String transform(UUIDLink uuidLink) {
        // TODO use a better way to determine if this is a binary
        // this should actually not even be here because totaly related to the fck editor
        if(uuidLink.getNodeData()!=null){
            return binaryTransformer.transform(uuidLink);
        }
        else{
            return linkTransformer.transform(uuidLink);
        }
    }
}
