package info.magnolia.cms.gui.misc;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.gui.control.File;

import javax.jcr.RepositoryException;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */ 
public class FileProperties {
	public static final String PROPERTIES_CONTENTNODE="properties";
	public static final String PROPERTY_CONTENTTYPE="contentType";
	public static final String PROPERTY_SIZE="size";
	public static final String PROPERTY_TEMPLATE="nodeDataTemplate";
	public static final String PROPERTY_EXTENSION="extension";
	public static final String PROPERTY_FILENAME="fileName";

	public static final String EXTENSION="extension"; // Pdf
	public static final String EXTENSION_LOWER_CASE="extensionLowerCase"; // pdf
	public static final String EXTENSION_UPPER_CASE="extensionUpperCase"; // PDF
	public static final String NAME="name"; // report2004.Pdf
	public static final String NAME_WITHOUT_EXTENSION="nameWithoutExtension"; // report2004
	public static final String CONTENT_TYPE="contentType"; // application/pdf
	public static final String TEMPLATE="template"; // ((according to dialog))
	public static final String HANDLE="handle"; // /en/mainColumnParagraph/04/file
	public static final String PATH_WITHOUT_NAME="pathWithoutName"; // /en/mainColumnParagraph/04/file.Pdf
	public static final String PATH="path"; // path including fileName: /en/mainColumnParagraph/04/file/report2004.Pdf
	public static final String SIZE_BYTES="sizeBytes"; // size in bytes: 263492
	public static final String SIZE_KB="sizeKB"; // size in KB: 257.3
	public static final String SIZE_MB="sizeMB"; // size in MB: 0.2
	public static final String SIZE="size"; // size and unit depending of size in bytes, KB, or MB: 257.3



	private Content content;
	private String nodeDataName;

	public FileProperties(Content content, String nodeDataName) {
		this.setContent(content);
		this.setNodeDataName(nodeDataName);
	}

	public void setContent(Content c) {this.content=c;}
	public Content getContent() {return this.content;}

	public void setNodeDataName(String s) {this.nodeDataName=s;}
	public String getNodeDataName() {return this.nodeDataName;}

	public String getProperty(String property) {
		String value="";
		try {
			ContentNode props=this.getContent().getContentNode(this.nodeDataName+"_"+PROPERTIES_CONTENTNODE);
			String filename=props.getNodeData(PROPERTY_FILENAME).getString();
			String ext=props.getNodeData(PROPERTY_EXTENSION).getString();
			String fullName=filename;
			String fullExt="";
			if (ext!=null && !ext.equals("")) {
				fullExt="."+ext;
				fullName+=fullExt;
			}

			if (property.equals(EXTENSION)) {
				value=ext;
			}
			else if (property.equals(EXTENSION_LOWER_CASE)) {
				value=ext.toLowerCase();
			}
			else if (property.equals(EXTENSION_UPPER_CASE)) {
				value=ext.toUpperCase();
			}
			else if (property.equals(NAME_WITHOUT_EXTENSION)) {
				value=filename;
			}
			else if (property.equals(CONTENT_TYPE)) {
				value=props.getNodeData(PROPERTY_CONTENTTYPE).getString();
			}
			else if (property.equals(TEMPLATE)) {
				value=props.getNodeData(PROPERTY_TEMPLATE).getString();
			}
			else if (property.equals(HANDLE)) {
				value=this.getContent().getHandle()+"/"+this.getNodeDataName();
			}
			else if (property.equals(NAME)) {
				value=fullName;
			}
			else if (property.equals(PATH_WITHOUT_NAME)) {
				value=this.getContent().getHandle()+"/"+this.getNodeDataName()+fullExt;
			}
			else if (property.equals(SIZE_BYTES)) {
				value=props.getNodeData(PROPERTY_SIZE).getString();
			}
			else if (property.equals(SIZE_KB)) {
				double size=props.getNodeData(PROPERTY_SIZE).getLong();
				String sizeStr;
				size=size/1024;
				sizeStr=Double.toString(size);
				sizeStr=sizeStr.substring(0,sizeStr.indexOf(".")+2);
				value=sizeStr;
			 }
			else if (property.equals(SIZE_MB)) {
				double size=props.getNodeData(PROPERTY_SIZE).getLong();
				String sizeStr;
				size=size/(1024*1024);
				sizeStr=Double.toString(size);
				sizeStr=sizeStr.substring(0,sizeStr.indexOf(".")+2);
				value=sizeStr;
			 }
			else if (property.equals(SIZE)) {
				double size=props.getNodeData(PROPERTY_SIZE).getLong();
				String unit="bytes";
				String sizeStr;
				if (size>=1000){
					size=size/1024;
					unit="KB";

					if (size>=1000) {
						size=size/1024;
						unit="MB";
					}
					sizeStr=Double.toString(size);
					sizeStr=sizeStr.substring(0,sizeStr.indexOf(".")+2);
				}
				else {
					sizeStr=Double.toString(size);
					sizeStr=sizeStr.substring(0,sizeStr.indexOf("."));
				}
				value=sizeStr+" "+unit;
			 }
			 else { //property.equals(PATH|null|""|any other value)
				value=this.getContent().getHandle()+"/"+this.getNodeDataName()+"/"+fullName;
			}
	 	}
		catch (RepositoryException re) {}
		return value;	}


}
