/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admincentral.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract support class for classes implementing the Driver interface.
 *
 * @param <T> the type of entity to edit
 * @author tmattsson
 */
public abstract class AbstractDriver<T> implements Driver<T> {

    /**
     * Trivial implementation of EditorError.
     *
     * @author tmattsson
     */
    private static class SimpleEditorError implements EditorError {

        private String path;
        private Editor editor;
        private String message;
        private Object value;

        private SimpleEditorError(String path, Editor editor, String message, Object value) {
            this.path = path;
            this.editor = editor;
            this.message = message;
            this.value = value;
        }

        public String getPath() {
            return path;
        }

        public Editor getEditor() {
            return editor;
        }

        public String getMessage() {
            return message;
        }

        public Object getValue() {
            return value;
        }
    }

    private List<EditorError> errors = new ArrayList<EditorError>();

    protected void addError(String path, Editor editor, String message, Object value) {
        this.errors.add(new SimpleEditorError(path, editor, message, value));
    }

    protected void clearErrors() {
        this.errors.clear();
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public List<EditorError> getErrors() {
        return Collections.unmodifiableList(this.errors);
    }
}
