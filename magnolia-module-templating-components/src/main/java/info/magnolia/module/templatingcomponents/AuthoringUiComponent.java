/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.templatingcomponents;

import java.io.IOException;

/**
 * Implementations of AuthoringUiComponent render specific "components" in templates.
 * They're usually exposed to templating engines via a specific wrapper; see the freemarker and jsp subpackages for examples.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $) 
 */
public interface AuthoringUiComponent {

    void render(Appendable out) throws IOException;

    /**
     * This method should be called by templating-engine wrappers after rendering the component AND its body.
     * Certain components (SingletonParagraphBar for instance) will need to let the wrapper render their body,
     * and call this afterwards.
     * Can be used, for instance, as a "cleanup" mechanism, if the component modified the context.
     */
    void postRender();
}
