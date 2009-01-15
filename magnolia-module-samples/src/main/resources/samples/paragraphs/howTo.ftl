[#-------------- HowTo Paragraph --------------]

[#assign cms=JspTaglibs["cms-taglib"]]
[#assign cmsu=JspTaglibs["cms-util-taglib"]]

[@cms.editBar /]
<h3>${content.title!("no title")}</h3>
<div>
    <div>
        Image: <img src="${ctx.contextPath}${content.image!"undefined"}" />
    </div>
    <br />
    <div>
        Text: ${content.text!("no text")}
    </div>
    <br />
    [#if content.date?has_content]
        The date you spedified: ${content.date?string("dd.MM.yyyy")}
    [/#if]
    <br />
</div>
<div>
    <ul>Parameter set by filter:
        <li>using model: ${model.filterAttribute!"not set"} </li>
        <li>get from request: ${ctx.sampleFilter!"not set"}</li>
    </ul>
</div>
<div id="search" >
    <form name="mgnlsearch" action="${ctx.contextPath}/SearchResult.html" method="post">
      <input id="query" name="query" value="${query!}" />
      <input type="submit" name="search" value="search" />
    </form>
    <br />
</div><!-- end search -->

<div>
    <h3>Display Sources</h3><br />
    <ul>
        <li><a href="${ctx.contextPath}/.sources/paragraphs/howTo.ftl">HowTo paragraph</a></li>
    </ul>
</div><br />
