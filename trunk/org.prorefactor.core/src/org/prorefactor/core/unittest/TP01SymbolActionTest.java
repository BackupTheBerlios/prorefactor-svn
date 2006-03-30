/**
 * SymbolParseActionTest.java
 * @author Peter Dalbadie
 * 21-Sep-2004
 * 
 */

package org.prorefactor.core.unittest;

import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.SymbolScope;
import org.prorefactor.treeparser01.TP01Support;
import org.prorefactor.treeparser01.TreeParser01;

import junit.framework.TestCase;
import antlr.RecognitionException;

import com.joanju.ProparseLdr;

/**
 * Tests for symbol parse action (TP01Support).
 *
 */
public class TP01SymbolActionTest extends TestCase {
	private Config config = null;
	private ProparseLdr parser = null;
	private TP01Support walkAction;
	private TreeParser01 walker;

	/**
	 * @param name
	 */
	public TP01SymbolActionTest(String name) {
		super(name);
	}
	
	private JPNode getTree() {
		int topNode = parser.getHandle();
		parser.nodeTop(topNode);
		JPNode ast = JPNode.getTree(topNode);
		return ast;
	}
	
	public void setUp(){
		config = Config.getInstance();
		parser = ProparseLdr.getInstance();
		walkAction = new TP01Support();
		walker = new TreeParser01();
		walker.setActionObject(walkAction);
	}

//	/**
//	 * Parse compile-file.p and verify that all
//	 * symbols are extracted correctly.
//	 */
//	public void testCompileFileFrames(){
//		try {
//			parser.parse(Config.testDir() + "compile-file.p");
//			assertTrue("Parse error.", parser.errorGetStatus() >= 0);
//			
//			walker.program(getTree());
//			
//			// Create expected symbols.
//			Frame getUserInformation = new Frame("getUserInformation");
//			assertTrue(table.has(getUserInformation.getId()));
//									
//		} catch (RecognitionException e) {
//			e.printStackTrace();
//			fail();
//		}
//	}

	/**
	 * Parse compile-file.p and verify that all
	 * symbols are extracted correctly.
	 */
	public void testCompileFileRoutines(){
		try {
			parser.parse(Config.testDir() + "compile-file.p");
			assertTrue("Parse error.", parser.errorGetStatus() >= 0);
			
			walker.program(getTree());
			
			// Create expected symbols.
			RoutineHandler enableUi = new RoutineHandler("enable-ui", walkAction);
			RoutineHandler userAction = new RoutineHandler("user-action", walkAction);
			RoutineHandler disableUi = new RoutineHandler("disable-ui", walkAction);
			RoutineHandler setState = new RoutineHandler("setState", walkAction);
			RoutineHandler getCompileList = new RoutineHandler("get-compile-list", walkAction);			
	
			// Routines expected in root scope.	
			assertTrue(rootScope().hasRoutine(enableUi.getName()));
			assertTrue(rootScope().hasRoutine(userAction.getName()));
			assertTrue(rootScope().hasRoutine(disableUi.getName()));
			assertTrue(rootScope().hasRoutine(setState.getName()));
			assertTrue(rootScope().hasRoutine(getCompileList.getName()));
	
						
		} catch (RecognitionException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Parse compile-file.p and verify that all
	 * symbols are extracted correctly.
	 */
	public void testCompileFileVars(){
		try {
			parser.parse(Config.testDir() + "compile-file.p");
			assertTrue("Parse error.", parser.errorGetStatus() >= 0);
			
			walker.program(getTree());
			
			// Create expected symbols.
			String sourcePath = new String("sourcePath");
			String currentPropath = new String("currentPropath");
			String compileFile = new String("compileFile");
			String currentStatus = new String("currentStatus");
			String test = new String("test");
			String aFile = new String("aFile");
			String aNewFile = new String("aNewFile");
			String aNewSrcDir = new String("aNewSrcDir");
			RoutineHandler getCompileList = new RoutineHandler("get-compile-list", walkAction);			

			// Variables expected in root scope.
			assertTrue(rootScope().lookupVariable(sourcePath) != null);
			assertTrue(rootScope().lookupVariable(currentPropath) != null);
			assertTrue(rootScope().lookupVariable(compileFile) != null);
			assertTrue(rootScope().lookupVariable(currentStatus) != null);
			assertTrue(rootScope().lookupVariable(test) != null);

			// Variables not expected in root scope.
			assertTrue(rootScope().lookupVariable(aFile) == null);
			assertTrue(rootScope().lookupVariable(aNewFile) == null);
			assertTrue(rootScope().lookupVariable(aNewSrcDir) == null);

			// Get get-compile-list scope.
			SymbolScope routineScope = getCompileList.getRoutineScope();
			
			// Variables expected in get-compile-list scope.
			assertTrue(routineScope.lookupVariable(aFile) != null);
			assertTrue(routineScope.lookupVariable(aNewFile) != null);
			assertTrue(routineScope.lookupVariable(aNewSrcDir) != null);

			// Variables visible from the open scope.
			assertTrue(routineScope.lookupVariable(sourcePath) != null);
			assertTrue(routineScope.lookupVariable(currentPropath) != null);
			assertTrue(routineScope.lookupVariable(compileFile) != null);
			assertTrue(routineScope.lookupVariable(currentStatus) != null);
			assertTrue(routineScope.lookupVariable(test) != null);
						
		} catch (RecognitionException e) {
			e.printStackTrace();
			fail();
		}
	}

	private SymbolScope rootScope() {
		return walkAction.getRootScope();
	}
}
