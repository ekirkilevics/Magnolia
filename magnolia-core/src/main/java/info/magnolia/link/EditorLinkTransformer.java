/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.link;

/**
 * Modifies links to the format suitable for the Editor. <br/>
 * <ul>
 * <li>Applies URI2Repository mappings to any passed link.</li>
 * <li>Adds the context path only to the binaries.</li>
 * <li>Doesn't do any i18n translation of the links</li>
 * </ul>
 * @author had
 * @version $Id: EditorLinkTransformer.java 21024 2009-01-06 20:58:05Z gjoseph $
 */
public class EditorLinkTransformer implements LinkTransformer {

    protected LinkTransformer binaryTransformer = new AbsolutePathTransformer(true,true,false);

    protected LinkTransformer linkTransformer = new AbsolutePathTransformer(false,true,false);

    public String transform(UUIDLink uuidLink) {
        // TODO use a better way to determine if this is a binary
        // this should actually not even be here because totally related to the fck editor
        if(uuidLink.getNodeData()!=null){
            return binaryTransformer.transform(uuidLink);
        }
        else{
            return linkTransformer.transform(uuidLink);
        }
    }
}

