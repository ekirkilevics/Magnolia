/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.cms.gui.i18n;

import info.magnolia.cms.gui.control.Control;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.objectfactory.Components;


/**
 * @author pbaerfuss
 * @version $Id$
 *
 */
public interface I18nAuthoringSupport {
    
    /**
     * The language chooser, if not null, will be displayed in the main bar
     */
    Control getLanguageChooser();
    
    /**
     * Will provide i18n authoring by extending the dialog.
     */
    void i18nIze(Dialog dialog);
    
    boolean isEnabled();

    static class Factory{
        public static I18nAuthoringSupport getInstance(){
            return Components.getSingleton(I18nAuthoringSupport.class);
        }
    }
    
}
