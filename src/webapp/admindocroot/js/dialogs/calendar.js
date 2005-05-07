/* ###################################
### calendar.js
### open and draw the calendar dialog
################################### */

var weekend = [0,6];
var weekendColor = "#f6f6f6";
var nullGif="/admindocroot/0.gif";
var buttonEvents="onmousedown=mgnlShiftPushButtonDown(this); onmouseout=mgnlShiftPushButtonOut(this);"

var selectedTime;

var gNow = new Date();
var ggWinCal;
isNav = (navigator.appName.indexOf("Netscape") != -1) ? true : false;
isIE = (navigator.appName.indexOf("Microsoft") != -1) ? true : false;

Calendar.Months = new Array()
for(i=1;i<=12;i++){
	Calendar.Months[i-1] = mgnlMessages.get('js.dialog.calendar.month' + i);
}

// Non-Leap year Month days..
Calendar.DOMonth = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
// Leap year Month days..
Calendar.lDOMonth = [31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

function Calendar(p_showTime, p_item, p_WinCal, p_month, p_year, p_date, p_hour, p_minute, p_second) {
	if ((p_month == null) && (p_year == null))	return;

	if (p_WinCal == null)
		this.gWinCal = ggWinCal;
	else
		this.gWinCal = p_WinCal;

	if (p_month == null) {
		this.gMonthName = null;
		this.gMonth = null;
		this.gYearly = true;
	} else {
		this.gMonthName = Calendar.get_month(p_month);
		this.gMonth = new Number(p_month);
		this.gYearly = false;
	}

	var x=Calendar_get_daysofmonth(p_month,p_year);
	if (p_date>x) p_date=x;

	this.gYear = p_year;
	this.gHour = p_hour;
	this.gMinute = p_minute;
	this.gSecond = p_second;
	this.gDate = p_date;
	this.gBGColor = "white";
	this.gFGColor = "black";
	this.gTextColor = "black";
	this.gHeaderColor = "black";
	this.gReturnItem = p_item;
	this.gShowTime=p_showTime;
}

Calendar.get_month = Calendar_get_month;
Calendar.get_daysofmonth = Calendar_get_daysofmonth;
Calendar.calc_month_year = Calendar_calc_month_year;

function Calendar_get_month(monthNo) {
	return Calendar.Months[monthNo];
}

function Calendar_get_daysofmonth(monthNo, p_year) {
	/*
	Check for leap year ..
	1.Years evenly divisible by four are normally leap years, except for...
	2.Years also evenly divisible by 100 are not leap years, except for...
	3.Years also evenly divisible by 400 are leap years.
	*/
	if ((p_year % 4) == 0) {
		if ((p_year % 100) == 0 && (p_year % 400) != 0)
			return Calendar.DOMonth[monthNo];

		return Calendar.lDOMonth[monthNo];
	} else
		return Calendar.DOMonth[monthNo];
}

function Calendar_calc_month_year(p_Month, p_Year, incr) {
	/*
	Will return an 1-D array with 1st element being the calculated month
	and second being the calculated year
	after applying the month increment/decrement as specified by 'incr' parameter.
	'incr' will normally have 1/-1 to navigate thru the months.
	*/
	var ret_arr = new Array();

	if (incr == -1) {
		// B A C K W A R D
		if (p_Month == 0) {
			ret_arr[0] = 11;
			ret_arr[1] = parseInt(p_Year) - 1;
		}
		else {
			ret_arr[0] = parseInt(p_Month) - 1;
			ret_arr[1] = parseInt(p_Year);
		}
	} else if (incr == 1) {
		// F O R W A R D
		if (p_Month == 11) {
			ret_arr[0] = 0;
			ret_arr[1] = parseInt(p_Year) + 1;
		}
		else {
			ret_arr[0] = parseInt(p_Month) + 1;
			ret_arr[1] = parseInt(p_Year);
		}
	}

	return ret_arr;
}


function Calendar_calc_month_year(p_Month, p_Year, incr) {
	/*
	Will return an 1-D array with 1st element being the calculated month
	and second being the calculated year
	after applying the month increment/decrement as specified by 'incr' parameter.
	'incr' will normally have 1/-1 to navigate thru the months.
	*/
	var ret_arr = new Array();

	if (incr == -1) {
		// B A C K W A R D
		if (p_Month == 0) {
			ret_arr[0] = 11;
			ret_arr[1] = parseInt(p_Year) - 1;
		}
		else {
			ret_arr[0] = parseInt(p_Month) - 1;
			ret_arr[1] = parseInt(p_Year);
		}
	} else if (incr == 1) {
		// F O R W A R D
		if (p_Month == 11) {
			ret_arr[0] = 0;
			ret_arr[1] = parseInt(p_Year) + 1;
		}
		else {
			ret_arr[0] = parseInt(p_Month) + 1;
			ret_arr[1] = parseInt(p_Year);
		}
	}

	return ret_arr;
}

Calendar.prototype.getMonthlyCalendarCode = function() {
	var vCode = "";
	var vHeader_Code = "";
	var vData_Code = "";

	// Begin Table Drawing code here..
	//vCode = vCode + "<table cellpadding='2' cellspacing='1' bgcolor=\"" + this.gBGColor + "\">";
	vCode = vCode + "<table cellpadding=0 cellspacing=0 border=0>";

	vHeader_Code = this.cal_header();
	vData_Code = this.cal_data();

	vCode = vCode + "<tr>";
	vCode = vCode + "<td><img src="+nullGif+" width=33 height=1></td>";
	vCode = vCode + "<td><img src="+nullGif+" width=33 height=1></td>";
	vCode = vCode + "<td><img src="+nullGif+" width=33 height=1></td>";
	vCode = vCode + "<td><img src="+nullGif+" width=33 height=1></td>";
	vCode = vCode + "<td><img src="+nullGif+" width=33 height=1></td>";
	vCode = vCode + "<td><img src="+nullGif+" width=33 height=1></td>";
	vCode = vCode + "<td><img src="+nullGif+" width=33 height=1></td>";
	vCode = vCode + "</tr>";

	vCode = vCode + "<tr><td class=mgnlDialogBoxLine colspan=7><img src="+nullGif+" width=1 height=1></td></tr>";

	vCode = vCode + vHeader_Code;

	vCode = vCode + "<tr><td class=mgnlDialogBoxLine colspan=7><img src="+nullGif+" width=1 height=1></td></tr>";

	vCode = vCode + vData_Code;

	vCode = vCode + "<tr><td class=mgnlDialogBoxLine colspan=7><img src="+nullGif+" width=1 height=1></td></tr>";

	vCode = vCode + "</table>";

	return vCode;
}

Calendar.prototype.show = function() {
	var vCode = "";

	this.gWinCal.document.open();

	// Setup the page...
	this.wwrite("<html>");
	this.wwrite("<head><title>Calendar</title>");
	this.wwrite("<link rel='stylesheet' type='text/css' href='/admindocroot/css/controls.css'>");
	this.wwrite("<link rel='stylesheet' type='text/css' href='/admindocroot/css/dialogs.css'>");
	this.wwrite("<link rel='stylesheet' type='text/css' href='/admindocroot/css/calendar.css'>");
	this.wwrite("<script language='javascript' src=/admindocroot/js/controls.js></script>");
	this.wwrite("<script language='javascript'>");

	this.wwrite("function saveData() {");
	this.wwrite("var returnValue=dateString();");
	this.wwrite("self.opener.document.getElementById('" + this.gReturnItem + "').value=returnValue;");
	this.wwrite("window.close();");
	this.wwrite("}");

	this.wwrite("function dateString() {");
	this.wwrite("var form=document.forms.mgnlFormMain;");
	//this.wwrite("var returnValue=form.selectedyyyy.value+'-'+form.selectedmm.value+'-'+form.selecteddd.value");
	this.wwrite("var returnValue=form.selectedyyyy.value+'-'+form.selectedmm.value+'-'+form.selecteddd.value");
	if (this.gShowTime==true) this.wwrite("returnValue+='T'+form.selectedHH.value+':'+form.selectedMM.value+':'+form.selectedSS.value;");
	this.wwrite("return returnValue;");
	this.wwrite("}");

	this.wwrite("function selectedDateShow() {");
	this.wwrite("}");


	this.wwrite("function highlight(id) {");
	this.wwrite("");
	this.wwrite("var i=1;");
	this.wwrite("while (document.getElementById('date'+i)) {");
	this.wwrite("if (id==i) document.getElementById('date'+i).className='mgnlCalendarHi';");
	this.wwrite("else document.getElementById('date'+i).className='mgnlCalendar';");
	this.wwrite("");
	this.wwrite("i++;");
	this.wwrite("}");
	this.wwrite("}");

	this.wwrite("function dayOver(elem) {");
	this.wwrite("if (elem.className!='mgnlCalendarHi') elem.className='mgnlCalendarOver';");
	this.wwrite("}");

	this.wwrite("function dayOut(elem) {");
	this.wwrite("if (elem.className!='mgnlCalendarHi') elem.className='mgnlCalendar';");
	this.wwrite("}");


	this.wwrite("</script>");
	this.wwrite("</head>");

	this.wwrite("<body style='margin:7px;' class='mgnlBgLight' " +
		"link=\"" + this.gLinkColor + "\" " +
		"vlink=\"" + this.gLinkColor + "\" " +
		"alink=\"" + this.gLinkColor + "\" " +
		"text=\"" + this.gTextColor + "\">");

	this.wwrite("<table width='1%' border='0' cellspacing='0' cellpadding='0'>");
	this.wwrite("<tr>");
	this.wwrite("<td><img src="+nullGif+" width=11 height=11></td>");
	this.wwrite("<td><img src="+nullGif+" width=231 height=1></td>");

	this.wwrite("<tr><td></td><td>");

	// Show navigation buttons
	var prevMMYYYY = Calendar.calc_month_year(this.gMonth, this.gYear, -1);
	var prevMM = prevMMYYYY[0];
	var prevYYYY = prevMMYYYY[1];

	var nextMMYYYY = Calendar.calc_month_year(this.gMonth, this.gYear, 1);
	var nextMM = nextMMYYYY[0];
	var nextYYYY = nextMMYYYY[1];

	this.wwrite("<table width='100%' border='0' cellspacing='0' cellpadding='0'><tr>");

	this.wwrite("<tr><td class=mgnlDialogBoxLine colspan=3><img src="+nullGif+" width=1 height=1></td></tr>");
	this.wwrite("<tr><td height=4></td></tr>");

	this.wwrite("<td width='1%' align='center'>");
	this.wwrite("<span "+buttonEvents+" class=mgnlControlButton onclick=\"" +
		"window.opener.Build(" +
        this.gShowTime + ",'" + this.gReturnItem + "', '" + prevMM + "', '" + prevYYYY + "',document.forms.mgnlFormMain.selecteddd.value,document.forms.mgnlFormMain.selectedHH.value, document.forms.mgnlFormMain.selectedMM.value, document.forms.mgnlFormMain.selectedSS.value);" +
		"\">&nbsp;&laquo;&nbsp;</span></td><td width='98%' align='center'>");
	this.wwrite(this.gMonthName+"</td><td width='1%' align='center'>");
    this.wwrite("<span "+buttonEvents+" class=mgnlControlButton onclick=\"" +
    	"window.opener.Build(" +
		this.gShowTime + ",'" + this.gReturnItem + "', '" + nextMM + "', '" + nextYYYY + "',document.forms.mgnlFormMain.selecteddd.value,document.forms.mgnlFormMain.selectedHH.value, document.forms.mgnlFormMain.selectedMM.value, document.forms.mgnlFormMain.selectedSS.value);" +
		"\">&nbsp;&raquo;&nbsp;</span></td>");
	this.wwrite("</tr></table>");



	this.wwrite("<table width='100%' border='0' cellspacing='0' cellpadding='0'>");

	this.wwrite("<tr><td><br></td></tr>");
	this.wwrite("<tr><td class=mgnlDialogBoxLine colspan=3><img src="+nullGif+" width=1 height=1></td></tr>");
	this.wwrite("<tr><td height=4></td></tr>");

	this.wwrite("<tr><td width='1%' align='center'>");

    this.wwrite("<span "+buttonEvents+" class=mgnlControlButton onclick=\"" +
		"window.opener.Build(" +
		this.gShowTime + ",'" + this.gReturnItem + "', '" + this.gMonth + "', '" + (parseInt(this.gYear)-1) + "',document.forms.mgnlFormMain.selecteddd.value,document.forms.mgnlFormMain.selectedHH.value, document.forms.mgnlFormMain.selectedMM.value, document.forms.mgnlFormMain.selectedSS.value);" +
		"\">&nbsp;&laquo;&nbsp;</span><\/a></td>");
	this.wwrite("<td width='98%' align='center'>");
	this.wwrite(this.gYear+"</td><td width='1%' align='center'>");
    this.wwrite("<span "+buttonEvents+" class=mgnlControlButton onclick=\"" +
		"window.opener.Build(" +
		this.gShowTime + ",'" + this.gReturnItem + "', '" + this.gMonth + "', '" + (parseInt(this.gYear)+1) + "',document.forms.mgnlFormMain.selecteddd.value,document.forms.mgnlFormMain.selectedHH.value, document.forms.mgnlFormMain.selectedMM.value, document.forms.mgnlFormMain.selectedSS.value);" +
		"\">&nbsp;&raquo;&nbsp;</span><\/a></td></tr></table>");

	this.wwrite("</td></tr>");
	this.wwrite("<tr><td colspan='2'><img src='"+nullGif+"' width='1' height='15'>");
	this.wwrite("</td></tr>")
	this.wwrite("<tr><td></td><td>");


	// Get the complete calendar code for the month..
	vCode = this.getMonthlyCalendarCode();
	this.wwrite(vCode);

	this.wwrite("</td><td width='1'><img src='"+nullGif+"' width='1' height='155'></td></tr>");

	this.wwrite("<tr><td></td><td><br>");
	this.wwrite("<form name=mgnlFormMain>");

	var type="hidden"; //dev mode: set to text
	this.wwrite("<input type='"+type+"' name='selectedyyyy' value='"+this.gYear+"'>");
	this.wwrite("<input type='"+type+"' name='selectedmm' value='"+this.formatNumber((this.gMonth*1)+1)+"'>");
	this.wwrite("<input type='"+type+"' name='selecteddd' value='"+this.formatNumber(this.gDate)+"'>");
	this.wwrite("<input type='"+type+"' name='selectedHH' value='"+this.formatNumber(this.gHour)+"'>");
	this.wwrite("<input type='"+type+"' name='selectedMM' value='"+this.formatNumber(this.gMinute)+"'>");
	this.wwrite("<input type='"+type+"' name='selectedSS' value='"+this.formatNumber(this.gSecond)+"'>");
	this.wwrite("</td></tr>");


	if (this.gShowTime==true)
		{
		this.wwriteA("<tr><td></td><td class=mgnlDialogBoxLine><img src="+nullGif+" width=1 height=1></td></tr>");
		this.wwrite("<tr><td height=3></td></tr>");
		this.wwrite("<tr><td></td><td>");

		this.wwrite("<span class=description>Time: </span>");

		var now = new Date();

		this.wwrite("<select class=mgnlDialogControlSelect name='hour' onChange=\"document.forms.mgnlFormMain.selectedHH.value=this.options[this.selectedIndex].text;selectedDateShow();\">");

		var hour = 0;
		var hourString = "0";
		while (hour < 24) {
			hourString = (hour > 9 ? hour : "0"+hourString);
			this.wwriteA("<option");
			if (hour == this.gHour) this.wwriteA(" selected='selected'");
			this.wwrite(">"+hourString+"</option>");
			hour++;
			hourString = hour;
		}
		this.wwrite("</select>");
		this.wwrite(":");

		this.wwrite("<select class=mgnlDialogControlSelect name='minute' onChange=\"document.forms.mgnlFormMain.selectedMM.value=this.options[this.selectedIndex].text;selectedDateShow();\">");
		var minute = 0;
		var minuteString = "0";
		while (minute < 60) {
			minuteString = (minute > 9 ? minute : "0"+minuteString);
			this.wwriteA("<option");
			if (minute == this.gMinute) this.wwriteA(" selected='selected'");
			this.wwrite(">"+minuteString+"</option>");
			minute++;
			minuteString = minute;
		}
		this.wwrite("</select>");
		this.wwrite(":");

		this.wwrite("<select class=mgnlDialogControlSelect name='second' onChange=\"document.forms.mgnlFormMain.selectedSS.value=this.options[this.selectedIndex].text;selectedDateShow();\">");
		var second = 0;
		var secondString = "0";
		while (second < 60) {
			secondString = (second > 9 ? second : "0"+secondString);
			this.wwriteA("<option");
			if (second == this.gSecond) this.wwriteA(" selected='selected'");
			this.wwrite(">"+secondString+"</option>");
			second++;
			secondString = second;
		}
		this.wwrite("</select>");
	}


	this.wwriteA("</form>");
	this.wwriteA("</td></tr>");

	this.wwriteA("<tr><td colspan='2'><br></td></tr>");


	if (navigator.platform == "MacPPC") this.wwriteA("<tr><td colspan='2'><img src="+nullGif+" width='1' height='5'></td></tr>");

	this.wwriteA("<tr><td></td><td class=mgnlDialogBoxLine><img src="+nullGif+" width=1 height=1></td></tr>");
	this.wwriteA("<tr><td height=4></td></tr>");

	this.wwriteA("<tr><td></td><td align=right><span onclick='javascript:saveData()' class='mgnlControlButton' "+buttonEvents+">&nbsp;&nbsp;&nbsp;&nbsp;Ok&nbsp;&nbsp;&nbsp;&nbsp;</span>");
	this.wwriteA("&nbsp;");
    this.wwriteA("<span onclick='javascript:window.close();' class='mgnlControlButton' "+buttonEvents+">&nbsp;&nbsp;&nbsp;Cancel&nbsp;&nbsp;&nbsp;</span>");
	this.wwriteA("</td></tr>");
	this.wwriteA("</table>");
	this.wwrite("</body></html>");
	this.gWinCal.document.close();
}


Calendar.prototype.wwrite = function(wtext) {
	this.gWinCal.document.writeln(wtext);
}

Calendar.prototype.wwriteA = function(wtext) {
	this.gWinCal.document.write(wtext);
}

Calendar.prototype.cal_header = function() {
	var vCode = "";
	vCode = vCode + "<tr bgcolor=ffffff>";
	vCode = vCode + "<td width='14%' class=mgnlCalendarHeader align=center bgcolor="+weekendColor+">" + mgnlMessages.get('js.dialog.calendar.sun') + "</td>";
	vCode = vCode + "<td width='14%' class=mgnlCalendarHeader align=center>" + mgnlMessages.get('js.dialog.calendar.mon') + "</td>";
	vCode = vCode + "<td width='14%' class=mgnlCalendarHeader align=center>" + mgnlMessages.get('js.dialog.calendar.tue') + "</td>";
	vCode = vCode + "<td width='14%' class=mgnlCalendarHeader align=center>" + mgnlMessages.get('js.dialog.calendar.wed') + "</td>";
	vCode = vCode + "<td width='14%' class=mgnlCalendarHeader align=center>" + mgnlMessages.get('js.dialog.calendar.thu') + "</td>";
	vCode = vCode + "<td width='14%' class=mgnlCalendarHeader align=center>" + mgnlMessages.get('js.dialog.calendar.fri') + "</td>";
	vCode = vCode + "<td width='16%' class=mgnlCalendarHeader align=center bgcolor="+weekendColor+">" + mgnlMessages.get('js.dialog.calendar.sat') + "</td>";
	vCode = vCode + "</tr>";

	return vCode;
}

Calendar.prototype.cal_data = function() {
	var vDate = new Date();
	vDate.setDate(1);
	vDate.setMonth(this.gMonth);
	vDate.setFullYear(this.gYear);

	var vFirstDay=vDate.getDay();
	var vDay=1;
	var vLastDay=Calendar.get_daysofmonth(this.gMonth, this.gYear);
	var vOnLastDay=0;
	var vCode = "";

	/*
	Get day for the 1st of the requested month/year..
	Place as many blank cells before the 1st day of the month as necessary.
	*/

	vCode = vCode + "<tr>";
	for (i=0; i<vFirstDay; i++) {
		vCode = vCode + "<td width='14%'" + this.write_weekend_string(i) + ">&nbsp;</td>";
	}
	// Write rest of the 1st week
	for (j=vFirstDay; j<7; j++) {
		vCode = vCode + "<td width='14%'" + this.write_weekend_string(j) + " align=center id=date"+vDay+" "+
			this.set_class(vDay) +" "+
			"onClick=\";document.forms.mgnlFormMain.selecteddd.value='"+this.formatNumber(vDay)+"';selectedDateShow();highlight("+vDay+");\" " +
			"onmouseover=dayOver(this); "+
			"onmouseout=dayOut(this); "+
			">"+
				vDay +
			"</td>";
		vDay=vDay + 1;
	}
	vCode = vCode + "</tr>";

	// Write the rest of the weeks
	for (k=2; k<7; k++) {
		vCode = vCode + "<tr>";

		for (j=0; j<7; j++) {
			vCode = vCode + "<td width='14%'" + this.write_weekend_string(j) + " align=center id=date"+vDay+" "+
				this.set_class(vDay) +" "+
				"onClick=\";document.forms.mgnlFormMain.selecteddd.value='"+this.formatNumber(vDay)+"';selectedDateShow();highlight("+vDay+");\" " +
				"onmouseover=dayOver(this); "+
				"onmouseout=dayOut(this); "+
				">"+
				vDay +
				"</td>";
			vDay=vDay + 1;

			if (vDay > vLastDay) {
				vOnLastDay = 1;
				break;
			}
		}

		if (j == 6)
			vCode = vCode + "</tr>";
		if (vOnLastDay == 1)
			break;
	}

	// Fill up the rest of last week with proper blanks, so that we get proper square blocks
	for (m=1; m<(7-j); m++) {
		if (this.gYearly)
			vCode = vCode + "<td width='14%'" + this.write_weekend_string(j+m) +
			">&nbsp;</td>";
		else
			vCode = vCode + "<td width='14%'" + this.write_weekend_string(j+m) +
			">&nbsp;</td>";

			//">" + m + "</td>";
	}

	return vCode;
}

Calendar.prototype.format_day = function(vday) {
	var vNowDay = gNow.getDate();
	var vNowMonth = gNow.getMonth();
	var vNowYear = gNow.getFullYear();

	//if (vday == vNowDay && this.gMonth == vNowMonth && this.gYear == vNowYear)
	//	return ("<b>" + vday + "</b>");
	if (vday == this.gDate)
		return "<b>"+vday+"</b>";
	else
		return (vday);
}

Calendar.prototype.set_class = function(vday) {
	var vNowDay = gNow.getDate();
	var vNowMonth = gNow.getMonth();
	var vNowYear = gNow.getFullYear();

	//if (vday == vNowDay && this.gMonth == vNowMonth && this.gYear == vNowYear)
	//	return ("<b>" + vday + "</b>");
	if (vday == this.gDate)
		return "class=mgnlCalendarHi";
	else
		return "class=mgnlCalendar";
}



Calendar.prototype.formatNumber = function(i) {
	i=i.toString();
	if (i.length<2) i="0"+i;
	return i;
}




Calendar.prototype.write_weekend_string = function(vday) {
	var i;
    var color = gCal.gNormalDayBGColor;
	// Return special formatting for the weekend day.
	for (i=0; i<weekend.length; i++) {
		if (vday == weekend[i]) {
		    color = weekendColor;
		}
	}
	return (" bgColor=\"" + color + "\"");
}

Calendar.prototype.format_data = function(p_day) {
	var vData;
	var vMonth = 1 + this.gMonth;
	vMonth = (vMonth.toString().length < 2) ? "0" + vMonth : vMonth;
	var vY4 = new String(this.gYear);
	var vDD = (p_day.toString().length < 2) ? "0" + p_day : p_day;
	vData = vY4 + "-" + vMonth + "-" + vDD;
	return vData;
}

function Build(p_showTime, p_item, p_month, p_year, p_date, p_hour, p_minute, p_second) {
	var p_WinCal = ggWinCal;
	gCal = new Calendar(p_showTime, p_item, p_WinCal, p_month, p_year, p_date, p_hour, p_minute, p_second);
	gCal.gBGColor="cccccc";
	gCal.gNormalDayBGColor = "ffffff";
	gCal.gLinkColor="444444";
	gCal.gTextColor="444444";
	gCal.gHeaderColor="444444";
	gCal.gHeaderBGColor="F4F6E8";

	gCal.show();
}





function show_calendar() {
	p_item = arguments[1];

	var showTime=arguments[0];

	var y=arguments[2];
	var M=arguments[3];
	var d=arguments[4];
	var H=arguments[5];
	var m=arguments[6];
	var s=arguments[7];

	if (!y) {
		var now=new Date();
		y=now.getYear()
		if (now.getYear()<1900) y+=1900;
		M=now.getMonth();
		d=now.getDate();
		H=now.getHours();
		m=now.getMinutes();
		s=now.getSeconds();
	}
	gNow.setFullYear(y);
	gNow.setMonth(M);
	gNow.setDate(d);
	if (!H) {
		H=false;
		m=false;
		s=false;
	}
	gNow.setHours(H);
	gNow.setMinutes(m);
	gNow.setSeconds(s);

	p_month = new String(gNow.getMonth());
	p_year = new String(gNow.getFullYear().toString());
	vWinCal = window.open("/admindocroot/0.gif", "Calendar","width=275,height=360,status=no,resizable=no");
	vWinCal.opener = self;
	ggWinCal = vWinCal;
	selectedTime =
	Build(showTime, p_item, p_month, p_year, d, H, m, s);
	vWinCal.focus();

}





