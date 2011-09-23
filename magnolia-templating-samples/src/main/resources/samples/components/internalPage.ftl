[@cms.edit/]
[#-- Assigns: Get and check Teaser Target --]
[#assign target = model.target!]
[#assign hasTarget = target?has_content]

[#-- Assigns: Macro assigning Values --]
[#macro assignValues]
    [#-- Assigns: Get Content --]
    [#assign title = content.teaserTitle!target.title]
    [#assign text = content.teaserAbstract!target.abstract!]
    [#assign teaserLink = model.teaserLink!]
    [#assign hasLinkList = content.hasLinkList!false]

    [#if !hideTeaserImage]
        [#assign imageLink = (model.image!).link!]
        [#if !imageLink?has_content && divIDPrefix="teaser"]  [#-- TODO: This is a hack, solution with imageLink return object must be implemented --]
            [#assign divClass = "${divClass} mod"]
        [/#if]
    [/#if]

    [#-- Assigns: Is Content Available --]
    [#assign hasImageLink = imageLink?has_content]
    [#assign hasText = text?has_content]

    [#-- Assigns: Define alt for image tag --]
    [#if hasImageLink]
        [#assign imageAlt = title]
    [#else]
        [#assign imageAlt = "${i18n['image.resolveError']}"]
    [/#if]
[/#macro]



[#-------------- RENDERING PART --------------]

[#-- Rendering: Teaser Internal Page --]
[#if hasTarget]

    <div>
        <h3>
            <a href="${model.teaserLink}">
                ${target.name!''}
            </a>
        </h3>
    </div><!-- end  -->

[#else]
    No content
[/#if]
