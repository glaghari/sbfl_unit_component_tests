package extract;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import antlr.JavaBaseListener;
import antlr.JavaParser;


public class MethodListener extends JavaBaseListener {
	private Clazz clazz = null;
	public MethodListener(Clazz clazz) {
		this.clazz = clazz;
	}
		
	@Override public void enterMethodBody(JavaParser.MethodBodyContext ctx) {
//		int line = -1;
//		if(ctx.block().blockStatement(0) != null) {
//			line = ctx.block().blockStatement(0).start.getLine();	
//		}
//		else {
//			line = ctx.block().stop.getLine();
//		}
		
//		int startLine = ctx.start.getLine();
//		int endLine = ctx.stop.getLine();
//		if(IntInterval.match(startLine, endLine)) {
//			Method method = new Method(startLine, endLine);
//			this.clazz.add(method);
//		}
		
		ctx.block().enterRule(this);
		
		// For statements
		StatementListener statementListener = new StatementListener(this.clazz);
		ctx.block().enterRule(statementListener);
	}
	
	@Override public void enterConstructorBody(JavaParser.ConstructorBodyContext ctx) {
//		int line = -1;
//		if(ctx.block().blockStatement(0) != null) {
//			line = ctx.block().blockStatement(0).start.getLine();	
//		}
//		else {
//			line = ctx.block().stop.getLine();
//		}
		
//		int startLine = ctx.start.getLine();
//		int endLine = ctx.stop.getLine();
//		if(IntInterval.match(startLine, endLine)) {
//			Method method = new Method(startLine, endLine);
//			this.clazz.add(method);
//		}
		
		addMethod(ctx);
		
		// For statements processing
		StatementListener statementListener = new StatementListener(this.clazz);
		if(ctx.block() != null)
			ctx.block().enterRule(statementListener);
	}
	
	@Override public void enterBlock(JavaParser.BlockContext ctx) {
		if(ctx.blockStatement(0) == null)
			return;

//		ctx.blockStatement().forEach(blockStatement -> blockStatement.enterRule(this));
		for(JavaParser.BlockStatementContext blockStatement : ctx.blockStatement()) {
			blockStatement.enterRule(this);
		}
	}
	
	@Override public void enterBlockStatement(JavaParser.BlockStatementContext ctx) {
		if(ctx.statement() != null) {
			ctx.statement().enterRule(this);
		}
		
		else if(ctx.localVariableDeclarationStatement() != null) {
			for(JavaParser.VariableDeclaratorContext variableDeclarator : ctx.localVariableDeclarationStatement().localVariableDeclaration().variableDeclarators().variableDeclarator()) {
				JavaParser.VariableInitializerContext variableInitializerContext = variableDeclarator.variableInitializer();
				if(variableInitializerContext != null && variableInitializerContext.expression() != null)
					variableInitializerContext.expression().enterRule(this);
			}
		}
		else if (ctx.typeDeclaration() != null) {
			if(ctx.typeDeclaration().classDeclaration() != null) {
				Clazz innerClazz = new Clazz();
				this.clazz.addAInnerClazz(innerClazz);
				ClassListener classListener = new ClassListener(innerClazz);
				ctx.typeDeclaration().classDeclaration().enterRule(classListener);
			}
			else if(ctx.typeDeclaration().enumDeclaration() != null) {
				Clazz innerClazz = new Clazz();
				this.clazz.addAInnerClazz(innerClazz);
				ClassListener classListener = new ClassListener(innerClazz);
				ctx.typeDeclaration().enumDeclaration().enterRule(classListener);
			}
			else if(ctx.typeDeclaration().interfaceDeclaration() != null) {
				Clazz innerClazz = new Clazz();
				this.clazz.addAInnerClazz(innerClazz);
				ClassListener classListener = new ClassListener(innerClazz);
				ctx.typeDeclaration().interfaceDeclaration().enterRule(classListener);
			}
			else if(ctx.typeDeclaration().annotationTypeDeclaration() != null) {
				Clazz innerClazz = new Clazz();
				this.clazz.addAInnerClazz(innerClazz);
				ClassListener classListener = new ClassListener(innerClazz);
				ctx.typeDeclaration().annotationTypeDeclaration().enterRule(classListener);
			}
		}
	}
	
	@Override public void enterStatement(JavaParser.StatementContext ctx) {
//		System.out.println("Enter statement " + ctx.start.getLine());
		
		if(ctx.block() != null) {
			ctx.block().enterRule(this);
		}
		
		if(ctx.statementExpression() != null) {
			ctx.statementExpression().enterRule(this);
		}
		
		if(ctx.statement() != null) {
//			ctx.statement().forEach(statement -> statement.enterRule(this));
			for(JavaParser.StatementContext statement : ctx.statement()) {
				statement.enterRule(this);
			}
		}
		
		if(ctx.parExpression() != null) {
			ctx.parExpression().expression().enterRule(this);
		}
		
		if(ctx.expression() != null) {
//			ctx.expression().forEach(expression -> expression.enterRule(this));
			for(JavaParser.ExpressionContext expression : ctx.expression()) {
				expression.enterRule(this);
			}
		}

	}
	
	@Override public void enterStatementExpression(JavaParser.StatementExpressionContext ctx) {
		ctx.expression().enterRule(this);
	}
	
	@Override public void enterExpression(JavaParser.ExpressionContext ctx) {
//		System.out.println("Enter expression " + ctx.start.getLine() + "-" + ctx.stop.getLine() + " --- " + ctx.getText() );
		
//		for(JavaParser.ExpressionContext eCtx : ctx.expression()) {
//			eCtx.enterRule(this);
//		}
		
		if(ctx.expression() != null)
			for(JavaParser.ExpressionContext eCtx : ctx.expression()) {
				eCtx.enterRule(this);
			}
		
		if(ctx.expressionList() != null)
			for(JavaParser.ExpressionContext eCtx : ctx.expressionList().expression()) {
				eCtx.enterRule(this);
			}
		
		if(ctx.explicitGenericInvocation() != null) {
			JavaParser.ExplicitGenericInvocationSuffixContext sufCtx = ctx.explicitGenericInvocation().explicitGenericInvocationSuffix();
			List<JavaParser.ExpressionContext> lstECtx = null;
			if(sufCtx.superSuffix() != null && sufCtx.superSuffix().arguments() != null && sufCtx.superSuffix().arguments().expressionList() != null)
				lstECtx = sufCtx.superSuffix().arguments().expressionList().expression();
			else if(sufCtx.arguments().expressionList() != null)
				lstECtx = sufCtx.arguments().expressionList().expression();
			
			if(lstECtx != null)
				for(JavaParser.ExpressionContext eCtx :  lstECtx) {
					eCtx.enterRule(this);
				}
		}
		
		if(ctx.creator() != null && ctx.creator().classCreatorRest() != null) {
//			System.out.println("Enter creator " + ctx.start.getLine());
			ctx.creator().classCreatorRest().enterRule(this);
		}
		
		if(ctx.innerCreator() != null) {
//			System.out.println("Enter inner creator " + ctx.start.getLine());
			ctx.innerCreator().classCreatorRest().enterRule(this);
		}
	}
		
	@Override public void enterClassCreatorRest(JavaParser.ClassCreatorRestContext ctx) {
		if(ctx.classBody() != null) {
//			System.out.println("Enter anonymous class");
//			System.out.println(this.clazz.getClassName());
			Clazz anonymousClazz = new Clazz(this.clazz.getAnonymousCounter());
			// This is for constructor in anonymous class
			int startLine = ctx.start.getLine();
			int endLine = ctx.stop.getLine();

			if(IntInterval.match(startLine, endLine)) {
				Method method = new Method(startLine, endLine);
				anonymousClazz.add(method);
			}
			
			this.clazz.addAnonymousClass(anonymousClazz);
			ClassListener classListener = new ClassListener(anonymousClazz);
			
//			ctx.classBody().classBodyDeclaration().forEach(classBodyDeclaration -> classBodyDeclaration.enterRule(classListener));
			for(JavaParser.ClassBodyDeclarationContext classBodyDeclaration : ctx.classBody().classBodyDeclaration()) {
				classBodyDeclaration.enterRule(classListener);
			}
		}
	}

	public void addMethod(ParserRuleContext ctx) {
		int startLine = ctx.start.getLine();
		int endLine = ctx.stop.getLine();
		
		if(!IntInterval.match(startLine, endLine))
			return;
		
		Method method = new Method(startLine, endLine);
		this.clazz.add(method);
	}
}
