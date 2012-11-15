/**
 * This file Copyright (c) 2012-2012 Magnolia International
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
package info.magnolia.module.admininterface;

import java.lang.reflect.Constructor;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.cms.gui.dialog.DialogFactory;

/**
 * Provides a dialog configured in the repository.
 *
 * @version $Id$
 * @see DialogHandlerProvider
 * @see DialogHandlerManager
 * @see ConfiguredDialogHandlerManager
 */
public class ConfiguredDialogHandlerProvider implements DialogHandlerProvider {

    private String id;
    private Content configNode;
    private Class<? extends DialogMVCHandler> dialogHandlerClass;

    public ConfiguredDialogHandlerProvider(String id, Content configNode, Class<? extends DialogMVCHandler> dialogHandlerClass) {
        this.id = id;
        this.configNode = configNode;
        this.dialogHandlerClass = dialogHandlerClass;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Content getDialogConfigNode() {
        return configNode;
    }

    @Override
    public Dialog getDialog() throws RepositoryException {
        return DialogFactory.getDialogInstance(null, null, null, configNode);
    }

    @Override
    public DialogMVCHandler getDialogHandler(HttpServletRequest request, HttpServletResponse response) {

        try {
            Content configNode = this.configNode;
            if (configNode != null) {
                try {
                    Constructor<? extends DialogMVCHandler> constructor = dialogHandlerClass.getConstructor(new Class[]{
                            String.class,
                            HttpServletRequest.class,
                            HttpServletResponse.class,
                            Content.class});
                    return constructor.newInstance(id, request, response, configNode);
                } catch (NoSuchMethodException e) {
                    Constructor<? extends DialogMVCHandler> constructor = dialogHandlerClass.getConstructor(new Class[]{
                            String.class,
                            HttpServletRequest.class,
                            HttpServletResponse.class});
                    return constructor.newInstance(id, request, response);
                }
            }

            Constructor<? extends DialogMVCHandler> constructor = dialogHandlerClass.getConstructor(new Class[]{
                    String.class,
                    HttpServletRequest.class,
                    HttpServletResponse.class});
            return constructor.newInstance(id, request, response);
        } catch (Exception e) {
            throw new InvalidDialogHandlerException(id, e);
        }
    }
}
