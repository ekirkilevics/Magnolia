// form; check for mandatory fields
function checkMandatories(formName,alertText)
	{
	var theForm=document[formName];
	var m=theForm.mgnlMandatory;
	var i=0;
	var ok=true;
	if (m)
		{
		if (!m[0])
			{
			var tmp=m;
			m=new Object();
			m[0]=tmp;
			}
		while (m[i])
			{
			var name=m[i].value;
            var type;

			if (theForm[name].type) type=theForm[name].type;
            else if (theForm[name][0] && theForm[name][0].type) type=theForm[name][0].type

			switch (type)
				{
				case "select-one":
					if (theForm[name].selectedIndex==0) ok=false;
					break;
                case "checkbox":
                case "radio":
                    var obj=new Object();
                    if (!theForm[name][0]) obj[0]=theForm[name];
                    else obj=theForm[name];
                    var okSmall=false;
                    var ii=0;
                    while (obj[ii])
                        {
                        if (obj[ii].checked)
                            {
                            okSmall=true;
                            break;
                            }
                        ii++;
                        }
                    if (!okSmall) ok=false;
                    break;
				default:
					if (!theForm[name].value) ok=false;
				}
			if (!ok)
				{
				while (alertText.indexOf("<br>")!=-1)
					{
					alertText=alertText.replace("<br>","\n");
					}
				alert(alertText);
				if (!theForm[name][0]) theForm[name].focus();
				return false;
				}
			i++;
			}
		}
	//if (ok)	theForm.submit();
	if (ok) return true;
	else return false;
	}