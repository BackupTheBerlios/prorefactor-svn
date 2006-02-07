/*

JPTreeParser.g - Primary tree parser.

Joanju Proparse Syntax Tree Structure Specification

Copyright (c) 2001-2005 Joanju Limited.
All rights reserved. This program and the accompanying materials 
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html


This tree parser has no actions. It is the basis for creating new
tree parsers.


Notes:
	- Token types always start with a capital letter
	- functions always start with a lowercase letter
	- ALLCAPS is by convention the name for a real token type
	- Mixed_case is by convention the name for a synthetic node's token type
	- This: #(
	  means that the first node is root, the rest are children of that root.
	- the pipe symbol "|" represents logical OR, of course
	- "something" is optional: (something)?
	- "something" must be present one or more times: (something)+
	- "something" may be there zero, one, or many times: (something)*
	- A period represents a token of any type.

*/


header {
	package org.prorefactor.treeparserbase;

	import org.prorefactor.core.IJPNode;
	import org.prorefactor.treeparser.IJPTreeParser;
}



options {
	language = "Java";
}



// Class preamble - this gets inserted near the top of the .java file.
{
} // Class preamble



// class definition options for Antlr
class JPTreeParser extends TreeParser;
options {
	importVocab = ProParser;
	defaultErrorHandler = false;
	classHeaderSuffix = IJPTreeParser;
}



// This is added to top of the class definitions
{

	// Where did the tree parser leave off parsing -- might give us at least a bit
	// of an idea where things left off if an exception was thrown.
	// See antlr/TreeParser and the generated code.
	public AST get_retTree() {
		return _retTree;
	}

	// Func for grabbing the "state2" attribute from the node at LT(1) 
	private boolean state2(AST node, int match) {
		return ((IJPNode)node).getState2() == match;
	}

}



///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin grammar
///////////////////////////////////////////////////////////////////////////////////////////////////


program
	:	#(Program_root (blockorstate)* Program_tail)
	;

code_block
	:	#(Code_block (blockorstate)* )
	;

blockorstate
	:	(	labeled_block
		|	statement
		|	// Expr_statement has a "statehead" node attribute
			#(Expr_statement expression (NOERROR_KW)? state_end)
		|	PROPARSEDIRECTIVE
		|	PERIOD
		|	DOT_COMMENT
		)
	;

labeled_block
	:	#(BLOCK_LABEL LEXCOLON (dostate|forstate|repeatstate) )
	;


block_colon
	:	LEXCOLON | PERIOD
	;
block_end
	:	EOF
	|	END state_end
	;
block_for
	:	#(FOR RECORD_NAME (COMMA RECORD_NAME)* )
	;
block_opt
	:	#(Block_iterator field EQUAL expression TO expression (BY constant)? )
	|	querytuningphrase 
	|	#(WHILE expression )
	|	TRANSACTION 
	|	on___phrase 
	|	framephrase 
	|	BREAK
	|	#(BY expression (DESCENDING)? )
	|	collatephrase
	|	#(GROUP ( #(BY expression (DESCENDING)? ) )+ )
	;
block_preselect
	:	#(PRESELECT for_record_spec )
	;

statement
// All statement first nodes have a node attribute of "statehead".
// Additionally, for those first statement nodes which are ambiguous
// (ex: CREATE), there is an additional disambiguating attribute of "state2".
	:						aatracestatement
	|						accumulatestate
 	|						altertablestate
 	|						analyzestate
	|						applystate
	|						assignstate
	|						bellstate
	|						buffercomparestate
	|						buffercopystate
	|						callstate
	|						casestate
	|						choosestate
	|						clearstate
	|	{state2(_t, 0)}?			closestate			// SQL
	|	{state2(_t, QUERY)}?			closequerystate
	|	{state2(_t, STOREDPROCEDURE)}?	closestoredprocedurestate
	|						colorstate
	|						compilestate
	|						connectstate  
	|						copylobstate
	|	{state2(_t, 0)}?			createstate
	|	{state2(_t, ALIAS)}?			createaliasstate
	|	{state2(_t, Automationobject)}?	createautomationobjectstate
	|	{state2(_t, BROWSE)}?			createbrowsestate
	|	{state2(_t, BUFFER)}?			createbufferstate
	|	{state2(_t, CALL)}?			createcallstate
	|	{state2(_t, DATABASE)}?		createdatabasestate
	|	{state2(_t, DATASET)}?			createdatasetstate
	|	{state2(_t, DATASOURCE)}?		createdatasourcestate
	|	{state2(_t, INDEX)}?			createindexstate		// SQL
	|	{state2(_t, QUERY)}?			createquerystate   
	|	{state2(_t, SAXREADER)}?		createsaxreaderstate
	|	{state2(_t, SERVER)}?			createserverstate
	|	{state2(_t, SERVERSOCKET)}?		createserversocketstate
	|	{state2(_t, SOAPHEADER)}?		createsoapheaderstate
	|	{state2(_t, SOAPHEADERENTRYREF)}?	createsoapheaderentryrefstate
	|	{state2(_t, SOCKET)}?			createsocketstate
	|	{state2(_t, TABLE)}?			createtablestate		// SQL
	|	{state2(_t, TEMPTABLE)}?		createtemptablestate
	|	{state2(_t, VIEW)}?			createviewstate			// SQL
	|	{state2(_t, WIDGET)}?			createwidgetstate
	|	{state2(_t, WIDGETPOOL)}?		createwidgetpoolstate
	|	{state2(_t, XDOCUMENT)}?		createxdocumentstate
	|	{state2(_t, XNODEREF)}?		createxnoderefstate
	|	{state2(_t, ADVISE)}?			ddeadvisestate
	|	{state2(_t, EXECUTE)}?		ddeexecutestate
	|	{state2(_t, GET)}?			ddegetstate
	|	{state2(_t, INITIATE)}?		ddeinitiatestate
	|	{state2(_t, REQUEST)}?		dderequeststate
	|	{state2(_t, SEND)}?			ddesendstate
	|	{state2(_t, TERMINATE)}?		ddeterminatestate	
	|						declarecursorstate
	|	{state2(_t, BROWSE)}?			definebrowsestate
	|	{state2(_t, BUFFER)}?			definebufferstate
	|	{state2(_t, BUTTON)}?			definebuttonstate
	|	{state2(_t, DATASET)}?			definedatasetstate
	|	{state2(_t, DATASOURCE)}?		definedatasourcestate
	|	{state2(_t, FRAME)}?			defineframestate
	|	{state2(_t, IMAGE)}?			defineimagestate
	|	{state2(_t, MENU)}?			definemenustate
	|	{state2(_t, PARAMETER)}?		defineparameterstate
	|	{state2(_t, QUERY)}?			definequerystate
	|	{state2(_t, RECTANGLE)}?		definerectanglestate
	|	{state2(_t, STREAM)}?			definestreamstate
	|	{state2(_t, SUBMENU)}?		definesubmenustate
	|	{state2(_t, TEMPTABLE)}?		definetemptablestate
	|	{state2(_t, WORKTABLE)}?		defineworktablestate
	|	{state2(_t, VARIABLE)}?		definevariablestate
	|						dictionarystate
	|	{state2(_t, 0)}?			deletestate
	|	{state2(_t, ALIAS)}?			deletealiasstate
	|	{state2(_t, FROM)}?			deletefromstate
	|	{state2(_t, OBJECT)}?			deleteobjectstate
	|	{state2(_t, PROCEDURE)}?		deleteprocedurestate
	|	{state2(_t, WIDGET)}?			deletewidgetstate
	|	{state2(_t, WIDGETPOOL)}?		deletewidgetpoolstate
	|	{state2(_t, 0)}?			disablestate
	|	{state2(_t, TRIGGERS)}?		disabletriggersstate
	|						disconnectstate
	|						displaystate
	|						dostate
	|						downstate
	|	{state2(_t, INDEX)}?			dropindexstate			// SQL
	|	{state2(_t, TABLE)}?			droptablestate			// SQL
	|	{state2(_t, VIEW)}?			dropviewstate			// SQL
	|						emptytemptablestate  
	|						enablestate
	|						exportstate
	|						fetchstate
	|						findstate
	|						forstate
	|						formstate
	|						functionstate
	|						getstate
	|						getkeyvaluestate  
	|						grantstate
	|						hidestate
	|						ifstate
	|						importstate  
	|	{state2(_t, CLEAR)}?			inputclearstate
	|	{state2(_t, CLOSE)}?			inputclosestate
	|	{state2(_t, FROM)}?			inputfromstate
	|	{state2(_t, THROUGH)}?		inputthroughstate
	|	{state2(_t, CLOSE)}?			inputoutputclosestate
	|	{state2(_t, THROUGH)}?		inputoutputthroughstate
	|	{state2(_t, INTO)}?			insertintostate			// SQL
	|	{state2(_t, 0)}?			insertstate
	|						leavestate
	|						loadstate  
	|						messagestate
	|						nextstate
	|						nextpromptstate
	|						onstate  
	|	{state2(_t, 0)}?			openstate			// SQL
	|	{state2(_t, QUERY)}?			openquerystate
	|						osappendstate
	|						oscommandstate
	|						oscopystate
	|						oscreatedirstate  
	|						osdeletestate
	|						osrenamestate
	|	{state2(_t, CLOSE)}?			outputclosestate
	|	{state2(_t, THROUGH)}?		outputthroughstate
	|	{state2(_t, TO)}?			outputtostate
	|						pagestate  
	|						pausestate
	|						procedurestate
	|						processeventsstate
	|						promptforstate
	|						publishstate
	|	{state2(_t, 0)}?			putstate
	|	{state2(_t, CURSOR)}?			putcursorstate
	|	{state2(_t, SCREEN)}?			putscreenstate
	|						putkeyvaluestate
	|						quitstate
	|						rawtransferstate
	|						readkeystate
	|	{state2(_t, 0)}?			releasestate
	|	{state2(_t, EXTERNAL)}?		releaseexternalstate
	|	{state2(_t, OBJECT)}?			releaseobjectstate
	|						repeatstate
	|						repositionstate  
	|						returnstate
	|						revokestate
	|	{state2(_t, 0)}?			runstate
	|	{state2(_t, STOREDPROCEDURE)}?	runstoredprocedurestate
	|	{state2(_t, SUPER)}?			runsuperstate
	|						savecachestate
	|						scrollstate
	|						seekstate  
	|						selectstate
	|						setstate
	|						showstatsstate
	|						statusstate  
	|						stopstate
	|						subscribestate
	|	{state2(_t, COLOR)}?			systemdialogcolorstate
	|	{state2(_t, FONT)}?			systemdialogfontstate
	|	{state2(_t, GETDIR)}?		systemdialoggetdirstate
	|	{state2(_t, GETFILE)}?		systemdialoggetfilestate
	|	{state2(_t, PRINTERSETUP)}?		systemdialogprintersetupstate
	|						systemhelpstate
	|						transactionmodeautomaticstate
	|						triggerprocedurestate
	|						underlinestate  
	|						undostate
	|						unloadstate
	|						unsubscribestate
	|						upstate  
	|						updatestatement
	|						usestate
	|						validatestate
	|						viewstate
	|						waitforstate
	;


pseudfn
// See PSC's grammar for <pseudfn> and for <asignmt>.
// These are functions that can (or, in some cases, must) be an l-value.
	:	#(FIXCODEPAGE funargs )
	|	#(OVERLAY funargs )
	|	#(PUTBITS funargs )
	|	#(PUTBYTE funargs )
	|	#(PUTBYTES funargs )
	|	#(PUTDOUBLE funargs )
	|	#(PUTFLOAT funargs )
	|	#(PUTLONG funargs )
	|	#(PUTSHORT funargs )
	|	#(PUTSTRING funargs )
	|	#(PUTUNSIGNEDSHORT funargs )
	|	#(SETBYTEORDER funargs )
	|	#(SETPOINTERVALUE funargs )
	|	#(SETSIZE funargs )
	|	AAMSG // not the whole func - we don't want its arguments here
	|	currentvaluefunc
	|	CURRENTWINDOW
	|	dynamiccurrentvaluefunc
	|	entryfunc
	|	lengthfunc
	|	nextvaluefunc
	|	rawfunc
	|	substringfunc
	|	widattr
	// Keywords from <optargfn> and <noargfn>. Assignments to those
	// are accepted by the compiler, however, assignment to them seems to have
	// no affect at runtime.
	// The following are from <optargfn>
	| PAGESIZE_KW | LINECOUNTER | PAGENUMBER | FRAMECOL
	| FRAMEDOWN | FRAMELINE | FRAMEROW | USERID | ETIME_KW
	// The following are from <noargfn>
	| DBNAME | TIME | OPSYS | RETRY | AASERIAL | AACONTROL
	| MESSAGELINES | TERMINAL | PROPATH | CURRENTLANGUAGE | PROMSGS
	| SCREENLINES | LASTKEY
	| FRAMEFIELD | FRAMEFILE | FRAMEVALUE | GOPENDING
	| PROGRESS | FRAMEINDEX | FRAMEDB | FRAMENAME | DATASERVERS
	| NUMDBS | NUMALIASES | ISATTRSPACE | PROCSTATUS
	| PROCHANDLE | CURSOR | OSERROR | RETURNVALUE | OSDRIVES
	| PROVERSION | TRANSACTION | MACHINECLASS 
	| AAPCONTROL | GETCODEPAGES | COMSELF
	;

functioncall
	:	#(ACCUMULATE accum_what (#(BY expression (DESCENDING)?))? expression )
	|	#(ADDINTERVAL LEFTPAREN expression COMMA expression COMMA expression RIGHTPAREN )
	|	#(CANFIND LEFTPAREN (findwhich)? recordphrase RIGHTPAREN )
	|	currentvaluefunc // is also a pseudfn.
	|	dynamiccurrentvaluefunc // is also a pseudfn.
	|	#(DYNAMICFUNCTION LEFTPAREN expression (#(IN_KW expression))? (COMMA parameter)* RIGHTPAREN (NOERROR_KW)? )
		// ENTERED and NOTENTERED are only dealt with as part of an expression term. See: exprt.
	|	entryfunc // is also a pseudfn.
	|	#(ETIME_KW (funargs)? )
	|	#(EXTENT LEFTPAREN field RIGHTPAREN )
	|	#(FRAMECOL (LEFTPAREN ID RIGHTPAREN)? )
	|	#(FRAMEDOWN (LEFTPAREN ID RIGHTPAREN)? )
	|	#(FRAMELINE (LEFTPAREN ID RIGHTPAREN)? )
	|	#(FRAMEROW (LEFTPAREN ID RIGHTPAREN)? )
	|	#(GETCODEPAGES (funargs)? )
	|	#(IF expression THEN expression ELSE expression )
	|	ldbnamefunc 
	|	lengthfunc // is also a pseudfn.
	|	#(LINECOUNTER (LEFTPAREN ID RIGHTPAREN)? )
	|	#(LOADPICTURE (funargs)? )
	|	#(MTIME LEFTPAREN (expression)? RIGHTPAREN )
	|	nextvaluefunc // is also a pseudfn.
		// ENTERED and NOTENTERED are only dealt with as part of an expression term. See: exprt.
	|	#(PAGENUMBER (LEFTPAREN ID RIGHTPAREN)? )
	|	#(PAGESIZE_KW (LEFTPAREN ID RIGHTPAREN)? )
	|	rawfunc // is also a pseudfn.
	|	#(SEEK LEFTPAREN (INPUT|OUTPUT|ID) RIGHTPAREN )
	|	substringfunc // is also a pseudfn.
	|	#(SUPER (parameterlist)? )
	|	#(TIMEZONE LEFTPAREN (expression)? RIGHTPAREN )
	|	#(USERID (funargs)? )
	|	#(USER (funargs)? )
	|	sqlaggregatefunc  
	|	argfunc
	|	noargfunc
	|	recordfunc
	;

argfunc
	:	#(AACBIT funargs )
	|	#(AAMSG funargs )
	|	#(ABSOLUTE funargs )
	|	#(ALIAS funargs )
	|	#(ASC funargs )
	|	#(BASE64DECODE funargs )
	|	#(BASE64ENCODE funargs )
	|	#(CANDO funargs )
	|	#(CANQUERY funargs )
	|	#(CANSET funargs )
	|	#(CAPS funargs )
	|	#(CHR funargs )
	|	#(CODEPAGECONVERT funargs )
	|	#(COLLATE funargs ) // See docs for BY phrase in FOR, PRESELECT, etc.
	|	#(COMPARE funargs )
	|	#(CONNECTED funargs )
	|	#(COUNTOF funargs )
	|	#(CURRENTRESULTROW funargs )
	|	#(DATE funargs )
	|	#(DATETIME funargs )
	|	#(DATETIMETZ funargs )
	|	#(DAY funargs )
	|	#(DBCODEPAGE funargs )
	|	#(DBCOLLATION funargs )
	|	#(DBPARAM funargs )
	|	#(DBRESTRICTIONS funargs )
	|	#(DBTASKID funargs )
	|	#(DBTYPE funargs )
	|	#(DBVERSION funargs )
	|	#(DECIMAL funargs )
	|	#(DECRYPT funargs )
	|	#(DYNAMICNEXTVALUE funargs )
	|	#(ENCODE funargs )
	|	#(ENCRYPT funargs )
	|	#(EXP funargs )
	|	#(FILL funargs )
	|	#(FIRST funargs )
	|	#(FIRSTOF funargs )
	|	#(GENERATEPBEKEY funargs )
	|	#(GETBITS funargs )
	|	#(GETBYTE funargs )
	|	#(GETBYTEORDER funargs )
	|	#(GETBYTES funargs )
	|	#(GETCOLLATIONS funargs )
	|	#(GETDOUBLE funargs )
	|	#(GETFLOAT funargs )
	|	#(GETLICENSE funargs )
	|	#(GETLONG funargs )
	|	#(GETPOINTERVALUE funargs )
	|	#(GETSHORT funargs )
	|	#(GETSIZE funargs )
	|	#(GETSTRING funargs )
	|	#(GETUNSIGNEDSHORT funargs )
	|	#(INDEX funargs )
	|	#(INTEGER funargs )
	|	#(INTERVAL funargs )
	|	#(ISCODEPAGEFIXED funargs )
	|	#(ISCOLUMNCODEPAGE funargs )
	|	#(ISLEADBYTE funargs )
	|	#(ISODATE funargs )
	|	#(KBLABEL funargs )
	|	#(KEYCODE funargs )
	|	#(KEYFUNCTION funargs )
	|	#(KEYLABEL funargs )
	|	#(KEYWORD funargs )
	|	#(KEYWORDALL funargs )
	|	#(LAST funargs )
	|	#(LASTOF funargs )
	|	#(LC funargs )
	|	#(LEFTTRIM funargs )
	|	#(LIBRARY funargs )
	|	#(LISTEVENTS funargs )
	|	#(LISTQUERYATTRS funargs )
	|	#(LISTSETATTRS funargs )
	|	#(LISTWIDGETS funargs )
	|	#(LOG funargs )
	|	#(LOGICAL funargs )
	|	#(LOOKUP funargs )
	|	#(MAXIMUM funargs )
	|	#(MD5DIGEST funargs )
	|	#(MEMBER funargs )
	|	#(MINIMUM funargs )
	|	#(MONTH funargs )
	|	#(NORMALIZE funargs )
	|	#(NUMENTRIES funargs )
	|	#(NUMRESULTS funargs )
	|	#(OSGETENV funargs )
	|	#(PDBNAME funargs )
	|	#(PROGRAMNAME funargs )
	|	#(QUERYOFFEND funargs )
	|	#(QUOTER funargs )
	|	#(RINDEX funargs )
	|	#(RANDOM funargs )
	|	#(REPLACE funargs )
	|	#(RGBVALUE funargs )
	|	#(RIGHTTRIM funargs )
	|	#(ROUND funargs )
	|	#(SDBNAME funargs )
	|	#(SEARCH funargs )
	|	#(SETUSERID funargs )
	|	#(SHA1DIGEST funargs )
	|	#(SQRT funargs )
	|	#(SSLSERVERNAME funargs )
	|	#(STRING funargs )
	|	#(SUBSTITUTE funargs )
	|	#(TOROWID funargs )
	|	#(TRIM funargs )
	|	#(TRUNCATE funargs )
	|	#(VALIDEVENT funargs )
	|	#(VALIDHANDLE funargs )
	|	#(WEEKDAY funargs )
	|	#(WIDGETHANDLE funargs )
	|	#(YEAR funargs )
	;

recordfunc
	:	#(AMBIGUOUS recordfunargs )
	|	#(AVAILABLE recordfunargs )
	|	#(CURRENTCHANGED recordfunargs )
	|	#(DATASOURCEMODIFIED recordfunargs )
	|	#(ERROR recordfunargs )
	|	#(LOCKED recordfunargs )
	|	#(NEW recordfunargs )
	|	#(RECID recordfunargs )
	|	#(RECORDLENGTH recordfunargs )
	|	#(REJECTED recordfunargs )
	|	#(ROWID recordfunargs )
	|	#(ROWSTATE recordfunargs )
	;
recordfunargs
	:	(LEFTPAREN RECORD_NAME RIGHTPAREN | RECORD_NAME)
	;

noargfunc
	:	AACONTROL
	|	AAPCONTROL
	|	AASERIAL
	|	CURRENTLANGUAGE
	|	CURSOR
	|	DATASERVERS
	|	DBNAME
	|	FRAMEDB
	|	FRAMEFIELD
	|	FRAMEFILE
	|	FRAMEINDEX
	|	FRAMENAME
	|	FRAMEVALUE
	|	GENERATEPBESALT
	|	GENERATERANDOMKEY
	|	GATEWAYS
	|	GOPENDING
	|	ISATTRSPACE
	|	LASTKEY
	|	MACHINECLASS
	|	MESSAGELINES
	|	NOW
	|	NUMALIASES
	|	NUMDBS
	|	OPSYS
	|	OSDRIVES
	|	OSERROR
	|	PROCHANDLE
	|	PROCSTATUS
	|	PROGRESS
	|	PROMSGS
	|	PROPATH
	|	PROVERSION
	|	RETRY
	|	RETURNVALUE
	|	SCREENLINES
	|	TERMINAL
	|	TIME
	|	TODAY
	|	TRANSACTION
	;


parameter
	:	#(BUFFER (RECORD_NAME | ID FOR RECORD_NAME ) )
	|	#(PARAMETER expression EQUAL expression )
	|	#(OUTPUT parameter_arg )
	|	#(INPUTOUTPUT parameter_arg )
	|	#(INPUT parameter_arg )
	;
parameter_arg
	:	TABLEHANDLE field (APPEND)?
	|	TABLE (FOR)? RECORD_NAME (APPEND)?
	|	DATASET ID (APPEND)? (BYVALUE|BYREFERENCE)?
	|	DATASETHANDLE ID (APPEND)? (BYVALUE|BYREFERENCE)?
	|	ID AS (datatype_var)
	|	expression
	;

parameterlist
	:	#(Parameter_list parameterlist_noroot )
	;
parameterlist_noroot
	:	LEFTPAREN (parameter)? (COMMA parameter)* RIGHTPAREN
	;

eventlist
	:	#(Event_list . (COMMA .)* )
	;

funargs
	:	LEFTPAREN expression (COMMA expression)* RIGHTPAREN
	;

anyorvalue
	:	#(VALUE LEFTPAREN expression RIGHTPAREN )
	|	TYPELESS_TOKEN
	;
filenameorvalue
	:	#(VALUE LEFTPAREN expression RIGHTPAREN )
	|	FILENAME
	;
valueexpression
	:	#(VALUE LEFTPAREN expression RIGHTPAREN )
	;
expressionorvalue
	:	#(VALUE LEFTPAREN expression RIGHTPAREN )
	|	expression
	;

findwhich
	:	CURRENT | EACH | FIRST | LAST | NEXT | PREV
	;

lockhow
	:	SHARELOCK | EXCLUSIVELOCK | NOLOCK
	;


expression
	:	#(OR expression expression )
	|	#(AND expression expression )
	|	#(NOT expression )
	|	#(MATCHES expression expression )
	|	#(BEGINS expression expression )
	|	#(CONTAINS expression expression )
	|	#(EQ expression expression )
	|	#(NE expression expression )
	|	#(GTHAN expression expression )
	|	#(GE expression expression )
	|	#(LTHAN expression expression )
	|	#(LE expression expression )
	|	#(PLUS expression expression )
	|	#(MINUS expression expression )
	|	#(MULTIPLY expression expression )
	|	#(DIVIDE expression expression )
	|	#(MODULO expression expression )
	|	#(UNARY_MINUS exprt )
	|	#(UNARY_PLUS exprt )
	|	exprt
	;

exprt
	:	constant
	|	#(USER_FUNC parameterlist_noroot )
	|	functioncall
	|	systemhandlename
	|	widattr
	|	field
	|	#(Entered_func field (NOT)? ENTERED )
	|	#(LEFTPAREN expression RIGHTPAREN )
	|	RECORD_NAME // for DISPLAY buffername, etc.
	;

widattr
	:	#(	Widget_ref
			(NORETURNVALUE)?
			s_widget
			(OBJCOLON . (array_subscript)? (method_param_list)? )+
			(#(IN_KW (MENU|FRAME|BROWSE|SUBMENU|BUFFER) ID ))? (AS .)?
		)
	;

gwidget
	:	#(Widget_ref s_widget (#(IN_KW (MENU|FRAME|BROWSE|SUBMENU|BUFFER) ID ))? )
	;

widgetlist
	:	gwidget (COMMA gwidget)*
	;

s_widget
	:	widname	| (FIELD)? field
	;

widname
	:	systemhandlename
	|	DATASET ID
	|	DATASOURCE ID
	|	FRAME ID
	|	MENU ID
	|	SUBMENU ID
	|	MENUITEM ID
	|	BROWSE ID
	|	QUERY ID
	|	TEMPTABLE ID
	|	BUFFER ID
	|	XDOCUMENT ID
	|	XNODEREF ID
	|	SOCKET ID
	;

field
	:	#(Field_ref (INPUT)? (#(FRAME ID) | #(BROWSE ID))? ID (array_subscript)? )
	;

array_subscript
	:	#(Array_subscript LEFTBRACE expression (FOR expression)? RIGHTBRACE )
	;

method_param_list
	:	#(Method_param_list LEFTPAREN (method_parameter)? (COMMA (method_parameter)?)* RIGHTPAREN )
	;
method_parameter
	:	#(	Method_parameter
			(OUTPUT|INPUTOUTPUT)?
			expression (AS datatype_com)?
			(BYPOINTER|BYVARIANTPOINTER)?
		)
	;

constant
	:	TRUE_KW | FALSE_KW | YES | NO | UNKNOWNVALUE | QSTRING | LEXDATE | NUMBER | NULL_KW
	|	NOWAIT | SHARELOCK | EXCLUSIVELOCK | NOLOCK
	|	BIGENDIAN
	|	FINDCASESENSITIVE | FINDGLOBAL | FINDNEXTOCCURRENCE | FINDPREVOCCURRENCE | FINDSELECT | FINDWRAPAROUND
	|	FUNCTIONCALLTYPE | GETATTRCALLTYPE | PROCEDURECALLTYPE | SETATTRCALLTYPE
	|	HOSTBYTEORDER | LITTLEENDIAN
	|	READAVAILABLE | READEXACTNUM
	|	ROWUNMODIFIED | ROWDELETED | ROWMODIFIED | ROWCREATED
	|	SAXCOMPLETE | SAXPARSERERROR | SAXRUNNING | SAXUNINITIALIZED
	|	SEARCHSELF | SEARCHTARGET
	|	WINDOWDELAYEDMINIMIZE | WINDOWMINIMIZED | WINDOWNORMAL | WINDOWMAXIMIZED
	;

systemhandlename
	:	AAMEMORY | ACTIVEWINDOW | CLIPBOARD | CODEBASELOCATOR | COLORTABLE | COMPILER 
	|	COMSELF | CURRENTWINDOW | DEBUGGER | DEFAULTWINDOW
	|	ERRORSTATUS | FILEINFORMATION | FOCUS | FONTTABLE | LASTEVENT | LOGMANAGER
	|	MOUSE | PROFILER | RCODEINFORMATION | SECURITYPOLICY | SELF | SESSION
	|	SOURCEPROCEDURE | TARGETPROCEDURE | TEXTCURSOR | THISPROCEDURE | WEBCONTEXT
	;

//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
//                   begin PROGRESS syntax features, in alphabetical order
//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////



aatracestatement
	:	#(	AATRACE						// statehead, Node attribute state2=...
			(	(stream_name)?
				(	(TO|FROM|THROUGH) io_phrase	//		""
				|	CLOSE				//		"CLOSE"
				)
			|	OFF					//		"OFF"
			|	#(ON (AALIST)? )			//		"ON"
			)
			state_end
		)
	;

accum_what
	:	AVERAGE|COUNT|MAXIMUM|MINIMUM|TOTAL|SUBAVERAGE|SUBCOUNT|SUBMAXIMUM|SUBMINIMUM|SUBTOTAL
	;

accumulatestate
	:	#(ACCUMULATE (display_item)* state_end )
	;

aggregatephrase
	:	#(Aggregate_phrase LEFTPAREN (aggregate_opt)+ ( #(BY expression (DESCENDING)? ) )* RIGHTPAREN )
	;
aggregate_opt
	:	#(AVERAGE (label_constant)? )
	|	#(COUNT (label_constant)? )
	|	#(MAXIMUM (label_constant)? )
	|	#(MINIMUM (label_constant)? )
	|	#(TOTAL (label_constant)? )
	|	#(SUBAVERAGE (label_constant)? )
	|	#(SUBCOUNT (label_constant)? )
	|	#(SUBMAXIMUM (label_constant)? )
	|	#(SUBMINIMUM (label_constant)? )
	|	#(SUBTOTAL (label_constant)? )
	;

analyzestate
	:	#(	ANALYZE filenameorvalue filenameorvalue
			( #(OUTPUT filenameorvalue ) )?
			(APPEND | ALL | NOERROR_KW)* state_end
		)
	;

applystate
	:	#(APPLY expression (#(TO gwidget ))? state_end )
	;

assign_opt
	:	#(ASSIGN ( #(EQUAL . expression ) )+ )
	;

assignstate
	:	#(ASSIGN assignment_list (NOERROR_KW)? state_end )
	;
assignment_list
	:	RECORD_NAME (#(EXCEPT (field)*))?
	|	(	assign_equal (#(WHEN expression))?
		|	#(Assign_from_buffer field ) (#(WHEN expression))?
		)*
	;
assign_equal
	:	#(EQUAL (pseudfn|field) expression )
	;

atphrase
	:	#(	AT
			(	atphraseab atphraseab
			|	expression
			)
			(COLONALIGNED|LEFTALIGNED|RIGHTALIGNED)?
		)
	;
atphraseab
	:	#(COLUMN expression )
	|	#(COLUMNOF referencepoint )
	|	#(ROW expression )
	|	#(ROWOF referencepoint )
	|	#(X expression )
	|	#(XOF referencepoint )
	|	#(Y expression )
	|	#(YOF referencepoint )
	;
referencepoint
	:	field ((PLUS|MINUS) expression)?
	;

bellstate
	:	#(BELL state_end )
	;

buffercomparestate
	:	#(	BUFFERCOMPARE
			RECORD_NAME
			(	#(EXCEPT (field)*)
			|	#(USING (field)+)
			)?
			TO RECORD_NAME
			(CASESENSITIVE|BINARY)?
			( #(SAVE ( #(RESULT IN_KW) )? field ) )?
			(EXPLICIT)?
			(	COMPARES
				(NOERROR_KW)?
				block_colon
				#(Code_block ( #(WHEN expression THEN blockorstate ) )* )
				#(END (COMPARES)? )
			)?
			(NOLOBS)?
			(NOERROR_KW)?
			state_end
		)
	;

buffercopystate
	:	#(	BUFFERCOPY RECORD_NAME
			(	#(EXCEPT (field)*)
			|	#(USING (field)+)
			)?
			TO RECORD_NAME
			( #(ASSIGN assignment_list ) )?
			(NOLOBS)?
			(NOERROR_KW)?
			state_end 
		)
	;

callstate
	:	#(CALL anyorvalue (expressionorvalue)* state_end )
	;

casesens_or_not
	:	#(Not_casesens NOT CASESENSITIVE )
	|	CASESENSITIVE
	;

casestate
	:	#(	CASE expression block_colon
			#(	Code_block
				(	#(WHEN case_expression THEN blockorstate )
				)*
			)
			( #(OTHERWISE blockorstate ) )?
			(EOF | #(END (CASE)? ) state_end)
		)
	;
case_expression
	:	(#(OR .))=> #(OR case_expression case_expression )
	|	#(WHEN expression)
	|	expression
	;

choosestate
	:	#(	CHOOSE (ROW|FIELD)
			( #(Form_item field (#(HELP constant))? ) )+
			(	AUTORETURN 
			|	#(COLOR anyorvalue) 
			|	goonphrase
			|	#(KEYS field )
			|	NOERROR_KW 
			|	#(PAUSE expression)
			)*
			(framephrase)?
			state_end
		)
	;

clearstate
	:	#(CLEAR (#(FRAME ID))? (ALL)? (NOPAUSE)? state_end )
	;

closequerystate
	:	#(CLOSE QUERY ID state_end )
	;

closestoredprocedurestate
	:	#(	CLOSE
			STOREDPROCEDURE ID
			( #(EQUAL field PROCSTATUS ) )?
			( #(WHERE PROCHANDLE EQ field ) )?
			state_end
		)
	;

collatephrase
	:	#(COLLATE funargs (DESCENDING)? )
	;

color_expr
	:	#(BGCOLOR expression )
	|	#(DCOLOR expression )
	|	#(FGCOLOR expression )
	|	#(PFCOLOR expression )
	;

colorspecification
	:	(options{greedy=true;}:color_expr)+
	|	#(	COLOR (DISPLAY)? anyorvalue
			( #(PROMPT anyorvalue) )?
		)
	;

colorstate
	:	#(	COLOR
			(	( #(DISPLAY anyorvalue) | #(PROMPT anyorvalue) )
				( #(DISPLAY anyorvalue) | #(PROMPT anyorvalue) )?
			)?
			(#(Form_item field (formatphrase)? ))*
			(framephrase)? state_end
		)
	;

columnformat
	:	#(	Format_phrase
			(	#(FORMAT expression)
			|	label_constant
			|	NOLABELS
			|	#(COLUMNFONT expression )
			|	#(COLUMNDCOLOR expression )
			|	#(COLUMNBGCOLOR expression )
			|	#(COLUMNFGCOLOR expression )
			|	#(COLUMNPFCOLOR expression )
			|	#(LABELFONT expression )
			|	#(LABELDCOLOR expression )
			|	#(LABELBGCOLOR expression )
			|	#(LABELFGCOLOR expression )
			|	#(LEXAT field (columnformat)? )
			|	#(WIDTH NUMBER )
			|	#(WIDTHPIXELS NUMBER )
			|	#(WIDTHCHARS NUMBER )
			)+ 
		)
	;

comboboxphrase
	:	#(	COMBOBOX
			(	#(LISTITEMS constant (COMMA constant)* )
			|	#(LISTITEMPAIRS constant (COMMA constant)* )
			|	#(INNERLINES expression )
			|	SORT
			|	tooltip_expr
			|	SIMPLE
			|	DROPDOWN
			|	DROPDOWNLIST
			|	#(MAXCHARS NUMBER )
			|	#(AUTOCOMPLETION (UNIQUEMATCH)? )
			|	sizephrase
			)*
		)
	;

compilestate
	:	#(	COMPILE filenameorvalue
			(	#(ATTRSPACE (#(EQUAL expression))? )
			|	NOATTRSPACE
			|	#(SAVE (#(EQUAL expression))? ( #(INTO filenameorvalue ) )? )
			|	#(	LISTING filenameorvalue
					(	compile_append
					|	#(PAGESIZE_KW expression)
					|	#(PAGEWIDTH expression)
					)*
				)
			|	#(XCODE expression )
			|	#(XREF filenameorvalue (compile_append)? )
			|	#(STRINGXREF filenameorvalue (compile_append)? )
			|	#(STREAMIO (#(EQUAL expression))? )
			|	#(MINSIZE (#(EQUAL expression))? )
			|	#(LANGUAGES LEFTPAREN (compile_lang (COMMA compile_lang)*)? RIGHTPAREN )
			|	#(TEXTSEGGROW #(EQUAL expression) )
			|	#(DEBUGLIST filenameorvalue )
			|	#(DEFAULTNOXLATE (#(EQUAL expression))? )
			|	#(GENERATEMD5 (#(EQUAL expression))? )
			|	#(PREPROCESS filenameorvalue )
			|	#(USEREVVIDEO (#(EQUAL expression))? )
			|	#(USEUNDERLINE (#(EQUAL expression))? )
			|	#(V6FRAME (#(EQUAL expression))? )
			|	NOERROR_KW
			)*
			state_end
		)
	;
compile_lang
	:	valueexpression | TYPELESS_TOKEN (LEXCOLON TYPELESS_TOKEN)*
	;
compile_append
	:	#(APPEND (#(EQUAL expression))? )
	;

connectstate
	:	#(CONNECT (NOERROR_KW|DDE|filenameorvalue)* state_end )
	;

convertphrase
	:	#(	CONVERT 
			( #(SOURCE (BASE64 | CODEPAGE expression (BASE64)?) ) )?
			( #(TARGET (BASE64 | CODEPAGE expression (BASE64)?) ) )?
		)
	;
	
copylobstate
	:	#(	COPYLOB (FROM)?
			( FILE expression | (OBJECT)? expression )
			( #(STARTING AT expression) )?
			( #(FOR expression) )?
			TO
			(	FILE expression (APPEND)?
			|	(OBJECT)? expression (OVERLAY AT expression (TRIM)?)?
			)
			( NOCONVERT | convertphrase )?
			( NOERROR_KW )?
		)
	;

createstate
	:	#(CREATE RECORD_NAME (#(USING (ROWID|RECID) expression))? (NOERROR_KW)? state_end )
	;

createaliasstate
	:	#(CREATE ALIAS anyorvalue FOR DATABASE anyorvalue (NOERROR_KW)? state_end )
	;

createautomationobjectstate
	:	#(CREATE QSTRING field (#(CONNECT (#(TO expression))?))? (NOERROR_KW)? state_end )
	;

createbrowsestate
	:	#(CREATE BROWSE field (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? (assign_opt)? (triggerphrase)? state_end )
	;

createbufferstate
	:	#(	CREATE BUFFER field FOR TABLE expression
			( #(BUFFERNAME expression) )?
			(#(IN_KW WIDGETPOOL expression))?
			(NOERROR_KW)? state_end
		)
	;

createcallstate
	:	#(CREATE CALL field (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
	;

createdatabasestate
	:	#(CREATE DATABASE expression (#(FROM expression ))? (REPLACE)? (NOERROR_KW)? state_end )
	;

createdatasetstate
	:	#(CREATE DATASET field (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
	;

createdatasourcestate
	:	#(CREATE DATASOURCE field (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
	;

createquerystate
	:	#(CREATE QUERY field (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
	;

createsaxreaderstate
	:	#(CREATE SAXREADER field (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
	;

createserverstate
	:	#(CREATE SERVER field (assign_opt)? state_end )
	;

createserversocketstate
	:	#(CREATE SERVERSOCKET field (NOERROR_KW)? state_end )
	;

createsoapheaderstate
	:	#(CREATE SOAPHEADER field (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
	;

createsoapheaderentryrefstate
	:	#(CREATE SOAPHEADERENTRYREF field (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
	;

createsocketstate
	:	#(CREATE SOCKET field (NOERROR_KW)? state_end )
	;

createtemptablestate
	:	#(CREATE TEMPTABLE field (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
	;

createwidgetstate
	:	#(	CREATE
			(	valueexpression
			|	BUTTON | COMBOBOX | CONTROLFRAME | DIALOGBOX | EDITOR | FILLIN | FRAME | IMAGE
			|	MENU | MENUITEM | RADIOSET | RECTANGLE | SELECTIONLIST | SLIDER
			|	SUBMENU | TEXT | TOGGLEBOX | WINDOW
			)
			field
			(#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? (assign_opt)? (triggerphrase)? state_end
		)
	;

createwidgetpoolstate
	:	#(CREATE WIDGETPOOL (expression)? (PERSISTENT)? (NOERROR_KW)? state_end )
	;

createxdocumentstate
	:	#(CREATE XDOCUMENT field (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
	;

createxnoderefstate
	:	#(CREATE XNODEREF field (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
	;

currentvaluefunc
	:	#(CURRENTVALUE LEFTPAREN ID (COMMA ID)? RIGHTPAREN )
	;

datatype_com
	:	SHORT | FLOAT | CURRENCY | UNSIGNEDBYTE | ERRORCODE | IUNKNOWN
	;

datatype_dll
	:	CHARACTER | datatype_dll_native  
	;

datatype_dll_native
	:	BYTE | DOUBLE | FLOAT | LONG | SHORT | UNSIGNEDSHORT
	;

datatype_field
	:	datatype_var | BLOB | CLOB
	;

datatype_param
	:	datatype_var | datatype_dll_native
	;

datatype_var
	:	CHARACTER | COMHANDLE | DATE | DATETIME | DATETIMETZ
		| DECIMAL | HANDLE | INTEGER | LOGICAL | LONGCHAR | MEMPTR
		| RAW | RECID | ROWID | WIDGETHANDLE
	;

ddeadvisestate
	:	#(DDE ADVISE expression (START|STOP) ITEM expression (#(TIME expression))? (NOERROR_KW)? state_end )
	;

ddeexecutestate
	:	#(DDE EXECUTE expression COMMAND expression (#(TIME expression))? (NOERROR_KW)? state_end )
	;

ddegetstate
	:	#(DDE GET expression TARGET field ITEM expression (#(TIME expression))? (NOERROR_KW)? state_end )
	;

ddeinitiatestate
	:	#(DDE INITIATE field FRAME expression APPLICATION expression TOPIC expression (NOERROR_KW)? state_end )
	;

dderequeststate
	:	#(DDE REQUEST expression TARGET field ITEM expression (#(TIME expression))? (NOERROR_KW)? state_end )
	;

ddesendstate
	:	#(DDE SEND expression SOURCE expression ITEM expression (#(TIME expression))? (NOERROR_KW)? state_end )
	;

ddeterminatestate
	:	#(DDE TERMINATE expression (NOERROR_KW)? state_end )
	;

definebrowsestate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? BROWSE ID
			(#(QUERY ID))? (lockhow|NOWAIT)*
			(	#(	DISPLAY
					(	#(	Form_item
							(	(RECORD_NAME)=> RECORD_NAME
							|	expression (columnformat)?
							|	spacephrase
							)
						)
					)*
					(#(EXCEPT (field)*))?
				)
				(	#(	ENABLE
						(	#(ALL (#(EXCEPT (field)*))?)
						|	(	#(	Form_item field
									(	#(HELP constant)
									|	#(VALIDATE funargs)
									|	AUTORETURN
									|	DISABLEAUTOZAP
									)*
								)
							)*
						)
					)
				)?
			)?
			(display_with)*
			(tooltip_expr)?
			(#(CONTEXTHELPID expression))?
			state_end
		)
	;

definebufferstate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? BUFFER ID
			FOR RECORD_NAME (PRESELECT)? (label_constant)? (#(FIELDS (field)* ))? state_end
		)
	;

definebuttonstate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? BUTTON ID
			(	AUTOGO
			|	AUTOENDKEY
			|	DEFAULT
			|	color_expr
			|	#(CONTEXTHELPID expression)
			|	DROPTARGET
			|	#(FONT expression)
			|	#(IMAGEDOWN (imagephrase_opt)+ )
			|	#(IMAGE (imagephrase_opt)+ )
			|	#(IMAGEUP (imagephrase_opt)+ )
			|	#(IMAGEINSENSITIVE (imagephrase_opt)+ )
			|	#(MOUSEPOINTER expression )
			|	label_constant
			|	#(LIKE field (VALIDATE)?)
			|	FLATBUTTON
			|	#(NOFOCUS (FLATBUTTON)? )
			|	NOCONVERT3DCOLORS
			|	tooltip_expr
			|	sizephrase (MARGINEXTRA)?
			)*
			(triggerphrase)?
			state_end
		)
	;

definedatasetstate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? DATASET ID
			FOR RECORD_NAME (COMMA RECORD_NAME)*
			( data_relation ( (COMMA)? data_relation)* )?
			state_end
		)
	;
data_relation
	:	#(	DATARELATION (ID)?
			FOR RECORD_NAME COMMA RECORD_NAME (field_mapping_phrase)? (REPOSITION)?
		)
	;
field_mapping_phrase
	:	#(RELATIONFIELDS LEFTPAREN field COMMA field ( COMMA field COMMA field )* RIGHTPAREN )
	;

definedatasourcestate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? DATASOURCE ID
			FOR (#(QUERY ID))?
			(source_buffer_phrase)? (COMMA source_buffer_phrase)*
		)
	;
source_buffer_phrase
	:	#(RECORD_NAME ( KEYS LEFTPAREN ( ROWID | field (COMMA field)* ) RIGHTPAREN )? )
	;

defineframestate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? FRAME ID
			(form_item)*
			(	#(HEADER (display_item)+ )
			|	#(BACKGROUND (display_item)+ )
			)?
			(#(EXCEPT (field)*))?  (framephrase)?  state_end
		)
	;

defineimagestate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? IMAGE ID
			(	#(LIKE field (VALIDATE)?)
			|	imagephrase_opt 
			|	sizephrase
			|	color_expr
			|	CONVERT3DCOLORS
			|	tooltip_expr
			|	#(STRETCHTOFIT (RETAINSHAPE)? )
			|	TRANSPARENT
			)*
			(triggerphrase)?
			state_end
		)
	;

definemenustate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? MENU ID
			(menu_opt)* (menu_list_item)* state_end
		)
	;
menu_opt
	:	color_expr
	|	#(FONT expression)
	|	#(LIKE field (VALIDATE)?)
	|	#(TITLE expression)
	|	MENUBAR
	|	PINNABLE
	|	SUBMENUHELP
	;
menu_list_item
	:	(	#(	MENUITEM ID
				(	#(ACCELERATOR expression )
				|	color_expr
				|	DISABLED
				|	#(FONT expression)
				|	label_constant
				|	READONLY
				|	TOGGLEBOX
				)*
				(triggerphrase)? 
			)
		|	#(SUBMENU ID (DISABLED | label_constant | #(FONT expression) | color_expr)* )
		|	#(RULE (#(FONT expression) | color_expr)* )
		|	SKIP
		)
		// You can have PERIOD between menu items.
		((PERIOD (RULE|SKIP|SUBMENU|MENUITEM))=> PERIOD)?
	;

defineparameterstate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)?
			(	PARAMETER BUFFER ID FOR RECORD_NAME (PRESELECT)? (label_constant)? (#(FIELDS (field)* ))?
			|	(INPUT|OUTPUT|INPUTOUTPUT|RETURN) PARAMETER
				(	TABLE FOR RECORD_NAME (APPEND)?
				|	TABLEHANDLE (FOR)? ID (APPEND)?
				|	DATASET FOR ID (APPEND|BYVALUE)*
				|	DATASETHANDLE ID (BYVALUE)?
				|	ID defineparam_var (triggerphrase)?
				)
			)
			state_end
		)
	;
defineparam_var
	:	(	#(	AS
				(	(HANDLE (TO)? datatype_dll)=> HANDLE (TO)? datatype_dll
				|	datatype_param
				)
			)
		)?
		(	options{greedy=true;}
		:	casesens_or_not | #(FORMAT expression) | #(DECIMALS expression ) | #(LIKE field (VALIDATE)?)
		|	initial_constant | label_constant | NOUNDO | extentphrase
		)*
	;

definequerystate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? QUERY ID
			FOR RECORD_NAME (record_fields)?
			(COMMA RECORD_NAME (record_fields)?)*
			( #(CACHE expression) | SCROLLING | RCODEINFORMATION)*
			state_end
		)
	;

definerectanglestate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? RECTANGLE ID
			(	NOFILL
			|	#(EDGECHARS expression )
			|	#(EDGEPIXELS expression )
			|	color_expr
			|	GRAPHICEDGE
			|	#(LIKE field (VALIDATE)?)
			|	sizephrase
			|	tooltip_expr
			)*
			(triggerphrase)?
			state_end
		)
	;

definestreamstate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? STREAM ID state_end
		)
	;

definesubmenustate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? SUBMENU ID
			(menu_opt)* (menu_list_item)* state_end
		)
	;
   
definetemptablestate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? TEMPTABLE ID
			(UNDO|NOUNDO)?
			(def_table_like)?
			(label_constant)?
			(#(BEFORETABLE ID))?
			(RCODEINFORMATION)?
			(def_table_field)*
			(	#(	INDEX ID ( (AS|IS)? (UNIQUE|PRIMARY|WORDINDEX) )*
					( ID (ASCENDING|DESCENDING|CASESENSITIVE)* )+
				)
			)*
			state_end
		)
	;
def_table_like
	:	#(	LIKE RECORD_NAME (VALIDATE)?
			( #(USEINDEX ID ((AS|IS) PRIMARY)? ) )*
		)
	;
def_table_field
	:	#(FIELD ID (fieldoption)* )
	;
   
defineworktablestate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? WORKTABLE ID
			(NOUNDO)? (def_table_like)? (label_constant)? (def_table_field)* state_end
		)
	;

definevariablestate
	:	#(	DEFINE (#(NEW (GLOBAL)? SHARED ) | SHARED)? VARIABLE ID
			(fieldoption)* (triggerphrase)? state_end
		)
	;

deletestate
	:	#(DELETE_KW RECORD_NAME (#(VALIDATE funargs))? (NOERROR_KW)? state_end )
	;

deletealiasstate
	:	#(DELETE_KW ALIAS (ID|QSTRING|valueexpression) state_end )
	;

deleteobjectstate
	:	#(DELETE_KW OBJECT expression (NOERROR_KW)? state_end )
	;

deleteprocedurestate
	:	#(DELETE_KW PROCEDURE expression (NOERROR_KW)? state_end )
	;

deletewidgetstate
	:	#(DELETE_KW WIDGET (gwidget)* state_end )
	;

deletewidgetpoolstate
	:	#(DELETE_KW WIDGETPOOL (expression)? (NOERROR_KW)? state_end )
	;

dictionarystate
	:	#(DICTIONARY state_end )
	;

disablestate
	:	#(DISABLE (UNLESSHIDDEN)? (#(ALL (#(EXCEPT (field)*))?) | (form_item)+)? (framephrase)? state_end )
	;

disabletriggersstate
	:	#(DISABLE TRIGGERS FOR (DUMP|LOAD) OF RECORD_NAME (ALLOWREPLICATION)? state_end )
	;

disconnectstate
	:	#(DISCONNECT filenameorvalue (NOERROR_KW)? state_end )
	;

displaystate
	:	#(	DISPLAY (stream_name)? (UNLESSHIDDEN)? (display_item)*
			(#(EXCEPT (field)*))? (#(IN_KW WINDOW expression))?
			(display_with)*
			(NOERROR_KW)?
			state_end
		)
	;
display_item
	:	#(	Form_item
			(	skipphrase
			|	spacephrase
			|	(expression|ID) (aggregatephrase|formatphrase)*
			)
		)
	;
display_with
	:	(#(WITH BROWSE ID))=> #(WITH BROWSE ID )
	|	framephrase
	;

dostate
	:	#(DO (block_for)? (block_preselect)? (block_opt)* block_colon code_block block_end )
	;

downstate
	:	#(DOWN ((stream_name (expression)?) | (expression (stream_name)?))? (framephrase)? state_end )
	;

// drop - see SQL grammar

dynamiccurrentvaluefunc
	:	#(DYNAMICCURRENTVALUE funargs)
	;

editorphrase
	:	#(	EDITOR
			(	#(INNERCHARS expression )
			|	#(INNERLINES expression )
			|	#(BUFFERCHARS expression )
			|	#(BUFFERLINES expression )
			|	LARGE
			|	#(MAXCHARS expression )
			|	NOBOX
			|	NOWORDWRAP
			|	SCROLLBARHORIZONTAL
			|	SCROLLBARVERTICAL
			|	tooltip_expr
			|	sizephrase
			)*
		)
	;

emptytemptablestate
	:	#(EMPTY TEMPTABLE RECORD_NAME (NOERROR_KW)? state_end )
	;

enablestate
	:	#(ENABLE (UNLESSHIDDEN)? (#(ALL (#(EXCEPT (field)*))?) | (form_item)+)? (#(IN_KW WINDOW expression))? (framephrase)? state_end )
	;

editingphrase
	:	#(Editing_phrase (ID LEXCOLON)? EDITING block_colon (blockorstate)* END )
	;

entryfunc
	:	#(ENTRY funargs )
	;

exportstate
	:	#(EXPORT (stream_name)? (#(DELIMITER constant))? (display_item)* (#(EXCEPT (field)*))? (NOLOBS)? state_end )
	;

extentphrase
	:	#(EXTENT (expression)? )
	;

fieldoption
	:	#(AS datatype_field )
	|	casesens_or_not
	|	color_expr
	|	#(COLUMNCODEPAGE expression )
	|	#(CONTEXTHELPID expression)
	|	#(DECIMALS expression )
	|	DROPTARGET
	|	extentphrase
	|	#(FONT expression)
	|	#(FORMAT expression)
	|	#(HELP constant)
	|	initial_constant
	|	label_constant
	|	#(LIKE field (VALIDATE)? )
	|	#(MOUSEPOINTER expression )
	|	NOUNDO
	|	viewasphrase
	|	TTCODEPAGE
	;

fillinphrase
	:	#(FILLIN (NATIVE | sizephrase | tooltip_expr)* )
	;

findstate
	:	#(FIND (findwhich)? recordphrase (NOWAIT|NOPREFETCH|NOERROR_KW)* state_end )
	;

forstate
	:	#(FOR for_record_spec (block_opt)* block_colon code_block block_end )
	;
for_record_spec
	:	(findwhich)? recordphrase (COMMA (findwhich)? recordphrase)*
	;

form_item
	:	#(	Form_item
			(	RECORD_NAME
			|	#(TEXT LEFTPAREN (form_item)* RIGHTPAREN )
			|	constant (formatphrase)?
			|	spacephrase
			|	skipphrase
			|	CARET
			|	field (aggregatephrase|formatphrase)*
			|	assign_equal
			)
		)
	;

formstate
	:	#(	FORMAT
			(form_item)*
			(	#(HEADER (display_item)+ )
			|	#(BACKGROUND (display_item)+ )
			)?
			( #(EXCEPT (field)*) )?
			(framephrase)?
			state_end
		)
	;

formatphrase
	:	#(	Format_phrase
			(	#(AS datatype_var )
			|	atphrase
			|	ATTRSPACE
			|	NOATTRSPACE
			|	AUTORETURN
			|	color_expr
			|	#(CONTEXTHELPID expression)
			|	BLANK 
			|	#(COLON expression )
			|	#(TO expression)
			|	DEBLANK 
			|	DISABLEAUTOZAP 
			|	#(FONT expression ) 
			|	#(FORMAT expression)
			|	#(HELP constant)
			|	label_constant
			|	#(LEXAT field (formatphrase)? )
			|	#(LIKE field )
			|	NOLABELS
			|	NOTABSTOP 
			|	#(VALIDATE funargs)
			|	#(WHEN expression)
			|	viewasphrase 
			)+
		)
	;

framephrase
	:	#(	WITH
			(	#(ACCUM (expression)? )
			|	ATTRSPACE | NOATTRSPACE
			|	#(CANCELBUTTON field )
			|	CENTERED 
			|	#(COLUMN expression )
			|	CONTEXTHELP | CONTEXTHELPFILE expression
			|	#(DEFAULTBUTTON field )
			|	EXPORT
			|	FITLASTCOLUMN
			|	#(FONT expression )
			|	FONTBASEDLAYOUT
			|	#(FRAME ID)
			|	#(LABELFONT expression )
			|	#(LABELDCOLOR expression )
			|	#(LABELFGCOLOR expression )
			|	#(LABELBGCOLOR expression )
			|	MULTIPLE | SINGLE | SEPARATORS | NOSEPARATORS | NOASSIGN| NOROWMARKERS
			|	NOSCROLLBARVERTICAL | SCROLLBARVERTICAL
			|	#(ROWHEIGHTCHARS expression )
			|	#(ROWHEIGHTPIXELS expression )
			|	EXPANDABLE | DROPTARGET | NOAUTOVALIDATE | NOCOLUMNSCROLLING
			|	KEEPTABORDER | NOBOX | NOEMPTYSPACE | NOHIDE | NOLABELS | USEDICTEXPS | NOVALIDATE 
			|	NOHELP | NOUNDERLINE | OVERLAY | PAGEBOTTOM | PAGETOP | NOTABSTOP
			|	#(RETAIN expression  )
			|	#(ROW expression )
			|	SCREENIO | STREAMIO
			|	#(SCROLL expression )
			|	SCROLLABLE | SIDELABELS 
			|	stream_name | THREED
			|	tooltip_expr
			|	TOPONLY | USETEXT
			|	V6FRAME | USEREVVIDEO | USEUNDERLINE
			|	#(	VIEWAS
					(	#(DIALOGBOX (DIALOGHELP (expression)?)? )
					|	MESSAGELINE
					|	STATUSBAR
					|	#(TOOLBAR (ATTACHMENT (TOP|BOTTOM|LEFT|RIGHT))? )
					)
				)
			|	#(WIDTH expression )
			|	#(IN_KW WINDOW expression)
			|	colorspecification | atphrase | sizephrase | titlephrase 
			|	#(With_columns expression COLUMNS )
			|	#(With_down expression DOWN )
			|	DOWN
			|	WITH
			)*
		)
	;

functionstate
	:	#(	FUNCTION ID
			(RETURNS|RETURN)? datatype_var (PRIVATE)?
			( #(Parameter_list LEFTPAREN (function_param)? (COMMA function_param)* RIGHTPAREN ) )?
			(	FORWARDS (LEXCOLON|PERIOD|EOF)
			|	(IN_KW SUPER)=> IN_KW SUPER (LEXCOLON|PERIOD|EOF)
			|	(MAP (TO)? ID)? IN_KW expression (LEXCOLON|PERIOD|EOF)
			|	block_colon
				code_block
				(	EOF
				|	#(END (FUNCTION)? ) state_end
				)
			)
		)
	;
function_param
	:	#(BUFFER (ID)? FOR RECORD_NAME (PRESELECT)? )
	|	#(INPUT function_param_arg )
	|	#(OUTPUT function_param_arg )
	|	#(INPUTOUTPUT function_param_arg )
	;
function_param_arg
	:	TABLE (FOR)? RECORD_NAME (APPEND)?
	|	TABLEHANDLE (FOR)? ID (APPEND)?
	|	(ID AS)? datatype_var (extentphrase)?
	;

getstate
	:	#(GET findwhich ID (lockhow|NOWAIT)* state_end )
	;

getkeyvaluestate
	:	#(GETKEYVALUE SECTION expression KEY (DEFAULT|expression) VALUE field state_end )
	;

goonphrase
	:	#(GOON LEFTPAREN goon_elem ((options{greedy=true;}:COMMA)? goon_elem)* RIGHTPAREN )
	;
goon_elem
	:	~(RIGHTPAREN) ( (OF)=> OF gwidget)?
	;

hidestate
	:	#(HIDE (stream_name)? (MESSAGE|ALL|(gwidget)*) (NOPAUSE)? (#(IN_KW WINDOW expression))? state_end )
	;

ifstate
	:	#(	IF expression THEN (blockorstate)?
			( #(ELSE (blockorstate)? ) )?
		)
	;

imagephrase_opt
	:	#(FILE expression )
	|	#(IMAGESIZE expression BY expression )
	|	#(IMAGESIZECHARS expression BY expression )
	|	#(IMAGESIZEPIXELS expression BY expression )
	|	#(	FROM
			( X expression | Y expression | ROW expression | COLUMN expression )
			( X expression | Y expression | ROW expression | COLUMN expression )
		)
	;

importstate
	:	#(	IMPORT (stream_name)?
			( #(DELIMITER constant) | UNFORMATTED )?
			(	RECORD_NAME (#(EXCEPT (field)*))?
			|	(field|CARET)+
			)?
			(NOLOBS)? (NOERROR_KW)? state_end
		)
	;

initial_constant
	:	#(	INITIAL
			(	LEFTBRACE (TODAY|constant) (COMMA (TODAY|constant))* RIGHTBRACE
			|	(TODAY|constant)
			)
		)
	;

inputclearstate
	:	#(INPUT CLEAR state_end )
	;

inputclosestate
	:	#(INPUT (stream_name)? CLOSE state_end )
	;

inputfromstate
	:	#(INPUT (stream_name)? FROM io_phrase state_end )
	;
   
inputthroughstate
	:	#(INPUT (stream_name)? THROUGH io_phrase state_end )
	;

inputoutputclosestate
	:	#(INPUTOUTPUT (stream_name)? CLOSE state_end )
	;

inputoutputthroughstate
	:	#(INPUTOUTPUT (stream_name)? THROUGH io_phrase state_end )
	;

insertstate
	:	#(INSERT RECORD_NAME (#(EXCEPT (field)*))? (#(USING (ROWID|RECID) expression))? (framephrase)? (NOERROR_KW)? state_end )
	;

io_phrase
	:	(	#(OSDIR LEFTPAREN expression RIGHTPAREN (NOATTRLIST)? )
		|	#(PRINTER  (.)? )
		|	TERMINAL
		|	(valueexpression | FILENAME) *
		)
		(	APPEND
		|	BINARY
		|	COLLATE
		|	#(CONVERT ((SOURCE|TARGET) expression)* )
		|	#(LOBDIR filenameorvalue )
		|	NOCONVERT
		|	ECHO | NOECHO
		|	KEEPMESSAGES 
		|	LANDSCAPE
		|	#(MAP anyorvalue )
		|	NOMAP
		|	#(NUMCOPIES anyorvalue )
		|	PAGED
		|	#(PAGESIZE_KW anyorvalue )
		|	PORTRAIT
		|	UNBUFFERED 
		)*
	;

label_constant
	:	#(COLUMNLABEL constant (COMMA constant)* )
	|	#(LABEL constant (COMMA constant)* )
	;

ldbnamefunc
	:	#(LDBNAME LEFTPAREN (#(BUFFER RECORD_NAME) | expression) RIGHTPAREN )
	;

leavestate
	:	#(LEAVE (BLOCK_LABEL)? state_end )
	;

lengthfunc
	:	#(LENGTH funargs )
	;

loadstate
	:	#(	LOAD expression
			(	#(DIR expression )
			|	APPLICATION
			|	DYNAMIC
			|	NEW
			|	#(BASEKEY expression )
			|	NOERROR_KW
			)*
			state_end
		)
	;

loadpicturefunc 
	:	#(LOADPICTURE (funargs)? )
	;

messagestate
	:	#(	MESSAGE
			( #(COLOR anyorvalue) )?
			( #(Form_item (skipphrase | expression) ) )*
			(	#(	VIEWAS ALERTBOX
					(MESSAGE|QUESTION|INFORMATION|ERROR|WARNING)?
					(BUTTONS (YESNO|YESNOCANCEL|OK|OKCANCEL|RETRYCANCEL) )?
					(#(TITLE expression))?
				)
			|	#(SET field (formatphrase)? )
			|	#(UPDATE field (formatphrase)? )
			)*
			( #(IN_KW WINDOW expression) )?
			state_end
		)
	;

nextstate
	:	#(NEXT (BLOCK_LABEL)? state_end )
	;

nextpromptstate
	:	#(NEXTPROMPT field (framephrase)? state_end )
	;

nextvaluefunc
	:	#(NEXTVALUE LEFTPAREN ID (COMMA ID)* RIGHTPAREN )
	;

onstate
	:	#(	ON
			(	(ASSIGN|CREATE|DELETE_KW|FIND|WRITE)=>
				(	(CREATE|DELETE_KW|FIND) OF RECORD_NAME (label_constant)?
				|	WRITE OF RECORD_NAME (label_constant)?
					((NEW (BUFFER)? ID) (label_constant)?)?
					((OLD (BUFFER)? ID) (label_constant)?)? 
				|	ASSIGN OF field
					(#(TABLE LABEL constant))?
					(OLD (VALUE)? ID (options{greedy=true;}:defineparam_var)?)?
		 		)
				(OVERRIDE)?
				(	REVERT state_end
				|	PERSISTENT runstate
				|	blockorstate
				)
			|	// ON keylabel keyfunction.
				(. . state_end)=> . . state_end
			|	eventlist
				(	ANYWHERE
				|	OF widgetlist
					(OR eventlist OF widgetlist)*
					(ANYWHERE)?
				)
				(	REVERT state_end
				|	PERSISTENT RUN filenameorvalue
					( #(IN_KW expression) )?
					(	#(	Parameter_list
							LEFTPAREN (INPUT)? expression
							(COMMA (INPUT)? expression)*
							RIGHTPAREN
						)
					)?
					state_end
				|	blockorstate
				)
			)
		)
	;

on___phrase
	:	#(	ON (ENDKEY|ERROR|STOP|QUIT)
			( #(UNDO (BLOCK_LABEL)? ) )?
			(	COMMA
				(	#(LEAVE (BLOCK_LABEL)? )
				|	#(NEXT (BLOCK_LABEL)? )
				|	#(RETRY (BLOCK_LABEL)? )
				|	#(RETURN (return_options)? )
				)
			)?
		)
	;

openquerystate
	:	#(	OPEN QUERY ID (FOR|PRESELECT) for_record_spec
			(	querytuningphrase
			|	#(BY expression (DESCENDING)? )
			|	collatephrase
			|	INDEXEDREPOSITION
			|	#(MAXROWS expression )
			)*
			state_end
		)
	;

osappendstate
	:	#(OSAPPEND anyorvalue anyorvalue state_end )
	;

oscommandstate
	:	#(OS400		(SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
	|	#(BTOS		(SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
	|	#(DOS		(SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
	|	#(MPE		(SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
	|	#(OS2		(SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
	|	#(OSCOMMAND	(SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
	|	#(UNIX		(SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
	|	#(VMS		(SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
	;

oscopystate
	:	#(OSCOPY anyorvalue anyorvalue state_end )
	;

oscreatedirstate
	:	#(OSCREATEDIR (anyorvalue)+ state_end )
	;

osdeletestate
	:	#(OSDELETE (valueexpression | ~(VALUE|RECURSIVE|PERIOD) )+ (RECURSIVE)? state_end )
	;

osrenamestate
	:	#(OSRENAME anyorvalue anyorvalue state_end )
	;

outputclosestate
	:	#(OUTPUT (stream_name)? CLOSE state_end )
	;

outputthroughstate
	:	#(OUTPUT (stream_name)? THROUGH io_phrase state_end )
	;

outputtostate
	:	#(OUTPUT (stream_name)? TO io_phrase state_end )
	;

pagestate
	:	#(PAGE (stream_name)? state_end )
	;

pausestate
	:	#(	PAUSE (expression)?
			(	BEFOREHIDE
			|	#(MESSAGE constant )
			|	NOMESSAGE
			|	#(IN_KW WINDOW expression)
			)*
			state_end
		)
	;

procedurestate
	:	#(	PROCEDURE ID
			(	#(	EXTERNAL constant
					(	CDECL_KW
					|	PASCAL_KW
					|	STDCALL_KW
					|	#(ORDINAL expression )
					|	PERSISTENT
					)*
				)
			|	PRIVATE
			|	IN_KW SUPER
			)?
			block_colon code_block (EOF | #(END (PROCEDURE)?) state_end)
		)
	;

processeventsstate
	:	#(PROCESS EVENTS state_end )
	;

promptforstate
	:	#(	PROMPTFOR (stream_name)? (UNLESSHIDDEN)? (form_item)*
			(goonphrase)?  (#(EXCEPT (field)*))?  (#(IN_KW WINDOW expression))?  (framephrase)?  (editingphrase)?
			state_end
		)
	;

publishstate
	:	#(PUBLISH expression (#(FROM expression) )? (parameterlist)? state_end )
	;

putstate
	:	#(	PUT	
			(stream_name)? (CONTROL|UNFORMATTED)?
			(	( #(NULL_KW (LEFTPAREN)? ) )=> #(NULL_KW (funargs)? )
			|	skipphrase
			|	spacephrase
			|	expression (#(FORMAT expression)|#(AT expression )|#(TO expression))*
			)*
			state_end
		)
	;

putcursorstate
	:	#(PUT CURSOR (OFF | (#(ROW expression)|#(COLUMN expression))* ) state_end )
	;

putscreenstate
	:	#(	PUT SCREEN
			( ATTRSPACE | NOATTRSPACE | #(COLOR anyorvalue) | #(COLUMN expression) | #(ROW expression) | expression )*
			state_end
		)
	;

putkeyvaluestate
	:	#(	PUTKEYVALUE
			(	SECTION expression KEY (DEFAULT|expression) VALUE expression
			|	(COLOR|FONT) (expression|ALL)
			)
			(NOERROR_KW)? state_end
		)
	;

querytuningphrase
	:	#(	QUERYTUNING LEFTPAREN
			(	ARRAYMESSAGE | NOARRAYMESSAGE
			|	BINDWHERE | NOBINDWHERE
			|	#(CACHESIZE NUMBER (ROW|BYTE)? )
			|	#(DEBUG (SQL|EXTENDED|CURSOR|DATABIND|PERFORMANCE|VERBOSE|SUMMARY|NUMBER)? )
			|	NODEBUG
			|	DEFERLOBFETCH
			|	#(HINT expression )
			|	INDEXHINT | NOINDEXHINT
			|	JOINBYSQLDB | NOJOINBYSQLDB
			|	LOOKAHEAD | NOLOOKAHEAD
			|	ORDEREDJOIN
			|	REVERSEFROM
			|	SEPARATECONNECTION | NOSEPARATECONNECTION
			)*
			RIGHTPAREN
		)
	;

quitstate
	:	#(QUIT state_end )
	;

radiosetphrase
	:	#(	RADIOSET
			(	#(HORIZONTAL (EXPAND)? )
			|	VERTICAL
			|	(sizephrase)
			|	#(RADIOBUTTONS 
					(QSTRING|UNQUOTEDSTRING) COMMA (constant|TODAY)
					(COMMA (QSTRING|UNQUOTEDSTRING) COMMA (constant|TODAY))*
				)
			|	tooltip_expr
			)*
		)
	;

rawfunc
	:	#(RAW funargs )
	;

rawtransferstate
	:	#(RAWTRANSFER (BUFFER|FIELD)? (RECORD_NAME|field) TO (BUFFER|FIELD)? (RECORD_NAME|field) (NOERROR_KW)? state_end )
	;

readkeystate
	:	#(READKEY (stream_name)? (#(PAUSE expression))? state_end )
	;

repeatstate
	:	#(REPEAT (block_for)? (block_preselect)? (block_opt)* block_colon code_block block_end )
	;

record_fields
	:	#(FIELDS (LEFTPAREN (field (#(WHEN expression))?)* RIGHTPAREN)? )
	|	#(EXCEPT (LEFTPAREN (field (#(WHEN expression))?)* RIGHTPAREN)? )
	;

recordphrase
	:	#(	RECORD_NAME (record_fields)? (options{greedy=true;}:TODAY|constant)?
			(	#(LEFT OUTERJOIN )
			|	OUTERJOIN
			|	#(OF RECORD_NAME )
			|	#(WHERE (expression)? )
			|	#(USEINDEX ID )
			|	#(USING field (AND field)* )
			|	lockhow
			|	NOWAIT
			|	NOPREFETCH
			|	NOERROR_KW
			)*
		)
	;

releasestate
	:	#(RELEASE RECORD_NAME (NOERROR_KW)? state_end )
	;

releaseexternalstate
	:	#(RELEASE EXTERNAL (PROCEDURE)? expression (NOERROR_KW)? state_end )
	;

releaseobjectstate
	:	#(RELEASE OBJECT expression (NOERROR_KW)? state_end )
	;

repositionstate
	:	#(	REPOSITION ID
			(	#(	TO
					(	ROWID expression (COMMA expression)* 
					|	RECID expression
					|	ROW expression
					)
				)
			|	#(ROW expression )
			|	#(FORWARDS expression )
			|	#(BACKWARDS expression )
			)
			(NOERROR_KW)? state_end
		)
	;

returnstate
	:	#(RETURN (return_options)? state_end )
	;

return_options
	:	(	( #(ERROR LEFTPAREN RECORD_NAME RIGHTPAREN) )=> expression
		|	(ERROR)=> ERROR (expression)?
		|	NOAPPLY (expression)?
		|	expression
		)
	;

runstate
	:	#(	RUN filenameorvalue
			(LEFTANGLE LEFTANGLE filenameorvalue RIGHTANGLE RIGHTANGLE)?
			(	#(PERSISTENT ( #(SET (field)? ) )? )
			|	#(SET (field)? )
			|	#(ON (SERVER)? expression (TRANSACTION (DISTINCT)?)? )
			|	#(IN_KW expression)
			|	#(	ASYNCHRONOUS ( #(SET (field)? ) )?
					( #(EVENTPROCEDURE expression ) )?
					(#(IN_KW expression))?
				)
			)*
			(parameterlist)?
			(NOERROR_KW|anyorvalue)*
			state_end
		)
	;

runstoredprocedurestate
	:	#(RUN STOREDPROCEDURE ID (assign_equal)? (NOERROR_KW)? (parameterlist)? state_end )
	;

runsuperstate
	:	#(RUN SUPER (parameterlist)? (NOERROR_KW)? state_end )
	;

savecachestate
	:	#(SAVE CACHE (CURRENT|COMPLETE) anyorvalue TO filenameorvalue (NOERROR_KW)? state_end )
	;

scrollstate
	:	#(SCROLL (FROMCURRENT)? (UP)? (DOWN)? (framephrase)? state_end )
	;

seekstate
	:	#(SEEK (INPUT|OUTPUT|stream_name) TO (expression|END) state_end )
	;

selectionlistphrase
	:	#(	SELECTIONLIST
			(	SINGLE
			|	MULTIPLE
			|	NODRAG
			|	#(LISTITEMS constant (COMMA constant)* )
			|	#(LISTITEMPAIRS constant (COMMA constant)* )
			|	SCROLLBARHORIZONTAL
			|	SCROLLBARVERTICAL
			|	#(INNERCHARS expression )
			|	#(INNERLINES expression )
			|	SORT
			|	tooltip_expr
			|	sizephrase
			)*
		)
	;

setstate
	:	#(	SET
			(stream_name)? (UNLESSHIDDEN)? (form_item)*
			(goonphrase)?  (#(EXCEPT (field)*))?  (#(IN_KW WINDOW expression))?  (framephrase)?  (editingphrase)?  (NOERROR_KW)?
			state_end
		)
	;

showstatsstate
	:	#(SHOWSTATS (CLEAR)? state_end )
	;

sizephrase
	:	#(SIZE expression BY expression )
	|	#(SIZECHARS expression BY expression )
	|	#(SIZEPIXELS expression BY expression )
	;

skipphrase
	:	#(SKIP (funargs)? )
	;

sliderphrase
	:	#(	SLIDER
			(	HORIZONTAL
			|	#(MAXVALUE expression )
			|	#(MINVALUE expression )
			|	VERTICAL
			|	NOCURRENTVALUE
			|	LARGETOSMALL
			|	#(TICMARKS (NONE|TOP|BOTTOM|LEFT|RIGHT|BOTH) (#(FREQUENCY expression))? )
			|	tooltip_expr
			|	sizephrase
			)*
		)
	;

spacephrase
	:	#(SPACE (funargs)? )
	;

state_end
	:	PERIOD | EOF
	;

statusstate
	:	#(	STATUS
			(	#(DEFAULT (expression)? )
			|	#(INPUT (OFF|expression)? )
			)
			(#(IN_KW WINDOW expression))?
		state_end
		)
	;

stopstate
	:	#(STOP state_end )
	;

stream_name
	:	#(STREAM ID )
	;

subscribestate
	:	#(	SUBSCRIBE ( #(PROCEDURE expression) )? (TO)? expression
			(ANYWHERE | #(IN_KW expression) )
			( #(RUNPROCEDURE expression) )?
			(NOERROR_KW)? state_end
		)
	;
   
substringfunc
	:	#(SUBSTRING funargs )
	;

systemdialogcolorstate
	:	#(SYSTEMDIALOG COLOR expression ( #(UPDATE field) )? (#(IN_KW WINDOW expression))? state_end )
	;

systemdialogfontstate
	:	#(	SYSTEMDIALOG FONT expression
			(	ANSIONLY
			|	FIXEDONLY
			|	#(MAXSIZE expression )
			|	#(MINSIZE expression )
			|	#(UPDATE field )
			|	#(IN_KW WINDOW expression)
			)*
			state_end
		)
	;

systemdialoggetdirstate
	:	#(	SYSTEMDIALOG GETDIR field
			(	#(INITIALDIR expression)
			|	RETURNTOSTARTDIR
			|	#(TITLE expression)
			)*
			state_end
		)
	;

systemdialoggetfilestate
	:	#(	SYSTEMDIALOG GETFILE field
			(	#(	FILTERS expression expression (COMMA expression expression)*
					( #(INITIALFILTER expression ) )?
				)
			|	ASKOVERWRITE
			|	CREATETESTFILE
			|	#(DEFAULTEXTENSION expression )
			|	#(INITIALDIR expression )
			|	MUSTEXIST
			|	RETURNTOSTARTDIR
			|	SAVEAS
			|	#(TITLE expression)
			|	USEFILENAME
			|	#(UPDATE field )
			|	#(IN_KW WINDOW expression)
			)*
			state_end
		)
	;

systemdialogprintersetupstate
	:	#(	SYSTEMDIALOG PRINTERSETUP
			( #(NUMCOPIES expression) | #(UPDATE field) | LANDSCAPE | PORTRAIT | #(IN_KW WINDOW expression) )*
			state_end
		)
	;

systemhelpstate
	:	#(	SYSTEMHELP expression
			( #(WINDOWNAME expression) )?
			(	#(ALTERNATEKEY expression )
			|	#(CONTEXT expression )
			|	CONTENTS 
			|	#(SETCONTENTS expression )
			|	FINDER
			|	#(CONTEXTPOPUP expression )
			|	#(HELPTOPIC expression )
			|	#(KEY expression )
			|	#(PARTIALKEY (expression)? )
			|	#(MULTIPLEKEY expression TEXT expression )
			|	#(COMMAND expression )
			|	#(POSITION (MAXIMIZE | X expression Y expression WIDTH expression HEIGHT expression) )
			|	FORCEFILE
			|	HELP
			|	QUIT
			)
			state_end
		)
	;

textphrase
	:	#(TEXT (sizephrase | tooltip_expr)* )
	;

titlephrase
	:	#(TITLE (color_expr | #(COLOR anyorvalue) | #(FONT expression) )* expression )
	;

toggleboxphrase
	:	#(TOGGLEBOX (sizephrase | tooltip_expr)* )
	;

tooltip_expr
	:	#(TOOLTIP (valueexpression | constant) )
	;

transactionmodeautomaticstate
	:	#(TRANSACTIONMODE AUTOMATIC (CHAINED)? state_end )
	;

triggerphrase
	:	#(	TRIGGERS block_colon
			#(	Code_block
				( #(ON eventlist (ANYWHERE)? (PERSISTENT runstate | blockorstate) ) )*
			)
			#(END (TRIGGERS)? )
		)
	;

triggerprocedurestate
	:	#(	TRIGGER PROCEDURE FOR
			(	(CREATE|DELETE_KW|FIND|REPLICATIONCREATE|REPLICATIONDELETE)
				OF RECORD_NAME (label_constant)?
			|	(WRITE|REPLICATIONWRITE) OF RECORD_NAME (label_constant)?
				(NEW (BUFFER)? ID (label_constant)?)?
				(OLD (BUFFER)? ID (label_constant)?)? 
			|	ASSIGN
				(	#(OF field (#(TABLE LABEL constant))? )
				|	#(NEW (VALUE)? id:ID defineparam_var )
				)? 
				(	#(OLD (VALUE)? id2:ID defineparam_var )
				)?
			)
			state_end
		)
	;

underlinestate
	:	#(UNDERLINE (stream_name)? (#(Form_item field (formatphrase)? ))* (framephrase)? state_end )
	;

undostate
	:	#(	UNDO (BLOCK_LABEL)?
			(	COMMA
				(	#(LEAVE (BLOCK_LABEL)? )
				|	#(NEXT (BLOCK_LABEL)? )
				|	#(RETRY (BLOCK_LABEL)? )
				|	#(RETURN (return_options)? )
				)
			)?
			state_end
		)
	;

unloadstate
	:	#(UNLOAD expression (NOERROR_KW)? state_end )
	;

unsubscribestate
	:	#(UNSUBSCRIBE (#(PROCEDURE expression))? (TO)? (expression|ALL) (#(IN_KW expression))? state_end )
	;

upstate
	:	#(UP (options{greedy=true;}:stream_name)? (expression)? (stream_name)? (framephrase)? state_end )
	;

updatestatement
	:	(#(UPDATE RECORD_NAME SET))=> sqlupdatestate
	|	updatestate
	;

updatestate
	:	#(	UPDATE
			(UNLESSHIDDEN)?	
			(form_item)*
			(goonphrase)?
			(#(EXCEPT (field)*))?
			(#(IN_KW WINDOW expression))?
			(framephrase)?
			(editingphrase)?
			(NOERROR_KW)?
			state_end
		)
	;

usestate
	:	#(USE expression (NOERROR_KW)? state_end )
	;

validatestate
	:	#(VALIDATE RECORD_NAME (NOERROR_KW)? state_end )
	;

viewstate
	:	#(VIEW (stream_name)? (gwidget)* (#(IN_KW WINDOW expression))? state_end )
	;

viewasphrase
	:	#(	VIEWAS
			(	comboboxphrase
			|	editorphrase
			|	fillinphrase
			|	radiosetphrase
			|	selectionlistphrase
			|	sliderphrase
			|	textphrase
			|	toggleboxphrase
			)
		)
	;

waitforstate
	:	#(	WAITFOR
			eventlist OF widgetlist
			(#(OR eventlist OF widgetlist))*
			(#(FOCUS gwidget))?
			(#(PAUSE expression))?
			(EXCLUSIVEWEBUSER (expression)?)?
			state_end
		)
	;




///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin SQL
///////////////////////////////////////////////////////////////////////////////////////////////////

altertablestate
	:	#(	ALTER TABLE RECORD_NAME
			(	ADD COLUMN sql_col_def
			|	DROP COLUMN field
			|	ALTER COLUMN field
				(  	#(FORMAT expression)
				|	label_constant
     				|	#(DEFAULT expression )
				| 	casesens_or_not
   				)*
			)
			state_end
		)
	;

closestate
	:	#(CLOSE ID state_end )
	;

createindexstate
	:	#(CREATE (UNIQUE)? INDEX ID ON RECORD_NAME #(Field_list LEFTPAREN field (COMMA field)* RIGHTPAREN ) state_end )
	;

createtablestate
	:	#(	CREATE TABLE ID 
			LEFTPAREN
			(	sql_col_def
			|	#(UNIQUE LEFTPAREN ID (COMMA ID)* RIGHTPAREN)
			)
			(	COMMA
				(	sql_col_def
				|	#(UNIQUE LEFTPAREN ID (COMMA ID)* RIGHTPAREN)
				)
			)*
			RIGHTPAREN
			state_end
		)
	;

createviewstate
	:	#(CREATE VIEW ID (#(Field_list LEFTPAREN field (COMMA field)* RIGHTPAREN ))? AS selectstatea state_end )
	;

declarecursorstate
	:	#(DECLARE ID CURSOR FOR selectstatea (#(FOR (#(READ (ONLY)?) | UPDATE)))? state_end )
	;

deletefromstate
	:	#(	DELETE_KW FROM RECORD_NAME
			( #(WHERE (sqlexpression | #(CURRENT OF ID))? ) )?
			state_end
		)
	;

dropindexstate
	:	#(DROP INDEX ID state_end )
	;

droptablestate
	:	#(DROP TABLE RECORD_NAME state_end )
	;

dropviewstate
	:	#(DROP VIEW ID state_end )
	;

fetchstate
	:	#(FETCH ID INTO field (fetch_indicator)? (COMMA field (fetch_indicator)? )* state_end )
	;
fetch_indicator
	:	#(INDICATOR field )
	|	field
	;

grantstate
	: 	#(GRANT (grant_rev_opt) ON (RECORD_NAME|ID) grant_rev_to (WITH GRANT OPTION)? state_end )
	;
grant_rev_opt
	:	#(ALL (PRIVILEGES)? )
	|	(	SELECT | INSERT | DELETE_KW
		|	#(UPDATE (#(Field_list LEFTPAREN field (COMMA field)* RIGHTPAREN ))? )
		|	COMMA
		)+
	;
grant_rev_to
	:	#(TO (PUBLIC | FILENAME (COMMA FILENAME)*) )
	|	#(FROM (PUBLIC | FILENAME (COMMA FILENAME)*) )
	;

insertintostate
	:	#(	INSERT INTO RECORD_NAME
			(#(Field_list LEFTPAREN field (COMMA field)* RIGHTPAREN ))?
			(	#(	VALUES LEFTPAREN sqlexpression (fetch_indicator)?
					(COMMA sqlexpression (fetch_indicator)?)* RIGHTPAREN
				)
			|	selectstatea
			)
			state_end
		)
	;

openstate
	: 	#(OPEN ID state_end )
	;

revokestate
	: 	#(REVOKE (grant_rev_opt) ON (RECORD_NAME|ID) grant_rev_to state_end )
	;

selectstate
	: 	selectstatea state_end
	;

selectstatea
	:	#(	SELECT
			(ALL | DISTINCT)?
			(	STAR
			|	#(	Sql_select_what
					(	(LEFTPAREN)=> LEFTPAREN sqlexpression (formatphrase)? RIGHTPAREN (formatphrase)?
					|	sqlexpression (formatphrase)?
					)
					(COMMA sqlexpression (formatphrase)?)*
				)
			)
			( #(INTO field (fetch_indicator)? (COMMA field (fetch_indicator)?)* ) )?
			#(FROM select_from_spec (COMMA select_from_spec)* )
			( #(GROUP BY expression (COMMA expression)* ) )?
			( #(HAVING sqlexpression) )?
			(	#(ORDER BY select_order_expr )
			|	#(BY select_order_expr )
			)?
			// Ick. I had trouble convincing antlr not to check the syntactic predicate
			// if next token _t was null.
			(	{_t != null}? ( ( #(WITH CHECK OPTION ) )=>{_t != null}? #(WITH CHECK OPTION ) | )
			|	// empty alt
			)
			(framephrase)?
			( #(UNION (ALL)? selectstatea) )?
		)
	;
select_from_spec
	:	select_sqltableref
		(	#(LEFT (OUTER)? JOIN select_sqltableref ON sqlexpression )
		|	#(RIGHT (OUTER)? JOIN select_sqltableref ON sqlexpression )
		|	#(INNER JOIN select_sqltableref ON sqlexpression )
		|	#(OUTER JOIN select_sqltableref ON sqlexpression )
		|	#(JOIN select_sqltableref ON sqlexpression )
		)*
		( #(WHERE sqlexpression) )?
	;
select_sqltableref
	:	(RECORD_NAME | ID) (ID)?
	;
select_order_expr
	:	sqlscalar (ASC|DESCENDING)? (COMMA sqlscalar (ASC|DESCENDING)?)*
	;

sqlupdatestate
	: 	#(	UPDATE RECORD_NAME SET sqlupdate_equal (COMMA sqlupdate_equal)*
			( #(WHERE (sqlexpression | CURRENT OF ID) ) )?
			state_end
		)
	;
sqlupdate_equal
	:	#(EQUAL field sqlexpression (fetch_indicator)? )
	;

///////////////////////////////////////////////////////////////////////////////////////////////////
// sql functions and phrases
///////////////////////////////////////////////////////////////////////////////////////////////////

sqlaggregatefunc
// also see maximumfunc and minimumfunc
	:	#(AVG sqlaggregatefunc_arg )
	|	#(COUNT sqlaggregatefunc_arg )
	|	#(SUM sqlaggregatefunc_arg )
	;
sqlaggregatefunc_arg
	:	LEFTPAREN
		(	DISTINCT
			(	LEFTPAREN field RIGHTPAREN
			|	field
			)
		|	STAR
		|	(ALL)? sqlscalar
		)
		RIGHTPAREN
	;

sql_col_def
	:	#(	ID
			. // datatype
			(PRECISION)?
			(LEFTPAREN NUMBER (COMMA NUMBER)? RIGHTPAREN)?
			( #(Not_null NOT NULL_KW (UNIQUE)? ) )?
			(	label_constant
			|	#(DEFAULT expression )
			|  	#(FORMAT expression)
			| 	casesens_or_not
			)*
		)
	;



///////////////////////////////////////////////////////////////////////////////////////////////////
// sqlexpression 
///////////////////////////////////////////////////////////////////////////////////////////////////

sqlexpression
	:	#(OR sqlexpression sqlexpression )
	|	#(AND sqlexpression sqlexpression )
	|	#(NOT sqlexpression )
	|	#(MATCHES	sqlscalar (sqlscalar | sql_comp_query) )
	|	#(BEGINS	sqlscalar (sqlscalar | sql_comp_query) )
	|	#(CONTAINS	sqlscalar (sqlscalar | sql_comp_query) )
	|	#(EQ		sqlscalar (sqlscalar | sql_comp_query) )
	|	#(NE		sqlscalar (sqlscalar | sql_comp_query) )
	|	#(GTHAN		sqlscalar (sqlscalar | sql_comp_query) )
	|	#(GE		sqlscalar (sqlscalar | sql_comp_query) )
	|	#(LTHAN		sqlscalar (sqlscalar | sql_comp_query) )
	|	#(LE		sqlscalar (sqlscalar | sql_comp_query) )
	|	#(EXISTS LEFTPAREN selectstatea RIGHTPAREN )
	|	#(Sql_begins (NOT)? BEGINS sqlscalar )
	|	#(Sql_between (NOT)? BETWEEN sqlscalar AND sqlscalar )
	|	#(Sql_in (NOT)? IN_KW LEFTPAREN (selectstatea | sql_in_val (COMMA sql_in_val)*) RIGHTPAREN )
	|	#(Sql_like (NOT)? LIKE sqlscalar (ESCAPE sqlscalar)? )
	|	#(Sql_null_test IS (NOT)? NULL_KW )
	|	sqlscalar
	;
sql_comp_query
	:	#(Sql_comp_query (ANY|ALL|SOME)? LEFTPAREN selectstatea RIGHTPAREN )
	;
sql_in_val
	:	field (fetch_indicator)? | constant | USERID
	;
sqlscalar
	:	#(PLUS sqlscalar sqlscalar )
	|	#(MINUS sqlscalar sqlscalar )
	|	#(MULTIPLY sqlscalar sqlscalar )
	|	#(DIVIDE sqlscalar sqlscalar )
	|	#(MODULO sqlscalar sqlscalar )
	|	#(UNARY_PLUS exprt )
	|	#(UNARY_MINUS exprt )
	|	(LEFTPAREN)=> #(LEFTPAREN sqlexpression RIGHTPAREN )
	|	exprt
	;


