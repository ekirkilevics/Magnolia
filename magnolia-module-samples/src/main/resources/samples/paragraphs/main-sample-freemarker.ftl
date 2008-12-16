
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

</div>
<div>
<ul>Parameter set by filter:
<li>using model: ${model.filterAttribute!("not set")} </li>
<li>get from request: ${ctx.sampleFilter!("not set")}</li>
</ul>
</div>
<div id="search" >
    <form name="mgnlsearch" action="" method="post">
      <input type="hidden" id="resultPage" name="resultPage" value="searchResultFTL" />
      <input id="query" name="query" value="${query!}" />
      <input type="submit" name="search" value="search" />
    </form>
</div><!-- end search -->
<div>
<h3>Display Sources</h3>
<ul>
<li><a href="${ctx.contextPath}/.sources/templates/main-sample.ftl">Main template</a></li>
<li><a href="${ctx.contextPath}/.sources/paragraphs/main-sample-freemarker.ftl">Main paragraph</a></li>
<li><a href="${ctx.contextPath}/.sources/paragraphs/search-freemarker.ftl">Search paragraph</a></li>
<li><a href="${ctx.contextPath}/.sources/templates/include/footer.ftl">Footer</a></li>
<li><a href="${ctx.contextPath}/.sources/templates/include/header.ftl">Header</a></li>
</ul>
</div>
