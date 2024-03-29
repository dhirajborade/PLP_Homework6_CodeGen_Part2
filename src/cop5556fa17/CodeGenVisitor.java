package cop5556fa17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
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
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		cw.visitField(ACC_STATIC, "x", "I", null, Integer.valueOf(0)).visitEnd();

		// if GRADE, generates code to add string to log

		cw.visitField(ACC_STATIC, "y", "I", null, Integer.valueOf(0)).visitEnd();

		// CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		// and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		// generates code to add string to log
		// CodeGenUtils.genLog(GRADE, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);

		// handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		// Sets max stack size and number of local vars.
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily set the parameter in the ClassWriter constructor to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		String description;
		if (declaration_Variable.e != null) {
			declaration_Variable.e.visit(this, arg);
		}
		Type decVar = declaration_Variable.getNodeType();
		switch (decVar) {
		case INTEGER:
			description = "I";
			cw.visitField(ACC_STATIC, declaration_Variable.name, description, null, Integer.valueOf(0)).visitEnd();
			break;
		case BOOLEAN:
			description = "Z";
			cw.visitField(ACC_STATIC, declaration_Variable.name, description, null, Boolean.valueOf(false)).visitEnd();
			break;
		default:
			throw new UnsupportedOperationException();
		}
		if (declaration_Variable.e != null) {
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, description);
		}
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		Label argOne = new Label();
		Label argTwo = new Label();
		if (expression_Binary.e0 != null) {
			expression_Binary.e0.visit(this, arg);
		}
		if (expression_Binary.e1 != null) {
			expression_Binary.e1.visit(this, arg);
		}
		Kind exprBinaryOp = expression_Binary.op;
		switch (exprBinaryOp) {
		case OP_PLUS:
			mv.visitInsn(IADD);
			break;
		case OP_MINUS:
			mv.visitInsn(ISUB);
			break;
		case OP_TIMES:
			mv.visitInsn(IMUL);
			break;
		case OP_DIV:
			mv.visitInsn(IDIV);
			break;
		case OP_MOD:
			mv.visitInsn(IREM);
			break;
		case OP_AND:
			mv.visitInsn(IAND);
			break;
		case OP_OR:
			mv.visitInsn(IOR);
			break;
		case OP_EQ:
			mv.visitJumpInsn(IF_ICMPEQ, argOne);
			mv.visitLdcInsn(Boolean.valueOf(false));
			mv.visitJumpInsn(GOTO, argTwo);
			mv.visitLabel(argOne);
			mv.visitLdcInsn(Boolean.valueOf(true));
			mv.visitLabel(argTwo);
			break;
		case OP_NEQ:
			mv.visitJumpInsn(IF_ICMPNE, argOne);
			mv.visitLdcInsn(Boolean.valueOf(false));
			mv.visitJumpInsn(GOTO, argTwo);
			mv.visitLabel(argOne);
			mv.visitLdcInsn(Boolean.valueOf(true));
			mv.visitLabel(argTwo);
			break;
		case OP_GE:
			mv.visitJumpInsn(IF_ICMPGE, argOne);
			mv.visitLdcInsn(Boolean.valueOf(false));
			mv.visitJumpInsn(GOTO, argTwo);
			mv.visitLabel(argOne);
			mv.visitLdcInsn(Boolean.valueOf(true));
			mv.visitLabel(argTwo);
			break;
		case OP_GT:
			mv.visitJumpInsn(IF_ICMPGT, argOne);
			mv.visitLdcInsn(Boolean.valueOf(false));
			mv.visitJumpInsn(GOTO, argTwo);
			mv.visitLabel(argOne);
			mv.visitLdcInsn(Boolean.valueOf(true));
			mv.visitLabel(argTwo);
			break;
		case OP_LE:
			mv.visitJumpInsn(IF_ICMPLE, argOne);
			mv.visitLdcInsn(Boolean.valueOf(false));
			mv.visitJumpInsn(GOTO, argTwo);
			mv.visitLabel(argOne);
			mv.visitLdcInsn(Boolean.valueOf(true));
			mv.visitLabel(argTwo);
			break;
		case OP_LT:
			mv.visitJumpInsn(IF_ICMPLT, argOne);
			mv.visitLdcInsn(Boolean.valueOf(false));
			mv.visitJumpInsn(GOTO, argTwo);
			mv.visitLabel(argOne);
			mv.visitLdcInsn(Boolean.valueOf(true));
			mv.visitLabel(argTwo);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		if (expression_Unary.e != null) {
			expression_Unary.e.visit(this, arg);
		}
		Kind exprUnaryOp = expression_Unary.op;
		Type exprUnaryType = expression_Unary.e.getNodeType();
		switch (exprUnaryOp) {
		case OP_PLUS:
			break;
		case OP_MINUS:
			mv.visitInsn(INEG);
			break;
		case OP_EXCL:
			switch (exprUnaryType) {
			case BOOLEAN:
				mv.visitInsn(ICONST_1);
				mv.visitInsn(IXOR);
				break;
			case INTEGER:
				mv.visitLdcInsn(Integer.valueOf(Integer.MAX_VALUE));
				mv.visitInsn(IXOR);
				break;
			default:
				throw new UnsupportedOperationException();
			}
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		if (index.e0 != null) {
			index.e0.visit(this, arg);
		}
		if (index.e1 != null) {
			index.e1.visit(this, arg);
		}
		if (index.isCartesian()) {
			return null;
		}
		mv.visitInsn(DUP2);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
		mv.visitInsn(DUP_X2);
		mv.visitInsn(POP);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, ImageSupport.ImageDesc);
		if (expression_PixelSelector.index != null) {
			expression_PixelSelector.index.visit(this, arg);
		}
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig, false);
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		if (expression_Conditional.condition != null) {
			expression_Conditional.condition.visit(this, arg);
		}
		Label argOne = new Label();
		Label argTwo = new Label();
		mv.visitJumpInsn(IFNE, argOne);
		if (expression_Conditional.falseExpression != null) {
			expression_Conditional.falseExpression.visit(this, arg);
		}
		mv.visitJumpInsn(GOTO, argTwo);
		mv.visitLabel(argOne);
		if (expression_Conditional.trueExpression != null) {
			expression_Conditional.trueExpression.visit(this, arg);
		}
		mv.visitLabel(argTwo);
		return null;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		Type declarationImage = declaration_Image.getNodeType();
		switch (declarationImage) {
		case IMAGE:
			cw.visitField(ACC_STATIC, declaration_Image.name, ImageSupport.ImageDesc, null, null).visitEnd();
			if (declaration_Image.source != null) {
				declaration_Image.source.visit(this, arg);

				if (declaration_Image.xSize != null && declaration_Image.ySize != null) {
					declaration_Image.xSize.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					declaration_Image.ySize.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);

				} else {
					mv.visitInsn(ACONST_NULL);
					mv.visitInsn(ACONST_NULL);
				}

				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			} else {
				if (declaration_Image.xSize != null && declaration_Image.ySize != null) {
					declaration_Image.xSize.visit(this, arg);
					declaration_Image.ySize.visit(this, arg);

				} else {
					mv.visitLdcInsn(256);
					mv.visitLdcInsn(256);
				}
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig, false);
			}
			break;

		default:
			throw new UnsupportedOperationException();
		}
		mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, ImageSupport.ImageDesc);
		return null;
	}

	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		mv.visitLdcInsn(String.valueOf(source_StringLiteral.fileOrUrl));
		return null;
	}

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD, 0);
		if (source_CommandLineParam.paramNum != null) {
			source_CommandLineParam.paramNum.visit(this, arg);
		}
		mv.visitInsn(AALOAD);
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, ImageSupport.StringDesc);
		return null;
	}

	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		cw.visitField(ACC_STATIC, declaration_SourceSink.name, ImageSupport.StringDesc, null, null).visitEnd();
		if (declaration_SourceSink.source != null) {
			declaration_SourceSink.source.visit(this, arg);
		}
		mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.name, ImageSupport.StringDesc);
		return null;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		mv.visitLdcInsn(Integer.valueOf(expression_IntLit.value));
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		if (expression_FunctionAppWithExprArg.arg != null) {
			expression_FunctionAppWithExprArg.arg.visit(this, arg);
		}
		Kind functionAppExprArgKind = expression_FunctionAppWithExprArg.getFunction();
		switch (functionAppExprArgKind) {
		case KW_abs:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
			break;
		case KW_log:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		if (expression_FunctionAppWithIndexArg.arg.e0 != null) {
			expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
		}
		if (expression_FunctionAppWithIndexArg.arg.e1 != null) {
			expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);
		}
		Kind functionAppIndexArgKind = expression_FunctionAppWithIndexArg.getFunction();
		switch (functionAppIndexArgKind) {
		case KW_cart_x:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			break;
		case KW_cart_y:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_xSig, false);
			break;
		case KW_polar_r:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
			break;
		case KW_polar_a:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		Kind predefinedName = expression_PredefinedName.kind;
		switch (predefinedName) {
		case KW_x:
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			break;
		case KW_y:
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			break;
		case KW_X:
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			break;
		case KW_Y:
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			break;
		case KW_r:
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
			break;
		case KW_a:
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
			break;
		case KW_Z:
			mv.visitLdcInsn(16777215);
			break;
		case KW_DEF_X:
			mv.visitLdcInsn(256);
			break;
		case KW_DEF_Y:
			mv.visitLdcInsn(256);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
	}

	/**
	 * For Integers and booleans, the only "sink"is the screen, so generate code to
	 * print to console. For Images, load the Image onto the stack and visit the
	 * Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		Type statementOutType = statement_Out.getDec().getNodeType();
		switch (statementOutType) {
		case INTEGER:
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "I");
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().getNodeType());
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(I)V", false);
			break;
		case BOOLEAN:
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Z");
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().getNodeType());
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Z)V", false);
			break;
		case IMAGE:
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, ImageSupport.ImageDesc);
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().getNodeType());
			statement_Out.sink.visit(this, arg);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 *
	 * In HW5, you only need to handle INTEGER and BOOLEAN Use
	 * java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean to convert
	 * String to actual type.
	 *
	 * In HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		if (statement_In.source != null) {
			statement_In.source.visit(this, arg);
		}
		Type statementInType = statement_In.getDec().getNodeType();
		switch (statementInType) {
		case INTEGER:
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");
			break;
		case BOOLEAN:
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");
			break;
		case IMAGE:
			Declaration_Image declaration_Image = (Declaration_Image) statement_In.getDec();
			if (declaration_Image.xSize != null && declaration_Image.ySize != null) {
				mv.visitFieldInsn(GETSTATIC, className, statement_In.name, ImageSupport.ImageDesc);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				mv.visitFieldInsn(GETSTATIC, className, statement_In.name, ImageSupport.ImageDesc);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			} else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, ImageSupport.ImageDesc);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */

	public Object visitStatement_Transform(Statement_Assign statement_Assign, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		Type lhsType = lhs.getNodeType();
		switch (lhsType) {
		case INTEGER:
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "I");
			break;
		case BOOLEAN:
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "Z");
			break;
		case IMAGE:
			mv.visitFieldInsn(GETSTATIC, className, lhs.name, ImageSupport.ImageDesc);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig, false);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		mv.visitMethodInsn(INVOKESTATIC, ImageFrame.className, "makeFrame", ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, ImageSupport.StringDesc);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		mv.visitLdcInsn(Boolean.valueOf(expression_BooleanLit.value));
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident, Object arg) throws Exception {
		Type exprIdent = expression_Ident.getNodeType();
		switch (exprIdent) {
		case INTEGER:
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "I");
			break;
		case BOOLEAN:
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "Z");
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		Type stateAssignType = statement_Assign.lhs.getNodeType();
		switch (stateAssignType) {
		case INTEGER:
		case BOOLEAN:
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
			break;
		case IMAGE:
			Label xStart = new Label();
			Label xEnd = new Label();
			Label yStart = new Label();
			Label yEnd = new Label();

			mv.visitLdcInsn(0);
			mv.visitLabel(yStart);
			mv.visitInsn(DUP);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			mv.visitJumpInsn(IF_ICMPEQ, yEnd);
			mv.visitLdcInsn(0);

			mv.visitLabel(xStart);
			mv.visitInsn(DUP);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitJumpInsn(IF_ICMPEQ, xEnd);

			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);

			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitJumpInsn(GOTO, xStart);
			mv.visitLabel(xEnd);
			mv.visitInsn(POP);

			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitJumpInsn(GOTO, yStart);
			mv.visitLabel(yEnd);
			mv.visitInsn(POP);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
	}

}
