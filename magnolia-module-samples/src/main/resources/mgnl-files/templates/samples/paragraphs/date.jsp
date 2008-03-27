<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
    xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />

    <cms:out nodeDataName="date" datePattern="EEEE, MMMM d, yyyy" />
    <br />
    <br />
    <div class="devShowBox"><span class="code">&amp;lt;cms:out nodeDataName="date" datePattern="..."
    dateLanguage="..."/&amp;gt;</span><br />
    <br />
    <b>datePattern (samples):</b><br />
    <br />
    <span class="code">yyyy-MM-dd</span> (default): <cms:out nodeDataName="date" /><br />
    <br />
    <span class="code">yyyy-MM-dd'T'HH:mm:ss:</span> <cms:out nodeDataName="date" datePattern="yyyy-MM-dd'T'HH:mm:ss" /><br />
    <br />
    <span class="code">d. M. yy</span>: <cms:out nodeDataName="date" datePattern="d. M. yy" /><br />
    <br />
    <span class="code">EEE d. MMM. yy - HH:mm</span>: <cms:out nodeDataName="date" datePattern="EEE d. MMM yyyy - HH:mm" /><br />
    <br />
    <span class="code">EEEE, d. MMMM. yyyy</span>: <cms:out nodeDataName="date" datePattern="EEEE, d. MMMM yyyy" /><br />
    <br />
    <br />
    <b>dateLanguage (samples):</b><br />
    <br />
    <span class="code">en</span>: <cms:out nodeDataName="date" datePattern="EEEE, d. MMMM yyyy" dateLanguage="en" /><br />
    <br />
    <span class="code">de</span>: <cms:out nodeDataName="date" datePattern="EEEE, d. MMMM yyyy" dateLanguage="de" /><br />
    <br />
    <span class="code">fi</span>: <cms:out nodeDataName="date" datePattern="EEEE, d. MMMM yyyy" dateLanguage="fi" /><br />
    </div>
</jsp:root>
