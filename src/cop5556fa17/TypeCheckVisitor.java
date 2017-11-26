package cop5556fa17;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super("line " + t.line + " pos " + t.pos_in_line + ": " + message);
			this.t = t;
		}

	}

	SymbolTable symTab = new SymbolTable();

	/**
	 * The program name is only used for naming the class. It does not rule out
	 * variables with the same name. It is returned for convenience.
	 *
	 * @throws Exception
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node : program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		if (declaration_Variable.e != null) {
			declaration_Variable.e.visit(this, null);
		}
		if (symTab.lookupNode(declaration_Variable.name)) {
			String message = "Visit Declaration Variable";
			throw new SemanticException(declaration_Variable.firstToken, message);
		}
		symTab.insertNode(declaration_Variable.name, declaration_Variable);
		declaration_Variable.setNodeType(TypeUtils.getType(declaration_Variable.type));
		if (declaration_Variable.e != null) {
			if (declaration_Variable.getNodeType() != declaration_Variable.e.getNodeType()) {
				String message = "Visit Declaration Variable";
				throw new SemanticException(declaration_Variable.firstToken, message);
			}
		}
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		if (expression_Binary.e0 != null) {
			expression_Binary.e0.visit(this, arg);
		}
		if (expression_Binary.e1 != null) {
			expression_Binary.e1.visit(this, arg);
		}
		if (expression_Binary.op == Kind.OP_EQ || expression_Binary.op == Kind.OP_NEQ) {
			expression_Binary.setNodeType(Type.BOOLEAN);
		} else if ((expression_Binary.op == Kind.OP_GE || expression_Binary.op == Kind.OP_GT
				|| expression_Binary.op == Kind.OP_LE || expression_Binary.op == Kind.OP_LT)
				&& expression_Binary.e0.getNodeType() == Type.INTEGER) {
			expression_Binary.setNodeType(Type.BOOLEAN);
		} else if ((expression_Binary.op == Kind.OP_AND || expression_Binary.op == Kind.OP_OR)
				&& (expression_Binary.e0.getNodeType() == Type.INTEGER
						|| expression_Binary.e0.getNodeType() == Type.BOOLEAN)) {
			expression_Binary.setNodeType(expression_Binary.e0.getNodeType());
		} else if ((expression_Binary.op == Kind.OP_DIV || expression_Binary.op == Kind.OP_MINUS
				|| expression_Binary.op == Kind.OP_MOD || expression_Binary.op == Kind.OP_PLUS
				|| expression_Binary.op == Kind.OP_POWER || expression_Binary.op == Kind.OP_TIMES)
				&& expression_Binary.e0.getNodeType() == Type.INTEGER) {
			expression_Binary.setNodeType(Type.INTEGER);
		} else {
			expression_Binary.setNodeType(null);
		}
		if (!(expression_Binary.e0.getNodeType() == expression_Binary.e1.getNodeType()
				&& expression_Binary.getNodeType() != null)) {
			String message = "Visit Binary Expression";
			throw new SemanticException(expression_Binary.firstToken, message);
		}
		return expression_Binary;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		if (expression_Unary.e != null) {
			expression_Unary.e.visit(this, null);
		}
		Type tempType = expression_Unary.e.getNodeType();
		if (expression_Unary.op == Kind.OP_EXCL && (tempType == Type.BOOLEAN || tempType == Type.INTEGER)) {
			expression_Unary.setNodeType(tempType);
		} else if ((expression_Unary.op == Kind.OP_PLUS || expression_Unary.op == Kind.OP_MINUS)
				&& tempType == Type.INTEGER) {
			expression_Unary.setNodeType(Type.INTEGER);
		} else {
			expression_Unary.setNodeType(null);
		}
		if (expression_Unary.getNodeType() == null) {
			String message = "Visit Unary Expression";
			throw new SemanticException(expression_Unary.firstToken, message);
		}
		return expression_Unary;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		if (index.e0 != null) {
			index.e0.visit(this, arg);
		}
		if (index.e1 != null) {
			index.e1.visit(this, arg);
		}
		if (index.e0.getNodeType() == Type.INTEGER && index.e1.getNodeType() == Type.INTEGER) {
			index.setCartesian(!(index.e0.firstToken.kind == Kind.KW_r && index.e1.firstToken.kind == Kind.KW_a));
		} else {
			String message = "Visit Index";
			throw new SemanticException(index.firstToken, message);
		}
		return index;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		if (expression_PixelSelector.index != null) {
			expression_PixelSelector.index.visit(this, null);
		}
		Declaration tempDec = symTab.getNode(expression_PixelSelector.name);
		if (tempDec.getNodeType() == Type.IMAGE) {
			expression_PixelSelector.setNodeType(Type.INTEGER);
		} else if (expression_PixelSelector.index == null) {
			expression_PixelSelector.setNodeType(tempDec.getNodeType());
		} else {
			expression_PixelSelector.setNodeType(null);
		}
		if (expression_PixelSelector.getNodeType() == null) {
			String message = "Visit Pixel Selector Expression";
			throw new SemanticException(expression_PixelSelector.firstToken, message);
		}
		return expression_PixelSelector;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		if (expression_Conditional.condition != null) {
			expression_Conditional.condition.visit(this, arg);
		}
		if (expression_Conditional.trueExpression != null) {
			expression_Conditional.trueExpression.visit(this, arg);
		}
		if (expression_Conditional.falseExpression != null) {
			expression_Conditional.falseExpression.visit(this, arg);
		}
		if (expression_Conditional.condition.getNodeType() == Type.BOOLEAN && expression_Conditional.trueExpression
				.getNodeType() == expression_Conditional.falseExpression.getNodeType()) {
			expression_Conditional.setNodeType(expression_Conditional.trueExpression.getNodeType());
		} else {
			String message = "Visit Conditional Expression";
			throw new SemanticException(expression_Conditional.firstToken, message);
		}
		return expression_Conditional;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		if (declaration_Image.source != null) {
			declaration_Image.source.visit(this, null);
		}
		if (declaration_Image.xSize != null) {
			declaration_Image.xSize.visit(this, null);
		}
		if (declaration_Image.ySize != null) {
			declaration_Image.ySize.visit(this, null);
		}
		if (symTab.lookupNode(declaration_Image.name)) {
			String message = "Visit Image Declaration";
			throw new SemanticException(declaration_Image.firstToken, message);
		}
		symTab.insertNode(declaration_Image.name, declaration_Image);
		declaration_Image.setNodeType(Type.IMAGE);

		if (declaration_Image.xSize != null) {
			if (!(declaration_Image.ySize != null && declaration_Image.xSize.getNodeType() == Type.INTEGER
					&& declaration_Image.ySize.getNodeType() == Type.INTEGER)) {
				String message = "Visit Image Declaration";
				throw new SemanticException(declaration_Image.firstToken, message);
			}
		}
		return declaration_Image;
	}

	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		try {
			new URL(source_StringLiteral.fileOrUrl).toURI();
			source_StringLiteral.nodeType = Type.URL;
		} catch (URISyntaxException exception) {
			source_StringLiteral.nodeType = Type.FILE;
		} catch (MalformedURLException exception) {
			source_StringLiteral.nodeType = Type.FILE;
		}
		return source_StringLiteral;
	}

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		if (source_CommandLineParam.paramNum != null) {
			source_CommandLineParam.paramNum.visit(this, null);
		}
		source_CommandLineParam.nodeType = source_CommandLineParam.paramNum.getNodeType();
		if (source_CommandLineParam.nodeType != Type.INTEGER) {
			String message = "Visit Source Command Line Parameter";
			throw new SemanticException(source_CommandLineParam.firstToken, message);
		}
		return source_CommandLineParam;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		if (!symTab.lookupNode(source_Ident.name)) {
			String message = "Visit Source Identifier";
			throw new SemanticException(source_Ident.firstToken, message);
		}
		source_Ident.nodeType = symTab.getNode(source_Ident.name).getNodeType();
		if (!(source_Ident.nodeType == Type.FILE || source_Ident.nodeType == Type.URL)) {
			String message = "Source Ident Type in Visit Source Identifier is not a File or URL";
			throw new SemanticException(source_Ident.firstToken, message);
		}
		return source_Ident;
	}

	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		String message = "Visit Source Sink Declaration";
		if (declaration_SourceSink.source != null) {
			declaration_SourceSink.source.visit(this, null);
		}
		if (symTab.lookupNode(declaration_SourceSink.name)) {
			throw new SemanticException(declaration_SourceSink.firstToken, message);
		}
		symTab.insertNode(declaration_SourceSink.name, declaration_SourceSink);
		switch (declaration_SourceSink.type) {
		case KW_file:
			declaration_SourceSink.setNodeType(Type.FILE);
			break;
		case KW_url:
			declaration_SourceSink.setNodeType(Type.URL);
			break;
		default:
			throw new SemanticException(declaration_SourceSink.firstToken, message);
		}
		if (declaration_SourceSink.getNodeType() != declaration_SourceSink.source.nodeType) {
			throw new SemanticException(declaration_SourceSink.firstToken, message);
		}
		return declaration_SourceSink;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		expression_IntLit.setNodeType(Type.INTEGER);
		return expression_IntLit;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		if (expression_FunctionAppWithExprArg.arg != null) {
			expression_FunctionAppWithExprArg.arg.visit(this, null);
		}
		if (expression_FunctionAppWithExprArg.arg.getNodeType() != Type.INTEGER) {
			String message = "Visit Function Application with Expression Argument";
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, message);
		} else {
			expression_FunctionAppWithExprArg.setNodeType(Type.INTEGER);
		}
		return expression_FunctionAppWithExprArg;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		if (expression_FunctionAppWithIndexArg.arg != null) {
			expression_FunctionAppWithIndexArg.arg.visit(this, arg);
		}
		expression_FunctionAppWithIndexArg.setNodeType(Type.INTEGER);
		return expression_FunctionAppWithIndexArg;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		expression_PredefinedName.setNodeType(Type.INTEGER);
		return expression_PredefinedName;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		String message = "Visit Statement Out";
		if (statement_Out.sink != null) {
			statement_Out.sink.visit(this, null);
		}
		if (!symTab.lookupNode(statement_Out.name)) {
			throw new SemanticException(statement_Out.firstToken, message);
		}
		Declaration name = symTab.getNode(statement_Out.name);

		if (((name.getNodeType() == Type.INTEGER || name.getNodeType() == Type.BOOLEAN)
				&& statement_Out.sink.nodeType == Type.SCREEN)
				|| (name.getNodeType() == Type.IMAGE
						&& (statement_Out.sink.nodeType == Type.FILE || statement_Out.sink.nodeType == Type.SCREEN))) {
			statement_Out.setDec(name);
		} else {
			throw new SemanticException(statement_Out.firstToken, message);
		}
		return statement_Out;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		String message = "Visit Statement In";
		if (symTab.lookupNode(statement_In.name)) {
			statement_In.source.visit(this, null);
			Declaration name = symTab.getNode(statement_In.name);
			statement_In.setDec(name);
		} else {
			throw new SemanticException(statement_In.firstToken, message);
		}
		return statement_In;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		if (statement_Assign.lhs != null) {
			statement_Assign.lhs.visit(this, null);
		}
		if (statement_Assign.e != null) {
			statement_Assign.e.visit(this, null);
		}
		if (statement_Assign.lhs.getNodeType() == statement_Assign.e.getNodeType()) {
			statement_Assign.setCartesian(statement_Assign.lhs.isCartesian);
		} else {
			String message = "Visit Statement Assignment";
			throw new SemanticException(statement_Assign.firstToken, message);
		}
		return statement_Assign;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		String message = "Visit LHS";
		if (lhs.index != null) {
			lhs.index.visit(this, arg);
		}
		if (!symTab.lookupNode(lhs.name)) {
			throw new SemanticException(lhs.firstToken, message);
		}
		lhs.declaration = symTab.getNode(lhs.name);
		lhs.setNodeType(lhs.declaration.getNodeType());
		lhs.isCartesian = lhs.index != null ? lhs.index.isCartesian() : false;
		return lhs;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		sink_SCREEN.nodeType = Type.SCREEN;
		return sink_SCREEN;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		if (!symTab.lookupNode(sink_Ident.name)) {
			String message = "Visit Sink Ident not found in Symbol Table";
			throw new SemanticException(sink_Ident.firstToken, message);
		}
		sink_Ident.nodeType = symTab.getNode(sink_Ident.name).getNodeType();
		if (sink_Ident.nodeType == Type.FILE) {
			// Do Nothing
		} else {
			String message = "Visit Sink Ident";
			throw new SemanticException(sink_Ident.firstToken, message);
		}
		return sink_Ident;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		expression_BooleanLit.setNodeType(Type.BOOLEAN);
		return expression_BooleanLit;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident, Object arg) throws Exception {
		String message = "Visit Expression Identifier";
		if (symTab.lookupNode(expression_Ident.name)) {
			expression_Ident.setNodeType(symTab.getNode(expression_Ident.name).getNodeType());
		} else {
			throw new SemanticException(expression_Ident.firstToken, message);
		}
		return expression_Ident;
	}

}
