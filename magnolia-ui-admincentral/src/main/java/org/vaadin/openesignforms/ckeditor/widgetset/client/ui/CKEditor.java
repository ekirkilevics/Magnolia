// CKEditor for Vaadin - Widget linkage for using CKEditor within a Vaadin application.
// Copyright (C) 2010 Yozons, Inc.
//
// This software is released under the Apache License 2.0 <http://www.apache.org/licenses/LICENSE-2.0.html>
//
package org.vaadin.openesignforms.ckeditor.widgetset.client.ui;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Wrapper around CKEDITOR.editor js object
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
	 	var rule = @org.vaadin.openesignforms.ckeditor.widgetset.client.ui.CKEditorService::convertJavaScriptStringToObject(Ljava/lang/String;)(jsRule);
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
 			ev.listenerData.@org.vaadin.openesignforms.ckeditor.widgetset.client.ui.CKEditorService.CKEditorListener::onBlur()();
    	}, null, listener);
    	
	 	this.on( 'focus', function( ev ) {
 			ev.listenerData.@org.vaadin.openesignforms.ckeditor.widgetset.client.ui.CKEditorService.CKEditorListener::onFocus()();
    	}, null, listener);
    	
     	this.on( 'vaadinsave', function( ev ) {
	 		ev.listenerData.@org.vaadin.openesignforms.ckeditor.widgetset.client.ui.CKEditorService.CKEditorListener::onSave()();
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
