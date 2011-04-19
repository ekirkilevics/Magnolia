<%@ taglib prefix="ui" uri="http://magnolia-cms.com/taglib/templating-components" %>

<div id="basic">
    <ui:edit/>
</div>
<div id="customDialog">
    <ui:edit dialog="myCustomDialog"/>
</div>
<div id="customEditLabel">
    <ui:edit editLabel="custom edit label"/>
</div>
<div id="noMove">
    <ui:edit move="false"/>
</div>
<div id="noDelete">
    <ui:edit delete="false"/>
</div>
<div id="onlyEdit">
    <ui:edit move="false" delete="false"/>
</div>

<!-- TODO try to set target too -->