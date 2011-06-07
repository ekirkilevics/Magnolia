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
 * GWT wrapper for CKEDITOR for use by our Vaadin-based CKEditorService.
 * <p>
 * Adapted from http://vaadin.com/directory#addon/ckeditor-wrapper-for-vaadin.
 */
public class CKEditorService {

    /**
     * Use this method to load editor to given identifier.
     *
     * @param id the string DOM &lt;div&gt; 'id' attribute value for the element you want to replace with CKEditor
     * @param listener the CKEditorService.CKEditorListener will get notified when the editor instance is ready, changed, etc.
     * @param jsInPageConfig the String possible custom "in page" configuration; note that this must be an expected JSON for the CKEDITOR in page config.
     * sent "as is" without any real syntax or security testing, so be sure you know it's valid and not malicious,
     * such as: <code>{toolbar : 'Basic', language : 'en'}</code>
     * @param jsCustomConfig path to the js custom configuration file.
     */
    public static native JavaScriptObject loadEditor(String id, CKEditorService.CKEditorListener listener, String jsInPageConfig, String jsCustomConfig)
    /*-{
         // Build our inPageConfig object based on the JSON jsInPageConfig sent to us.
         var inPageConfig = @info.magnolia.ckeditor.widgetset.client.ui.CKEditorService::convertJavaScriptStringToObject(Ljava/lang/String;)(jsInPageConfig);

         var myEditor;

         if ( inPageConfig ) {
            myEditor = $wnd.CKEDITOR.appendTo( id, inPageConfig );
         } else {

            myEditor = $wnd.CKEDITOR.appendTo( id );
            if (jsCustomConfig){
                $wnd.CKEDITOR.replace( id,
                        {
                            customConfig : jsCustomConfig
                        });
               }
         }


         // The 'listener' passed to us is used as 'listenerData' for the callback.
        myEditor.on( 'instanceReady', function( ev ) {
            ev.listenerData.@info.magnolia.ckeditor.widgetset.client.ui.CKEditorService.CKEditorListener::onInstanceReady()();
        }, null, listener);

        return myEditor;

    }-*/
    ;

    public native static String version()
    /*-{
        return $wnd.CKEDITOR.version;
    }-*/;

    public native static void overrideBlurToForceBlur()
    /*-{
        $wnd.CKEDITOR.focusManager.prototype['blur'] =  $wnd.CKEDITOR.focusManager.prototype['forceBlur'];
    }-*/;

    /**
     * Returns a javascript CKEDITOR.editor instance for given id.
     *
     * @param id the String id of the editor instance
     * @return the overlay for CKEDITOR.editor or null in not yet initialized
     */
    public native static CKEditor get(String id)
    /*-{
        return $wnd.CKEDITOR.instances[ id ];
    }-*/;

    // TODO: Never tested yet
    public native static void addStylesSet(String name, String jsStyles)
    /*-{
         var styles = @info.magnolia.ckeditor.widgetset.client.ui.CKEditorService::convertJavaScriptStringToObject(Ljava/lang/String;)(jsStyles);
        $wnd.CKEDITOR.addStylesSet(name,styles);
    }-*/;


    // TODO: Never tested yet
    public native static void addTemplates(String name, String jsDefinition)
    /*-{
         var definition = @info.magnolia.ckeditor.widgetset.client.ui.CKEditorService::convertJavaScriptStringToObject(Ljava/lang/String;)(jsDefinition);
        $wnd.CKEDITOR.addTemplates(name,definition);
    }-*/;

    public native static JavaScriptObject convertJavaScriptStringToObject(String jsString)
    /*-{
        try {
             return eval('('+jsString+')');
         } catch (e) {
             alert('convertJavaScriptStringToObject() INVALID JAVASCRIPT: ' + jsString);
             return null;
         }
    }-*/;


    /**
     * An interface for the VCKEditorTextField to get events from the CKEditor.
     */
    public interface CKEditorListener {
        public void onInstanceReady();
        public void onBlur();
        public void onFocus();
        public void onSave();
    }

}
