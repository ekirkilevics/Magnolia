/* ###################################
### controls.js
################################### */

var mgnlControlButtonBorderDark="#396101";
var mgnlControlButtonBorderLight="#ADC97B";
var mgnlControlButtonPUSHED="_PUSHED";

function mgnlShiftDividedButton(id)
	{
	var button=document.getElementById(id);
	if (button)
		{
		var state;
		if (button.type=="checkbox") state=!button.checked;
		else state=true;
		button.checked=state;
		}
	}

function mgnlShiftPushButtonDown(button)
	{
	if (button.className.indexOf(mgnlControlButtonPUSHED)==-1)
		{
		button.style.borderTopColor=mgnlControlButtonBorderDark;
		button.style.borderLeftColor=mgnlControlButtonBorderDark;
		button.style.borderBottomColor=mgnlControlButtonBorderLight;
		button.style.borderRightColor=mgnlControlButtonBorderLight;
		}
	}

function mgnlShiftPushButtonOut(button)
	{
	button.style.borderTopColor="";
	button.style.borderLeftColor="";
	button.style.borderBottomColor="";
	button.style.borderRightColor="";
	}

function mgnlShiftPushButtonClick(button)
	{
	if (button.id.indexOf("_SETBUTTON_")!=-1)
		{
		//is part of a button set (radio, e.g. tab buttons) -> disable all other buttons
		var baseId=button.id.substring(0,button.id.lastIndexOf("_"));
		var i=0;
		var className=button.className;
		if (className.indexOf(mgnlControlButtonPUSHED)==-1)
			{
			while (document.getElementById(baseId+"_"+i))
				{
				var currentButton=document.getElementById(baseId+"_"+i)
				currentButton.className=className;
				i++;
				}
			button.className=className+mgnlControlButtonPUSHED;
			}
		}
	mgnlShiftPushButtonOut(button); //get rid of hilighted borders
	}


