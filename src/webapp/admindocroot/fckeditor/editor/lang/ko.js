/*
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003-2004 Frederico Caldeira Knabben
 * 
 * Licensed under the terms of the GNU Lesser General Public License:
 * 		http://www.opensource.org/licenses/lgpl-license.php
 * 
 * For further information visit:
 * 		http://www.fckeditor.net/
 * 
 * File Name: ko.js
 * 	Korean language file.
 * 
 * Version:  2.0 RC2
 * Modified: 2004-11-30 09:18:34
 * 
 * File Authors:
 * 		Taehwan Kwag (thkwag@nate.com)
 */

var FCKLang =
{
// Language direction : "ltr" (left to right) or "rtl" (right to left).
Dir					: "ltr",

ToolbarCollapse		: "툴바 감추기",
ToolbarExpand		: "툴바 보이기",

// Toolbar Items and Context Menu
Save				: "저장하기",
NewPage				: "새 문서",
Preview				: "미리보기",
Cut					: "잘라내기",
Copy				: "복사하기",
Paste				: "붙여넣기",
PasteText			: "텍스트로 붙여넣기",
PasteWord			: "MS Word 형식에서 붙여넣기",
Print				: "인쇄하기",
SelectAll			: "전체선택",
RemoveFormat		: "포맷 지우기",
InsertLinkLbl		: "링크",
InsertLink			: "링크 삽입/변경",
RemoveLink			: "링크 삭제",
InsertImageLbl		: "이미지",
InsertImage			: "이미지 삽입/변경",
InsertTableLbl		: "표",
InsertTable			: "표 삽입/변경",
InsertLineLbl		: "수평선",
InsertLine			: "수평선 삽입",
InsertSpecialCharLbl: "특수문자 삽입",
InsertSpecialChar	: "특수문자 삽입",
InsertSmileyLbl		: "아이콘",
InsertSmiley		: "아이콘 삽입",
About				: "FCKeditor에 대하여",
Bold				: "진하게",
Italic				: "이텔릭",
Underline			: "밑줄",
StrikeThrough		: "취소선",
Subscript			: "아래 첨자",
Superscript			: "위 첨자",
LeftJustify			: "왼쪽 정렬",
CenterJustify		: "가운데 정렬",
RightJustify		: "오른쪽 정렬",
BlockJustify		: "양쪽 맞춤",
DecreaseIndent		: "내어쓰기",
IncreaseIndent		: "들여쓰기",
Undo				: "취소",
Redo				: "재실행",
NumberedListLbl		: "순서있는 목록",
NumberedList		: "순서있는 목록",
BulletedListLbl		: "순서없는 목록",
BulletedList		: "순서없는 목록",
ShowTableBorders	: "표 테두리 보기",
ShowDetails			: "문서기호 보기",
Style				: "스타일",
FontFormat			: "포맷",
Font				: "폰트",
FontSize			: "글자 크기",
TextColor			: "글자 색상",
BGColor				: "배경 색상",
Source				: "소스",
Find				: "찾기",
Replace				: "바꾸기",

// Context Menu
EditLink			: "링크 수정",
InsertRow			: "가로줄 삽입",
DeleteRows			: "가로줄 삭제",
InsertColumn		: "세로줄 삽입",
DeleteColumns		: "세로줄 삭제",
InsertCell			: "셀 삽입",
DeleteCells			: "셀 삭제",
MergeCells			: "셀 합치기",
SplitCell			: "셀 나누기",
CellProperties		: "셀 속성",
TableProperties		: "표 속성",
ImageProperties		: "이미지 속성",

FontFormats			: "Normal;Formatted;Address;Heading 1;Heading 2;Heading 3;Heading 4;Heading 5;Heading 6",

// Alerts and Messages
ProcessingXHTML		: "XHTML 처리중. 잠시만 기다려주십시요.",
Done				: "완료",
PasteWordConfirm	: "붙여넣기 할 텍스트는 MS Word에서 복사한 것입니다. 붙여넣기 전에 MS Word 포멧을 삭제하시겠습니까?",
NotCompatiblePaste	: "이 명령은 인터넷익스플로러 5.5 버전 이상에서만 작동합니다. 포멧을 삭제하지 않고 붙여넣기 하시겠습니까?",
UnknownToolbarItem	: "알수없는 툴바입니다. : \"%1\"",
UnknownCommand		: "알수없는 기능입니다. : \"%1\"",
NotImplemented		: "기능이 실행되지 않았습니다.",
UnknownToolbarSet	: "툴바 설정이 없습니다. : \"%1\"",

// Dialogs
DlgBtnOK			: "예",
DlgBtnCancel		: "아니오",
DlgBtnClose			: "닫기",
DlgAdvancedTag		: "자세히",

// General Dialogs Labels
DlgGenNotSet		: "&lt;설정되지 않음&gt;",
DlgGenId			: "ID",
DlgGenLangDir		: "쓰기 방향",
DlgGenLangDirLtr	: "왼쪽에서 오른쪽 (LTR)",
DlgGenLangDirRtl	: "오른쪽에서 왼쪽 (RTL)",
DlgGenLangCode		: "언어 코드",
DlgGenAccessKey		: "엑세스 키",
DlgGenName			: "Name",
DlgGenTabIndex		: "탭 순서",
DlgGenLongDescr		: "URL 설명",
DlgGenClass			: "Stylesheet Classes",
DlgGenTitle			: "Advisory Title",
DlgGenContType		: "Advisory Content Type",
DlgGenLinkCharset	: "Linked Resource Charset",
DlgGenStyle			: "Style",

// Image Dialog
DlgImgTitle			: "이미지 설정",
DlgImgInfoTab		: "이미지 정보",
DlgImgBtnUpload		: "서버로 전송",
DlgImgURL			: "URL",
DlgImgUpload		: "업로드",
DlgImgBtnBrowse		: "서버 보기",
DlgImgAlt			: "이미지 설명",
DlgImgWidth			: "너비",
DlgImgHeight		: "높이",
DlgImgLockRatio		: "비율 유지",
DlgBtnResetSize		: "원래 크기로",
DlgImgBorder		: "테두리",
DlgImgHSpace		: "수평여백",
DlgImgVSpace		: "수직여백",
DlgImgAlign			: "정렬",
DlgImgAlignLeft		: "왼쪽",
DlgImgAlignAbsBottom: "줄아래(Abs Bottom)",
DlgImgAlignAbsMiddle: "줄중간(Abs Middle)",
DlgImgAlignBaseline	: "기준선",
DlgImgAlignBottom	: "아래",
DlgImgAlignMiddle	: "중간",
DlgImgAlignRight	: "오른쪽",
DlgImgAlignTextTop	: "글자위(Text Top)",
DlgImgAlignTop		: "위",
DlgImgPreview		: "미리보기",
DlgImgMsgWrongExt	: "죄송합니다. 다음 확장자를 가진 파일만 업로드 할 수 있습니다. :\n\n" + FCKConfig.ImageUploadAllowedExtensions + "\n\n작업이 취소되었습니다.",
DlgImgAlertSelect	: "업로드 할 이미지를 선택하십시요.",
DlgImgAlertUrl		: "이미지 URL을 입력하십시요",

// Link Dialog
DlgLnkWindowTitle	: "링크",
DlgLnkInfoTab		: "링크 정보",
DlgLnkTargetTab		: "타겟",

DlgLnkType			: "링크 종류",
DlgLnkTypeURL		: "URL",
DlgLnkTypeAnchor	: "책갈피",
DlgLnkTypeEMail		: "이메일",
DlgLnkProto			: "프로토콜",
DlgLnkProtoOther	: "&lt;기타&gt;",
DlgLnkURL			: "URL",
DlgLnkBtnBrowse		: "서버 보기",
DlgLnkAnchorSel		: "책갈피 선택",
DlgLnkAnchorByName	: "책갈피 이름",
DlgLnkAnchorById	: "책갈피 ID",
DlgLnkNoAnchors		: "&lt;문서에 책갈피가 없습니다.&gt;",
DlgLnkEMail			: "이메일 주소",
DlgLnkEMailSubject	: "제목",
DlgLnkEMailBody		: "내용",
DlgLnkUpload		: "업로드",
DlgLnkBtnUpload		: "서버로 전송",

DlgLnkTarget		: "타겟",
DlgLnkTargetFrame	: "&lt;프레임&gt;",
DlgLnkTargetPopup	: "&lt;팝업창&gt;",
DlgLnkTargetBlank	: "새 창 (_blank)",
DlgLnkTargetParent	: "부모 창 (_parent)",
DlgLnkTargetSelf	: "현재 창 (_self)",
DlgLnkTargetTop		: "최 상위 창 (_top)",
DlgLnkTargetFrame	: "타겟 프레임 이름",
DlgLnkPopWinName	: "팝업창 이름",
DlgLnkPopWinFeat	: "팝업창 설정",
DlgLnkPopResize		: "크기조정",
DlgLnkPopLocation	: "주소표시줄",
DlgLnkPopMenu		: "메뉴바",
DlgLnkPopScroll		: "스크롤바",
DlgLnkPopStatus		: "상태바",
DlgLnkPopToolbar	: "툴바",
DlgLnkPopFullScrn	: "전체화면 (IE)",
DlgLnkPopDependent	: "Dependent (Netscape)",
DlgLnkPopWidth		: "너비",
DlgLnkPopHeight		: "높이",
DlgLnkPopLeft		: "왼쪽 위치",
DlgLnkPopTop		: "윗쪽 위치",

DlgLnkMsgWrongExtA	: "죄송합니다. 다음 확장자를 가진 파일만 업로드 할 수 있습니다. :\n\n" + FCKConfig.LinkUploadAllowedExtensions + "\n\n작업이 취소되었습니다.",
DlgLnkMsgWrongExtD	: "죄송합니다. 다음 확장자를 가진 파일은 업로드 할 수 없습니다. :\n\n" + FCKConfig.LinkUploadDeniedExtensions + "\n\n작업이 취소되었습니다.",

DlnLnkMsgNoUrl		: "링크 URL을 입력하십시요.",	
DlnLnkMsgNoEMail	: "이메일주소를 입력하십시요.",	
DlnLnkMsgNoAnchor	: "책갈피명을 입력하십시요.",	

// Color Dialog
DlgColorTitle		: "색상 선택",
DlgColorBtnClear	: "지우기",
DlgColorHighlight	: "현재",
DlgColorSelected	: "선택됨",

// Smiley Dialog
DlgSmileyTitle		: "아이콘 삽입",

// Special Character Dialog
DlgSpecialCharTitle	: "특수문자 선택",

// Table Dialog
DlgTableTitle		: "표 설정",
DlgTableRows		: "가로줄",
DlgTableColumns		: "세로줄",
DlgTableBorder		: "테두리 크기",
DlgTableAlign		: "정렬",
DlgTableAlignNotSet	: "<설정되지 않음>",
DlgTableAlignLeft	: "왼쪽",
DlgTableAlignCenter	: "가운데",
DlgTableAlignRight	: "오른쪽",
DlgTableWidth		: "너비",
DlgTableWidthPx		: "픽셀",
DlgTableWidthPc		: "퍼센트",
DlgTableHeight		: "높이",
DlgTableCellSpace	: "셀 간격",
DlgTableCellPad		: "셀 여백",
DlgTableCaption		: "캡션",

// Table Cell Dialog
DlgCellTitle		: "셀 설정",
DlgCellWidth		: "너비",
DlgCellWidthPx		: "픽셀",
DlgCellWidthPc		: "퍼센트",
DlgCellHeight		: "높이",
DlgCellWordWrap		: "워드랩",
DlgCellWordWrapNotSet	: "<설정되지 않음>",
DlgCellWordWrapYes	: "예",
DlgCellWordWrapNo	: "아니오",
DlgCellHorAlign		: "수평 정렬",
DlgCellHorAlignNotSet	: "<설정되지 않음>",
DlgCellHorAlignLeft	: "왼쪽",
DlgCellHorAlignCenter	: "가운데",
DlgCellHorAlignRight: "오른쪽",
DlgCellVerAlign		: "수직 정렬",
DlgCellVerAlignNotSet	: "<설정되지 않음>",
DlgCellVerAlignTop	: "위",
DlgCellVerAlignMiddle	: "중간",
DlgCellVerAlignBottom	: "아래",
DlgCellVerAlignBaseline	: "기준선",
DlgCellRowSpan		: "세로 합치기",
DlgCellCollSpan		: "가로 합치기",
DlgCellBackColor	: "배경 색상",
DlgCellBorderColor	: "테두리 색상",
DlgCellBtnSelect	: "선택",

// Find Dialog
DlgFindTitle		: "찾기",
DlgFindFindBtn		: "찾기",
DlgFindNotFoundMsg	: "문자열을 찾을 수 없습니다.",

// Replace Dialog
DlgReplaceTitle			: "바꾸기",
DlgReplaceFindLbl		: "찾을 문자열:",
DlgReplaceReplaceLbl	: "바꿀 문자열:",
DlgReplaceCaseChk		: "대소문자 구분",
DlgReplaceReplaceBtn	: "바꾸기",
DlgReplaceReplAllBtn	: "모두 바꾸기",
DlgReplaceWordChk		: "온전한 단어",

// Paste Operations / Dialog
PasteErrorPaste	: "브라우저의 보안설정때문에 붙여넣기 기능을 실행할 수 없습니다. 키보드 명령을 사용하십시요. (Ctrl+V).",
PasteErrorCut	: "브라우저의 보안설정때문에 잘라내기 기능을 실행할 수 없습니다. 키보드 명령을 사용하십시요. (Ctrl+X).",
PasteErrorCopy	: "브라우저의 보안설정때문에 복사하기 기능을 실행할 수 없습니다. 키보드 명령을 사용하십시요.  (Ctrl+C).",

PasteAsText		: "텍스트로 붙여넣기",
PasteFromWord	: "MS Word 형식에서 붙여넣기",

DlgPasteMsg		: "브라우저의 <STRONG>보안설정/STRONG> 때문에 붙여넣기 할 수 없습니다. <BR>키보드 명령(<STRONG>Ctrl+V</STRONG>)을 이용하여 붙여넣은 다음 <STRONG>예</STRONG>버튼을 클릭하십시요.",

// Color Picker
ColorAutomatic	: "기본색상",
ColorMoreColors	: "색상선택...",

// About Dialog
DlgAboutVersion	: "버전",
DlgAboutLicense	: "Licensed under the terms of the GNU Lesser General Public License",
DlgAboutInfo	: "For further information go to"
}