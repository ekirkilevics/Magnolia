<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page import="info.magnolia.cms.core.Content" />
    <jsp:directive.page import="info.magnolia.cms.util.Resource" />
    <jsp:directive.page import="info.magnolia.cms.gui.misc.Sources" />
    <jsp:directive.page import="info.magnolia.cms.gui.inline.BarMain" />
    <jsp:directive.page import="info.magnolia.cms.gui.misc.FileProperties" />
    <jsp:directive.page import="info.magnolia.cms.gui.control.Button" />

<!--
     sample 1: using the mainBar tag
     use the main bar tag to use the main bar as it is
     "paragraph" specifies the paragraph evoked by the "Properties" button
-->
<jsp:text><![CDATA[

]]></jsp:text>
    <cms:mainBar paragraph="samplesPageProperties"/>
<jsp:text><![CDATA[

]]></jsp:text>

<jsp:scriptlet><![CDATA[
    /*
    // sample 2: customise the main bar

    Content currentPage=Resource.getActivePage(request);

    BarMain bar=new BarMain(request);

    //set bar to an absolute position
    bar.setTop(20);
    bar.setLeft(100);

    //set width of bar to any css compatible value
    bar.setWidth("800px");

    // overlay mode; true (default): main bar overlays content - false: main bar moves content downwards
    // note: top, left and width do not apply if overlay is set to false
    // note: since all content divs of samples are positioned absolut, overlay mode "false" does only affect the header image
    //bar.setOverlay(false);

    //path is needed for the links of the buttons
    bar.setPath(currentPage.getHandle());

    //"paragraph" specifies the paragraph evoked by the "Properties" button
    bar.setParagraph("samplesPageProperties");

    // initialize the default buttons (preview, site admin, properties)
    // note: buttons are not placed through init (see below)
    bar.setDefaultButtons();

    // to overwrite single properties of the default buttons, use getButtonXXX() methods:
    bar.getButtonProperties().setLabel("*P*R*O*P*S*");
    bar.getButtonPreview().setLabel("&laquo; #P#RR#E#V#I#E#W#");
    bar.getButtonSiteAdmin().setLabel("s i t e a d m i n");


    //add customized buttons to the main bar
    Button cb1=new Button();
    cb1.setLabel("Custom I");
    cb1.setOnclick("alert('Whatever you want.');");
    bar.setButtonsLeft(cb1);

    Button cb2=new Button();
    cb2.setLabel("Custom II");
    cb2.setOnclick("alert('Whatever you want.');");
    bar.setButtonsRight(cb2);

    // places the preview and the site admin button to the very left and the properties button to the very right
    bar.placeDefaultButtons();


    // right buttons added after calling placeDefaultButtons() will be placed on the right of the properties button
    Button cb3=new Button();
    cb3.setLabel("Custom III");
    cb3.setOnclick("alert('Whatever you want.');");
    bar.setButtonsRight(cb3);

    //draw the main bar
    bar.drawHtml(out);
    */

]]></jsp:scriptlet>
</jsp:root>

