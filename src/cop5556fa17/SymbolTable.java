/**
 * 
 */
package cop5556fa17;

import java.util.HashMap;
import java.util.Stack;

import cop5556fa17.TypeCheckVisitor.SemanticException;
import cop5556fa17.AST.Declaration;

/**
 * @author Dhiraj Borade
 *
 */
public class SymbolTable {

	private int currentScope;
	private int nextScope;
	private Stack<Integer> scopeStack;
	private HashMap<String, Declaration> symbolTable;

	public SymbolTable() {
		this.currentScope = 0;
		this.nextScope = 1;
		this.scopeStack = new Stack<Integer>();
		this.scopeStack.push(0);
		this.symbolTable = new HashMap<String, Declaration>();
	}

	public void enterScope() {
		this.currentScope = this.nextScope++;
		scopeStack.push(this.currentScope);
	}

	public void leaveScope() {
		scopeStack.pop();
		this.currentScope = scopeStack.peek();
	}

	public boolean insertNode(String ident, Declaration dec) throws SemanticException {
		boolean result = false;
		if (!this.symbolTable.containsKey(ident)) {
			this.symbolTable.put(ident, dec);
			result = true;
		}
		return result;
	}

	public boolean lookupNode(String ident) {
		boolean result = false;
		if (this.symbolTable.containsKey(ident)) {
			result = true;
		}
		return result;
	}

	public Declaration getNode(String ident) {
		Declaration decNode = null;
		if (this.lookupNode(ident)) {
			decNode = this.symbolTable.get(ident);
		}
		return decNode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append("Scope Stack: ").append(this.scopeStack.size()).append(" Symbol Table: ")
				.append(this.symbolTable.size()).toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
