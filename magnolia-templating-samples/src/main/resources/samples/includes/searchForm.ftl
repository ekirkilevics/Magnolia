
<div id="search" >
    <form name="mgnlsearch" action="${ctx.contextPath}/sample-site/ftl-searchResult.html" method="post">
      <input id="query" name="query" value="${ctx.query!}" />
      <input type="submit" name="search" value="search" />
    </form>
</div><!-- end search -->