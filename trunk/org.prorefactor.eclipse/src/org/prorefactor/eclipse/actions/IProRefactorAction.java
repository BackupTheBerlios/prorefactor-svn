/* IProRefactorAction.java
 * Created on Oct 8, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.actions;


/**
 */
public interface IProRefactorAction {
	
	public RefactorResult processFile(int topNode);

	public String processTargetSet();

}
