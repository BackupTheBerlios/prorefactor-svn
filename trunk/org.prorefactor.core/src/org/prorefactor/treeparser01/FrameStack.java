/* Created on 23-Jan-2006
 * Authors: john
 */
package org.prorefactor.treeparser01;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.TokenTypes;
import org.prorefactor.core.schema.Field;
import org.prorefactor.nodetypes.BlockNode;
import org.prorefactor.nodetypes.FieldRefNode;
import org.prorefactor.nodetypes.RecordNameNode;
import org.prorefactor.treeparser.Block;
import org.prorefactor.treeparser.FieldBuffer;
import org.prorefactor.treeparser.FieldContainer;
import org.prorefactor.treeparser.FieldLookupResult;
import org.prorefactor.treeparser.Symbol;
import org.prorefactor.treeparser.SymbolScope;
import org.prorefactor.treeparser.TableBuffer;
import org.prorefactor.treeparser.Variable;
import org.prorefactor.widgettypes.Browse;
import org.prorefactor.widgettypes.Frame;



/** Keeps a stack of most recently "referenced" frames.
 * A frame may be "referenced" at up to two different occassions. Once when
 * the frame is created (like in a DEFINE FRAME statement), and once when
 * the frame is "initialized" (like in a DISPLAY statement). The frame's
 * scope is determined at the time it is initialized.
 * Also deals with BROWSE widgets and the fields in those.
 */
public class FrameStack {

	FrameStack() { }

	private LinkedList<Frame> frameMRU = new LinkedList<Frame>();
	private FieldContainer containerForCurrentStatement = null;
	private JPNode currStatementWholeTableFormItemNode = null;

	
	
	/** The ID node in a BROWSE ID pair.
	 * The ID node might have already had the symbol assigned to it at the point
	 * where the statement head was processed.
	 */
	void browseRefNode(JPNode idNode, SymbolScope symbolScope) {
		if (idNode.getSymbol()==null) browseRefSet(idNode, symbolScope);
		
	}
	
	
	private Browse browseRefSet(JPNode idNode, SymbolScope symbolScope) {
		Browse browse = (Browse)symbolScope.lookupFieldLevelWidget(idNode.getText());
		idNode.setLink(JPNode.SYMBOL, browse);
		return browse;
	}
	

	/** For a Form_item node which is for a whole table reference, get a list of the
	 * FieldBuffers that would be added to the frame, respecting any EXCEPT fields list.
	 */
	public ArrayList<FieldBuffer> calculateFormItemTableFields(JPNode formItemNode) {
		assert formItemNode.getType() == TokenTypes.Form_item;
		assert formItemNode.firstChild().getType() == TokenTypes.RECORD_NAME;
		RecordNameNode recordNameNode = (RecordNameNode) formItemNode.firstChild();
		TableBuffer tableBuffer = recordNameNode.getTableBuffer();
		HashSet<Field> fieldSet = new HashSet<Field>(tableBuffer.getTable().getFieldSet());
		JPNode exceptNode = formItemNode.parent().findDirectChild(TokenTypes.EXCEPT);
		if (exceptNode!=null) for (JPNode n = exceptNode.firstChild(); n!=null; n=n.nextSibling()) {
			if (! (n instanceof FieldRefNode)) continue;
			Field f = ((FieldBuffer)((FieldRefNode)n).getSymbol()).getField();
			fieldSet.remove(f);
		}
		ArrayList<FieldBuffer> returnList = new ArrayList<FieldBuffer>();
		for (Field field : fieldSet) {
			returnList.add(tableBuffer.getFieldBuffer(field));
		}
		return returnList;
	}
	
	
	/** Create a frame object.
	 * Adds the new frame object to the MRU list.
	 */
	private Frame createFrame(String frameName, SymbolScope symbolScope) {
		Frame frame = new Frame(frameName, symbolScope);
		frameMRU.addFirst(frame);
		return frame;
	}

	
	/** Recieve a Form_item node for a field which should be referenceable on the frame|browse.
	 * This checks for LEXAT (DISPLAY thisField @ anotherField) which would keep thisField
	 * from being added to the frame but would instead add anotherField to the frame.
	 * This must be called <b>after</b> any Field_ref symbols have been resolved.
	 * This only does anything if the first child of the Form_item is RECORD_NAME or Field_ref.
	 * Tree parser rules like display_item and form_item sometimes get used in statements that
	 * don't actually affect frames. In those cases, containerForCurrentStatement==null, and
	 * this function is a no-op.
	 */
	void formItem(JPNode formItemNode) {
		if (containerForCurrentStatement==null) return;
		assert formItemNode.getType() == TokenTypes.Form_item;
		JPNode firstChild = formItemNode.firstChild();
		if (firstChild.getType()==TokenTypes.RECORD_NAME) {
			// Delay processing until the end of the statement. We need any EXCEPT fields resolved first.
			currStatementWholeTableFormItemNode = formItemNode;
			return;
		} else if (firstChild.getType()==TokenTypes.Field_ref) {
			FieldRefNode fieldRefNode = (FieldRefNode)firstChild;
			JPNode tempNode = formItemNode.findDirectChild(TokenTypes.Format_phrase);
			if (tempNode!=null) {
				tempNode = formItemNode.findDirectChild(TokenTypes.LEXAT);
				if (tempNode!=null) fieldRefNode = (FieldRefNode) tempNode.firstChild();
			}
			containerForCurrentStatement.addSymbol(fieldRefNode.getSymbol());
		} else return;
	}
	
	
	/** The ID node in a FRAME ID pair.
	 * For "WITH FRAME id", the ID was already set when we processed the statement head.
	 */
	void frameRefNode(JPNode idNode, SymbolScope symbolScope) {
		if (idNode.getSymbol()==null) frameRefSet(idNode, symbolScope);
	}
	
	
	private Frame frameRefSet(JPNode idNode, SymbolScope symbolScope) {
		String frameName = idNode.getText();
		Frame frame = (Frame) symbolScope.lookupWidget(TokenTypes.FRAME, frameName);
		if (frame==null) frame = createFrame(frameName, symbolScope);
		idNode.setLink(JPNode.SYMBOL, frame);
		return frame;
	}
	
	
	/** For a statement that might have #(WITH ... #([FRAME|BROWSE] ID)), get the FRAME|BROWSE node. */
	private JPNode getContainerTypeNode(JPNode stateNode) {
		JPNode withNode = stateNode.findDirectChild(TokenTypes.WITH);
		if (withNode==null) return null;
		JPNode typeNode = withNode.findDirectChild(TokenTypes.FRAME);
		if (typeNode==null) typeNode = withNode.findDirectChild(TokenTypes.BROWSE);
		return typeNode;
	}
	
	
	/** Create the frame if necessary, set its scope if that hasn't already been done. */
	private Frame initializeFrame(Frame frame, Block currentBlock) {
		// If we don't have a frame then get or create the unnamed default frame for the block.
		if (frame==null) frame = currentBlock.getDefaultFrame();
		boolean newFrame = frame==null;
		if (newFrame) {
			frame = createFrame("", currentBlock.getSymbolScope());
			frame.setFrameScopeUnnamedDefault(currentBlock);
		}
		if (!frame.isInitialized()) {
			frame.initialize(currentBlock);
			if (!newFrame) {
				frameMRU.remove(frame);
				frameMRU.addFirst(frame);
			}
		}
		return frame;
	}
	
	
	/** Deals with the INPUT function.
	 * For a Field_ref node where it matches #(Field_ref INPUT ...), determine which
	 * frame field is being referenced.
	 * Sets the FieldContainer attribute (a Frame or Browse object) on the INPUT node.
	 * @see org.prorefactor.core.JPNode#getFieldContainer().
	 */
	FieldLookupResult inputFieldLookup(FieldRefNode fieldRefNode, SymbolScope currentScope) {
		JPNode inputNode = fieldRefNode.firstChild();
		assert inputNode.getType() == TokenTypes.INPUT;
		JPNode idNode = fieldRefNode.getIdNode();
		Field.Name inputName = new Field.Name(idNode.getText().toLowerCase());
		FieldContainer fieldContainer = null;
		Symbol fieldOrVariable = null;
		int nodeType = inputNode.nextSibling().getType();
		if (nodeType==TokenTypes.BROWSE || nodeType==TokenTypes.FRAME) {
			fieldContainer = (FieldContainer) inputNode.nextSibling().nextNode().getSymbol();
			fieldOrVariable = fieldContainer.lookupFieldOrVar(inputName);
		} else {
			for (Frame frame : frameMRU) {
				if (! frame.getScope().isActiveIn(currentScope)) continue;
				fieldOrVariable = frame.lookupFieldOrVar(inputName);
				if (fieldOrVariable!=null) {
					fieldContainer = frame;
					break;
				}
			}
		}
		if (fieldOrVariable==null) throw new Error("Could not find INPUT field " + idNode.getText() + " " + inputNode.getFilename() + ":" + inputNode.getLine());
		inputNode.setFieldContainer(fieldContainer);
		FieldLookupResult result = new FieldLookupResult();
		if (fieldOrVariable instanceof Variable) {
			// Variables cannot be abbreviated (or unqualified)
			result.variable = (Variable)fieldOrVariable;
		} else {
			result.field = (FieldBuffer)fieldOrVariable;
			Field.Name resName = new Field.Name(fieldOrVariable.fullName());
			if (inputName.table==null) result.isUnqualified = true;
			if (	inputName.field.length() < resName.field.length()
				||	(	inputName.table!=null
					&&	(inputName.table.length() < resName.table.length())
					)
				) result.isAbbreviated = true;
		}
		return result;
	}
	
	
	/** FOR|REPEAT|DO blocks need to be checked for explicit WITH FRAME phrase. */
	void nodeOfBlock(JPNode blockNode, Block currentBlock) {
		JPNode containerTypeNode = getContainerTypeNode(blockNode);
		if (containerTypeNode==null) return;
		// No such thing as DO WITH BROWSE...
		assert containerTypeNode.getType() == TokenTypes.FRAME;
		JPNode frameIDNode = containerTypeNode.nextNode();
		assert frameIDNode.getType() == TokenTypes.ID;
		Frame frame = frameRefSet(frameIDNode, currentBlock.getSymbolScope());
		frame.setFrameScopeBlockExplicitDefault(((BlockNode)blockNode).getBlock());
		blockNode.setFieldContainer(frame);
		containerForCurrentStatement = frame;
	}
	
	
	/** Called at tree parser DEFINE BROWSE statement. */
	void nodeOfDefineBrowse(Browse newBrowseSymbol) {
		containerForCurrentStatement = newBrowseSymbol;
	}
	
	
	/** Called at tree parser DEFINE FRAME statement.
	 * A DEFINE FRAME statement might hide a frame symbol from a higher symbol scope.
	 * A DEFINE FRAME statement is legal for a frame symbol already in use, sort of
	 * like how you can have multiple FORM statements, I suppose.
	 * A DEFINE FRAME statement does not initialize the frame's scope.
	 */
	void nodeOfDefineFrame(JPNode defNode, JPNode idNode, SymbolScope currentSymbolScope) {
		String frameName = idNode.getText();
		Frame frame = (Frame) currentSymbolScope.lookupSymbolLocally(TokenTypes.FRAME, frameName);
		if (frame==null) frame = createFrame(frameName, currentSymbolScope);
		frame.setDefOrIdNode(defNode);
		idNode.setLink(JPNode.SYMBOL, frame);
		defNode.setFieldContainer(frame);
		containerForCurrentStatement = frame;
	}
	
	
	/** For an IO/UI statement which would initialize a frame, compute the frame
	 * and set the frame attribute on the statement head node.
	 * This is not used from DEFINE FRAME, HIDE FRAME, or any other "frame" statements
	 * which would not count as a "reference" for frame scoping purposes.
	 */
	void nodeOfInitializingStatement(JPNode stateNode, Block currentBlock) {
		JPNode containerTypeNode = getContainerTypeNode(stateNode);
		JPNode idNode = null;
		if (containerTypeNode!=null) {
			idNode = containerTypeNode.nextNode();
			assert idNode.getType() == TokenTypes.ID;
		}
		if (containerTypeNode!=null && containerTypeNode.getType()==TokenTypes.BROWSE) {
			containerForCurrentStatement = browseRefSet(idNode, currentBlock.getSymbolScope());
		} else {
			Frame frame = null;
			if (idNode!=null) frame = frameRefSet(idNode, currentBlock.getSymbolScope());
			// This returns the frame whether it already exists or it creates it new.
			frame = initializeFrame(frame, currentBlock);
			containerForCurrentStatement = frame;
		}
		stateNode.setFieldContainer(containerForCurrentStatement);
	}

	
	/** For frame init statements like VIEW and CLEAR which have no frame phrase.
	 * Called at the end of the statement, after all symbols (including FRAME ID) have been resolved.
	 */
	void simpleFrameInitStatement(JPNode headNode, JPNode frameIDNode, Block currentBlock) {
		Frame frame = (Frame) frameIDNode.getSymbol();
		assert frame!=null;
		initializeFrame(frame, currentBlock);
		headNode.setFieldContainer(frame);
	}


	/** Called at the end of a frame affecting statement. */
	void statementEnd() {
		/* For something like DISPLAY customer, we delay adding the fields to the frame until
		 * the end of the statement. That's because any fields in an EXCEPT fields phrase need
		 * to have their symbols resolved first.
		 */
		if (currStatementWholeTableFormItemNode!=null) {
			ArrayList<FieldBuffer> fields = calculateFormItemTableFields(currStatementWholeTableFormItemNode);
			for (FieldBuffer fieldBuffer : fields) {
				containerForCurrentStatement.addSymbol(fieldBuffer);
			}
			currStatementWholeTableFormItemNode = null;
		}
		containerForCurrentStatement = null;
	}
	
	
}
