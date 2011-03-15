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
package info.magnolia.ui.framework.editor;

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
        private boolean consumed;

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

        public boolean isConsumed() {
            return consumed;
        }

        public void setConsumed(boolean consumed) {
            this.consumed = consumed;
        }
    }

    /**
     * Interface for use by subclasses when iterating the editor hierarchy.
     */
    protected static interface EditorVisitor {
        void visit(Editor editor);
    }

    private List<EditorError> errors = new ArrayList<EditorError>();
    private HasEditors view;

    public void initialize(HasEditors view) {
        this.view = view;

        // TODO should this really happen here?

        visitEditors(view, new EditorVisitor() {
            public void visit(final Editor editor) {
                if ((editor instanceof HasEditorDelegate) && (editor instanceof ValueEditor)) {
                    ((HasEditorDelegate)editor).setDelegate(new EditorDelegate() {
                        public void recordError(String message, Object value) {
                            addError(((ValueEditor)editor).getPath(), editor, message, value);
                        }
                    });
                }
            }
        });
    }

    protected void addError(String path, Editor editor, String message, Object value) {
        this.errors.add(new SimpleEditorError(path, editor, message, value));
    }

    protected void clearErrors() {
        this.errors.clear();
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public boolean hasErrors(String name) {
        for (EditorError error : errors) {
            if (error.getPath().equals(name))
                return true;
        }
        return false;
    }

    public List<EditorError> getErrors() {
        ArrayList<EditorError> unconsumedErrors = new ArrayList<EditorError>();
        for (EditorError error : errors) {
            if (!error.isConsumed())
                unconsumedErrors.add(error);
        }
        return unconsumedErrors;
    }

    public List<EditorError> getAllErrors() {
        return Collections.unmodifiableList(errors);
    }

    protected HasEditors getView() {
        return view;
    }

    protected void visitEditors(HasEditors hasEditors, EditorVisitor visitor) {
        for (Editor editor : hasEditors.getEditors()) {
            visitor.visit(editor);
            if (editor instanceof HasEditors)
                visitEditors((HasEditors) editor, visitor);
        }
    }
}
