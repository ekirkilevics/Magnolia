package info.magnolia.cms.gui.misc;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */ 
public class Sources {

	public Sources() {

	}

	public String getHtmlJs() {
		StringBuffer html=new StringBuffer();
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/js/generic.js\"></script>");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/js/general.js\"></script>");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/js/controls.js\"></script>");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/js/dialogs/dialogs.js\"></script>");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/js/dialogs/calendar.js\"></script>");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/js/tree.js\"></script>");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/js/adminCentral.js\"></script>");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/js/inline.js\"></script>");
		return html.toString();
	}


	public String getHtmlCss() {
		StringBuffer html=new StringBuffer();
		html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/admindocroot/css/general.css\">");
		html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/admindocroot/css/controls.css\">");
		html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/admindocroot/css/tree.css\">");
		html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/admindocroot/css/dialogs.css\">");
		html.append("<link rel=\"stylesheet\" type=\"text/css\" href='/admindocroot/css/adminCentral.css'/>");
		return html.toString();
	}




	public String getHtmlRichEdit() {
		StringBuffer html=new StringBuffer();
		html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/admindocroot/richE/kupustyles.css\">");
		html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/admindocroot/richE/kupucustom.css\">");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/richE/sarissa.js\"> </script>");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/richE/kupuhelpers.js\"> </script>");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/richE/kupueditor.js\"> </script>");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/richE/kupubasetools.js\"> </script>");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/richE/kupuloggers.js\"> </script>");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/richE/kupucontentfilters.js\"> </script>");
		html.append("<script type=\"text/javascript\" src=\"/admindocroot/richE/kupuinit.js\"> </script>");
		return html.toString();
	}


}
