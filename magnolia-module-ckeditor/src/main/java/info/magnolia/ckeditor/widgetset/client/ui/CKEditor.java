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
package info.magnolia.ckeditor.widgetset.client.ui;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Wrapper around CKEDITOR.editor js object. Copied verbatim from source from addon http://vaadin.com/directory#addon/ckeditor-wrapper-for-vaadin.
 */
public class CKEditor extends JavaScriptObject {

    protected CKEditor() {
    }

    public final native boolean checkDirty()
    /*-{
        return this.checkDirty();
    }-*/;

    public final native void resetDirty()
    /*-{
        this.resetDirty();
    }-*/;

    public final native String getData()
    /*-{
        return this.getData();
    }-*/;

    public final native void setData(String htmlData)
    /*-{
        return this.setData(htmlData);
    }-*/;

    public final native void setWriterRules(String tagName, String jsRule)
    /*-{
         var rule = @info.magnolia.ckeditor.widgetset.client.ui.CKEditorService::convertJavaScriptStringToObject(Ljava/lang/String;)(jsRule);
        this.dataProcessor.writer.setRules(tagName, rule);
    }-*/;

    public final native void setWriterIndentationChars(String indentationChars)
    /*-{
        this.dataProcessor.writer.indentationChars = indentationChars;
    }-*/;

    public final native void instanceReady(CKEditorService.CKEditorListener listener)
    /*-{
         // The 'listener' passed to us is used as 'listenerData' for the callback.
         this.on( 'blur', function( ev ) {
             ev.listenerData.@info.magnolia.ckeditor.widgetset.client.ui.CKEditorService.CKEditorListener::onBlur()();
        }, null, listener);

         this.on( 'focus', function( ev ) {
             ev.listenerData.@info.magnolia.ckeditor.widgetset.client.ui.CKEditorService.CKEditorListener::onFocus()();
        }, null, listener);

         this.on( 'vaadinsave', function( ev ) {
             ev.listenerData.@info.magnolia.ckeditor.widgetset.client.ui.CKEditorService.CKEditorListener::onSave()();
        }, null, listener);
    }-*/;

    public final native void execCommand(String cmd)
    /*-{
        this.execCommand(cmd);
    }-*/;

    public final native void updateElement()
    /*-{
        this.updateElement();
    }-*/;

    public final native void destroy(boolean noUpdate)
    /*-{
        this.destroy(noUpdate);
    }-*/;

    public final native void destroy()
    /*-{
        this.destroy();
    }-*/;
}
