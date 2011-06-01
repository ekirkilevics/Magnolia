// Copyright (C) 2010-2011 Yozons, Inc.
// CKEditor for Vaadin - Widget linkage for using CKEditor within a Vaadin application.
//
// This software is released under the Apache License 2.0 <http://www.apache.org/licenses/LICENSE-2.0.html>
//
package org.vaadin.openesignforms.ckeditor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

/**
 * TODO clean up and adapt to our config needs. Move to separate module. Do we want to support inPageConfig?
 *<p>
 * Configuration utility for using the CKEditorTextField.  You can use this "config javascript builder" object for our
 * tested/common options, or just set the options using a JavaScript/JSON string as you prefer.
 */
public class CKEditorConfig implements java.io.Serializable {
	private static final long serialVersionUID = 4360029660001644525L;

	// If this is set, we'll just use it and ignore everything else.
	private String inPageConfig;

	private String customConfig;

	// Otherwise, we'll build the config based on settings contained here
	private HashMap<String,String> writerRules = null;
	private String writerIndentationChars = null;
	private LinkedList<String> extraPlugins = null;
	private LinkedList<String> removePlugins = null;
	private LinkedList<String> customToolbarLines = null;
	private Boolean toolbarCanCollapse = null;
	private Boolean disableNativeSpellChecker = null;
	private String resizeDir = null;
	private String width = null;
	private String height = null;
	private Integer baseFloatZIndex = null;
	private Boolean pasteFromWordNumberedHeadingToList = null;
	private String startupMode = null; // either "source" or "wysiwyg" (defaults to wysiwyg, so generally only used if you'd like to startup in source mode)
	private String[] contentsCssFiles = null;
	private String stylesCombo_stylesSet = null;
	private String bodyClass = null;

	private String filebrowserBrowseUrl = null;
	private String filebrowserUploadUrl = null;
	private String filebrowserWindowWidth = null; // defaults to 80% width
	private String filebrowserWindowHeight = null; // defaults to 70% height

	private String filebrowserImageBrowseUrl = null;
	private String filebrowserImageUploadUrl = null;
	private String filebrowserImageWindowWidth = null; // defaults to 80% width
	private String filebrowserImageWindowHeight = null; // defaults to 70% height

	private String filebrowserFlashBrowseUrl = null;
	private String filebrowserFlashUploadUrl = null;
	private String filebrowserLinkBrowseUrl = null;

	public CKEditorConfig() {
	}

	public String getInPageConfig() {
		if ( inPageConfig != null ) {
			return inPageConfig;
		}
		return null;

		// Build the JSON config
//		StringBuilder config = new StringBuilder(1024);
//		config.append("{ "); // we assume this is 2 chars in the buffer append routines to know whether anything more has been appended yet
//
//		if ( customToolbarLines != null ) {
//
//			StringBuilder buf = new StringBuilder();
//			ListIterator<String> iter = customToolbarLines.listIterator();
//			while( iter.hasNext() ) {
//				if ( buf.length() > 0 )
//					buf.append(",'/',");
//				String js = iter.next();
//				if ( js.startsWith("[") ) { // if it has a band, assume fully banded
//					buf.append(js);
//				} else {
//					buf.append("[").append(js).append("]"); // put entire line in one band
//				}
//			}
//
//			appendJSONConfig(config, "toolbar : 'Custom'");
//			appendJSONConfig(config, "toolbar_Custom : [" + buf.toString() + "]");
//		}
//
//		if ( toolbarCanCollapse != null ) {
//			appendJSONConfig(config, "toolbarCanCollapse : " + toolbarCanCollapse);
//		}
//
//		if ( resizeDir != null ) {
//			appendJSONConfig(config, "resize_dir : '" + resizeDir + "'");
//		}
//
//		if ( extraPlugins != null ) {
//			StringBuilder buf = new StringBuilder();
//			ListIterator<String> iter = extraPlugins.listIterator();
//			while( iter.hasNext() ) {
//				if ( buf.length() > 0 )
//					buf.append(",");
//				buf.append(iter.next());
//			}
//
//			appendJSONConfig(config, "extraPlugins : '" + buf.toString() + "'");
//		}
//
//		if ( removePlugins != null ) {
//			StringBuilder buf = new StringBuilder();
//			ListIterator<String> iter = removePlugins.listIterator();
//			while( iter.hasNext() ) {
//				if ( buf.length() > 0 )
//					buf.append(",");
//				buf.append(iter.next());
//			}
//
//			appendJSONConfig(config, "removePlugins : '" + buf.toString() + "'");
//		}
//
//		if ( width != null ) {
//			appendJSONConfig(config, "width : '" + width + "'");
//		}
//
//		if ( height != null ) {
//			appendJSONConfig(config, "height : '" + height + "'");
//		}
//
//		if ( baseFloatZIndex != null ) {
//			appendJSONConfig(config, "baseFloatZIndex : " + baseFloatZIndex);
//		}
//
//		if ( pasteFromWordNumberedHeadingToList != null ) {
//			appendJSONConfig(config, "pasteFromWordNumberedHeadingToList : " + pasteFromWordNumberedHeadingToList);
//		}
//
//		if ( startupMode != null ) {
//			appendJSONConfig(config, "startupMode : '" + startupMode + "'");
//		}
//
//		if ( contentsCssFiles != null && contentsCssFiles.length > 0 ) {
//			if ( contentsCssFiles.length == 1 ) {
//				appendJSONConfig(config, "contentsCss : '" + contentsCssFiles[0] + "'");
//			} else {
//				StringBuilder buf = new StringBuilder();
//				for( String file : contentsCssFiles ) {
//					if ( buf.length() > 0 )
//						buf.append(",");
//					buf.append("'").append(file).append("'");
//				}
//				appendJSONConfig(config, "contentsCss : [" + buf.toString() + "]");
//			}
//		}
//
//		if ( bodyClass != null ) {
//			appendJSONConfig(config, "bodyClass : '" + bodyClass + "'");
//		}
//
//		if ( disableNativeSpellChecker != null ) {
//			appendJSONConfig(config, "disableNativeSpellChecker : " + disableNativeSpellChecker);
//		}
//
//		if ( stylesCombo_stylesSet != null ) {
//			appendJSONConfig(config, "stylesCombo_stylesSet : '" + stylesCombo_stylesSet + "'");
//		}
//
//		if ( filebrowserBrowseUrl != null ) {
//			appendJSONConfig(config, "filebrowserBrowseUrl : '" + filebrowserBrowseUrl + "'");
//		}
//		if ( filebrowserUploadUrl != null ) {
//			appendJSONConfig(config, "filebrowserUploadUrl : '" + filebrowserUploadUrl + "'");
//		}
//		if ( filebrowserWindowWidth != null ) {
//			appendJSONConfig(config, "filebrowserWindowWidth : '" + filebrowserWindowWidth + "'");
//		}
//		if ( filebrowserWindowHeight != null ) {
//			appendJSONConfig(config, "filebrowserWindowHeight : '" + filebrowserWindowHeight + "'");
//		}
//
//		if ( filebrowserImageBrowseUrl != null ) {
//			appendJSONConfig(config, "filebrowserImageBrowseUrl : '" + filebrowserImageBrowseUrl + "'");
//		}
//		if ( filebrowserImageUploadUrl != null ) {
//			appendJSONConfig(config, "filebrowserImageUploadUrl : '" + filebrowserImageUploadUrl + "'");
//		}
//		if ( filebrowserImageWindowWidth != null ) {
//			appendJSONConfig(config, "filebrowserImageWindowWidth : '" + filebrowserImageWindowWidth + "'");
//		}
//		if ( filebrowserImageWindowHeight != null ) {
//			appendJSONConfig(config, "filebrowserImageWindowHeight : '" + filebrowserImageWindowHeight + "'");
//		}
//
//		if ( filebrowserFlashBrowseUrl != null ) {
//			appendJSONConfig(config, "filebrowserFlashBrowseUrl : '" + filebrowserFlashBrowseUrl + "'");
//		}
//		if ( filebrowserFlashUploadUrl != null ) {
//			appendJSONConfig(config, "filebrowserFlashUploadUrl : '" + filebrowserFlashUploadUrl + "'");
//		}
//		if ( filebrowserLinkBrowseUrl != null ) {
//			appendJSONConfig(config, "filebrowserLinkBrowseUrl : '" + filebrowserLinkBrowseUrl + "'");
//		}
//
//		config.append(" }");
//		return config.toString();
	}

	private StringBuilder appendJSONConfig(StringBuilder configBuf, String oneOptions) {
		if ( configBuf.length() > 2 )
			configBuf.append(", ");
		configBuf.append(oneOptions);
		return configBuf;
	}

	/**
	 * You can use this to just set the JavaScript/JSON notation for setting the 'in page config' option when the editor is
	 * created.  If you don't use this, you can use all of the other builder routines that set options that are then used
	 * to generate the JSON notation to use
	 * @param js the String JSON 'config' for the new editor instance.
	 */
	public void setInPageConfig(String js) {
		inPageConfig = js;
	}

	public boolean hasWriterRules() {
		return writerRules != null && ! writerRules.isEmpty();
	}
	public Set<String> getWriterRulesTagNames() {
		return writerRules == null ? new HashSet<String>() : writerRules.keySet();
	}
	public String getWriterRuleByTagName(String tagName) {
		return writerRules == null ? null : writerRules.get(tagName);
	}
	public synchronized void addWriterRules(String tagName, String jsRule) {
		if ( writerRules == null ) {
			writerRules = new HashMap<String,String>();
		}
		writerRules.put( tagName, jsRule );
	}

	// A convenience method to set a bunch of compact HTML rules.
	public void useCompactTags() {
		addWriterRules("p",  "{indent : false, breakBeforeOpen : true, breakAfterOpen : false, breakBeforeClose : false, breakAfterClose : true}" );
		addWriterRules("h1", "{indent : false, breakBeforeOpen : true, breakAfterOpen : false, breakBeforeClose : false, breakAfterClose : true}" );
		addWriterRules("h2", "{indent : false, breakBeforeOpen : true, breakAfterOpen : false, breakBeforeClose : false, breakAfterClose : true}" );
		addWriterRules("h3", "{indent : false, breakBeforeOpen : true, breakAfterOpen : false, breakBeforeClose : false, breakAfterClose : true}" );
		addWriterRules("h4", "{indent : false, breakBeforeOpen : true, breakAfterOpen : false, breakBeforeClose : false, breakAfterClose : true}" );
		addWriterRules("h5", "{indent : false, breakBeforeOpen : true, breakAfterOpen : false, breakBeforeClose : false, breakAfterClose : true}" );
		addWriterRules("h6", "{indent : false, breakBeforeOpen : true, breakAfterOpen : false, breakBeforeClose : false, breakAfterClose : true}" );
		addWriterRules("li", "{indent : true,  breakBeforeOpen : true, breakAfterOpen : false, breakBeforeClose : false, breakAfterClose : true}" );
	}

	public String getWriterIndentationChars() {
		return writerIndentationChars;
	}
	public boolean hasWriterIndentationChars() {
		return writerIndentationChars != null;
	}
	public void setWriterIndentationChars(String v) {
		writerIndentationChars = v;
	}

	public synchronized void addToExtraPlugins(String pluginName) {
		if ( extraPlugins == null ) {
			extraPlugins = new LinkedList<String>();
		}
		if ( ! extraPlugins.contains(pluginName) ) {
			extraPlugins.add(pluginName);
		}
		// If for whatever reason this plugin name is in the remove plugins, we remove it.
		if ( removePlugins != null && removePlugins.contains(pluginName) ) {
			removePlugins.remove(pluginName);
		}
	}

	/**
	 * This enables the vaadinsave plugin.  You will also need a custom toolbar with the entry 'VaadinSave' included to put it
	 * the specified position.
	 */
	public void enableVaadinSavePlugin() {
		addToExtraPlugins("vaadinsave");
	}


	/**
	 * This enables the 'tableresize' plugin. This is generally useful, so we make it stand out compared to other
	 * optional extra plugins.
	 */
	public void enableTableResizePlugin() {
		addToExtraPlugins("tableresize");
	}

	/**
	 * Convenience method for the Open eSignForms project sponsors to set the plugins and configuration in a common way needed.
	 */
	public void setupForOpenESignForms(String contextPath, String ckeditorContextIdInSession, String bodyCssClass, String... extraCssFiles) {
		addCustomToolbarLine("'Styles','Format','Bold','Italic','Underline','TextColor','BGColor','-','Font','FontSize','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'");
		addCustomToolbarLine("'Cut','Copy','Paste','PasteText','PasteFromWord','-','Find','Replace','-','Undo','Redo','-','NumberedList','BulletedList','-','Outdent','Indent','CreateDiv','-','Table','HorizontalRule','PageBreak','SpecialChar','-','Image','Link','-','Source','ShowBlocks'");
		setToolbarCanCollapse(false);

		enableTableResizePlugin();

		setHeight("300px");
		setBaseFloatZIndex(32000);

		disableSpellChecker();
		setDisableNativeSpellChecker(false);
		disableResizeEditor();
		setPasteFromWordNumberedHeadingToList(true);

		useCompactTags();
		addWriterRules("script", "{indent : false, breakBeforeOpen : true, breakAfterOpen : true, breakBeforeClose : true, breakAfterClose : true}" );
		addWriterRules("style",  "{indent : false, breakBeforeOpen : true, breakAfterOpen : true, breakBeforeClose : true, breakAfterClose : true}" );
		setWriterIndentationChars("    ");

		setStylesCombo_stylesSet("esfStyleSet:" + contextPath + "/static/esf/esfStyleSet.js");
		if ( extraCssFiles == null )
			setContentsCss(contextPath + "/static/esf/esf.css");
		else
		{
			String[] cssFiles = new String[extraCssFiles.length+1];
			cssFiles[0] = contextPath + "/static/esf/esf.css";
			for(int i=0; i < extraCssFiles.length; ++i )
				cssFiles[i+1] = contextPath + extraCssFiles[i];
			setContentsCss(cssFiles);
		}
		if ( bodyCssClass == null || "".equals(bodyCssClass) )
			bodyCssClass = "esf";
		else
			bodyCssClass = "esf " + bodyCssClass;
		setBodyClass(bodyCssClass);
		setFilebrowserImageBrowseUrl(contextPath + "/ckeditorImageBrowser.jsp?ccid="+ckeditorContextIdInSession);
		setFilebrowserImageWindowWidth("600");
		setFilebrowserImageWindowHeight("500");
	}

	public synchronized void addToRemovePlugins(String pluginName) {
		if ( removePlugins == null ) {
			removePlugins = new LinkedList<String>();
		}
		if ( ! removePlugins.contains(pluginName) ) {
			removePlugins.add(pluginName);
		}
		// If for whatever reason this plugin is defined in the extraPlugins list, we remove it.
		if ( extraPlugins != null && extraPlugins.contains(pluginName) ) {
			extraPlugins.remove(pluginName);
		}
	}

	public void disableElementsPath() {
		addToRemovePlugins("elementspath");
	}

	public void disableResizeEditor() {
		addToRemovePlugins("resize");
	}

	public void disableSpellChecker() {
		addToRemovePlugins("scayt");
	}

	public void setDisableNativeSpellChecker(boolean v) {
		disableNativeSpellChecker = new Boolean(v);
	}



	/**
	 * If no custom toolbar is defined, it will use the Full toolbar by default (config.toolbar = 'Full').
	 *
	 * Note that each line is generally one 'band' so that they all appear together.
	 * For example:
	 * 'Styles','Format','Font','FontSize','TextColor','BGColor','Maximize', 'ShowBlocks','-','About'
	 * we treat this as:
	 * ['Styles','Format','Font','FontSize','TextColor','BGColor','Maximize', 'ShowBlocks','-','About']
	 *
	 * If the toolbarLineJS you add begins with a '[' then we assume you are building your own bands and won't put in any bands.
	 * For example:
	 * ['Styles','Format','Font','FontSize'],['TextColor','BGColor'],['Maximize', 'ShowBlocks','-','About']
	 * The above will be on one line, and there will be the 3 banded items (and we use it 'as is').
	 *
	 * Add a custom toolbar line of options.  It is basically a list of features separated by commas, or '-' for the separator, such as:
	 * 'Styles','Format','Bold','Italic','TextColor','BGColor','-','Font','FontSize','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','Image','Link'
	 * 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo','-','NumberedList','BulletedList','-','Outdent','Indent','-','Table','HorizontalRule','-','Maximize','-','Source','ShowBlocks','-','VaadinSave'
	 * 'Styles','Format','Bold','Italic','-','VaadinSave'
	 * @param toolbarLineJS
	 */
	public synchronized void addCustomToolbarLine(String toolbarLineJS) {
		if ( customToolbarLines == null ) {
			customToolbarLines = new LinkedList<String>();
		}
		if ( ! customToolbarLines.contains(toolbarLineJS) ) {
			customToolbarLines.add(toolbarLineJS);
		}
	}

	public void setToolbarCanCollapse(boolean v) {
		toolbarCanCollapse = v;
	}

	public enum RESIZE_DIR { BOTH, VERTICAL, HORIZONTAL }

	public void setResizeDir(RESIZE_DIR dir) {
		if ( dir.equals(RESIZE_DIR.BOTH) ) {
			resizeDir = "both";
		} else if ( dir.equals(RESIZE_DIR.VERTICAL) ) {
			resizeDir = "vertical";
		} else if ( dir.equals(RESIZE_DIR.HORIZONTAL) ) {
			resizeDir = "horizontal";
		} else {
			resizeDir = null; // won't set it since it's not what we expect
		}
	}

	public void setWidth(String cssSize) {
		width = cssSize;
	}

	public void setHeight(String cssSize) {
		height = cssSize;
	}

	public void setBaseFloatZIndex(int zIndex) {
		baseFloatZIndex = zIndex;
	}

	public void setPasteFromWordNumberedHeadingToList(boolean v)
	{
		pasteFromWordNumberedHeadingToList = v;
	}

	public void setStartupModeSource()
	{
		startupMode = "source";
	}
	public void setStartupModeWysiwyg()
	{
		startupMode = "wysiwyg";
	}

	/**
	 * Sets the file or list of files for the contents to be applied to the editor.  Used to set the styles that will
	 * be used in the page where the HTML will actually be rendered.
	 * @param cssFiles zero or more String file URL paths -- for same system, starting with context path is recommended: /myapp/path/to/cssfile.css
	 */
	public void setContentsCss(String... cssFiles) {
		contentsCssFiles = cssFiles;
	}


	/**
	 * Sets the body class for the HTML editor, so if you render the results in a body with a given class, you can give it here
	 * too so that the editor will show the same styles you may have in your contents css.
	 * @param bc
	 */
	public void setBodyClass(String bc) {
		bodyClass = bc;
	}

	/**
	 * Sets the stylesCombo_stylesSet config option, which is the registered style name
	 * @param styleSetSpec
	 */
	public void setStylesCombo_stylesSet(String styleSetSpec) {
		stylesCombo_stylesSet = styleSetSpec;
	}

	/**
	 * Sets the filebrowserBrowseUrl config option, which is an URL that will list files a user can select from
	 * @param url
	 */
	public void setFilebrowserBrowseUrl(String url) {
		filebrowserBrowseUrl = url;
	}

	/**
	 * Sets the filebrowserUploadUrl config option, which is an URL that will allow a file to be uploaded
	 * @param url
	 */
	public void setFilebrowserUploadUrl(String url) {
		filebrowserUploadUrl = url;
	}

	/**
	 * Sets the filebrowserWindowWidth config option, which is a width size spec (like "600" for 600 pixels); CKEditor defaults to 80%
	 * @param url
	 */
	public void setFilebrowserWindowWidth(String size) {
		filebrowserWindowWidth = size;
	}

	/**
	 * Sets the filebrowserWindowHeight config option, which is a height size spec (like "600" for 600 pixels); CKEditor defaults to 70%
	 * @param url
	 */
	public void setFilebrowserWindowHeight(String size) {
		filebrowserWindowHeight = size;
	}

	/**
	 * Sets the filebrowserImageBrowseUrl config option, which is an URL that will list images a user can select from
	 * @param url
	 */
	public void setFilebrowserImageBrowseUrl(String url) {
		filebrowserImageBrowseUrl = url;
	}

	/**
	 * Sets the filebrowserImageUploadUrl config option, which is an URL that will allow an image file to be uploaded
	 * @param url
	 */
	public void setFilebrowserImageUploadUrl(String url) {
		filebrowserImageUploadUrl = url;
	}

	/**
	 * Sets the filebrowserImageWindowWidth config option, which is a width size spec (like "600" for 600 pixels); CKEditor defaults to 80%
	 * @param url
	 */
	public void setFilebrowserImageWindowWidth(String size) {
		filebrowserImageWindowWidth = size;
	}

	/**
	 * Sets the filebrowserImageWindowHeight config option, which is a height size spec (like "600" for 600 pixels); CKEditor defaults to 70%
	 * @param url
	 */
	public void setFilebrowserImageWindowHeight(String size) {
		filebrowserImageWindowHeight = size;
	}


	/**
	 * Sets the filebrowserFlashBrowseUrl config option, which is an URL that will allow browsing for Flash
	 * @param url
	 */
	public void setFilebrowserFlashBrowseUrl(String url) {
		filebrowserFlashBrowseUrl = url;
	}

	/**
	 * Sets the filebrowserFlashUploadUrl config option, which is an URL that will allow a Flash file to be uploaded
	 * @param url
	 */
	public void setFilebrowserFlashUploadUrl(String url) {
		filebrowserFlashUploadUrl = url;
	}

	/**
	 * Sets the filebrowserLinkBrowseUrl config option, which is an URL that will allow for link browsing
	 * @param url
	 */
	public void setFilebrowserLinkBrowseUrl(String url) {
		filebrowserLinkBrowseUrl = url;
	}

	public void setCustomConfig(String customConfig) {
            this.customConfig = customConfig;
        }

	public String getCustomConfig() {
            return customConfig;
        }

}
