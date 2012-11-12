<%@ taglib prefix="cmsfn" uri="http://magnolia-cms.com/taglib/templating-components/cmsfn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head/>

<body>
<div id="1">
    res=${(cmsfn:children(content, 'mgnl:area'))[0]['@path']}
</div>
<div id="2">
    res=${(cmsfn:children(content,''))[1]['@path']}
</div>
<div id="3">
    res=${(cmsfn:children(content,null))[1]['@path']}
</div>

</body>

</html>