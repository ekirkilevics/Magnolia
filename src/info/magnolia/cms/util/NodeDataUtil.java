package info.magnolia.cms.util;

import info.magnolia.cms.core.NodeData;

import javax.jcr.PropertyType;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: enz
 * Date: Sep 6, 2004
 * Time: 8:40:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class NodeDataUtil {
	NodeData nodeData=null;

	public NodeDataUtil(NodeData nodeData) {
		this.setNodeData(nodeData);
	}


	public void setNodeData(NodeData nodeData) {this.nodeData=nodeData;}
	public NodeData getNodeData() {return this.nodeData;}


	/**
	 * <p>Returns the representation of the value as a String:
	 * </p>
	 *
	 * @return String
	 */
	public String getValueString() {
		return getValueString(null);
	}


	/**
	 * <p>Returns the representation of the value as a String:
	 * </p>
	 *
	 * @return String
	 */
	public String getValueString(String dateFormat) {
		try {
			NodeData nodeData=this.getNodeData();
			switch (nodeData.getType()) {
				case (PropertyType.STRING):
					return nodeData.getString();
				case (PropertyType.DOUBLE):
					return Double.toString(nodeData.getDouble());
				case (PropertyType.LONG):
					return Long.toString(nodeData.getLong());
				case (PropertyType.BOOLEAN):
					return Boolean.toString(nodeData.getBoolean());
				case (PropertyType.DATE):
					Date valueDate = nodeData.getDate().getTime();
					return new DateUtil().getFormattedDate(valueDate,dateFormat);
				case (PropertyType.BINARY):
					//???
			}
		} catch (Exception e) {}
		return "";
	}

	public String getTypeName(int type) {
		if (type==PropertyType.STRING) return PropertyType.TYPENAME_STRING;
		else if (type==PropertyType.BOOLEAN) return PropertyType.TYPENAME_BOOLEAN;
		else if (type==PropertyType.DATE) return PropertyType.TYPENAME_DATE;
		else if (type==PropertyType.LONG) return PropertyType.TYPENAME_LONG;
		else if (type==PropertyType.DOUBLE) return PropertyType.TYPENAME_DOUBLE;
		else if (type==PropertyType.BINARY) return PropertyType.TYPENAME_BINARY;
		return "";
	}

}
