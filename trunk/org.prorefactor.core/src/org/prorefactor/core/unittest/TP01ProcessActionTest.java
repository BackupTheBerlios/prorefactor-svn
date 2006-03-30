/**
 * SymbolParseActionTest.java
 * @author Peter Dalbadie
 * 21-Sep-2004
 * 
 * Copyright (c) 2004,2006 ProRefactor.org.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.core.unittest;

import java.util.ArrayList;
import java.util.Map;

import junit.framework.TestCase;

import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.Call;
import org.prorefactor.treeparser01.TP01ProcessAction;
import org.prorefactor.treeparser01.TP01Support;
import org.prorefactor.treeparser01.TreeParser01;

import antlr.RecognitionException;

import com.joanju.ProparseLdr;

/**
 * Tests for ProcessParseAction.
 *
 */
public class TP01ProcessActionTest extends TestCase {
	private ProparseLdr parser = null;
	private TP01Support symbolAction;
	private TP01ProcessAction processAction;
	private TreeParser01 treeWalker;

	private String testDir = Config.testDir();
	
	/**
	 * @param name
	 */
	public TP01ProcessActionTest(String name) {
		super(name);
	}
	
	private JPNode getTree() {
		int topNode = parser.getHandle();
		parser.nodeTop(topNode);
		JPNode ast = JPNode.getTree(topNode);
		return ast;
	}
	
	public void setUp(){
		parser = ProparseLdr.getInstance();
		symbolAction = new TP01Support();
		processAction = new TP01ProcessAction(symbolAction.getRootScope());
		treeWalker = new TreeParser01();
	}

	/**
	 * Parse compile-file.p and verify that all calls
	 * are registered correctly, for each scope.
	 */
	public void testCompileFileCalls(){
		try {
			String externalName = "compile-file.p";
			parser.parse(testDir + externalName);
			assertTrue("Parse error.", parser.errorGetStatus() >= 0);
			
			JPNode ast = getTree();
			
			treeWalker.setActionObject(symbolAction);
			treeWalker.program(ast);

			treeWalker.setActionObject(processAction);
			treeWalker.program(ast);
			

			// Define routine handlers for expected routines.
			RoutineHandler enableUi = new RoutineHandler("enable-ui", symbolAction);
			RoutineHandler userAction = new RoutineHandler("user-action", symbolAction);
			RoutineHandler disableUi = new RoutineHandler("disable-ui", symbolAction);
			RoutineHandler setState = new RoutineHandler("setState", symbolAction);
			RoutineHandler getCompileList = new RoutineHandler("get-compile-list", symbolAction);			


			// Define call objects for expected calls.
			Call enableUiCall = new Call(externalName,enableUi.getName());
			Call userActionCall = new Call(externalName,userAction.getName());
			Call disableUiCall = new Call(externalName,disableUi.getName());
			Call setStateCall = new Call(externalName,setState.getName());
			Call getCompileListCall = new Call(externalName,getCompileList.getName());
			
			// Create expected result set for root scope: enable-ui, user-action, disable-ui.
			ArrayList<Call> expectedRootCalls = new ArrayList<Call>();
			expectedRootCalls.add(disableUiCall);
			expectedRootCalls.add(enableUiCall);
			expectedRootCalls.add(userActionCall);
			

			// Get actual calls found in code and test against expected.
			Map<String, Call> actualRootCalls = processAction.getRootScope().getCallMap();
			assertTrue(actualRootCalls.values().containsAll(expectedRootCalls));
			assertTrue(! actualRootCalls.values().contains(setStateCall));			
			assertTrue(! actualRootCalls.values().contains(getCompileListCall));			
			
			// Internal proc enable-ui calls: setState.
			Map<String, Call> actualEnableUiCalls = enableUi.getRoutineScope().getCallMap();
			assertTrue(actualEnableUiCalls.values().contains(setStateCall));
			
			// Internal proc user-action calls: get-compile-list.
			Map<String, Call> actualUserActionCalls = userAction.getRoutineScope().getCallMap();
			assertTrue(actualUserActionCalls.values().contains(getCompileListCall));
			
			// Internal proc get-compile-list calls: setState x 3.
			Map<String, Call> actualGetCompileListCalls = getCompileList.getRoutineScope().getCallMap();
			assertTrue(actualGetCompileListCalls.values().contains(setStateCall));
						
		} catch (RecognitionException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Parse persistent-run.p and verify that:
	 * a) run <proc1> persistent set <h> results in the
	 * handle variable being updated.
	 * b) run <proc2> in <h> is registered as a call to
	 * proc1.proc2.
	 */
	public void testPersistenProc(){
		try {
			String externalName = "persistent-run.p";
			parser.parse(testDir + externalName);
			assertTrue("Parse error.", parser.errorGetStatus() >= 0);
			
			JPNode ast = getTree();

			treeWalker.setActionObject(symbolAction);
			treeWalker.program(ast);

			treeWalker.setActionObject(processAction);
			treeWalker.program(ast);

			assertTrue(processAction.getErrorList().size() == 0);

			// Define routines.
			RoutineHandler test01 = new RoutineHandler("test_01", symbolAction);
			RoutineHandler test02 = new RoutineHandler("test_02", symbolAction);
			
			// Define calls.
			String targetProc = "persistent-proc.p";
			Call persistentProcCall = new Call(targetProc,null);
			Call test01InHandleCall = new Call(targetProc,test01.getName());
			Call test02InHandleCall = new Call(targetProc,test02.getName());
			Call test01InternalCall = new Call(externalName,test01.getName());
			
			// Expected root procedure calls.
			ArrayList<Call> expectedRootCalls = new ArrayList<Call>();
			expectedRootCalls.add(persistentProcCall);
			expectedRootCalls.add(test01InHandleCall);
			expectedRootCalls.add(test01InternalCall);

			// Expected calls in procedure test_01
			ArrayList<Call> expectedTest01Calls = new ArrayList<Call>();
			expectedTest01Calls.add(test02InHandleCall);
			
			// Test actual root calls agains expected root calls.
			Map<String, Call> actualRootCalls = processAction.getCallList();
			assertTrue(actualRootCalls.values().containsAll(expectedRootCalls));
			assertTrue(! actualRootCalls.values().contains(test02InHandleCall));			

			// Test actual calls in test_01 against expected calls.
			Map<String, Call> actualTest01Calls = test01.getRoutineScope().getCallMap();
			assertTrue(actualTest01Calls.values().containsAll(expectedTest01Calls));
			
		} catch (RecognitionException e) {
			e.printStackTrace();
			fail();
		}

	}
}
