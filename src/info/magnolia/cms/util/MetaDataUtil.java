package info.magnolia.cms.util;

import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MetaData;

import javax.jcr.PropertyType;
import java.util.Date;

/**
 *
 * User: enz
 * Date: Sep 6, 2004
 * Time: 2:24:36 PM
 *
 */
public class MetaDataUtil {
	private Content content;


	public MetaDataUtil(Content c) {
		this.setContent(c);
	}


	public void setContent(Content c) {this.content=c;}
	public Content getContent() {return this.content;}




	public String getPropertyValueString(String propertyName) {
		return getPropertyValueString(propertyName,null);
	}


	/**
	 * <p>Returns the representation of the value as a String:
	 * </p>
	 *
	 * @return String
	 */
	public String getPropertyValueString(String propertyName,String dateFormat) {
		try {
			if (propertyName.equals(MetaData.CREATION_DATE) ||
			propertyName.equals(MetaData.LAST_MODIFIED) ||
			propertyName.equals(MetaData.LAST_ACTION) ||
			propertyName.equals(MetaData.START_TIME) ||
			propertyName.equals(MetaData.END_TIME)) {
				Date date=this.getContent().getMetaData().getDateProperty(propertyName).getTime();
				return new DateUtil().getFormattedDate(date,dateFormat);
			}
			else if (propertyName.equals(MetaData.ACTIVATED)) {
				return Boolean.toString(this.getContent().getMetaData().getBooleanProperty(propertyName));
			}
			else {
				return this.getContent().getMetaData().getStringProperty(propertyName);
			}
		} catch (Exception e) {}
		return "";
	}





}
