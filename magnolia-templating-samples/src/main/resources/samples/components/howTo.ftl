[#-------------- HowTo Component --------------]


<h3>${content.title!content.@name}</h3>

Text: ${cmsfn.decode(content).text!"No text defined yet"}

<p>
    [#if content.image?has_content]
        Image: <img src="${cmsfn.link(content.image)}" />
    [#else]
        Image: No image uploaded yet.
    [/#if]
</p>

<p>
    [#if content.date?has_content]
        The date you specified: ${content.date?string("EEEE, d. MMMM yyyy")}
    [#else]
        No date specified yet.
    [/#if]
</p>


<h4>New Search</h4>
[#include "/samples/includes/searchForm.ftl" ]


<h4>Display Component's Sources</h4>
<ul>
    <li><a href="${ctx.contextPath}/.sources/components/howTo.ftl">'HowTo' component</a></li>
</ul>
