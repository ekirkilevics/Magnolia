[#-------------- HowTo Component --------------]


[@cms.edit /]

<h3>${content.title!content.@name}</h3>

Text: ${content.text!("No text defined yet")}

<p>
    [#if content.imageurl?has_content]
        Image: <img src="${ctx.contextPath}${content.imageurl}" />
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


<ul>Parameters set by filter:
    <li>using model: ${model.filterAttribute} </li>
    <li>get from request: ${ctx.sampleFilter}</li>
</ul>



[#include "/samples/includes/inc.searchForm.ftl" ]


<h4>Display Component's Sources</h4>
<ul>
    <li><a href="${ctx.contextPath}/.sources/components/howTo.ftl">'HowTo' component</a></li>
</ul>
