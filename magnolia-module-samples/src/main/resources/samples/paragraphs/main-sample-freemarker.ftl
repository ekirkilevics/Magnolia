<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
[#assign cms=JspTaglibs["cms-taglib"]]
[#assign cmsu=JspTaglibs["cms-util-taglib"]]

[@cms.editBar /]
<h3>${content.title!("no title")}</h3>
<div>

Image: <img src="${ctx.contextPath}${content.image}" />
<br />
Text: ${content.text!("no text")}
<br />
The date you spedified: ${content.date?string("dd.MM.yyyy")}
<br />
<ul>Parameter set by filter:
<li>using model: ${model.filterAttribute!("non set")} </li>
<li>get from request: ${ctx.getAttribute("sampleFilter")!("non set")}</li>
</ul>
</div>

