/*

treeparser01.g - Primary tree walker.

This tree parser adds base attributes to the tree, such as
name resolution, scoping, etc.

To find actions taken within this grammar, search for "action.",
which is the tree parser action object.

Copyright (C) 2001-2006 Joanju Software (www.joanju.com)
All rights reserved. This program and the accompanying materials 
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html

*/


header {
	package org.prorefactor.treeparser01;

	import org.prorefactor.core.IJPNode;
	import org.prorefactor.treeparser.CQ;
	import org.prorefactor.treeparser.IJPTreeParser;
	
	import java.util.ArrayList;
}


options {
        language = "Java";
}


// Class preamble - anything here gets inserted in front of the class definition.
{
} // Class preamble


// class definition options
class TreeParser01 extends JPTreeParser;
options {
	importVocab = ProParser;
	defaultErrorHandler = false;
	classHeaderSuffix = IJPTreeParser;
}



// This is added to top of the class definitions
{

	// --- The following are required in all tree parsers ---

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


	// --- The above are for all tree parsers, below are for TreeParser01 ---


	/** Create a tree parser with a specific action object. */
	public TreeParser01(TP01Action actionObject) {
		action = actionObject;
	}
	
	/** By default, the action object is a new TP01Support. */
	TP01Action action = null; // See initialization block, below.

	// Initialization block
	{
		if (action==null) action = new TP01Support();
	}

	/** Get the action object. getActionObject and getTpSupport are identical. */
	public TP01Action getActionObject() { return action; }

	/** Set the action object.
	 * By default, the support object is a new TP01Support,
	 * but you can configure this to be any TP01Action object.
	 * setTpSupport and setActionObject are identical.
	 */
	public void setActionObject(TP01Action action) { this.action = action; }


	/** This tree parser's stack. I think it is best to keep the stack
	 * in the tree parser grammar for visibility sake, rather than hide
	 * it in the support class. If we move grammar and actions around
	 * within this .g, the effect on the stack should be highly visible.
	 */	
	private ArrayList stack = new ArrayList();
	private void push(Object o) { stack.add(o); }
	private Object pop() { return stack.remove(stack.size()-1); }


} // end of what's added to the top of the class definition



///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin grammar
///////////////////////////////////////////////////////////////////////////////////////////////////


program
	:	#(	p:Program_root {action.programRoot(#p);}
			(blockorstate)*
			Program_tail
			{action.programTail();}
		)
	;


block_for
	:	#(	FOR rn1:tbl[CQ.BUFFERSYMBOL] {action.strongScope(#rn1);}
			(COMMA rn2:tbl[CQ.BUFFERSYMBOL] {action.strongScope(#rn2);} )*
		)
	;
block_opt
	:	#(Block_iterator fld[CQ.UPDATING] EQUAL expression TO expression (BY constant)? )
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
	:	#(PRESELECT for_record_spec[CQ.INITWEAK] )
	;

functioncall
	:	#(ACCUMULATE accum_what (#(BY expression (DESCENDING)?))? expression )
	|	#(ADDINTERVAL LEFTPAREN expression COMMA expression COMMA expression RIGHTPAREN )
	|	#(AUDITENABLED LEFTPAREN (expression)? RIGHTPAREN )
	|	canfindfunc // has extra "action." support handling in this tree parser.
	|	#(CAST LEFTPAREN field COMMA TYPE_NAME RIGHTPAREN )
	|	currentvaluefunc // is also a pseudfn.
	|	dynamiccurrentvaluefunc // is also a pseudfn.
	|	#(DYNAMICFUNCTION LEFTPAREN expression (#(IN_KW expression))? (COMMA parameter)* RIGHTPAREN (NOERROR_KW)? )
		// ENTERED and NOTENTERED are only dealt with as part of an expression term. See: exprt.
	|	entryfunc // is also a pseudfn.
	|	#(ETIME_KW (funargs)? )
	|	#(EXTENT LEFTPAREN fld[CQ.SYMBOL] RIGHTPAREN )
	|	#(FRAMECOL (LEFTPAREN ID RIGHTPAREN)? )
	|	#(FRAMEDOWN (LEFTPAREN ID RIGHTPAREN)? )
	|	#(FRAMELINE (LEFTPAREN ID RIGHTPAREN)? )
	|	#(FRAMEROW (LEFTPAREN ID RIGHTPAREN)? )
	|	#(GETCODEPAGES (funargs)? )
	|	#(GUID LEFTPAREN (expression)? RIGHTPAREN )
	|	#(IF expression THEN expression ELSE expression )
	|	ldbnamefunc 
	|	lengthfunc // is also a pseudfn.
	|	#(LINECOUNTER (LEFTPAREN ID RIGHTPAREN)? )
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

recordfunargs
	:	(LEFTPAREN tbl[CQ.REF] RIGHTPAREN | tbl[CQ.REF])
	;

parameter
	:	#(BUFFER tbl[CQ.INIT])
	|	#(PARAMETER expression EQUAL expression )
	|	#(OUTPUT parameter_arg )
	|	#(INPUTOUTPUT parameter_arg )
	|	#(INPUT parameter_arg )
	;
parameter_arg
	:	(	TABLEHANDLE fld[CQ.INIT] parameter_dataset_options
		|	TABLE (FOR)? tbl[CQ.TEMPTABLESYMBOL] parameter_dataset_options
		|	DATASET ID parameter_dataset_options
		|	DATASETHANDLE fld[CQ.INIT] parameter_dataset_options
		|	ID AS (	CLASS TYPE_NAME | datatype_com | datatype_var )
		|	expression (AS datatype_com)?
		)
		(BYPOINTER|BYVARIANTPOINTER)?
	;

filenameorvalue 
	:	#(VALUE LEFTPAREN exp:expression RIGHTPAREN ) { action.fnvExpression(#exp); }
	|	fn:FILENAME { action.fnvFilename(#fn); }
	;

exprt
	:	#(LEFTPAREN expression RIGHTPAREN )
	|	constant
	|	widattr
	|	#(USER_FUNC parameterlist_noroot )
	|	#(LOCAL_METHOD_REF parameterlist_noroot )
	|	( #(NEW TYPE_NAME) )=> #(NEW TYPE_NAME parameterlist )
	|	// SUPER is amibiguous between functioncall and systemhandlename
		(	options{generateAmbigWarnings=false;}
		:	functioncall
		|	systemhandlename
		)
	|	fld[CQ.REF]
	|	#(Entered_func fld[CQ.SYMBOL] (NOT)? ENTERED )
	|	tbl[CQ.REF] // for DISPLAY buffername, etc.
	;

s_widget
	:	widname	| fld[CQ.REF]
	;

widname
	:	systemhandlename
	|	DATASET ID
	|	DATASOURCE ID
	|	FIELD fld[CQ.REF]
	|	FRAME f:ID { action.frameRef(#f); }
	|	MENU ID
	|	SUBMENU ID
	|	MENUITEM ID
	|	BROWSE b:ID  { action.browseRef(#b); }
	|	QUERY ID
	|	TEMPTABLE ID
	|	BUFFER ID
	|	XDOCUMENT ID
	|	XNODEREF ID
	|	SOCKET ID
	;

tbl[int contextQualifier]
	:	id:RECORD_NAME {action.recordNameNode(#id, contextQualifier);}
	;

// The only difference between fld and fld1 is that fld1 passes 1 to
// field(), telling it that this field can only be a member of the last
// referenced table. fld2 indicates that this must be a field of the *previous*
// referenced table.
fld[int contextQualifier]
	:	#(ref:Field_ref (INPUT)? (frame_ref|browse_ref)? id:ID (array_subscript)? )
		// Note that sequence is important. This must be called after the full Field_ref branch has
		// been walked, because any frame or browse ID must be resolved before trying to resolve Field_ref.
		// (For example, this is required for resolving if the INPUT function was used.)
		{action.field(#ref, #id, contextQualifier, 0);}
	;
fld1[int contextQualifier]
	:	#(ref:Field_ref (INPUT)? (frame_ref|browse_ref)? id:ID (array_subscript)? )
		{action.field(#ref, #id, contextQualifier, 1);}
	;
fld2[int contextQualifier]
	:	#(ref:Field_ref (INPUT)? (frame_ref|browse_ref)? id:ID (array_subscript)? )
		{action.field(#ref, #id, contextQualifier, 2);}
	;


//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
//                   begin PROGRESS syntax features, in alphabetical order
//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////


aggregate_opt

	// It appears that the compiler treats COUNT, MAX, TOTAL, etc as new variables.
	// TODO: To get an accurrate datatype for things like MAXIMUM, we would have to work out
	// the datatype of the expression being accumulated.
	:	#(id1:AVERAGE (label_constant)? {action.addToSymbolScope(action.defineVariable(#id1, #id1, DECIMAL));} )
	|	#(id2:COUNT (label_constant)? {action.addToSymbolScope(action.defineVariable(#id2, #id2, INTEGER));} )
	|	#(id3:MAXIMUM (label_constant)? {action.addToSymbolScope(action.defineVariable(#id3, #id3, DECIMAL));} )
	|	#(id4:MINIMUM (label_constant)? {action.addToSymbolScope(action.defineVariable(#id4, #id4, DECIMAL));} )
	|	#(id5:TOTAL (label_constant)? {action.addToSymbolScope(action.defineVariable(#id5, #id5, DECIMAL));} )
	|	#(id6:SUBAVERAGE (label_constant)? {action.addToSymbolScope(action.defineVariable(#id6, #id6, DECIMAL));} )
	|	#(id7:SUBCOUNT (label_constant)? {action.addToSymbolScope(action.defineVariable(#id7, #id7, DECIMAL));} )
	|	#(id8:SUBMAXIMUM (label_constant)? {action.addToSymbolScope(action.defineVariable(#id8, #id8, DECIMAL));} )
	|	#(id9:SUBMINIMUM (label_constant)? {action.addToSymbolScope(action.defineVariable(#id9, #id9, DECIMAL));} )
	|	#(id10:SUBTOTAL (label_constant)? {action.addToSymbolScope(action.defineVariable(#id10, #id10, DECIMAL));} )
	;

assignment_list
	:	tbl[CQ.UPDATING] (#(EXCEPT (fld1[CQ.SYMBOL])*))?
	|	(	assign_equal (#(WHEN expression))?
		|	#(	Assign_from_buffer fld[CQ.UPDATING]	)
			(#(WHEN expression))?
		)*
	;
assign_equal
	:	#(EQUAL
			(	pseudfn
			|	fld[CQ.UPDATING]
			)
			expression
		)
	;

referencepoint
	:	fld[CQ.SYMBOL] ((PLUS|MINUS) expression)?
	;

browse_ref
	:	#(BROWSE i:ID) { action.frameRef(#i); }
	;

buffercomparestate
	:	#(	BUFFERCOMPARE
			tbl[CQ.REF]
			(	#(EXCEPT (fld1[CQ.SYMBOL])*)
			|	#(USING (fld1[CQ.REF])+)
			)?
			TO tbl[CQ.REF]
			(CASESENSITIVE|BINARY)?
			( #(SAVE ( #(RESULT IN_KW) )? fld[CQ.UPDATING] ) )?
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
	:	#(	BUFFERCOPY tbl[CQ.REF]
			(	#(EXCEPT (fld1[CQ.SYMBOL])*)
			|	#(USING (fld1[CQ.REF])+)
			)?
			TO tbl[CQ.UPDATING]
			( #(ASSIGN assignment_list ) )?
			(NOLOBS)?
			(NOERROR_KW)?
			state_end 
		)
	;

canfindfunc
	:	#(	cf:CANFIND LEFTPAREN (findwhich)?
			#(	r:RECORD_NAME
				{	action.canFindBegin(#cf, #r);
					action.recordNameNode(#r, CQ.INIT);
				}
				recordphrase
				{action.canFindEnd(#cf);}
			)
			RIGHTPAREN
		)
	;
 

choosestate
	:	#(	head:CHOOSE (ROW|FIELD)  { action.frameInitializingStatement(#head); }
			( #(fi:Form_item fld[CQ.UPDATING] {action.formItem(#fi);} (#(HELP constant))? ) )+
			(	AUTORETURN 
			|	#(COLOR anyorvalue) 
			|	goonphrase
			|	#(KEYS fld[CQ.UPDATING] )
			|	NOERROR_KW 
			|	#(PAUSE expression)
			)*
			(framephrase)?
			state_end  { action.frameStatementEnd(); }
		)
	;

classstate
	:	#(	c:CLASS {action.classState(#c);}
			TYPE_NAME
			( #(INHERITS TYPE_NAME) )?
			( #(IMPLEMENTS TYPE_NAME (COMMA TYPE_NAME)* ) )?
			( FINAL )?
			block_colon
			code_block
			#(END (CLASS)? )
			state_end
		)
	;

clearstate
	:	#(c:CLEAR (frame_ref)? (ALL)? (NOPAUSE)? state_end {action.clearState(#c);} )
	;

closestoredprocedurestate
	:	#(	CLOSE
			STOREDPROCEDURE ID
			( #(EQUAL fld[CQ.REF] PROCSTATUS ) )?
			( #(WHERE PROCHANDLE EQ fld[CQ.REF] ) )?
			state_end
		)
	;

colorstate
	:	#(	head:COLOR  { action.frameInitializingStatement(#head); }
			(	( #(DISPLAY anyorvalue) | #(PROMPT anyorvalue) )
				( #(DISPLAY anyorvalue) | #(PROMPT anyorvalue) )?
			)?
			(	#(	fi:Form_item fld[CQ.SYMBOL]
					// formItem() must be called after fld[], but before formatphrase.
					{action.formItem(#fi);}  (formatphrase)?
				)
			)*
			(framephrase)? state_end  { action.frameStatementEnd(); }
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
			|	#(LEXAT fld[CQ.SYMBOL] (columnformat)? )
			|	#(WIDTH NUMBER )
			|	#(WIDTHPIXELS NUMBER )
			|	#(WIDTHCHARS NUMBER )
			)+ 
		)
	;

constructorstate
	:	#(	c:CONSTRUCTOR
			{action.scopeAdd(#c);}
			(PUBLIC|PROTECTED) TYPE_NAME function_params
			block_colon code_block #(END (CONSTRUCTOR)? ) state_end
			{action.scopeClose(#c);}
		)
	;
	
createstate
	:	#(CREATE tbl[CQ.UPDATING] (#(USING (ROWID|RECID) expression))? (NOERROR_KW)? state_end )
	;

create_whatever_args
	:	fld[CQ.UPDATING] (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)?
	;

createautomationobjectstate
	:	#(CREATE QSTRING fld[CQ.UPDATING] (#(CONNECT (#(TO expression))?))? (NOERROR_KW)? state_end )
	;

createbrowsestate
	:	#(CREATE BROWSE fld[CQ.UPDATING] (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? (assign_opt)? (triggerphrase)? state_end )
	;

createbufferstate
	:	#(	CREATE BUFFER fld[CQ.UPDATING] FOR TABLE expression
			( #(BUFFERNAME expression) )?
			(#(IN_KW WIDGETPOOL expression))?
			(NOERROR_KW)? state_end
		)
	;

createserverstate
	:	#(CREATE SERVER fld[CQ.UPDATING] (assign_opt)? state_end )
	;

createserversocketstate
	:	#(CREATE SERVERSOCKET fld[CQ.UPDATING] (NOERROR_KW)? state_end )
	;

createsocketstate
	:	#(CREATE SOCKET fld[CQ.UPDATING] (NOERROR_KW)? state_end )
	;

createtemptablestate
	:	#(CREATE TEMPTABLE fld[CQ.UPDATING] (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
	;

createwidgetstate
	:	#(	CREATE
			(	valueexpression
			|	BUTTON | COMBOBOX | CONTROLFRAME | DIALOGBOX | EDITOR | FILLIN | FRAME | IMAGE
			|	MENU | MENUITEM | RADIOSET | RECTANGLE | SELECTIONLIST | SLIDER
			|	SUBMENU | TEXT | TOGGLEBOX | WINDOW
			)
			fld[CQ.UPDATING]
			(#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? (assign_opt)? (triggerphrase)? state_end
		)
	;

ddegetstate
	:	#(DDE GET expression TARGET fld[CQ.UPDATING] ITEM expression (#(TIME expression))? (NOERROR_KW)? state_end )
	;

ddeinitiatestate
	:	#(DDE INITIATE fld[CQ.UPDATING] FRAME expression APPLICATION expression TOPIC expression (NOERROR_KW)? state_end )
	;

dderequeststate
	:	#(DDE REQUEST expression TARGET fld[CQ.UPDATING] ITEM expression (#(TIME expression))? (NOERROR_KW)? state_end )
	;

definebrowsestate
	:	#(	def:DEFINE (def_shared)? (def_visib)? BROWSE
			id:ID { push(action.defineBrowse(#def, #id)); }
			(#(QUERY ID))? (lockhow|NOWAIT)*
			(	#(	DISPLAY
					(	#(	fi1:Form_item
							(	(tbl[CQ.INIT])=> tbl[CQ.INIT]
							|	expression (columnformat)?
							|	spacephrase
							)
							// Note for DISPLAY, formItem() is called *after* any potential format '@' phrase.
							{ action.formItem(#fi1); }
						)
					)*
					(#(EXCEPT (fld1[CQ.SYMBOL])*))?
				)
				(	#(	ENABLE
						(	#(ALL (#(EXCEPT (fld[CQ.SYMBOL])*))? )
						|	(	#(	fi2:Form_item fld[CQ.SYMBOL]  { action.formItem(#fi2); }
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
			{ action.addToSymbolScope(pop()); }
		)
	;

definebufferstate
	:	#(	def:DEFINE (def_shared)? (def_visib)? BUFFER id:ID
			FOR (TEMPTABLE)? rec:tbl[CQ.SYMBOL]
			{ action.defineBuffer(#def, #id, #rec, false); }
			(PRESELECT)? (label_constant)?
			(namespace_uri)? (namespace_prefix)?
			(#(FIELDS (fld1[CQ.SYMBOL])* ))? state_end
		)
	;

definebuttonstate
	:	#(	def:DEFINE (def_shared)? (def_visib)? BUTTON 
			id:ID { push(action.defineSymbol(BUTTON, #def, #id)); }
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
			|	#(LIKE fld[CQ.SYMBOL] (VALIDATE)?)
			|	FLATBUTTON
			|	#(NOFOCUS (FLATBUTTON)? )
			|	NOCONVERT3DCOLORS
			|	tooltip_expr
			|	sizephrase (MARGINEXTRA)?
			)*
			(triggerphrase)?
			state_end
			{ action.addToSymbolScope(pop()); }
		)
	;

definedatasetstate
	:	#(	def:DEFINE (def_shared)? (def_visib)? DATASET
			id:ID { push(action.defineSymbol(DATASET, #def, #id)); }
			(namespace_uri)? (namespace_prefix)? (REFERENCEONLY)?
			FOR tbl[CQ.INIT] (COMMA tbl[CQ.INIT])*
			( data_relation ( (COMMA)? data_relation)* )?
			state_end
			{ action.addToSymbolScope(pop()); }
		)
	;
data_relation
	:	#(	DATARELATION (ID)? FOR tbl[CQ.INIT] COMMA tbl[CQ.INIT]
			(field_mapping_phrase)? (REPOSITION)?
		)
	;
field_mapping_phrase
	:	#(RELATIONFIELDS LEFTPAREN fld2[CQ.SYMBOL] COMMA fld1[CQ.SYMBOL]
		( COMMA fld2[CQ.SYMBOL] COMMA fld1[CQ.SYMBOL] )* RIGHTPAREN )
	;

definedatasourcestate
	:	#(	def:DEFINE (def_shared)? (def_visib)? DATASOURCE
			id:ID { push(action.defineSymbol(DATASOURCE, #def, #id)); }
			FOR (#(QUERY ID))?
			(source_buffer_phrase)? (COMMA source_buffer_phrase)*
			state_end
			{ action.addToSymbolScope(pop()); }
		)
	;
source_buffer_phrase
	:	#(	r:RECORD_NAME {action.recordNameNode(#r, CQ.INIT);}
			( KEYS LEFTPAREN ( ROWID | fld[CQ.SYMBOL] (COMMA fld[CQ.SYMBOL])* ) RIGHTPAREN )?
		)
	;

defineframestate
	:	#(	def:DEFINE (def_shared)?
			// Note that frames cannot be inherited. If that ever changes, then things will get tricky
			// when creating the symbol tables for inheritance caching. See Frame.copyBare(), and the
			// attributes of Frame that it does not deal with.
			(PRIVATE)?  // important: see note above.
			FRAME
			id:ID { action.frameDef(#def, #id); }
			(form_item[CQ.SYMBOL])*
			(	#(HEADER (display_item)+ )
			|	#(BACKGROUND (display_item)+ )
			)?
			(#(EXCEPT (fld1[CQ.SYMBOL])*))?  (framephrase)?  state_end  { action.frameStatementEnd(); }
			// Frames are automatically and immediately added to the SymbolScope. No need to do it here.
		)
	;

defineimagestate
	:	#(	def:DEFINE (def_shared)? (def_visib)? IMAGE
			id:ID { push(action.defineSymbol(IMAGE, #def, #id)); }
			(	#(LIKE fld[CQ.SYMBOL] (VALIDATE)?)
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
			{ action.addToSymbolScope(pop()); }
		)
	;

definemenustate
	:	#(	def:DEFINE (def_shared)? (def_visib)? MENU
			id:ID { push(action.defineSymbol(MENU, #def, #id)); }
			(menu_opt)* (menu_list_item)* state_end
			{ action.addToSymbolScope(pop()); }
		)
	;
menu_opt
	:	color_expr
	|	#(FONT expression)
	|	#(LIKE fld[CQ.SYMBOL] (VALIDATE)?)
	|	#(TITLE expression)
	|	MENUBAR
	|	PINNABLE
	|	SUBMENUHELP
	;
menu_list_item
	:	(	#(	MENUITEM
				id:ID { push(action.defineSymbol(MENUITEM, #id, #id)); }
				(	#(ACCELERATOR expression )
				|	color_expr
				|	DISABLED
				|	#(FONT expression)
				|	label_constant
				|	READONLY
				|	TOGGLEBOX
				)*
				(triggerphrase)? 
				{ action.addToSymbolScope(pop()); }
			)
		|	#(	SUBMENU
				id2:ID { push(action.defineSymbol(SUBMENU, #id2, #id2)); }
				(DISABLED | label_constant | #(FONT expression) | color_expr)*
				{ action.addToSymbolScope(pop()); }
			)
		|	#(RULE (#(FONT expression) | color_expr)* )
		|	SKIP
		)
		// You can have PERIOD between menu items.
		((PERIOD (RULE|SKIP|SUBMENU|MENUITEM))=> PERIOD)?
	;

defineparameterstate
	:	#(	def:DEFINE (def_shared)? (def_visib)?
			(	PARAMETER BUFFER bid:ID FOR brec:tbl[CQ.SYMBOL]
				{action.defineBuffer(#def, #bid, #brec, true);}
				(PRESELECT)? (label_constant)? (#(FIELDS (fld1[CQ.SYMBOL])* ))?
			|	(INPUT|OUTPUT|INPUTOUTPUT|RETURN) PARAMETER
				(	TABLE FOR tbl[CQ.TEMPTABLESYMBOL] (APPEND|BIND)*
				|	TABLEHANDLE (FOR)? id:ID (APPEND|BIND)*
					{ action.addToSymbolScope(action.defineVariable(#def, #id, HANDLE)); }
				|	DATASET FOR ds:ID (APPEND|BYVALUE|BIND)*
					{ action.addToSymbolScope(action.defineSymbol(DATASET, #ds, #ds)); }
				|	DATASETHANDLE id3:ID (APPEND|BYVALUE|BIND)*
					{ action.addToSymbolScope(action.defineVariable(#def, #id3, HANDLE)); }
				|	id2:ID { push(action.defineVariable(#def, #id2)); }
					defineparam_var (triggerphrase)?
					{ action.addToSymbolScope(pop()); }
				)
			)
			state_end
		)
	;
defineparam_var
	:	(	#(	as:AS
				(	(HANDLE (TO)? datatype_dll)=> HANDLE (TO)? datatype_dll
				|	CLASS TYPE_NAME
				|	datatype_param
				)
			)
			{action.defAs(#as);}
		)?
		(	options{greedy=true;}
		:	casesens_or_not | #(FORMAT expression) | #(DECIMALS expression )
		|	#(li:LIKE fld[CQ.SYMBOL] (VALIDATE)?) {action.defLike(#li);}
		|	initial_constant | label_constant | NOUNDO | extentphrase
		)*
	;

definequerystate
	:	#(	def:DEFINE (def_shared)? (def_visib)? QUERY
			id:ID { push(action.defineSymbol(QUERY, #def, #id)); }
			FOR tbl[CQ.INIT] (record_fields)?
			(COMMA tbl[CQ.INIT] (record_fields)?)*
			( #(CACHE expression) | SCROLLING | RCODEINFORMATION)*
			state_end
		)
		{ action.addToSymbolScope(pop()); }
	;

definerectanglestate
	:	#(	def:DEFINE (def_shared)? (def_visib)? RECTANGLE
			id:ID { push(action.defineSymbol(RECTANGLE, #def, #id)); }
			(	NOFILL
			|	#(EDGECHARS expression )
			|	#(EDGEPIXELS expression )
			|	color_expr
			|	GRAPHICEDGE
			|	#(LIKE fld[CQ.SYMBOL] (VALIDATE)?)
			|	sizephrase
			|	tooltip_expr
			)*
			(triggerphrase)?
			state_end
		)
		{ action.addToSymbolScope(pop()); }
	;

definestreamstate
	:	#(	def:DEFINE (def_shared)? (def_visib)? STREAM id:ID state_end )
		{ action.addToSymbolScope(action.defineSymbol(STREAM, #def, #id)); }
	;

definesubmenustate
	:	#(	def:DEFINE (def_shared)? (def_visib)? SUBMENU
			id:ID { push(action.defineSymbol(SUBMENU, #def, #id)); }
			(menu_opt)* (menu_list_item)* state_end
		)
		{ action.addToSymbolScope(pop()); }
	;
   
definetemptablestate
	:	#(	def:DEFINE (def_shared)? (def_visib)? TEMPTABLE id:ID
			{	action.defineTemptable(#def, #id); }
			(UNDO|NOUNDO)?
			(namespace_uri)? (namespace_prefix)?
			(REFERENCEONLY)?
			(def_table_like)?
			(label_constant)?
			(	#(	BEFORETABLE bt:ID
					{	action.defineBuffer(#bt, #bt, #id, false); }
				)
			)?
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
	:	#(	LIKE rec:tbl[CQ.SYMBOL] (VALIDATE)?
			( #(USEINDEX ID ((AS|IS) PRIMARY)? ) )*
		)
		{ action.defineTableLike(#rec); }
	;
def_table_field
	:	#(	FIELD id:ID
			{ action.defineTableField(#id); }
			(fieldoption)*
		)
	;
   
defineworktablestate
	:	#(	def:DEFINE (def_shared)? (def_visib)? WORKTABLE id:ID
			{	action.defineWorktable(#def, #id); }
			(NOUNDO)? (def_table_like)? (label_constant)? (def_table_field)* state_end
		)
	;

definevariablestate
	:	#(	def:DEFINE (def_shared)? (def_visib)? VARIABLE
			id:ID { push(action.defineVariable(#def, #id)); }
			(fieldoption)* (triggerphrase)? state_end
		)
		{ action.addToSymbolScope(pop()); }
	;

deletestate
	:	#(DELETE_KW tbl[CQ.UPDATING] (#(VALIDATE funargs))? (NOERROR_KW)? state_end )
	;

destructorstate
	:	#(	d:DESTRUCTOR 
			{action.scopeAdd(#d);}
			PUBLIC TYPE_NAME LEFTPAREN RIGHTPAREN block_colon
			code_block #(END (DESTRUCTOR)? ) state_end
			{action.scopeClose(#d);}
		)
	;
	
disablestate
	:	#(	head:DISABLE  { action.frameInitializingStatement(#head); }
			(UNLESSHIDDEN)? (#(ALL (#(EXCEPT (fld[CQ.SYMBOL])*))?) | (form_item[CQ.SYMBOL])+)? (framephrase)?
			state_end  { action.frameStatementEnd(); }
		)
	;

disabletriggersstate
	:	#(DISABLE TRIGGERS FOR (DUMP|LOAD) OF tbl[CQ.SYMBOL] (ALLOWREPLICATION)? state_end )
	;

disconnectstate
	:	#(DISCONNECT filenameorvalue (NOERROR_KW)? state_end )
	;

displaystate
	:	#(	head:DISPLAY  { action.frameInitializingStatement(#head); }
			(stream_name)? (UNLESSHIDDEN)? (displaystate_item)*
			(#(EXCEPT (fld1[CQ.SYMBOL])*))? (#(IN_KW WINDOW expression))?
			(display_with)*
			(NOERROR_KW)?
			state_end  { action.frameStatementEnd(); }
		)
	;
displaystate_item
	:	#(	fi:Form_item
			(	skipphrase
			|	spacephrase
			|	(expression|ID) (aggregatephrase|formatphrase)*
				// Note for the DISPLAY statement, formItem() is called *after* any potential formatphrase '@' phrase.
				{ action.formItem(#fi); }
			)
		)
	;

display_item
// In TP01, this is used by lots of statements, but not actually by the DISPLAY statement. See displaystate_item above.
	:	#(	fi:Form_item
			(	skipphrase
			|	spacephrase
			|	(expression|ID)
				// For everything except DISPLAY, the call to formItem() must happen *before* formatphrase (but after fld[]).
				{action.formItem(#fi);}  (aggregatephrase|formatphrase)*
			)
		)
	;

dostate
	:	#(	d:DO
			{	action.blockBegin(#d);
				action.frameBlockCheck(#d);
			}
			(block_for)? (block_preselect)? (block_opt)* block_colon {action.frameStatementEnd();}
			code_block block_end {action.blockEnd();}
		)
	;

downstate
	:	#(	head:DOWN  { action.frameInitializingStatement(#head); }
			((stream_name (expression)?) | (expression (stream_name)?))? (framephrase)?
			state_end  { action.frameStatementEnd(); }
		)
	;

emptytemptablestate
	:	#(EMPTY TEMPTABLE tbl[CQ.TEMPTABLESYMBOL] (NOERROR_KW)? state_end )
	;

enablestate
	:	#(	head:ENABLE  { action.frameInitializingStatement(#head); }
			(UNLESSHIDDEN)? (#(ALL (#(EXCEPT (fld[CQ.SYMBOL])*))?) | (form_item[CQ.SYMBOL])+)?
			(#(IN_KW WINDOW expression))? (framephrase)? state_end  { action.frameStatementEnd(); }
		)
	;

exportstate
	:	#(EXPORT (stream_name)? (#(DELIMITER constant))? (display_item)* (#(EXCEPT (fld1[CQ.SYMBOL])*))? (NOLOBS)? state_end )
	;

fieldoption
	:	#(as:AS
			(	CLASS TYPE_NAME
			|	datatype_field
			)
		)
		{action.defAs(#as);}
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
	|	#(li:LIKE fld[CQ.SYMBOL] (VALIDATE)? ) {action.defLike(#li);}
	|	#(MOUSEPOINTER expression )
	|	NOUNDO
	|	viewasphrase
	|	TTCODEPAGE
	|	xml_data_type
	|	xml_node_type
	;

findstate
	:	#(	FIND (findwhich)?
			#(	r:RECORD_NAME
				{action.recordNameNode(#r, CQ.INIT);}
				recordphrase
			)
			(NOWAIT|NOPREFETCH|NOERROR_KW)* state_end
		)
	;

fixcodepage_pseudfn
	:	#(FIXCODEPAGE LEFTPAREN fld[CQ.SYMBOL] RIGHTPAREN )
	;

forstate
	:	#(	f:FOR 
			{	action.blockBegin(#f); 
				action.frameBlockCheck(#f);
			}
			for_record_spec[CQ.INITWEAK] (block_opt)* block_colon {action.frameStatementEnd();}
			code_block block_end {action.blockEnd();}
		)
	;
for_record_spec[int contextQualifier]
	// Also used in PRESELECT
	:	(findwhich)?
		#(	rp1:RECORD_NAME
			{action.recordNameNode(#rp1, contextQualifier);}
			recordphrase
		)
		(	COMMA (findwhich)?
			#(	rp2:RECORD_NAME
				{action.recordNameNode(#rp2, contextQualifier);}
				recordphrase
			)
		)*
	;

form_item[int contextQualifier]
{	int tblQualifier = contextQualifier;
	if (contextQualifier==CQ.SYMBOL) tblQualifier = CQ.BUFFERSYMBOL;
}
	:	#(	fi:Form_item
			(	tbl[tblQualifier]  {action.formItem(#fi);}
			|	#(TEXT LEFTPAREN (form_item[contextQualifier])* RIGHTPAREN )
			|	constant (formatphrase)?
			|	spacephrase
			|	skipphrase
			|	widget_id
			|	CARET
			|	// formItem() must be called after fld[], but before formatphrase.
				fld[contextQualifier] {action.formItem(#fi);} (aggregatephrase|formatphrase)*
			|	assign_equal
			)
		)
	;

formstate
	:	#(	head:FORMAT  { action.frameInitializingStatement(#head); }
			(form_item[CQ.SYMBOL])*
			(	#(HEADER (display_item)+ )
			|	#(BACKGROUND (display_item)+ )
			)?
			(#(EXCEPT (fld1[CQ.SYMBOL])*))?
			(framephrase)?
			state_end  { action.frameStatementEnd(); }
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
			|	#(LEXAT fld[CQ.SYMBOL] (formatphrase)? )
			|	#(LIKE fld[CQ.SYMBOL] )
			|	NOLABELS
			|	NOTABSTOP 
			|	#(VALIDATE funargs)
			|	#(WHEN expression)
			|	viewasphrase 
			)+
		)
	;

frame_ref
	:	#(FRAME f:ID) { action.frameRef(#f); }
	;

framephrase
	:	#(	WITH
			(	#(ACCUMULATE (expression)? )
			|	ATTRSPACE | NOATTRSPACE
			|	#(CANCELBUTTON fld[CQ.SYMBOL] )
			|	CENTERED 
			|	#(COLUMN expression )
			|	CONTEXTHELP | CONTEXTHELPFILE expression
			|	#(DEFAULTBUTTON fld[CQ.SYMBOL] )
			|	EXPORT
			|	FITLASTCOLUMN
			|	#(FONT expression )
			|	FONTBASEDLAYOUT
			|	frame_ref
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
			|	widget_id
			|	WITH
			)*
		)
	;

functionstate
	:	#(	f:FUNCTION id:ID
			{	action.scopeAdd(#f); }	
			(RETURNS|RETURN)?
			( CLASS TYPE_NAME | datatype_var ) 
			(PRIVATE)?
			( function_params )?
			(	FORWARDS (LEXCOLON|PERIOD|EOF) {action.funcForward(#id);}
			|	(IN_KW SUPER)=> IN_KW SUPER (LEXCOLON|PERIOD|EOF)  {action.funcForward(#id);}
			|	(MAP (TO)? ID)? IN_KW expression (LEXCOLON|PERIOD|EOF)  {action.funcForward(#id);}
			|	block_colon {action.funcDef(#f, #id);} code_block
				(	EOF
				|	#(END (FUNCTION)? ) state_end
				)
			)
		)
		{	action.scopeClose(#f); }
	;
function_param
	:	#(BUFFER (id:ID)? FOR rec:tbl[CQ.SYMBOL] (PRESELECT)? )
		{ action.defineBuffer(#id, #id, #rec, true); }
	|	#(INPUT function_param_arg )
	|	#(OUTPUT function_param_arg )
	|	#(INPUTOUTPUT function_param_arg )
	;
function_param_arg
	:	TABLE (FOR)? tbl[CQ.TEMPTABLESYMBOL] (APPEND)? (BIND)?
	|	TABLEHANDLE (FOR)? id2:ID (APPEND)? (BIND)?
		{ action.addToSymbolScope(action.defineVariable(#id2, #id2, HANDLE)); }
	|	DATASET ds:ID (APPEND)? (BIND)?
		{ action.addToSymbolScope(action.defineSymbol(DATASET, #ds, #ds)); }
	|	DATASETHANDLE dsh:ID (APPEND)? (BIND)?
		{ action.addToSymbolScope(action.defineVariable(#dsh, #dsh, HANDLE)); }
	|	// ID AS is optional - you are allowed to list just the datatype.
		(id:ID as:AS)? (CLASS TYPE_NAME | datatype_var) (extentphrase)?
		{	if (#id != null) {
				action.addToSymbolScope(action.defineVariable(#id, #id));
				action.defAs(#as);
			}
		}
	;
getkeyvaluestate
	:	#(GETKEYVALUE SECTION expression KEY (DEFAULT|expression) VALUE fld[CQ.UPDATING] state_end )
	;

importstate
	:	#(	IMPORT (stream_name)?
			( #(DELIMITER constant) | UNFORMATTED )?
			(	tbl[CQ.UPDATING] (#(EXCEPT (fld1[CQ.SYMBOL])*))?
			|	( fld[CQ.UPDATING] | CARET )+
			)?
			(NOLOBS)? (NOERROR_KW)? state_end
		)
	;

insertstate
	:	#(	head:INSERT  { action.frameInitializingStatement(#head); }
			tbl[CQ.UPDATING] (#(EXCEPT (fld1[CQ.SYMBOL])*))? (#(USING (ROWID|RECID) expression))?
			(framephrase)? (NOERROR_KW)? state_end  { action.frameStatementEnd(); }
		)
	;

ldbnamefunc
	:	#(LDBNAME LEFTPAREN (#(BUFFER tbl[CQ.BUFFERSYMBOL]) | expression) RIGHTPAREN )
	;

messagestate
	:	#(	MESSAGE
			( #(COLOR anyorvalue) )?
			( #(Form_item (skipphrase | expression) ) )* // No call to formItem() for MESSAGE.
			(	#(	VIEWAS ALERTBOX
					(MESSAGE|QUESTION|INFORMATION|ERROR|WARNING)?
					(BUTTONS (YESNO|YESNOCANCEL|OK|OKCANCEL|RETRYCANCEL) )?
					(#(TITLE expression))?
				)
			|	#(SET fld[CQ.UPDATING] (formatphrase)? )
			|	#(UPDATE fld[CQ.REFUP] (formatphrase)? )
			)*
			( #(IN_KW WINDOW expression) )?
			state_end
		)
	;

methodstate
	:	#(	m:METHOD (PRIVATE|PROTECTED|PUBLIC|OVERRIDE|FINAL)*
			(	VOID
			|	CLASS TYPE_NAME
			|	datatype_var
			)
			id:ID
			{	// Create the scope before calling methodDef.
				action.scopeAdd(#m);
				action.methodDef(#id);
			}
			function_params
			(	// Ambiguous on PERIOD, since a block_colon may be a period, and we may also
				// be at the end of the method declaration for an INTERFACE.
				// We predicate on the next node being Code_block.
				// (Upper/lowercase matters. Node: Code_block. Rule/branch: code_block.)
				(block_colon Code_block)=> block_colon code_block #(END (METHOD)? ) state_end
			|	state_end
			)
			{action.scopeClose(#m);}
		)
	;

nextpromptstate
// Note that NEXT-PROMPT would not initialize a frame, add fields to a frame, etc.
	:	#(NEXTPROMPT fld[CQ.SYMBOL] (framephrase)? state_end )
	;

onstate
	:	#(	onNode:ON
			{action.scopeAdd(#onNode);}
			(	(ASSIGN|CREATE|DELETE_KW|FIND|WRITE)=>
				(	(CREATE|DELETE_KW|FIND) OF t1:tbl[CQ.SYMBOL] (label_constant)?
					{action.defineBufferForTrigger(#t1);}
				|	WRITE OF rec:tbl[CQ.SYMBOL] (label_constant)?
					(	(NEW (BUFFER)? id1:ID) (label_constant)?
						{action.defineBuffer(#id1, #id1, #rec, true);}
					)?
					{if (#id1 == null) action.defineBufferForTrigger(#rec);}
					(	(OLD (BUFFER)? id2:ID) (label_constant)?
						{action.defineBuffer(#id2, #id2, #rec, true);}
					)? 
				|	ASSIGN OF fld:fld[CQ.INIT]
					(#(TABLE LABEL constant))?
					(	OLD (VALUE)?
						id:ID { push(action.defineVariable(#id, #id, #fld)); }
						(options{greedy=true;}:defineparam_var)?
						{ action.addToSymbolScope(pop()); }
					)?
		 		)
				(OVERRIDE)?
				(	REVERT state_end
				|	PERSISTENT runstate
				|	blockorstate
				)
			|	// ON keylabel keyfunction.
				( . . state_end )=>  . . state_end
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
			{action.scopeClose(#onNode);}
		)
	;

openquerystate
	:	#(	OPEN QUERY ID (FOR|PRESELECT) for_record_spec[CQ.INIT]
			(	querytuningphrase
			|	#(BY expression (DESCENDING)? )
			|	collatephrase
			|	INDEXEDREPOSITION
			|	#(MAXROWS expression )
			)*
			state_end
		)
	;

procedurestate
	:	#(	p:PROCEDURE id:ID
			{	action.procedureBegin(#p, #id); }
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
			{	action.procedureEnd(#p); }
		)
	;

promptforstate
	:	#(	head:PROMPTFOR  { action.frameInitializingStatement(#head); }
			(stream_name)? (UNLESSHIDDEN)? (form_item[CQ.SYMBOL])*
			(goonphrase)?  (#(EXCEPT (fld1[CQ.SYMBOL])*))?  (#(IN_KW WINDOW expression))?
			(framephrase)?  { action.frameStatementEnd(); }
			(editingphrase)? state_end
		)
	;

rawtransferstate
	:	#(RAWTRANSFER (BUFFER|FIELD)? (tbl[CQ.REF]|fld[CQ.REF]) TO (BUFFER|FIELD)? (tbl[CQ.UPDATING]|fld[CQ.UPDATING]) (NOERROR_KW)? state_end )
	;

record_fields
	:	#(FIELDS (LEFTPAREN (fld1[CQ.SYMBOL] (#(WHEN expression))?)* RIGHTPAREN)? )
	|	#(EXCEPT (LEFTPAREN (fld1[CQ.SYMBOL] (#(WHEN expression))?)* RIGHTPAREN)? )
	;

recordphrase
	:	(record_fields)? (options{greedy=true;}:TODAY|constant)?
		(	#(LEFT OUTERJOIN )
		|	OUTERJOIN
		|	#(OF tbl[CQ.REF] )
		|	#(WHERE (expression)? )
		|	#(USEINDEX ID )
		|	#(USING fld1[CQ.SYMBOL] (AND fld1[CQ.SYMBOL])* )
		|	lockhow
		|	NOWAIT
		|	NOPREFETCH
		|	NOERROR_KW
		)*
	;

releasestate
	:	#(RELEASE tbl[CQ.REF] (NOERROR_KW)? state_end )
	;

repeatstate
	:	#(	r:REPEAT
			{	action.blockBegin(#r);
				action.frameBlockCheck(#r);
			}
			(block_for)? (block_preselect)? (block_opt)* block_colon {action.frameStatementEnd();}
			code_block block_end {action.blockEnd();}
		)
	;

runstate
	:	#(	r:RUN filenameorvalue { action.runBegin(#r); } 
			(LEFTANGLE LEFTANGLE filenameorvalue RIGHTANGLE RIGHTANGLE)?
			(	#(PERSISTENT ( #(SET (hnd:fld[CQ.UPDATING] { action.runPersistentSet(#hnd); } )? ) )? )
			|	#(SET (fld[CQ.UPDATING])? )
			|	#(ON (SERVER)? expression (TRANSACTION (DISTINCT)?)? )
			|	#(IN_KW hexp:expression) { action.runInHandle(#hexp); } 
			|	#(	ASYNCHRONOUS ( #(SET (fld[CQ.UPDATING])? ) )?
					(#(EVENTPROCEDURE expression ) )?
					(#(IN_KW expression))?
				)
			)*
			{ action.runEnd(#r); }
			(parameterlist)?
			(NOERROR_KW|anyorvalue)*
			state_end
		)
	;

scrollstate
	:	#(	head:SCROLL  { action.frameInitializingStatement(#head); }
			(FROMCURRENT)? (UP)? (DOWN)? (framephrase)?
			state_end  { action.frameStatementEnd(); }
		)
	;

setstate
	:	#(	head:SET  { action.frameInitializingStatement(#head); }
			(stream_name)? (UNLESSHIDDEN)?
			(form_item[CQ.UPDATING])*
			(goonphrase)?  (#(EXCEPT (fld1[CQ.SYMBOL])*))?  (#(IN_KW WINDOW expression))?
			(framephrase)?  { action.frameStatementEnd(); }
			(editingphrase)? (NOERROR_KW)? state_end  
		)
	;

systemdialogcolorstate
	:	#(SYSTEMDIALOG COLOR expression ( #(UPDATE fld[CQ.UPDATING]) )? (#(IN_KW WINDOW expression))? state_end )
	;

systemdialogfontstate
	:	#(	SYSTEMDIALOG FONT expression
			(	ANSIONLY
			|	FIXEDONLY
			|	#(MAXSIZE expression )
			|	#(MINSIZE expression )
			|	#(UPDATE fld[CQ.UPDATING] )
			|	#(IN_KW WINDOW expression)
			)*
			state_end
		)
	;

systemdialoggetdirstate
	:	#(	SYSTEMDIALOG GETDIR fld[CQ.REFUP]
			(	#(INITIALDIR expression)
			|	RETURNTOSTARTDIR
			|	#(TITLE expression)
			)*
			state_end
		)
	;

systemdialoggetfilestate
	:	#(	SYSTEMDIALOG GETFILE fld[CQ.REFUP]
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
			|	#(UPDATE fld[CQ.UPDATING] )
			|	#(IN_KW WINDOW expression)
			)*
			state_end
		)
	;

systemdialogprintersetupstate
	:	#(	SYSTEMDIALOG PRINTERSETUP
			( #(NUMCOPIES expression) | #(UPDATE fld[CQ.UPDATING]) | LANDSCAPE | PORTRAIT | #(IN_KW WINDOW expression) )*
			state_end
		)
	;

triggerphrase
	:	#(	TRIGGERS block_colon
			#(	Code_block
				(	#(	on:ON {action.scopeAdd(#on);}
						eventlist (ANYWHERE)?
						(PERSISTENT runstate | blockorstate)
						{action.scopeClose(#on);}
					) 
				)*
			)
			#(END (TRIGGERS)? )
		)
	;

triggerprocedurestate
	:	#(	TRIGGER PROCEDURE FOR
			(	(CREATE|DELETE_KW|FIND|REPLICATIONCREATE|REPLICATIONDELETE)
				OF t1:tbl[CQ.SYMBOL] (label_constant)?
				{action.defineBufferForTrigger(#t1);}
			|	(WRITE|REPLICATIONWRITE) OF rec:tbl[CQ.SYMBOL] (label_constant)?
				(	NEW (BUFFER)? id4:ID (label_constant)?
					{action.defineBuffer(#id4, #id4, #rec, true);}
				)?
				{if (#id4 == null) action.defineBufferForTrigger(#rec);}
				(	OLD (BUFFER)? id3:ID (label_constant)? 
					{action.defineBuffer(#id3, #id3, #rec, true);}
				)?
			|	ASSIGN
				(	#(OF fld[CQ.SYMBOL] (#(TABLE LABEL constant))? )
				|	#(	NEW (VALUE)?
						id:ID { push(action.defineVariable(#id, #id)); }
						defineparam_var
						{ action.addToSymbolScope(pop()); }
					)
					
				)? 
				(	#(	OLD (VALUE)?
						id2:ID { push(action.defineVariable(#id2, #id2)); }
						defineparam_var
					)
					{ action.addToSymbolScope(pop()); }
				)?
			)
			state_end
		)
	;

underlinestate
	:	#(	head:UNDERLINE  { action.frameInitializingStatement(#head); }
			(stream_name)? (#(fi:Form_item fld[CQ.SYMBOL] {action.formItem(#fi);} (formatphrase)? ))* (framephrase)?
			state_end  { action.frameStatementEnd(); }
		)
	;

upstate
	:	#(	head:UP  { action.frameInitializingStatement(#head); }
			(options{greedy=true;}:stream_name)? (expression)? (stream_name)? (framephrase)?
			state_end  { action.frameStatementEnd(); }
		)
	;

updatestatement
	:	(#(UPDATE tbl[CQ.SYMBOL] SET))=> sqlupdatestate
	|	updatestate
	;

updatestate
	:	#(	head:UPDATE  { action.frameInitializingStatement(#head); }
			(UNLESSHIDDEN)?	
			(form_item[CQ.REFUP])*
			(goonphrase)?
			(#(EXCEPT (fld1[CQ.SYMBOL])*))?
			(#(IN_KW WINDOW expression))?
			(framephrase)?  { action.frameStatementEnd(); }
			(editingphrase)? (NOERROR_KW)? state_end
		)
	;

validatestate
	:	#(VALIDATE tbl[CQ.REF] (NOERROR_KW)? state_end )
	;

viewstate
	:	#(v:VIEW (stream_name)? (gwidget)* (#(IN_KW WINDOW expression))? state_end {action.viewState(#v);} )
	;

///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin SQL
///////////////////////////////////////////////////////////////////////////////////////////////////

altertablestate
	:	#(	ALTER TABLE tbl[CQ.SCHEMATABLESYMBOL]
			(	ADD COLUMN sql_col_def
			|	DROP COLUMN fld[CQ.SYMBOL]
			|	ALTER COLUMN fld[CQ.SYMBOL]
				(  	#(FORMAT expression)
				|	label_constant
     				|	#(DEFAULT expression )
				| 	casesens_or_not
   				)*
			)
			state_end
		)
	;

createindexstate
	:	#(CREATE (UNIQUE)? INDEX ID ON tbl[CQ.SCHEMATABLESYMBOL] #(Field_list LEFTPAREN fld[CQ.SYMBOL] (COMMA fld[CQ.SYMBOL])* RIGHTPAREN ) state_end )
	;

createviewstate
	:	#(CREATE VIEW ID (#(Field_list LEFTPAREN fld[CQ.SYMBOL] (COMMA fld[CQ.SYMBOL])* RIGHTPAREN ))? AS selectstatea state_end )
	;

deletefromstate
	:	#(	DELETE_KW FROM tbl[CQ.SCHEMATABLESYMBOL]
			( #(WHERE (sqlexpression | #(CURRENT OF ID))? ) )?
			state_end
		)
	;

droptablestate
	:	#(DROP TABLE tbl[CQ.SCHEMATABLESYMBOL] state_end )
	;

fetchstate
	:	#(FETCH ID INTO fld[CQ.UPDATING] (fetch_indicator)? (COMMA fld[CQ.UPDATING] (fetch_indicator)? )* state_end )
	;
fetch_indicator
	:	#(INDICATOR fld[CQ.UPDATING] )
	|	fld[CQ.UPDATING]
	;

grantstate
	: 	#(GRANT (grant_rev_opt) ON (tbl[CQ.SCHEMATABLESYMBOL]|ID) grant_rev_to (WITH GRANT OPTION)? state_end )
	;
grant_rev_opt
	:	#(ALL (PRIVILEGES)? )
	|	(	SELECT | INSERT | DELETE_KW
		|	#(UPDATE (#(Field_list LEFTPAREN fld[CQ.UPDATING] (COMMA fld[CQ.UPDATING])* RIGHTPAREN ))? )
		|	COMMA
		)+
	;

insertintostate
	:	#(	INSERT INTO tbl[CQ.SCHEMATABLESYMBOL]
			(#(Field_list LEFTPAREN fld[CQ.UPDATING] (COMMA fld[CQ.UPDATING])* RIGHTPAREN ))?
			(	#(	VALUES LEFTPAREN sqlexpression (fetch_indicator)?
					(COMMA sqlexpression (fetch_indicator)?)* RIGHTPAREN
				)
			|	selectstatea
			)
			state_end
		)
	;

revokestate
	: 	#(REVOKE (grant_rev_opt) ON (tbl[CQ.SCHEMATABLESYMBOL]|ID) grant_rev_to state_end )
	;

selectstate
// selectstate_AST_in is the name of the input node within the Antlr generated code.
// Hopefully Antlr won't change how that works.  :-/  I don't know if there's a "proper" way
// of getting the next or input token for Antlr generated tree parsers.
	: 	{ action.frameInitializingStatement(selectstate_AST_in); }
		selectstatea state_end
		{ action.frameStatementEnd(); }
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
			( #(INTO fld[CQ.UPDATING] (fetch_indicator)? (COMMA fld[CQ.UPDATING] (fetch_indicator)?)* ) )?
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

select_sqltableref
	:	(tbl[CQ.SCHEMATABLESYMBOL] | ID) (ID)?
	;

sqlupdatestate
	: 	#(	UPDATE tbl[CQ.SCHEMATABLESYMBOL] SET sqlupdate_equal (COMMA sqlupdate_equal)*
			( #(WHERE (sqlexpression | CURRENT OF ID) ) )?
			state_end
		)
	;
sqlupdate_equal
	:	#(EQUAL fld[CQ.REF] sqlexpression (fetch_indicator)? )
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
			(	LEFTPAREN fld[CQ.REF] RIGHTPAREN
			|	fld[CQ.REF]
			)
		|	STAR
		|	(ALL)? sqlscalar
		)
		RIGHTPAREN
	;

///////////////////////////////////////////////////////////////////////////////////////////////////
// sqlexpression 
///////////////////////////////////////////////////////////////////////////////////////////////////

sql_in_val
	:	fld[CQ.REF] (fetch_indicator)? | constant | USERID
	;
