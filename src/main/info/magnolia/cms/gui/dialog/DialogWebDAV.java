/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */



package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.misc.Sources;

import javax.servlet.jsp.JspWriter;
import javax.jcr.PropertyType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.Property;
import org.apache.webdav.lib.methods.XMLResponseMethodBase;
import org.apache.util.HttpsURL;
import org.apache.util.HttpURL;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */ 
public class DialogWebDAV extends DialogBox {
	private static Logger log = Logger.getLogger(DialogWebDAV.class);
	//dev; remove values later (""; not null!)
	private String host="";
	private int port=0;
	private String directory="";
	private String subDirectory="";
	private String user="";
	private String password="";
	private String protocol="http";
	private WebdavResource DAVConnection=null;

	public DialogWebDAV(ContentNode configNode,Content websiteNode) {
		super(configNode,websiteNode);
		initIconExtensions();
	}

	public DialogWebDAV() {
		initIconExtensions();
	}


	public void setHost(String s) {this.host=s;}
	public String getHost() {return this.host;}

	public void setPort(String s) {
		try {
			this.port=(new Integer(s)).intValue();
		}
		catch (NumberFormatException nfe) {
			this.port=0;
		}
	}
	public void setPort(int i) {this.port=i;}
	public int getPort() {
		if (this.port==0) return 80;
		else return this.port;
	}

	public void setDirectory(String s) {this.directory=s;}
	public String getDirectory() {return this.directory;}

	public void setSubDirectory(String s) {this.subDirectory=s;}
	public String getSubDirectory() {return this.subDirectory;}

	public void setUser(String s) {this.user=s;}
	public String getUser() {return this.user;}

	public void setPassword(String s) {this.password=s;}
	public String getPassword() {return this.password;}

	public void setProtocol(String s) {this.protocol=s;}
	public String getProtocol() {return this.protocol;}

	public String getHtmlDecodeURI(String s) {
		return "<script>document.write(decodeURI(\""+s+"\"));</script>";
	}

	public void setDAVConnection(WebdavResource w) {this.DAVConnection=w;}
	public WebdavResource getDAVConnection() {return this.DAVConnection;}


	private String getSizeValue() {
		if (this.getWebsiteNode()!=null) return this.getWebsiteNode().getNodeData(this.getName()+"_size").getString();
		else return "";
	}

	private String getModDateValue() {
		if (this.getWebsiteNode()!=null) return this.getWebsiteNode().getNodeData(this.getName()+"_lastModified").getString();
		else return "";
	}

	public void setDAVConnection() {
		WebdavResource wdr = null;
		try {
			if (this.getProtocol().equalsIgnoreCase("https")) {
				wdr = new WebdavResource(new HttpsURL(this.getUser(),this.getPassword(),this.getHost(),this.getPort(),this.getDirectory()));
			}
			else {
				wdr = new WebdavResource(new HttpURL(this.getUser(),this.getPassword(),this.getHost(),this.getPort(),this.getDirectory()));
			}
			/*
			//todo: proxy config
			//proxy not yet supported
			if (!proxy.equals("")) {
			wdr.setProxy(proxy,(new Integer(proxyPort)).intValue());
			String proxyUserName = nodeDataDefinition.getNodeData("proxyUserName").getString();
			String proxyPswd = nodeDataDefinition.getNodeData("proxyPassword").getString();
			if (!proxyUserName.equals("")) {
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxyUserName,proxyPswd);
			wdr.setProxyCredentials(credentials);
			}
			}
			*/
		} catch (Exception e) {e.printStackTrace();}
		this.setDAVConnection(wdr);
	}


	public void drawHtml(JspWriter out) {
		this.drawHtmlPre(out);
		this.setDAVConnection();
		this.setSessionAttribute();

		String showName="&nbsp;";
		String showPath="";
		String showIcon="";
		if (!this.getValue().equals("")) {
			String valueTmp="";
			boolean isDirectory=false;
			if (this.getValue().lastIndexOf("/")==this.getValue().length()-1) {
				//value is directory
				valueTmp=this.getValue().substring(0,this.getValue().length()-1);
				isDirectory=true;
				this.setSubDirectory(this.getValue());
				showIcon=ICONS_PATH+ICONS_FOLDER;
			}
			else {
				//value is file
				valueTmp=this.getValue();
				showIcon=this.getIconPath(this.getValue());
			}
			if (valueTmp.indexOf("/")!=-1) {
				showName=valueTmp.substring(valueTmp.lastIndexOf("/")+1);
				if (!isDirectory) this.setSubDirectory(valueTmp.substring(0,valueTmp.lastIndexOf("/")+1));
			}
			else {
				showName=valueTmp;
				if (!isDirectory) this.setSubDirectory("");
			}
			showPath="/"+this.getSubDirectory().substring(0,this.getSubDirectory().lastIndexOf("/")+1);
			showPath="<a href=\"javascript:mgnlDialogDAVBrowse('"+this.getName()+"_iFrame','selectedValue');\">"+this.getHtmlDecodeURI(showPath)+"</a>";
		}
		else {
			showPath="<i>No selection</i>";
			showIcon=NULLGIF;
		}


		try {
			DialogSpacer spacer=new DialogSpacer();

			this.setDescription("Connected to: "+this.getProtocol()+"://"+this.getHost()+":"+this.getPort()+this.getDirectory()+"<br>"+this.getDescription());

			out.println(spacer.getHtml(2));

			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
			out.println("<tr>");
			out.println("<td><img id=\""+this.getName()+"_showIcon\" src=\""+showIcon+"\" width=\""+ICONS_WIDTH+"\" height=\""+ICONS_HEIGHT+"\"></td>");
			out.println("<td id=\""+this.getName()+"_showName\">"+this.getHtmlDecodeURI(showName)+"</td>");
			out.println("</tr><tr height=\"4\"><td></td></tr><tr>");
			out.println("<td><img src=\""+ICONS_PATH+ICONS_FOLDER+"\"></td>");
			out.println("<td id=\""+this.getName()+"_showPath\">"+showPath+"</td>");
			out.println("</tr></table>");

			out.println(new Hidden(this.getName(),this.getValue()).getHtml());


			Hidden size=new Hidden(this.getName()+"_size",this.getSizeValue());
			size.setType(PropertyType.TYPENAME_LONG);
			out.println(size.getHtml());

			Hidden lastMod=new Hidden(this.getName()+"_lastModified",this.getModDateValue());
			lastMod.setType(PropertyType.TYPENAME_DATE);
			out.println(lastMod.getHtml());

			out.println(this.getHtmlSessionAttributeRemoveControl());

			out.println(spacer.getHtml(12));


			Button home=new Button();
			home.setSaveInfo(false);
			home.setLabel("Home");
			home.setOnclick("mgnlDialogDAVBrowse('"+this.getName()+"_iFrame','homeDirectory')");
			out.println(home.getHtml());

			Button refresh=new Button();
			refresh.setSaveInfo(false);
			refresh.setLabel("Refresh");
			refresh.setOnclick("mgnlDialogDAVBrowse('"+this.getName()+"_iFrame','refreshDirectory')");
			out.println(refresh.getHtml());


			Button up=new Button();
			up.setSaveInfo(false);
			up.setId(this.getName()+"_upDiv");
			up.setLabel("Parent directory");
			up.setOnclick("mgnlDialogDAVBrowse('"+this.getName()+"_iFrame','parentDirectory')");
			out.println(up.getHtml());


			out.println(spacer.getHtml(3));


			//#################
			//iFrame
			//#################
			out.println("<iframe");
			out.println(" id=\""+this.getName()+"_iFrame\"");
			out.println(" class=\""+CSSCLASS_WEBDAVIFRAME+"\"");
			if (this.getConfigValue("height",null)!=null) out.println(" style=\"height:"+this.getConfigValue("height")+";\")");
			out.println(" frameborder=\"0\"");
			out.println(" src=\"/.magnolia/dialogs/webDAVIFrame.html?"+SESSION_ATTRIBUTENAME_DIALOGOBJECT+"="+this.getConfigValue(SESSION_ATTRIBUTENAME_DIALOGOBJECT)+"&mgnlCK="+new Date().getTime()+"\"");
			out.println(" reloadsrc=\"0\"");
			out.println(" usecss=\"1\"");
			out.println(" strict_output=\"1\"");
			out.println(" content_type=\"application/xhtml+xml\"");
			out.println(" scrolling=\"auto\"");
			out.println("></iframe>");
		}
		catch (IOException ioe) {log.error("");}
		this.drawHtmlPost(out);

	}




	public void drawHtmlList(JspWriter out) {
		Enumeration fileList = null;
		String dir=this.getDirectory()+this.getSubDirectory();
		WebdavResource wdr=this.getDAVConnection();
		//System.out.println("dir:"+dir);
		try {
			dir=URLDecoder.decode(dir,"UTF-8");
		}
		catch (UnsupportedEncodingException uee) {}
		//System.out.println("URLDecoder.decode(dir,\"UTF-8\"): "+dir);
		try {
			if (dir == null || (dir.equals("")))
				fileList = wdr.propfindMethod(1);
			else {
				try {
					fileList = wdr.propfindMethod(dir,1);
				}
				catch (Exception e) {
					//System.out.println("path ["+path+"] not found; use defaultPath ["+defaultPath+"]");
					dir=this.getDirectory();
					fileList = wdr.propfindMethod(dir,1);
				}
			}
			if (wdr == null) {

				//System.out.println(" WEBDAV RESOURCE - NULL ");
			}

			out.println("<html><head>");
			out.println(new Sources().getHtmlCss());
			out.println(new Sources().getHtmlJs());

			String parentDirectory="";
			if (!this.getDirectory().equals(dir)) {
				parentDirectory=this.getSubDirectory().substring(0,this.getSubDirectory().length()-1); //get rid of last / (/dir/home/ -> /dir/home)
				parentDirectory=parentDirectory.substring(0,parentDirectory.lastIndexOf("/")+1);
				out.println("<script>mgnlDialogDAVShow('"+this.getName()+"_upDiv',true);</script>");
			}
			else {
				//home
				out.println("<script>mgnlDialogDAVShow('"+this.getName()+"_upDiv',false);</script>");
			}


            out.println("</head>");
			out.println("<body marginwidth=\"0\" topmargin=\"5\" marginheight=\"5\" leftmargin=\"0\">");

			out.println("<table cellpadding=\"3\" cellspacing=\"0\" border=\"0\" width=\"100%\">");


			out.println("<form name=\"mgnlDialogDAVBrowseForm\" method=\"post\">");
			out.println(new Hidden("subDirectory","",false).getHtml());
			out.println(new Hidden("parentDirectory",parentDirectory,false).getHtml());
			out.println(new Hidden("homeDirectory","",false).getHtml());
			out.println(new Hidden("refreshDirectory",this.getSubDirectory(),false).getHtml());
			out.println(new Hidden("selectedValue",this.getValue(),false).getHtml());
			out.println(new Hidden(SESSION_ATTRIBUTENAME_DIALOGOBJECT,this.getConfigValue(SESSION_ATTRIBUTENAME_DIALOGOBJECT),false).getHtml());
			out.println("</form>");


			ArrayList fileListAS = new ArrayList();
			ArrayList dirListAS = new ArrayList();
			ArrayList selfAS=new ArrayList();

			while (fileList.hasMoreElements()) {
				XMLResponseMethodBase.Response response = (XMLResponseMethodBase.Response) fileList.nextElement();
				Hashtable properties = this.getDAVProperties(response);
				//System.out.println("\nnext element (name):"+properties.get("name"));

				if (properties.get("name") == null || properties.get("name").equals(""))
					continue;

				String name= (String)properties.get("name");

				if (this.getSubDirectory().equals(name) || this.getSubDirectory().equals(name+"/")) {
					//self directory
					properties.put("isSelf","true");
				}
				if (name.indexOf("/")!=-1) {
					//when path contains spaces, the name contains the entire path -> get rid of path
					name=name.substring(name.lastIndexOf("/")+1);
				}

				if (name.startsWith("._")) {
					continue;
				}

				if (name.startsWith(".") && !this.getConfigValue("showHiddenFiles","false").equals("true")) {
					continue;
				}

				properties.put("name",name);

				String displayType = (String)properties.get("displayType");
				if (properties.get("isSelf")!=null) selfAS.add(properties);
				else if (displayType.equals("folder")) dirListAS.add(properties);
				else fileListAS.add(properties);
			}


			int i=0;
			ArrayList parentAS=new ArrayList();
			if (!this.getDirectory().equals(dir)) {
				Hashtable parentProp=new Hashtable();
				String name="";
				if (parentDirectory.equals("")) name="/";
				else {
					name=parentDirectory.substring(0,parentDirectory.length()-1);
					if (name.indexOf("/")!=-1) name=name.substring(0,name.lastIndexOf("/"));
				}
				parentProp.put("name",name);
				parentProp.put("isParent","true");
				parentProp.put("href",parentDirectory);
				parentProp.put("displayType","folder");
				parentProp.put("sizeStringValue","");
				parentProp.put("sizeStringUnit","");
				parentProp.put("lastModifiedString","");
				parentAS.add(parentProp);
				i=drawHtmlList(out,parentAS,i);
			}

			i=drawHtmlList(out,selfAS,i);
			i=drawHtmlList(out,dirListAS,i);
			i=drawHtmlList(out,fileListAS,i);
			if (i==1) {
				out.println("<tr><td colspan=\"3\"></td><td colspan=\"3\"><i>Directory is empty</i></td></tr>");
			}
			out.println("</table>");
			out.println("</body></html>");
		}
		catch (Exception e) {}
	}

	public int drawHtmlList(JspWriter out, List as,int i) {
		try {
			boolean alt=false;
			if (i % 2 == 0) alt=true;

			//todo: better sorting
			Collections.sort(as,new DialogWebDAVComparator());
			Iterator it = as.iterator();
			while (it.hasNext()) {
				Hashtable properties=(Hashtable) it.next();

				String displayType = (String)properties.get("displayType");
				String name=(String)properties.get("name");

				if (!alt) out.println("<tr>");
				else out.println("<tr class="+CSSCLASS_BGALT+">");
				alt=!alt;

				out.println("<td></td>");

				out.println("<td>");
				if (properties.get("isParent")==null && (displayType.indexOf("folder")==-1 || this.getConfigValue("allowDirectorySelection").equals("true"))) {
					String lastModified="";
					if (properties.get("lastModified")!=null) lastModified=((String)properties.get("lastModified")).replaceAll(" ","%20");
					out.println("<input type=\"radio\" name=\""+this.getName()+"_radio\"");
					out.println(" onclick=mgnlDialogDAVSelect(\""+this.getName()+"\",\""+name+"\",\""+i+"\",\""+(String)properties.get("size")+"\",\""+lastModified+"\");");
					//if (this.getValue().equals(this.getSubDirectory()+name) || this.getValue().equals(this.getSubDirectory()+name+"/")) out.println(" checked");
					boolean checked=false;
					if (properties.get("isSelf")!=null) {
						if (this.getValue().equals(this.getSubDirectory())) checked=true;
					}
					else {
						if (this.getValue().equals(this.getSubDirectory()+name) || this.getValue().equals(this.getSubDirectory()+name+"/")) checked=true;
					}
					if (checked) out.println(" checked");
					out.println(">");
					//if (checked) out.println("X");
				}
				out.println("</td>");
				//out.println("<td>"+this.getValue()+"..."+this.getSubDirectory()+"..."+name+"</td>");

				String idHidden=this.getName()+"_"+i+"_hidden";
				String idIcon=this.getName()+"_"+i+"_icon";
				i++;

				String iconPath;
			    if (displayType.equals("folder")) {
					iconPath=ICONS_PATH+ICONS_FOLDER;
				}
				else {
					iconPath=this.getIconPath(name);
				}
				out.println("<td>");
				out.print("<img src=\""+iconPath+"\" border=\"0\" id=\""+idIcon+"\">");
				out.println("</td>");

				out.println("<td width=\"100%\">");
				if (displayType.indexOf("folder")==0) {
					if (properties.get("isSelf")!=null) {
						out.println(this.getHtmlDecodeURI("<b><i>.&nbsp;&nbsp;"+name+"</i></b>"));
					}
					else {
						if (properties.get("isParent")!=null) name="<b><i>..&nbsp;&nbsp;"+name+"</i></b>";
						out.print("<a href=\"javascript:mgnlDialogDAVBrowse('','"+idHidden+"');\">");
						out.print(this.getHtmlDecodeURI(name));
						out.print("</a>");
					}
				} else {
					out.println(this.getHtmlDecodeURI(name));
					out.println("[<a href=\""+this.getProtocol()+"://"+this.getHost()+":"+this.getPort()+this.getDirectory()+(String)properties.get("href")+"\" target=\"blank\">view</a>]");
				}
				out.println(new Hidden(idHidden,(String)properties.get("href"),false).getHtml());
				out.println("</td>");

				out.println("<td style=\"text-align:right;\">"+(String)properties.get("sizeStringValue")+"</td>");
				out.println("<td>"+(String)properties.get("sizeStringUnit")+"</td>");

				out.println("<td>&nbsp;&nbsp;</td>");

				out.println("<td><nobr>"+(String)properties.get("lastModifiedString")+"</nobr></td>");


				out.println("<td>&nbsp;&nbsp;</td>");
				out.println("</tr>");
			}
		}
		catch (IOException ioe) {}
		return i;
	}



	public Hashtable getDAVProperties(XMLResponseMethodBase.Response response) {
		Hashtable properties = new Hashtable();
		/* get the short name */
		Enumeration props = response.getProperties();
		String href = response.getHref();

		properties.put("href",href.replaceFirst(this.getDirectory(),""));

		while (props.hasMoreElements()) {
			Property property = (Property)props.nextElement();
			if (property.getLocalName().equalsIgnoreCase("getcontenttype")) {
				if (property.getPropertyAsString().equalsIgnoreCase("httpd/unix-directory")) {
					properties.put("displayType","folder");
					if (href.length() > this.getDirectory().length()) {
						//System.out.println("href:"+href);
						//System.out.println("getDir:"+this.getDirectory());
						String name = href.replaceFirst(this.getDirectory(),"");
						name = name.substring(0,name.length()-1);
						//properties.put("name",name.substring(0,name.length()));
						name=name.substring(0,name.length());
						//System.out.println("name:"+name);
						properties.put("name",name);
					}
				}
			} else if (property.getLocalName().equalsIgnoreCase("getcontentlength")) {
				properties.put("size",property.getPropertyAsString());
			} else if (property.getLocalName().equalsIgnoreCase("getlastmodified")) {
			    properties.put("lastModifiedString",this.getFormattedDate(property.getPropertyAsString(),"MMM dd yyyy"));
			    properties.put("lastModified",this.getFormattedDate(property.getPropertyAsString(),"yyyy-MM-dd, HH:mm:ss"));
			}
		}

		if (properties.get("name")==null) {
			if (href.length() > this.getDirectory().length()) {
				String name = href.replaceFirst(this.getDirectory(),"");
				properties.put("name",name);
			}
			int index = href.lastIndexOf(".");
			if (index > -1)
				properties.put("displayType",(href.substring(index+1)).toLowerCase());
			else
				properties.put("displayType","general");
		}

		if (properties.get("size") == null) {
			properties.put("size","");
			properties.put("sizeStringValue","");
			properties.put("sizeStringUnit","");
		}
		else {
			String size[]=this.getFileSizeString((String)properties.get("size")).split(" ");
			properties.put("sizeStringValue",size[0]);
			properties.put("sizeStringUnit",size[1]);
		}

		return properties;
	}






	public String getFileSizeString(String fileSize) {
		int bytes = (new Integer(fileSize)).intValue();
		int size = (bytes/1024);
		if (size == 0)
			return (bytes+" Bytes");
		else if (size >= 1024)
			return ((size/1024)+" MB");
		return (size+" KB");
	}



	public String getFormattedDate(String date,String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);

		//todo: replace deprecated (new Date(String date))
		Date x=new Date(date);
		try {
			return sdf.format(x).toString();
		} catch(Exception e) { e.printStackTrace(); return date; }
	}


}



