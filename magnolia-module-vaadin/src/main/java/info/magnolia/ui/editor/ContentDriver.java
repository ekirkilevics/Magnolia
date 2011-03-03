/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.editor;

import info.magnolia.exception.RuntimeRepositoryException;

import java.util.Calendar;
import javax.jcr.Node;
import javax.jcr.RepositoryException;


/**
 * Automates editing of entities defined by the content model.
 *
 * @author tmattsson
 */
public class ContentDriver extends AbstractDriver<Node> {

    public void edit(Node node) {

        // TODO default values should also be set here

        try {
            for (Editor<Object> editor : getView().getEditors()) {
                String name = editor.getName();
                Class<?> type = editor.getType();
                if (type.equals(String.class)) {
                    if (node.hasProperty(name))
                        editor.setValue(node.getProperty(name).getString());
                } else if (type.equals(Calendar.class)) {
                    if (node.hasProperty(name))
                        editor.setValue(node.getProperty(name).getDate());
                }
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void flush(Node node) {

        // TODO should clear previous errors

        try {
            for (Editor<Object> editor : getView().getEditors()) {
                String name = editor.getName();
                Class<?> type = editor.getType();
                Object value = editor.getValue();
                if (!hasErrors(name)) {
                    if (type.equals(String.class)) {
                        node.setProperty(name, (String) value);
                    } else if (type.equals(Calendar.class)) {
                        node.setProperty(name, (Calendar) value);
                    }
                }
            }
            if (!hasErrors())
                node.getSession().save();
            else {
                for (EditorError error : super.getErrors()) {
                    if (error.getEditor() instanceof HasEditorErrors) {
                        HasEditorErrors has = (HasEditorErrors) error.getEditor();
                        has.showErrors(getErrors());
                    }
                }
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
}
