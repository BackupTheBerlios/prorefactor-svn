/*

JPTreeParser.g - Primary tree parser.

Joanju Proparse Syntax Tree Structure Specification

Copyright (c) 2001-2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html


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

pseudfn
	:	AAMSG
	|	currentvaluefunc
	|	CURRENTWINDOW
	|	dynamiccurrentvaluefunc
	|	entryfunc
	|	fixcodepage_pseudfn
	|	lengthfunc
	|	nextvaluefunc
	|	overlay_pseudfn
	|	putbits_pseudfn
	|	putbyte_pseudfn
	|	putbytes_pseudfn
	|	putdouble_pseudfn
	|	putfloat_pseudfn
	|	putlong_pseudfn
	|	putshort_pseudfn
	|	putstring_pseudfn
	|	putunsignedshort_pseudfn
	|	rawfunc
	|	setbyteorder_pseudfn
	|	setpointervalue_pseudfn
	|	setsize_pseudfn
	|	substringfunc
	|	widattr
	|	PAGESIZE_KW | LINECOUNTER | PAGENUMBER | FRAMECOL
	|	FRAMEDOWN | FRAMELINE | FRAMEROW | USERID | ETIME_KW
	|	DBNAME | TIME | OPSYS | RETRY | AASERIAL | AACONTROL
	|	MESSAGELINES | TERMINAL | PROPATH | CURRENTLANGUAGE | PROMSGS
	|	SCREENLINES | LASTKEY
	|	FRAMEFIELD | FRAMEFILE | FRAMEVALUE | GOPENDING
	|	PROGRESS | FRAMEINDEX | FRAMEDB | FRAMENAME | DATASERVERS
	|	NUMDBS | NUMALIASES | ISATTRSPACE | PROCSTATUS
	|	PROCHANDLE | CURSOR | OSERROR | RETURNVALUE | OSDRIVES
	|	PROVERSION | TRANSACTION | MACHINECLASS 
	|	AAPCONTROL | GETCODEPAGES | COMSELF
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

functioncall
	:	aacbitfunc | aacontrolfunc | aamsgfunc | aapcontrolfunc | aaserialfunc
	|	absolutefunc | accumfunc | addintervalfunc | aliasfunc | ambiguousfunc | ascfunc 
	|	availablefunc 
	|	base64decodefunc | base64encodefunc
	|	candofunc | canfindfunc | canqueryfunc | cansetfunc | capsfunc 
	|	chrfunc | codepageconvertfunc | collatefunc | comparefunc | connectedfunc 
	|	countoffunc | currentchangedfunc 
	|	currentlanguagefunc | currentresultrowfunc | currentvaluefunc | cursorfunc | dataserversfunc 
	|	datasourcemodifiedfunc
	|	datefunc | datetimefunc | datetimetzfunc | dayfunc 
	|	dbcodepagefunc | dbcollationfunc | dbnamefunc | dbparamfunc 
	|	dbrestrictionsfunc | dbtaskidfunc | dbtypefunc | dbversionfunc | decimalfunc | decryptfunc
	|	dynamiccurrentvaluefunc | dynamicnextvaluefunc
	|	dynamicfuncfunc | encodefunc | encryptfunc | entryfunc 
	|	errorfunc | etimefunc | expfunc | extentfunc 
	|	fillfunc | firstfunc | firstoffunc | framecolfunc | framedbfunc | framedownfunc 
	|	framefieldfunc | framefilefunc | frameindexfunc | framelinefunc | framenamefunc 
	|	framerowfunc | framevaluefunc
	|	generatepbekeyfunc | generatepbesaltfunc | generaterandomkeyfunc
	|	gatewaysfunc | getbitsfunc | getbytefunc 
	|	getbyteorderfunc | getbytesfunc | getcodepagesfunc | getcollationsfunc 
	|	getdoublefunc | getfloatfunc
	|	getlicensefunc
	|	getlongfunc | getpointervaluefunc | getshortfunc 
	|	getsizefunc | getstringfunc | getunsignedshortfunc | gopendingfunc | iffunc 
	|	indexfunc
	|	integerfunc | intervalfunc
	|	isattrspacefunc | iscodepagefixedfunc | iscolumncodepagefunc | isleadbytefunc
	|	isodatefunc
	|	kblabelfunc | keycodefunc | keyfuncfunc | keylabelfunc | keywordfunc 
	|	keywordallfunc | lastfunc | lastoffunc | lastkeyfunc | lcfunc | ldbnamefunc 
	|	lefttrimfunc | lengthfunc | libraryfunc | linecounterfunc | listeventsfunc 
	|	listqueryattrsfunc | listsetattrsfunc | listwidgetsfunc | loadpicturefunc 
	|	lockedfunc | logfunc |logicalfunc | lookupfunc
	|	machineclassfunc
	| 	maximumfunc | md5digestfunc | memberfunc | messagelinesfunc |	minimumfunc
	|	monthfunc | mtimefunc | newfunc | nextvaluefunc | normalizefunc | nowfunc 
	|	numaliasesfunc | numdbsfunc 
	|	numentriesfunc | numresultsfunc | opsysfunc | osdrivesfunc | oserrorfunc 
	|	osgetenvfunc | pagenumberfunc | pagesizefunc | pdbnamefunc | prochandlefunc 
	|	procstatusfunc | programnamefunc | progressfunc | promsgsfunc | propathfunc 
	|	proversionfunc | queryoffendfunc | quoterfunc | rindexfunc | randomfunc | rawfunc | recidfunc 
	|	recordlengthfunc | rejectedfunc | replacefunc | retryfunc | returnvaluefunc | rgbvaluefunc 
	|	righttrimfunc | roundfunc | rowidfunc | rowstatefunc | screenlinesfunc | sdbnamefunc | searchfunc 
	|	seekfunc | setuseridfunc | sha1digestfunc | sqlaggregatefunc | sqrtfunc
	|	sslservernamefunc | stringfunc | substitutefunc 
	|	substringfunc | superfunc | torowidfunc | terminalfunc | timefunc | timezonefunc | todayfunc 
	|	transactionfunc | trimfunc | truncatefunc | useridfunc | valideventfunc 
	|	validhandlefunc | weekdayfunc | widgethandlefunc | yearfunc
	;

parameter
	:	#(BUFFER (RECORD_NAME | ID FOR RECORD_NAME ) )
	|	#(PARAM expression EQUAL expression )
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

widgettype
	:	BROWSE | BUFFER | BUTTON | COMBOBOX | CONTROLFRAME | DIALOGBOX
	|	EDITOR | FIELD | FILLIN | FRAME | IMAGE | MENU
	| 	MENUITEM | QUERY | RADIOSET | RECTANGLE | SELECTIONLIST 
	|	SLIDER | SOCKET | SUBMENU | TEMPTABLE | TEXT | TOGGLEBOX | WINDOW
	|	XDOCUMENT | XNODEREF
	;


//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
//                   begin PROGRESS syntax features, in alphabetical order
//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////



// Because Antlr doesn't let us start names with underscore,
// we had to use AASERIAL instead of _SERIAL, etc.


aacbitfunc
	:	#(AACBIT funargs )
	;

aacontrolfunc
	:	AACONTROL
	;

aamsgfunc
	:	#(AAMSG funargs )
	;

aapcontrolfunc
	:	AAPCONTROL
	;

aaserialfunc
	:	AASERIAL
	;

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

absolutefunc
	:	#(ABSOLUTE funargs )
	;

accum_what
	:	AVERAGE|COUNT|MAXIMUM|MINIMUM|TOTAL|SUBAVERAGE|SUBCOUNT|SUBMAXIMUM|SUBMINIMUM|SUBTOTAL
	;

accumfunc
	:	#(ACCUMULATE accum_what ( #(BY expression (DESCENDING)? ) )? expression )
	;

accumulatestate
	:	#(ACCUMULATE (display_item)* state_end )
	;

addintervalfunc
	:	#(ADDINTERVAL LEFTPAREN expression COMMA expression COMMA expression RIGHTPAREN )
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

aliasfunc
	:	#(ALIAS funargs )
	;

ambiguousfunc
	:	#(AMBIGUOUS (RECORD_NAME | LEFTPAREN RECORD_NAME RIGHTPAREN) )
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

ascfunc
	:	#(ASC funargs )
	;

assign_opt
	:	#(ASSIGN ( #(EQUAL (ID|keyword) expression ) )+ )
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

availablefunc
	:	#(AVAILABLE (RECORD_NAME | LEFTPAREN RECORD_NAME RIGHTPAREN) )
	;

base64decodefunc
	:	#(BASE64DECODE funargs )
	;

base64encodefunc
	:	#(BASE64ENCODE funargs )
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

candofunc
	:	#(CANDO funargs )
	;

canfindfunc
	:	#(CANFIND LEFTPAREN (findwhich)? recordphrase RIGHTPAREN )
	;
 
canqueryfunc
	:	#(CANQUERY funargs )
	;

cansetfunc
	:	#(CANSET funargs )
	;

capsfunc
	:	#(CAPS funargs )
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

chrfunc
	:	#(CHR funargs )
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

codepageconvertfunc
	:	#(CODEPAGECONVERT funargs )
	;

collatefunc
	:	#(COLLATE funargs)
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

comparefunc
	:	#(COMPARE funargs )
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

connectedfunc
	:	#(CONNECTED funargs )
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

countoffunc
	:	#(COUNTOF funargs )
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

currentchangedfunc
	:	#(CURRENTCHANGED (RECORD_NAME | LEFTPAREN RECORD_NAME RIGHTPAREN) )
	;

currentlanguagefunc
	:	CURRENTLANGUAGE
	;

currentresultrowfunc
	:	#(CURRENTRESULTROW funargs )
	;

currentvaluefunc
	:	#(CURRENTVALUE LEFTPAREN ID (COMMA ID)? RIGHTPAREN )
	;

cursorfunc
	:	CURSOR
	;

dataserversfunc
	:	DATASERVERS
	;

datasourcemodifiedfunc
	:	#(DATASOURCEMODIFIED LEFTPAREN RECORD_NAME RIGHTPAREN )
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

datefunc
	:	#(DATE funargs )
	;

datetimefunc
	:	#(DATETIME funargs )
	;

datetimetzfunc
	:	#(DATETIMETZ funargs )
	;

dayfunc
	:	#(DAY funargs )
	;

dbcodepagefunc
	:	#(DBCODEPAGE funargs )
	;

dbcollationfunc
	:	#(DBCOLLATION funargs )
	;

dbnamefunc
	:	DBNAME
	;

dbparamfunc
	:	#(DBPARAM funargs )
	;

dbrestrictionsfunc
	:	#(DBRESTRICTIONS funargs )
	;

dbtaskidfunc
	:	#(DBTASKID funargs )
	;

dbtypefunc
	:	#(DBTYPE funargs )
	;

dbversionfunc
	:	#(DBVERSION funargs )
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

decimalfunc
	:	#(DECIMAL funargs )
	;

decryptfunc
	:	#(DECRYPT funargs )
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

dynamicnextvaluefunc
	:	#(DYNAMICNEXTVALUE funargs)
	;

dynamicfuncfunc
	:	#(DYNAMICFUNCTION LEFTPAREN expression (#(IN_KW expression))? (COMMA parameter)* RIGHTPAREN (NOERROR_KW)? )
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

encodefunc
	:	#(ENCODE funargs )
	;

encryptfunc
	:	#(ENCRYPT funargs )
	;

entryfunc
	:	#(ENTRY funargs )
	;

errorfunc
	:	#(ERROR LEFTPAREN RECORD_NAME RIGHTPAREN )
	;

etimefunc
	:	#(ETIME_KW (funargs)? )
	;

expfunc
	:	#(EXP funargs )
	;

exportstate
	:	#(EXPORT (stream_name)? (#(DELIMITER constant))? (display_item)* (#(EXCEPT (field)*))? (NOLOBS)? state_end )
	;

extentfunc
	:	#(EXTENT LEFTPAREN field RIGHTPAREN )
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

fillfunc
	:	#(FILL funargs )
	;

findstate
	:	#(FIND (findwhich)? recordphrase (NOWAIT|NOPREFETCH|NOERROR_KW)* state_end )
	;

firstfunc
	:	#(FIRST funargs )
	;

firstoffunc
	:	#(FIRSTOF funargs )
	;

fixcodepage_pseudfn
	:	#(FIXCODEPAGE LEFTPAREN field RIGHTPAREN )
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

framecolfunc
	:	#(FRAMECOL (LEFTPAREN ID RIGHTPAREN)? )
	;

framedbfunc
	:	FRAMEDB
	;

framedownfunc
	:	#(FRAMEDOWN (LEFTPAREN ID RIGHTPAREN)? )
	;

framefieldfunc
	:	FRAMEFIELD
	;

framefilefunc
	:	FRAMEFILE
	;

frameindexfunc
	:	FRAMEINDEX
	;

framelinefunc
	:	#(FRAMELINE (LEFTPAREN ID RIGHTPAREN)? )
	;

framenamefunc
	:	FRAMENAME
	;

framerowfunc
	:	#(FRAMEROW (LEFTPAREN ID RIGHTPAREN)? )
	;

framevaluefunc
	:	FRAMEVALUE
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

generatepbekeyfunc
	:	#(GENERATEPBEKEY funargs )
	;

generatepbesaltfunc
	:	GENERATEPBESALT
	;

generaterandomkeyfunc
	:	GENERATERANDOMKEY
	;

gatewaysfunc
	:	GATEWAYS
	;

getstate
	:	#(GET findwhich ID (lockhow|NOWAIT)* state_end )
	;

getbitsfunc
	:	#(GETBITS funargs )
	;

getbytefunc
	:	#(GETBYTE funargs )
	;

getbyteorderfunc
	:	#(GETBYTEORDER funargs )
	;

getbytesfunc
	:	#(GETBYTES funargs )
	;

getcodepagesfunc
	:	#(GETCODEPAGES (funargs)? )
	;

getcollationsfunc
	:	#(GETCOLLATIONS funargs )
	;

getdoublefunc
	:	#(GETDOUBLE funargs )
	;

getfloatfunc
	:	#(GETFLOAT funargs )
	;

getkeyvaluestate
	:	#(GETKEYVALUE SECTION expression KEY (DEFAULT|expression) VALUE field state_end )
	;

getlicensefunc
	:	#(GETLICENSE funargs )
	;

getlongfunc
	:	#(GETLONG funargs )
	;

getpointervaluefunc
	:	#(GETPOINTERVALUE funargs )
	;

getshortfunc
	:	#(GETSHORT funargs )
	;

getsizefunc
	:	#(GETSIZE funargs )
	;

getstringfunc
	:	#(GETSTRING funargs )
	;

getunsignedshortfunc
	:	#(GETUNSIGNEDSHORT funargs )
	;

goonphrase
	:	#(GOON LEFTPAREN goon_elem ((options{greedy=true;}:COMMA)? goon_elem)* RIGHTPAREN )
	;
goon_elem
	:	~(RIGHTPAREN) ( (OF)=> OF gwidget)?
	;

gopendingfunc
	:	GOPENDING
	;

hidestate
	:	#(HIDE (stream_name)? (MESSAGE|ALL|(gwidget)*) (NOPAUSE)? (#(IN_KW WINDOW expression))? state_end )
	;

iffunc
	:	#(IF expression THEN expression ELSE expression )
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

indexfunc
	:	#(INDEX funargs )
	;

initial_constant
	:	#(	INITIAL
			(	LEFTBRACE (TODAY|constant) (COMMA (TODAY|constant))* RIGHTBRACE
			|	(TODAY|constant)
			)
		)
	;

// INPUT function: see "field"

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

integerfunc
	:	#(INTEGER funargs )
	;

intervalfunc
	:	#(INTERVAL funargs )
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

isattrspacefunc
	:	ISATTRSPACE
	;

iscodepagefixedfunc
	:	#(ISCODEPAGEFIXED funargs )
	;

iscolumncodepagefunc
	:	#(ISCOLUMNCODEPAGE funargs )
	;

isleadbytefunc
	:	#(ISLEADBYTE funargs )
	;

isodatefunc
	:	#(ISODATE funargs )
	;

kblabelfunc
	:	#(KBLABEL funargs )
	;

keycodefunc
	:	#(KEYCODE funargs )
	;

keyfuncfunc
	:	#(KEYFUNCTION funargs )
	;

keylabelfunc
	:	#(KEYLABEL funargs )
	;

keywordfunc
	:	#(KEYWORD funargs )
	;

keywordallfunc
	:	#(KEYWORDALL funargs )
	;

label_constant
	:	#(COLUMNLABEL constant (COMMA constant)* )
	|	#(LABEL constant (COMMA constant)* )
	;

lastfunc
	:	#(LAST funargs )
	;

lastoffunc
	:	#(LASTOF funargs )
	;

lastkeyfunc
	:	LASTKEY
	;

lcfunc
	:	#(LC funargs )
	;

ldbnamefunc
	:	#(LDBNAME LEFTPAREN (#(BUFFER RECORD_NAME) | expression) RIGHTPAREN )
	;

leavestate
	:	#(LEAVE (BLOCK_LABEL)? state_end )
	;

lefttrimfunc
	:	#(LEFTTRIM funargs )
	;

lengthfunc
	:	#(LENGTH funargs )
	;

libraryfunc
	:	#(LIBRARY funargs )
	;

linecounterfunc
	:	#(LINECOUNTER (LEFTPAREN ID RIGHTPAREN)? )
	;

listeventsfunc
	:	#(LISTEVENTS funargs )
	;

listqueryattrsfunc
	:	#(LISTQUERYATTRS funargs )
	;

listsetattrsfunc
	:	#(LISTSETATTRS funargs )
	;

listwidgetsfunc
	:	#(LISTWIDGETS funargs )
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

lockedfunc
	:	#(LOCKED (RECORD_NAME | LEFTPAREN RECORD_NAME RIGHTPAREN) )
	;

logfunc
	:	#(LOG funargs )
	;

logicalfunc
	:	#(LOGICAL funargs )
	;

lookupfunc
	:	#(LOOKUP funargs )
	;

machineclassfunc
	:	MACHINECLASS
	;

maximumfunc
	:	#(	MAXIMUM 
			(	(LEFTPAREN (DISTINCT|STAR|ALL))=> sqlaggregatefunc_arg
			|	funargs
			)
		)
	;

md5digestfunc
	:	#(MD5DIGEST funargs )
	;

memberfunc
	:	#(MEMBER funargs )
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

messagelinesfunc
	:	MESSAGELINES
	;

minimumfunc
	:	#(	MINIMUM
			(	(LEFTPAREN (DISTINCT|STAR|ALL))=> sqlaggregatefunc_arg
			|	funargs
			)
		)
	;

monthfunc
	:	#(MONTH funargs )
	;

mtimefunc
	:	#(MTIME LEFTPAREN (expression)? RIGHTPAREN )
	;

newfunc
	:	#(NEW (LEFTPAREN RECORD_NAME RIGHTPAREN | RECORD_NAME) )
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

normalizefunc
	:	#(NORMALIZE funargs )
	;

nowfunc
	:	NOW
	;

numaliasesfunc
	:	NUMALIASES
	;

numdbsfunc
	:	NUMDBS
	;

numentriesfunc
	:	#(NUMENTRIES funargs )
	;

numresultsfunc
	:	#(NUMRESULTS funargs )
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
				( (ID|keyword) (ID|keyword|QSTRING) state_end )=> 
				(ID|keyword) (ID|keyword|QSTRING) state_end
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

opsysfunc
	:	OPSYS
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

osdrivesfunc
	:	OSDRIVES
	;

oserrorfunc
	:	OSERROR
	;

osgetenvfunc
	:	#(OSGETENV funargs )
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

overlay_pseudfn
	:	#(OVERLAY funargs )
	;

pagestate
	:	#(PAGE (stream_name)? state_end )
	;

pagenumberfunc
	:	#(PAGENUMBER (LEFTPAREN ID RIGHTPAREN)? )
	;

pagesizefunc
	:	#(PAGESIZE_KW (LEFTPAREN ID RIGHTPAREN)? )
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

pdbnamefunc
	:	#(PDBNAME funargs )
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

prochandlefunc
	:	PROCHANDLE
	;

procstatusfunc
	:	PROCSTATUS
	;

processeventsstate
	:	#(PROCESS EVENTS state_end )
	;

programnamefunc
	:	#(PROGRAMNAME funargs )
	;

progressfunc
	:	PROGRESS
	;

promptforstate
	:	#(	PROMPTFOR (stream_name)? (UNLESSHIDDEN)? (form_item)*
			(goonphrase)?  (#(EXCEPT (field)*))?  (#(IN_KW WINDOW expression))?  (framephrase)?  (editingphrase)?
			state_end
		)
	;

promsgsfunc
	:	PROMSGS
	;

propathfunc
	:	PROPATH
	;

proversionfunc
	:	PROVERSION
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

putbits_pseudfn
	:	#(PUTBITS funargs )
	;

putbyte_pseudfn
	:	#(PUTBYTE funargs )
	;

putbytes_pseudfn
	:	#(PUTBYTES funargs )
	;

putdouble_pseudfn
	:	#(PUTDOUBLE funargs )
	;

putfloat_pseudfn
	:	#(PUTFLOAT funargs )
	;

putkeyvaluestate
	:	#(	PUTKEYVALUE
			(	SECTION expression KEY (DEFAULT|expression) VALUE expression
			|	(COLOR|FONT) (expression|ALL)
			)
			(NOERROR_KW)? state_end
		)
	;

putlong_pseudfn
	:	#(PUTLONG funargs )
	;

putshort_pseudfn
	:	#(PUTSHORT funargs )
	;

putstring_pseudfn
	:	#(PUTSTRING funargs )
	;

putunsignedshort_pseudfn
	:	#(PUTUNSIGNEDSHORT funargs )
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

queryoffendfunc
	:	#(QUERYOFFEND funargs )
	;

quitstate
	:	#(QUIT state_end )
	;

quoterfunc
	:	#(QUOTER funargs )
	;

rindexfunc
	:	#(RINDEX funargs )
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

randomfunc
	:	#(RANDOM funargs )
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

recidfunc
	:	#(RECID LEFTPAREN RECORD_NAME RIGHTPAREN )
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

recordlengthfunc
	:	#(RECORDLENGTH LEFTPAREN RECORD_NAME RIGHTPAREN )
	;

rejectedfunc
	:	#(REJECTED LEFTPAREN RECORD_NAME RIGHTPAREN )
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

replacefunc
	:	#(REPLACE funargs )
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

retryfunc
	:	RETRY
	;

returnstate
	:	#(RETURN (return_options)? state_end )
	;

return_options
	:	(	(errorfunc)=> expression
		|	(ERROR)=> ERROR (expression)?
		|	NOAPPLY (expression)?
		|	expression
		)
	;

returnvaluefunc
	:	RETURNVALUE
	;

rgbvaluefunc
	:	#(RGBVALUE funargs )
	;

righttrimfunc
	:	#(RIGHTTRIM funargs )
	;

roundfunc
	:	#(ROUND funargs )
	;

rowidfunc
	:	#(ROWID LEFTPAREN RECORD_NAME RIGHTPAREN )
	;

rowstatefunc
	:	#(ROWSTATE LEFTPAREN RECORD_NAME RIGHTPAREN )
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

screenlinesfunc
	:	SCREENLINES
	;

scrollstate
	:	#(SCROLL (FROMCURRENT)? (UP)? (DOWN)? (framephrase)? state_end )
	;

sdbnamefunc
	:	#(SDBNAME funargs )
	;

searchfunc
	:	#(SEARCH funargs )
	;

seekfunc
	:	#(SEEK LEFTPAREN (INPUT|OUTPUT|ID) RIGHTPAREN )
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

setbyteorder_pseudfn
	:	#(SETBYTEORDER funargs )
	;

setstate
	:	#(	SET
			(stream_name)? (UNLESSHIDDEN)? (form_item)*
			(goonphrase)?  (#(EXCEPT (field)*))?  (#(IN_KW WINDOW expression))?  (framephrase)?  (editingphrase)?  (NOERROR_KW)?
			state_end
		)
	;

setpointervalue_pseudfn
	:	#(SETPOINTERVALUE funargs )
	;

setsize_pseudfn
	:	#(SETSIZE funargs )
	;

setuseridfunc
	:	#(SETUSERID funargs )
	;

showstatsstate
	:	#(SHOWSTATS (CLEAR)? state_end )
	;

sha1digestfunc
	:	#(SHA1DIGEST funargs )
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

sqrtfunc
	:	#(SQRT funargs )
	;

sslservernamefunc
	:	#(SSLSERVERNAME funargs )
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

stringfunc
	:	#(STRING funargs )
	;

subscribestate
	:	#(	SUBSCRIBE ( #(PROCEDURE expression) )? (TO)? expression
			(ANYWHERE | #(IN_KW expression) )
			( #(RUNPROCEDURE expression) )?
			(NOERROR_KW)? state_end
		)
	;
   
substitutefunc
	:	#(SUBSTITUTE funargs )
	;

substringfunc
	:	#(SUBSTRING funargs )
	;

superfunc
	:	#(SUPER (parameterlist)? )
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

terminalfunc
	:	TERMINAL
	;

textphrase
	:	#(TEXT (sizephrase | tooltip_expr)* )
	;

timefunc
	:	TIME
	;

timezonefunc
	:	#(TIMEZONE LEFTPAREN (expression)? RIGHTPAREN )
	;

titlephrase
	:	#(TITLE (color_expr | #(COLOR anyorvalue) | #(FONT expression) )* expression )
	;

todayfunc
	:	TODAY
	;

toggleboxphrase
	:	#(TOGGLEBOX (sizephrase | tooltip_expr)* )
	;

tooltip_expr
	:	#(TOOLTIP (valueexpression | constant) )
	;

torowidfunc
	:	#(TOROWID funargs )
	;

transactionfunc
	:	TRANSACTION
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

trimfunc
	:	#(TRIM funargs )
	;

truncatefunc
	:	#(TRUNCATE funargs )
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

useridfunc
	:	#(USERID (funargs)? )
	|	#(USER (funargs)? )
	;

valideventfunc
	:	#(VALIDEVENT funargs )
	;

validhandlefunc
	:	#(VALIDHANDLE funargs )
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

weekdayfunc
	:	#(WEEKDAY funargs )
	;

widgethandlefunc
	:	#(WIDGETHANDLE funargs )
	;

yearfunc
	:	#(YEAR funargs )
	;



// Due to name collisions, we use NULL_KW instead of NULL, and ETIME_KW instead of ETIME.
// Newer keywords at end of list...
keyword
	:
AACBIT| AACONTROL| AALIST| AAMEMORY| AAMSG| AAPCONTROL| AASERIAL| AATRACE|
ABSOLUTE| ACCELERATOR| ACCUMULATE| ACTIVEWINDOW| ADD| ADDINTERVAL| ADVISE| ALERTBOX| ALIAS| ALL|
ALLOWREPLICATION| ALTER| ALTERNATEKEY|
AMBIGUOUS| ANALYZE| AND| ANSIONLY| ANY| ANYWHERE| APPEND| APPLICATION| APPLY|
ARRAYMESSAGE| AS| ASC| ASCENDING| ASKOVERWRITE| ASSIGN| ASYNCHRONOUS | AT| ATTACHMENT|
ATTRSPACE| AUTHORIZATION| 
AUTOCOMPLETION| AUTOENDKEY| AUTOGO| AUTOMATIC|
AUTORETURN| AVAILABLE| AVERAGE| AVG| BACKGROUND| BACKWARDS| BASE64| BASEKEY| BEFOREHIDE| 
BEGINS| BELL| BETWEEN| BGCOLOR| BIGENDIAN| BINARY| BINDWHERE| BLANK| BLOB| BOTH| BOTTOM| BREAK| BROWSE| 
BTOS| BUFFER| BUFFERCHARS| BUFFERCOMPARE| BUFFERCOPY| BUFFERLINES| BUFFERNAME| BUTTON| BUTTONS| 
BY| BYPOINTER| BYREFERENCE| BYTE| BYVALUE| BYVARIANTPOINTER| 
CACHE| CACHESIZE| CALL| CANDO| CANFIND| CANQUERY|
CANSET| CANCELBUTTON| CAPS| CASE| CASESENSITIVE| CDECL_KW| CENTERED| CHAINED| CHARACTER| CHARACTERLENGTH| CHECK|
CHOOSE| CHR| CLEAR| CLIPBOARD| CLOSE| CLOB| CODEBASELOCATOR|
CODEPAGE| CODEPAGECONVERT| COLLATE| COLOF| COLON| COLONALIGNED| COLOR| COLORTABLE|
COLUMN| COLUMNBGCOLOR| COLUMNCODEPAGE| COLUMNDCOLOR| COLUMNFGCOLOR| COLUMNFONT| COLUMNLABEL| COLUMNOF| COLUMNPFCOLOR| COLUMNS|
COMHANDLE| COMBOBOX| COMMAND| COMPARES| COMPLETE| COMPILE| COMPILER| COMSELF| CONFIGNAME| CONNECT| CONNECTED| CONTAINS|
CONTENTS| CONTEXT| CONTEXTHELP| CONTEXTHELPFILE| CONTEXTHELPID| CONTEXTPOPUP| 
CONTROL| CONTROLFRAME| CONVERT| CONVERT3DCOLORS| COPYLOB| COUNT| COUNTOF| CREATE|
CREATETESTFILE| CURRENCY| CURRENT| CURRENTCHANGED| CURRENTENVIRONMENT| CURRENTLANGUAGE| CURRENTRESULTROW| CURRENTVALUE|
CURRENTWINDOW| CURSOR| DATABASE| DATABIND| DATARELATION|
DATASERVERS| DATASET| DATASETHANDLE| DATASOURCE| 
DATE| DATETIME| DATETIMETZ| DAY| DBCODEPAGE| DBCOLLATION| DBIMS| DBNAME|
DBPARAM| DBRESTRICTIONS| DBTASKID| DBTYPE| DBVERSION| DCOLOR| DDE| DEBLANK| DEBUG| DEBUGLIST| DEBUGGER|
DECIMAL| DECIMALS| DECLARE| DEFAULT| DEFAULTBUTTON| DEFAULTEXTENSION| 
DEFAULTNOXLATE| DEFAULTWINDOW| DEFERLOBFETCH| DEFINE| DEFINED|
DELETE_KW| DELETERESULTLISTENTRY| DELIMITER| DESC| DESCENDING| DESELECTION| DIALOGBOX| DIALOGHELP|
DICTIONARY| DIR| DISABLE| DISABLEAUTOZAP| DISABLED| DISCONNECT| DISPLAY| DISTINCT| DO| DOS| DOUBLE| DOWN| DROP| 
DROPDOWN| DROPDOWNLIST| DROPFILENOTIFY| DROPTARGET| DUMP| 
DYNAMIC| DYNAMICCURRENTVALUE| DYNAMICNEXTVALUE| DYNAMICFUNCTION| EACH| ECHO| EDGECHARS| EDGEPIXELS| 
EDITUNDO| EDITING| EDITOR| ELSE| EMPTY| ENABLE| ENCODE| END| ENDMOVE| ENDRESIZE| ENDROWRESIZE| ENDKEY| ENTERED|
ENTRY| EQ| ERROR| ERRORCODE| ERRORSTATUS| ESCAPE| ETIME_KW| EVENTPROCEDURE |EVENTS| EXCEPT| EXCLUSIVEID| EXCLUSIVELOCK| 
EXCLUSIVEWEBUSER| EXECUTE|
EXISTS| EXP| EXPAND| EXPANDABLE| EXPLICIT| EXPORT| EXTENDED| EXTENT| EXTERNAL| FALSE_KW| FETCH| FGCOLOR| FIELD|
FIELDS| FILE| FILEINFORMATION| FILL| FILLIN| FILTERS| FIND| FINDCASESENSITIVE| FINDER| FINDGLOBAL|
FINDNEXTOCCURRENCE| FINDPREVOCCURRENCE| FINDSELECT| FINDWRAPAROUND| FIRST| FIRSTOF| 
FITLASTCOLUMN| FIXCODEPAGE| FIXEDONLY| FLATBUTTON|
FLOAT| FOCUS| FONT| FONTTABLE| FOR| FORCEFILE| FORMINPUT| FORMAT| FORWARDS| FRAME|
FRAMECOL| FRAMEDB| FRAMEDOWN| FRAMEFIELD| FRAMEFILE| FRAMEINDEX| FRAMELINE| FRAMENAME| FRAMEROW| FRAMEVALUE| 
FREQUENCY| FROM| FROMCURRENT| FUNCTION| FUNCTIONCALLTYPE| GE| GENERATEMD5|
GET| GETATTRCALLTYPE| GETBITS| GETBUFFERHANDLE| GETBYTE| GETBYTES| GETBYTEORDER| GETCGILIST|
GETCGIVALUE| GETCODEPAGES| GETCOLLATIONS| GETCONFIGVALUE| GETDIR|
GETDOUBLE| GETFILE| GETFLOAT| GETKEYVALUE| GETLICENSE| GETLONG|
GETPOINTERVALUE| GETSHORT| GETSIZE| GETSTRING| GETUNSIGNEDSHORT|
GLOBAL| GOON| GOPENDING| GRANT| GRAPHICEDGE| GROUP|
GTHAN| HANDLE| HAVING| HEADER| HEIGHT| HELP| HELPTOPIC|
HIDE| HINT| HORIZONTAL| HOSTBYTEORDER| HTMLENDOFLINE| HTMLFRAMEBEGIN| HTMLFRAMEEND|
HTMLHEADERBEGIN| HTMLHEADEREND| HTMLTITLEBEGIN| HTMLTITLEEND| IF| IMAGE| IMAGEDOWN| IMAGEINSENSITIVE|
IMAGESIZE| IMAGESIZECHARS| IMAGESIZEPIXELS| IMAGEUP| IMPORT| IN_KW| INCREMENTEXCLUSIVEID| INDEX| INDEXHINT|
INDEXEDREPOSITION| INDICATOR| INFORMATION| INITIAL| INITIALDIR| INITIALFILTER| INITIATE| INNER| INNERCHARS| INNERLINES|
INPUT| INPUTOUTPUT| INSERT| INTEGER| INTERVAL|
INTO| IS| ISATTRSPACE| ISCODEPAGEFIXED| ISCOLUMNCODEPAGE| ISLEADBYTE| 
ISODATE| ITEM| IUNKNOWN| JOIN| JOINBYSQLDB| KBLABEL|
KEEPMESSAGES| KEEPTABORDER| KEY| KEYCODE| KEYFUNCTION| KEYLABEL| KEYS| KEYWORD| KEYWORDALL| LABEL| LABELBGCOLOR| 
LABELDCOLOR| LABELFGCOLOR| LABELFONT| LANDSCAPE|
LANGUAGES| LARGE| LARGETOSMALL| LAST| LASTEVENT| LASTOF| LASTKEY| LC| LDBNAME| 
LE| LEAVE| LEFT| LEFTALIGNED| LEFTTRIM| LENGTH| LIBRARY| LIKE| LINECOUNTER| LISTEVENTS| LISTITEMPAIRS| LISTITEMS|
LISTQUERYATTRS| LISTSETATTRS| LISTWIDGETS| LISTING| LITTLEENDIAN| LOAD| LOADPICTURE| LOBDIR| LOCKED|
LOGMANAGER| LOG| LOGICAL| LONG| LONGCHAR|
LOOKAHEAD| LOOKUP| LTHAN| 
MACHINECLASS| MAP| MARGINEXTRA| MATCHES| MAX| MAXCHARS| MAXROWS| MAXSIZE| MAXVALUE| MAXIMIZE| MAXIMUM| 
MEMBER| MEMPTR| MENU| MENUITEM| MENUBAR| MESSAGE| 
MESSAGELINE| MESSAGELINES| 
MIN| MINSIZE| MINVALUE| MINIMUM| MODULO|
MONTH| MOUSE|
MOUSEPOINTER| MPE| MTIME| MULTIPLE| MULTIPLEKEY| MUSTEXIST| NATIVE| NE| NEW| NEXT| NEXTPROMPT| NEXTVALUE| NO| NOAPPLY| 
NOARRAYMESSAGE| NOASSIGN| NOATTRLIST| NOATTRSPACE| NOAUTOVALIDATE| NOBINDWHERE| NOBOX| NOCOLUMNSCROLLING| NOCONSOLE|
NOCONVERT| NOCONVERT3DCOLORS| NOCURRENTVALUE| NODEBUG| NODRAG| NOECHO| 
NOEMPTYSPACE| NOERROR_KW| NOFILL| NOFOCUS| NOHELP| NOHIDE| 
NOINDEXHINT| NOJOINBYSQLDB| NOLABELS| NOLOBS|
NOLOCK| NOLOOKAHEAD| NOMAP| NOMESSAGE| NONE| NOPAUSE| NOPREFETCH| 
NORETURNVALUE| NORMAL| NOROWMARKERS| NOSCROLLBARVERTICAL| NOSEPARATECONNECTION| NOSEPARATORS| NOTABSTOP| NOUNDERLINE|
NOUNDO| NOVALIDATE| NOWAIT| NOWORDWRAP| NOT| NOW| NULL_KW| 
NUMALIASES| NUMCOPIES| NUMDBS| NUMENTRIES|
NUMRESULTS| NUMERIC| OBJECT| OCTETLENGTH| OF| OFF| OK| OKCANCEL| OLD| ON| ONLY| OPEN| OPSYS| OPTION|
OR| ORDER| ORDEREDJOIN|
ORDINAL| OS2| OS400| OSAPPEND| OSCOMMAND| OSCOPY| OSCREATEDIR| OSDELETE| OSDIR| OSDRIVES| OSERROR|
OSGETENV| OSRENAME| OTHERWISE| OUTER| OUTERJOIN| OUTPUT| OVERLAY| OVERRIDE| PAGE| PAGEBOTTOM| PAGENUMBER| PAGESIZE_KW|
PAGETOP| PAGEWIDTH| PAGED| PARAMETER| PARENT| PARTIALKEY| PASCAL_KW| PAUSE| PDBNAME| PERFORMANCE| PERSISTENT| PFCOLOR|
PINNABLE| PORTRAIT| POSITION| PRECISION| PREPROCESS| PRESELECT| PREV| PRIMARY| PRINTER| PRINTERSETUP| PRIVATE|
PRIVILEGES| PROCEDURECALLTYPE| PROCTEXT| PROCTEXTBUFFER| PROCHANDLE| PROCSTATUS| PROCEDURE| PROCESS| PROFILER | PROGRAMNAME|
PROGRESS| PROMPT| PROMPTFOR| PROMSGS| PROPATH| PROVERSION| PUBLIC| PUBLISH| PUT| PUTBITS| PUTBYTE| PUTBYTES|
PUTDOUBLE| PUTFLOAT| PUTKEYVALUE| PUTLONG| PUTSHORT| PUTSTRING| QUERY| QUERYCLOSE| QUERYOFFEND|
QUERYTUNING| QUESTION| QUIT| QUOTER | RINDEX| RADIOBUTTONS| RADIOSET| RANDOM| RAW| RAWTRANSFER|
RCODEINFORMATION| READ| READAVAILABLE| READEXACTNUM|
READONLY| READKEY| REAL| RECID| RECORDLENGTH| RECTANGLE| RECURSIVE| RELATIONFIELDS| RELEASE|
REPEAT| REPLACE| REPLICATIONCREATE| REPLICATIONDELETE| REPLICATIONWRITE|
REPOSITION| REPOSITIONFORWARD| REPOSITIONBACKWARD| REPOSITIONTOROW| REPOSITIONTOROWID|
REQUEST| RESULT| RETAIN| RETAINSHAPE| RETRY| RETRYCANCEL| RETURN|
RETURNTOSTARTDIR| RETURNVALUE| RETURNS| REVERSEFROM|
REVERT| REVOKE| RGBVALUE| RIGHT| RIGHTALIGNED| RIGHTTRIM| ROUND| ROW|
ROWHEIGHTCHARS| ROWHEIGHTPIXELS| ROWID| ROWOF| RULE| RUN| RUNPROCEDURE| SAVE| SAVECACHE| SAVEAS| 
SAXCOMPLETE| SAXPARSERERROR| SAXREADER| SAXRUNNING| SAXUNINITIALIZED|
SCHEMA| SCREEN| SCREENIO| SCREENLINES| SCROLL| SCROLLABLE| SCROLLBARHORIZONTAL| SCROLLBARVERTICAL|
SCROLLING| SDBNAME| SEARCH| SEARCHSELF| SEARCHTARGET| SECTION| SEEK| SELECT| SELECTION| SELECTIONLIST| SELF| SEND| 
SENDSQLSTATEMENT| SEPARATECONNECTION| SEPARATORS| SERVER| SERVERSOCKET| SESSION| SET| 
SETATTRCALLTYPE| SETBYTEORDER| SETCONTENTS| 
SETCURRENTVALUE| SETPOINTERVALUE|
SETSIZE| SETUSERID| SHARELOCK| SHARED| SHORT| SHOWSTATS| SIDELABELS| SILENT| SIMPLE| SINGLE|
SIZE| SIZECHARS| SIZEPIXELS| SKIP| SKIPDELETEDRECORD|
SLIDER| SMALLINT| SOAPHEADER| SOAPHEADERENTRYREF| SOCKET| SOME| SORT| SOURCE| SOURCEPROCEDURE|
SPACE| SQL| SQRT| START| STARTING| STARTMOVE| STARTRESIZE| STARTROWRESIZE| 
STATUS| STATUSBAR| STDCALL_KW| STRETCHTOFIT|
STOP| STOREDPROCEDURE| STREAM| STREAMIO| STRING| STRINGXREF| SUBAVERAGE| SUBCOUNT| SUBMAXIMUM| SUBMENU|
SUBMENUHELP| SUBMINIMUM| SUBTOTAL| SUBSCRIBE| SUBSTITUTE| SUBSTRING| SUM| SUMMARY| SUPER| SYSTEMDIALOG| SYSTEMHELP| 
TABLE| TABLEHANDLE| TABLENUMBER| TARGET| TARGETPROCEDURE| TEMPTABLE| TERMINAL| TERMINATE| TEXT| 
TEXTCURSOR| TEXTSEGGROW|
THEN| THISPROCEDURE| THREED| THROUGH| TICMARKS| TIME| TIMEZONE| TITLE| TO| TOOLTIP| TOP| TOROWID| TODAY| TOGGLEBOX| 
TOOLBAR|
TOPONLY| TOPIC| TOTAL| TRANSACTION| TRANSACTIONMODE| TRANSPARENT| TRAILING| TRIGGER| TRIGGERS| TRIM| TRUE_KW| 
TRUNCATE| TTCODEPAGE| UNBUFFERED| UNDERLINE| UNDO| UNFORMATTED| UNLESSHIDDEN| 
UNION| UNIQUE| UNIQUEMATCH| UNIX| UNLOAD| UNSIGNEDBYTE| UNSIGNEDSHORT| UNSUBSCRIBE| 
UP| UPDATE| URLDECODE| URLENCODE| USE| USEDICTEXPS| USEFILENAME| USEINDEX| 
USEREVVIDEO| USETEXT| USEUNDERLINE| USER| USERID| USING| 
V6FRAME| VALIDEVENT| VALIDHANDLE| VALIDATE| VALUE| VALUECHANGED| VALUES| VARIABLE| VERBOSE| VERTICAL| VIEW| VIEWAS| 
VMS| WAIT| WAITFOR| WARNING| WEBCONTEXT| WEEKDAY| WHEN| WHERE| WHILE| WIDGET| WIDGETHANDLE| WIDGETPOOL| WIDTH|
WIDTHCHARS| WIDTHPIXELS| WINDOW| WINDOWDELAYEDMINIMIZE|
WINDOWMAXIMIZED| WINDOWMINIMIZED| WINDOWNAME| WINDOWNORMAL| WITH| 
WORDINDEX| WORKTABLE| WRITE| X| XDOCUMENT| XNODEREF| XOF| XCODE| XREF| Y| YOF| YEAR| YES| YESNO| YESNOCANCEL |
// 10.0B
BASE64DECODE | BASE64ENCODE | BATCHSIZE | BEFORETABLE | COPYDATASET | COPYTEMPTABLE | 
DATASOURCEMODIFIED | DECRYPT | DELETECHARACTER | ENABLEDFIELDS | ENCRYPT | ENCRYPTIONSALT | 
FORMLONGINPUT | GENERATEPBEKEY | GENERATEPBESALT | GENERATERANDOMKEY | GETCGILONGVALUE | 
LASTBATCH | MD5DIGEST | MERGEBYFIELD | NORMALIZE | PBEHASHALGORITHM | PBEKEYROUNDS | 
PREFERDATASET | REJECTED | REPOSITIONMODE | ROWSTATE | ROWUNMODIFIED | ROWDELETED | 
ROWMODIFIED | ROWCREATED | SECURITYPOLICY | SHA1DIGEST | SSLSERVERNAME | SYMMETRICENCRYPTIONALGORITHM | 
SYMMETRICENCRYPTIONIV | SYMMETRICENCRYPTIONKEY | SYMMETRICSUPPORT | TRANSINITPROCEDURE
	;

// Newer keywords at end of list...
unreservedkeyword
	:
AACBIT | AACONTROL | AALIST | AAMEMORY | AAMSG | AAPCONTROL | AASERIAL | AATRACE |
ABSOLUTE | ACCELERATOR | ADDINTERVAL | ADVISE | ALERTBOX | ALLOWREPLICATION | ALTERNATEKEY |
ANALYZE | ANSIONLY | ANYWHERE | APPEND | 
APPLICATION | ARRAYMESSAGE | AS | ASC | ASKOVERWRITE | ASYNCHRONOUS | ATTACHMENT |
AUTOCOMPLETION | AUTOENDKEY | AUTOGO | AUTOMATIC |
AVERAGE | AVG | BACKWARDS | BASE64 | BASEKEY | BGCOLOR | BINARY | BINDWHERE |
BLOB | BOTH | BOTTOM | BROWSE | BTOS | BUFFER | 
BUFFERCHARS | BUFFERLINES | BUFFERNAME | BUTTON | BUTTONS | 
BYREFERENCE | BYVALUE | BYTE | CACHE | CACHESIZE | CANQUERY | CANSET | 
CANCELBUTTON | CAPS | CDECL_KW | CHAINED | CHARACTER | CHARACTERLENGTH | CHOOSE | CLOB | CLOSE | 
CODEBASELOCATOR | CODEPAGE | CODEPAGECONVERT | COLLATE |
COLOF | COLONALIGNED | COLORTABLE | COLUMN | COLUMNBGCOLOR | 
COLUMNCODEPAGE | COLUMNDCOLOR | COLUMNFGCOLOR | COLUMNFONT | COLUMNOF | 
COLUMNPFCOLOR | COLUMNS | COMHANDLE | COMBOBOX | COMMAND | COMPARES | COMPLETE | COMPILE | CONFIGNAME | CONNECT | 
CONTAINS | CONTENTS | CONTEXT | CONTEXTHELP | CONTEXTHELPFILE | CONTEXTHELPID | 
CONTEXTPOPUP | CONTROLFRAME | CONVERT | CONVERT3DCOLORS | COUNT | 
CREATETESTFILE | CURRENCY | CURRENTENVIRONMENT | CURRENTRESULTROW | CURRENTVALUE | 
DATABIND | DATE | DATETIME | DATETIMETZ | DAY | DBIMS | DCOLOR | DEBUG | DECIMAL | 
DEFAULTBUTTON | DEFAULTEXTENSION | DEFAULTNOXLATE | DEFERLOBFETCH |
DEFINED | DELETERESULTLISTENTRY | DESC | 
DESELECTION | DIALOGBOX | DIALOGHELP |
DIR | DISABLED | DOUBLE | DROPDOWN | DROPDOWNLIST | DROPFILENOTIFY | DROPTARGET | 
DUMP | DYNAMIC | DYNAMICCURRENTVALUE | DYNAMICNEXTVALUE |
ECHO | EDGECHARS | EDGEPIXELS | EDITUNDO | EDITOR | EMPTY | ENDMOVE | ENDRESIZE | ENDROWRESIZE | 
ENDKEY | ENTERED | EQ | ERROR | ERRORCODE | EVENTPROCEDURE | 
EVENTS | EXCLUSIVEID | EXCLUSIVEWEBUSER | EXECUTE | EXP | EXPAND | 
EXPANDABLE | EXPLICIT | EXTENDED | EXTENT | EXTERNAL | 
FGCOLOR | FILE | FILLIN | FILTERS | FINDER | FITLASTCOLUMN | FIXCODEPAGE | FIXEDONLY | 
FLATBUTTON | FLOAT | FONTTABLE | FORCEFILE | FORMINPUT | FORWARDS | FREQUENCY | FROMCURRENT | FUNCTION | 
GE | GENERATEMD5 | GET | GETBITS | GETBYTE | GETBYTES | GETBYTEORDER | GETCGILIST | 
GETCGIVALUE | GETCONFIGVALUE | GETDIR | GETDOUBLE | 
GETFILE | GETFLOAT | GETLICENSE |
GETLONG | GETPOINTERVALUE | GETSHORT | GETSIZE | GETSTRING | GETUNSIGNEDSHORT | GTHAN | HANDLE | HEIGHT |
HELPTOPIC | HINT |
HORIZONTAL | HTMLENDOFLINE | HTMLFRAMEBEGIN | HTMLFRAMEEND | HTMLHEADERBEGIN | HTMLHEADEREND | HTMLTITLEBEGIN | 
HTMLTITLEEND | IMAGE | IMAGEDOWN | IMAGEINSENSITIVE | IMAGESIZE | IMAGESIZECHARS | IMAGESIZEPIXELS | 
IMAGEUP | INCREMENTEXCLUSIVEID | INDEXHINT | INDEXEDREPOSITION | INFORMATION | INITIAL | INITIALDIR | 
INITIALFILTER | INITIATE | INNER | INNERCHARS | INNERLINES | INTEGER | INTERVAL | ITEM | 
ISCODEPAGEFIXED | ISCOLUMNCODEPAGE | ISODATE | IUNKNOWN |
JOINBYSQLDB | KEEPMESSAGES | KEEPTABORDER | 
KEY | KEYCODE | KEYFUNCTION | KEYLABEL | KEYWORDALL | LABELBGCOLOR | LABELDCOLOR | LABELFGCOLOR | LABELFONT | 
LANDSCAPE | LANGUAGES | LARGE | LARGETOSMALL | LC | LE | LEFT | 
LEFTALIGNED | LEFTTRIM | LENGTH | LISTEVENTS | LISTITEMPAIRS | 
LISTITEMS | LISTQUERYATTRS | LISTSETATTRS | LISTWIDGETS | 
LOAD | LOADPICTURE | LOBDIR | LOG | LOGICAL | LONG | LONGCHAR | LOOKAHEAD | 
LTHAN | MACHINECLASS | MARGINEXTRA | MATCHES | MAX | MAXCHARS | 
MAXROWS | MAXSIZE | MAXVALUE | MAXIMIZE | MAXIMUM | MEMPTR | MENU | 
MENUITEM | MENUBAR | MESSAGELINE |
MIN | MINSIZE | MINVALUE | MINIMUM | MODULO | MONTH | MOUSE | MOUSEPOINTER | MPE | MTIME | MULTIPLE | 
MULTIPLEKEY | MUSTEXIST | NATIVE | NE | NEXTVALUE | NOAPPLY | NOARRAYMESSAGE | NOASSIGN | NOAUTOVALIDATE | 
NOBINDWHERE | NOBOX | NOCOLUMNSCROLLING | NOCONSOLE | NOCONVERT | NOCONVERT3DCOLORS | NOCURRENTVALUE | NODEBUG | 
NODRAG | NOECHO | NOEMPTYSPACE | 
NOINDEXHINT | NOJOINBYSQLDB | NOLOOKAHEAD | NONE | NORMAL | NOROWMARKERS | NOSCROLLBARVERTICAL | 
NOSEPARATECONNECTION | NOSEPARATORS | NOTABSTOP | NOUNDERLINE | NOWORDWRAP | NUMCOPIES | NUMRESULTS | NUMERIC | 
OBJECT | OCTETLENGTH | OK | OKCANCEL | ONLY | ORDER | ORDEREDJOIN | ORDINAL |
OS2 | OS400 | OSDRIVES | OSERROR | OSGETENV | OUTER | OUTERJOIN | OVERRIDE | PAGESIZE_KW | 
PAGEWIDTH | PAGED | PARENT | PARTIALKEY | PASCAL_KW | PERFORMANCE |
PFCOLOR | PINNABLE | PORTRAIT | POSITION | PRECISION | PRESELECT | PREV | PRIMARY | 
PRINTER | PRINTERSETUP | PRIVATE | PROCTEXT | PROCTEXTBUFFER | PROCEDURE | 
PROFILER | PROMPT | PUBLIC | PUBLISH | PUTBITS | 
PUTBYTES | PUTDOUBLE | PUTFLOAT | PUTLONG | PUTSHORT | PUTSTRING | QUESTION | QUOTER | RADIOBUTTONS | RADIOSET | RANDOM | 
RAW | RAWTRANSFER | READ | 
READONLY | REAL | RECORDLENGTH | RECURSIVE | RELATIONFIELDS | REPLACE | 
REPLICATIONCREATE | REPLICATIONDELETE | REPLICATIONWRITE | REPOSITIONFORWARD | 
REQUEST | RESULT | RETAINSHAPE | RETRYCANCEL | RETURNS | RETURNTOSTARTDIR | 
RETURNVALUE | REVERSEFROM | RGBVALUE | RIGHT | RIGHTALIGNED | RIGHTTRIM | ROUND | 
ROW | ROWHEIGHTCHARS | ROWHEIGHTPIXELS | ROWID | ROWOF | RULE | RUNPROCEDURE | SAVECACHE | SAVEAS | SAXREADER | SCROLLABLE | 
SCROLLBARHORIZONTAL | SCROLLBARVERTICAL | SCROLLING | SECTION | SELECTION | SELECTIONLIST | SEND | SENDSQLSTATEMENT | 
SEPARATECONNECTION | SEPARATORS | SERVER | SERVERSOCKET | SETBYTEORDER | SETCONTENTS | SETCURRENTVALUE | 
SETPOINTERVALUE |
SETSIZE | SIDELABELS | SILENT | SIMPLE | SINGLE | SIZE | SIZECHARS | SIZEPIXELS | SHORT | SLIDER | SMALLINT | 
SOAPHEADER | SOAPHEADERENTRYREF | SOCKET | SORT | SOURCE | SOURCEPROCEDURE | 
SQL | SQRT | START | STARTING | STARTMOVE | STARTRESIZE | 
STARTROWRESIZE | STATUSBAR | STDCALL_KW | 
STRETCHTOFIT | STOP | STOREDPROCEDURE | STRING | STRINGXREF | SUBAVERAGE | SUBCOUNT | SUBMAXIMUM | SUBMENU | 
SUBMENUHELP | SUBMINIMUM | SUBTOTAL | SUBSCRIBE | SUBSTITUTE | SUBSTRING | SUM | 
SUMMARY | SUPER | SYSTEMHELP | TARGET | 
TARGETPROCEDURE | TEMPTABLE | TERMINATE | TEXTCURSOR | 
TEXTSEGGROW | THREED | THROUGH | TICMARKS | TIMEZONE | TODAY | TOGGLEBOX |
TOOLBAR | TOOLTIP | 
TOP | TOPIC | TOTAL | TRANSACTIONMODE | TRANSPARENT | TRAILING | 
TRUNCATE | TTCODEPAGE | UNBUFFERED | UNIQUEMATCH | UNLOAD | UNSIGNEDBYTE | UNSIGNEDSHORT | UNSUBSCRIBE | 
URLDECODE | URLENCODE | USE | USEDICTEXPS | USEFILENAME | 
USEREVVIDEO | USETEXT | USEUNDERLINE | USER | VALIDEVENT | VALIDHANDLE | 
VALIDATE | VARIABLE | VERBOSE | VERTICAL | VMS | 
WAIT | WARNING | WEBCONTEXT | WEEKDAY | WIDGET | WIDGETHANDLE | WIDGETPOOL | 
WIDTH | WIDTHCHARS | WIDTHPIXELS | WINDOWNAME | WORDINDEX | 
X | XDOCUMENT | XNODEREF | XOF | Y | YOF | YEAR | YESNO | YESNOCANCEL |
// 10.0B
BASE64DECODE | BASE64ENCODE | BATCHSIZE | BEFORETABLE | COPYDATASET | COPYTEMPTABLE | 
DATASOURCEMODIFIED | DECRYPT | DELETECHARACTER | ENABLEDFIELDS | ENCRYPT | ENCRYPTIONSALT | 
FORMLONGINPUT | GENERATEPBEKEY | GENERATEPBESALT | GENERATERANDOMKEY | GETCGILONGVALUE | 
LASTBATCH | MD5DIGEST | MERGEBYFIELD | NORMALIZE | PBEHASHALGORITHM | PBEKEYROUNDS | 
PREFERDATASET | REJECTED | REPOSITIONMODE | ROWSTATE
SHA1DIGEST | SSLSERVERNAME | SYMMETRICENCRYPTIONALGORITHM | 
SYMMETRICENCRYPTIONIV | SYMMETRICENCRYPTIONKEY | SYMMETRICSUPPORT | TRANSINITPROCEDURE
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
			(keyword | ID)
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


