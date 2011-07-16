[@cms.edit /]

[#assign target = model.target!]

[#if target?has_content]
<li><a href="${model.targetLink!}">${content.title!target.title!target.@name}</a></li>
[#else]
<li><a href="${content.target!}">${content.title!content.target!}</a></li>
[/#if]
