/* Created on Apr 29, 2005
 * Authors: John Green
 * 
 * Copyright (c) 2005 Joanju (www.joanju.com)
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.nodetypes;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.TokenTypes;

import com.joanju.ProparseLdr;


/** Create a JPNode, or an object of the appropriate subclass.
 * <p>
 * <b>This factory is final - it may not be subclassed or replaced.<b><br>
 * ProRefactor expects nodes to be of certain subtypes. If third parties
 * were to use their own node factories, then when ProRefactor adds
 * new JPNode subtypes, the third party factories would be out of date, and
 * cause ProRefactor to fail.
 * <p>
 * An alternative implementation would have been to restrict ProRefactor to
 * only reference JPNode, so that third parties could subclass JPNode any 
 * which way. This was decided against, in favor of allowing JPNode subclasses
 * to be used within ProRefactor itself. Third parties should use other
 * mechanisms to extend JPNode, such as attributes and setLink/getLink.
 * It is ProRefactor's responsibility to provide appropriate node subtypes so
 * that the tree is easy to work with.
 */
public final class NodeFactory {
	
	private static ProparseLdr parser = ProparseLdr.getInstance();
	
	public static JPNode create(int handle) {
		switch (parser.getNodeTypeI(handle)) {
		case TokenTypes.Program_root:
			return new ProgramRootNode(handle, JPNode.nullConfig);
		case TokenTypes.Field_ref:
			return new FieldRefNode(handle, JPNode.nullConfig);
		case TokenTypes.RECORD_NAME:
			return new RecordNameNode(handle, JPNode.nullConfig);
		case TokenTypes.PROPARSEDIRECTIVE:
			return new ProparseDirectiveNode(handle, JPNode.nullConfig);
		case TokenTypes.DO:
		case TokenTypes.FOR:
		case TokenTypes.REPEAT:
		case TokenTypes.FUNCTION:
		case TokenTypes.PROCEDURE:
		case TokenTypes.CONSTRUCTOR:
		case TokenTypes.DESTRUCTOR:
		case TokenTypes.METHOD:
			// We check that these are statement heads, whether the keyword is reserved or not.
			if (parser.attrGetI(handle, IConstants.STATEHEAD) != 0 )
				return new BlockNode(handle, JPNode.nullConfig);
			else
				return new JPNode(handle, JPNode.nullConfig);
		case TokenTypes.CANFIND:
			// CANFIND is reserved, and only used in the syntax for the CAN-FIND function.
			// It is a "block" because it has special buffer/index-cursor handling.
			return new BlockNode(handle, JPNode.nullConfig);
		case TokenTypes.ON:
			{
				if (parser.attrGetI(handle, IConstants.STATEHEAD) != 0 )
					return new BlockNode(handle, JPNode.nullConfig);
				int temp = parser.getHandle();
				int childType = parser.nodeFirstChildI(handle, temp);
				parser.releaseHandle(temp);
				if (childType == TokenTypes.Event_list)
					return new BlockNode(handle, JPNode.nullConfig);
				return new JPNode(handle, JPNode.nullConfig);
			}
		default:
			return new JPNode(handle, JPNode.nullConfig);
		}
	}
	
	public static JPNode createByIndex(int index) {
		switch (index) {
		case 1:
			return new JPNode();
		case 2:
			return new BlockNode();
		case 3:
			return new FieldRefNode();
		case 4:
			return new RecordNameNode();
		case 5:
			return new ProparseDirectiveNode();
		case 6:
			return new ProgramRootNode();
		default:
			throw new IllegalArgumentException();
		}
	}

}
