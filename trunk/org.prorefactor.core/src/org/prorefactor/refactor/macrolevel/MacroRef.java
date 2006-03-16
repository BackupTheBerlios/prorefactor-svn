/* MacroRef.java
 * Created on Nov 29, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.refactor.macrolevel;


import java.util.ArrayList;
import java.util.Iterator;

import org.prorefactor.core.Util;
import org.prorefactor.refactor.RefactorException;


/** Abstract class for a macro reference.
 * There are two subclasses: one for references to named macros
 * (i.e. those named with &global, &scoped, or an include argument),
 * and one for references to include files.
 */
public abstract class MacroRef {

	public int refColumn;
	public int refLine;
	public MacroRef parent = null;

	/** A list of macro references and defines that are in this macro's source */
	public ArrayList macroEventList = new ArrayList();



	/** Find <i>external macro references</i>.
	 * An external macro is an include file, a &GLOBAL or a &SCOPED from another file,
	 * and include args.
	 * 
	 * TODO: (Jan 26) This doesn't seem right to me anymore. An &UNDEFINE only affects
	 * the local scope. If re-implemented after building a pseudoprocessor, consider
	 * dropping this.
	 * &UNDEFINE of a &GLOBAL or of a &SCOPED from another file is considered a reference.
	 * &UNDEFINE of an include argument is considered a reference.
	 * 
	 * The subroutine is recursive, because a local define may incur an external reference!
	 * @return An array of objects: MacroRef and MacroDef (for UNDEFINE).
	 */
	public ArrayList findExternalMacroReferences() {
		ArrayList ret = new ArrayList();
		for (Iterator it = macroEventList.iterator(); it.hasNext(); ) {
			findExternalMacroReferences(it.next(), ret);
		}
		return ret;
	} // findExternalMacroReferences
	/**
	 * @see #findExternalMacroReferences()
	 * @param begin An array of two integers to indicate the beginning line/column.
	 * May be null to indicate the beginning of the range is open ended. 
	 * @param end An array of two integers to indicate the ending line/column.
	 * May be null to indicate the ending of the range is open ended. 
	 */
	public ArrayList findExternalMacroReferences(int [] begin, int [] end) {
		ArrayList ret = new ArrayList();
		for (Iterator it = macroEventList.iterator(); it.hasNext(); ) {
			Object next = it.next();
			if (next instanceof MacroRef) {
				MacroRef ref = (MacroRef)next;
				if (Util.isInRange(ref.refLine, ref.refColumn, begin, end)) {
					findExternalMacroReferences(ref, ret);
				}
				continue;
			}
			if (next instanceof MacroDef) {
				MacroDef def = (MacroDef)next;
				if (Util.isInRange(def.line, def.column, begin, end))
					findExternalMacroReferences(def, ret);
			}
		}
		return ret;
	} // findExternalMacroReferences
	private void findExternalMacroReferences(Object obj, ArrayList list) {
		if (obj==null) return;
		if (obj instanceof IncludeRef) {
			list.add(obj);
			return;
		}
		if (obj instanceof MacroDef) {
			MacroDef def = (MacroDef)obj;
			if (def.type==MacroDef.UNDEFINE) {
				if (def.undefWhat.type==MacroDef.NAMEDARG) {
					list.add(def);
					return;
				}
				if (! isMine(def.undefWhat.parent)) list.add(def);
			}
			return;
		}
		// Only one last type we're interested in...
		if (! (obj instanceof NamedMacroRef)) return;
		NamedMacroRef ref = (NamedMacroRef)obj;
		if (! isMine(ref)) {
			list.add(ref);
			return;
		}
		// It's possible for an internal macro to refer to an external macro
		for (Iterator it = ref.macroEventList.iterator(); it.hasNext(); ) {
			findExternalMacroReferences(it.next(), list);			
		}
	} // findExternalMacroReferences



	/** Find references to an include file by the include file's file index number.
	 * Search is recursive, beginning at this MacroRef object.
	 * @param fileIndex The fileIndex for the include file we want references to.
	 * @return An array of IncludeRef objects.
	 */
	public ArrayList findIncludeReferences(int fileIndex) {
		ArrayList ret = new ArrayList();
		findIncludeReferences(fileIndex, this, ret);
		return ret;
	} // findIncludeReferences
	private void findIncludeReferences(int fileIndex, MacroRef ref, ArrayList list) {
		if (ref==null) return;
		if (ref instanceof IncludeRef) {
			IncludeRef incl = (IncludeRef) ref;
			if (incl.fileIndex==fileIndex) list.add(ref);
		}
		for (Iterator it = ref.macroEventList.iterator(); it.hasNext(); ) {
			Object next = it.next();
			if (next instanceof MacroRef)
				findIncludeReferences(fileIndex, (MacroRef)next, list);
		}
	} // findIncludeReferences



	/** Get an int[3] file/line/column position for this macro reference.
	 */
	public int[] getPosition() throws RefactorException {
		if (! (parent instanceof IncludeRef)) {
			int [] refpos = MacroLevel.getDefinitionPosition(
				((NamedMacroRef)parent).macroDef
				);
			throw new RefactorException(refpos, "Macro source for macro reference.");
		}
		int [] ret = { ((IncludeRef)parent).fileIndex, refLine, refColumn };
		return ret;
	} // getPosition



	/** Is a macro ref/def myself, or, a child of mine?
	 * @param ref
	 * @return
	 */
	private boolean isMine(Object obj) {
		if (obj==null) return false;
		if (obj==this) return true;
		MacroRef parent = null;
		if (obj instanceof MacroDef) parent = ((MacroDef)obj).parent;
		if (obj instanceof MacroRef) parent = ((MacroRef)obj).parent;
		return isMine(parent);
	}


}
