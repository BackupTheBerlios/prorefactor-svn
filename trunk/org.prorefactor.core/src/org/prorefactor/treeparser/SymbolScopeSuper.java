/**
 * @author John Green
 * 17-Mar-2006
 * www.joanju.com
 * 
 * Copyright (c) 2006 Joanju Software.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.treeparser;

import java.util.Collections;
import java.util.Map;

import org.prorefactor.util.Cache;

/** Contains skeleton symbols for purposes of inheritance.
 * Since these are cached indefinately, they never have references
 * to syntax tree nodes or child scopes.
 */
public class SymbolScopeSuper extends SymbolScopeRoot {

	public SymbolScopeSuper(String className) {
		super();
		setClassName(className);
	}
	
	/** TreeParser01 stores and looks up SymbolScopeSuper objects in this cache, which
	 * by default is a synchronizedMap wrapped org.prorefactor.util.Cache object
	 * with a maximum cache size of 50. It is safe for any application to completely
	 * override this. (Well, of course, be careful that you provide some mechanism
	 * for keeping the cache from growing too large.) Since it's just a cache, it's
	 * completely exposed. Just don't make it null.  :)
	 */
	public static Map<String, SymbolScopeSuper> cache = Collections.synchronizedMap(new Cache<String, SymbolScopeSuper>(50));
	

	/** INVALID This method is illegal for super scopes.
	 * Super scopes are cached indefinately, and as such, should never have
	 * references to child scopes, ASTs, etc.
	 */
	@Override
	public SymbolScope addScope() {
		assert false;
		return null;
	}
	
	
}
