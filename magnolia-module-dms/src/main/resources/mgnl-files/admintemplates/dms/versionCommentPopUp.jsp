<jsp:root version="2.0" 
	xmlns:jsp="http://java.sun.com/JSP/Page" 
	xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core" 
	xmlns:cms="urn:jsptld:cms-taglib">
    
    <jsp:directive.page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" />
    <jsp:directive.page import="info.magnolia.cms.gui.control.Button" />

	<script>
		var MgnlDMSVersionPopup ={
		
			checkEnter: false, 
			
			show: function(){
				var txt = document.getElementById("commentPopUpTextArea");
				var div = document.getElementById("commentPopUpDiv");
				var blockInputDiv = document.getElementById("blockInputDiv");

				txt.value="";
				
				div.style.left = mgnlGetWindowSize().w/2-div.clientWidth/2;
				div.style.top = mgnlGetWindowSize().h/2-div.clientHeight/2;
				div.style.visibility = "visible";

				blockInputDiv.style.width = mgnlGetWindowSize().w;
				blockInputDiv.style.height = mgnlGetWindowSize().h;
				blockInputDiv.style.visibility = "visible";

				this.checkEnter=true;
			},

			save: function(){
				var txt = document.getElementById("commentPopUpTextArea");
				var hidden = document.getElementById("versionComment");
				hidden.value=txt.value;
				mgnlDialogFormSubmit();
			},

			cancel: function(){
				var div = document.getElementById("commentPopUpDiv");
				var blockInputDiv = document.getElementById("blockInputDiv");

				div.style.visibility = "hidden";
				blockInputDiv.style.visibility = "hidden";
			},
			
			keyDown: function(e) { 
				var evt=(e)?e:(window.event)?window.event:null; 
				if(evt){ 
					var key=(evt.charCode)?evt.charCode: ((evt.keyCode)?evt.keyCode:((evt.which)?evt.which:0));
					if(MgnlDMSVersionPopup.checkEnter &amp;&amp; key==13) {
						MgnlDMSVersionPopup.save();
					}
				} 
			}
		};
		document.onkeydown = MgnlDMSVersionPopup.keyDown;
	</script>
	
	<div style="position:absolute; visibility: hidden" id="blockInputDiv">&amp;nbsp;</div>

	 <div style="position:absolute;border-style:solid;border-top-style:hidden; border-left-color: gray;border-right-color: black;border-bottom-color: black; border-width: thin; background-color: #F0F2E6; z-index:1; visibility: hidden" id="commentPopUpDiv">
		<div style="border-bottom:thin solid gray;background-image: url(${pageContext.request.contextPath}/.resources/controls/list/dmsListHeaderBackground.gif);padding: 3px;font-weight: bold;">Version Comment</div>
		<!-- <div style="background-color:gray;padding: 3px;font-weight: bold;">Version Comment</div>-->
		<div style="padding: 10px;">
			<div style="padding-bottom: 5px;">Enter a comment for this version (optional)</div>
			<div style="padding-bottom: 5px;"><textarea onclick="MgnlDMSVersionPopup.checkEnter=false" id="commentPopUpTextArea" rows="4" cols="40" class="mgnlDialogControlEdit">Dummy Text</textarea></div>
			<div align="right">
				<jsp:scriptlet>
					Button button = new Button("save", "Save");
					button.setEvent("onclick", "MgnlDMSVersionPopup.save();");
					out.println(button.getHtml());
					
					button = new Button("cancel", "Cancel");
					button.setEvent("onclick", "MgnlDMSVersionPopup.cancel();");
					out.println(button.getHtml());
				</jsp:scriptlet>
			</div>
		</div>
	</div>

</jsp:root>