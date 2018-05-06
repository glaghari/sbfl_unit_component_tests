package extract;

import java.util.List;

import antlr.JavaBaseListener;
import antlr.JavaParser;


public class StatementListener extends JavaBaseListener {
	private Clazz clazz = null;
	public StatementListener(Clazz clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public void enterBlock(JavaParser.BlockContext ctx) {
		if(ctx.blockStatement(0) == null)
			return;

//		ctx.blockStatement().forEach(blockStatement -> blockStatement.enterRule(this));
		for(JavaParser.BlockStatementContext blockStatement : ctx.blockStatement()) {
			blockStatement.enterRule(this);
		}
	}
	
	@Override
	public void enterBlockStatement(JavaParser.BlockStatementContext ctx) {
		if(ctx.statement() != null) {
			ctx.statement().enterRule(this);
		}
		
		if(ctx.localVariableDeclarationStatement() != null) {
//			ctx.localVariableDeclarationStatement().localVariableDeclaration().variableDeclarators().variableDeclarator().forEach(variableDeclarator -> variableDeclarator.enterRule(this));
			for(JavaParser.VariableDeclaratorContext variableDeclarator : ctx.localVariableDeclarationStatement().localVariableDeclaration().variableDeclarators().variableDeclarator()) {
				variableDeclarator.enterRule(this);
			}
		}
		
		if(ctx.typeDeclaration() != null) {
			if(ctx.typeDeclaration().classDeclaration() != null) {
				String className = (this.clazz.readOnlyAnonymousCounter()) + ctx.typeDeclaration().classDeclaration().Identifier().getText();
				Clazz innerClazz = new Clazz();
				innerClazz.setClassName(className);
				this.clazz.addAInnerClazz(innerClazz);
				ClassListener classListener = new ClassListener(innerClazz);
				//			ctx.typeDeclaration().classDeclaration().classBody().classBodyDeclaration().forEach(classBodyDeclaration -> classBodyDeclaration.enterRule(classListener));
				for(JavaParser.ClassBodyDeclarationContext classBodyDeclaration : ctx.typeDeclaration().classDeclaration().classBody().classBodyDeclaration()) {
					classBodyDeclaration.enterRule(classListener);
				}
			}
			
			else if(ctx.typeDeclaration().enumDeclaration() != null) {
				String className = (this.clazz.readOnlyAnonymousCounter()) + ctx.typeDeclaration().enumDeclaration().Identifier().getText();
				Clazz innerClazz = new Clazz();
				innerClazz.setClassName(className);
				this.clazz.addAInnerClazz(innerClazz);
				ClassListener classListener = new ClassListener(innerClazz);
				//			ctx.typeDeclaration().enumDeclaration().enumBodyDeclarations().forEach(classBodyDeclaration -> classBodyDeclaration.enterRule(classListener));
				for(JavaParser.ClassBodyDeclarationContext classBodyDeclaration : ctx.typeDeclaration().enumDeclaration().enumBodyDeclarations().classBodyDeclaration()) {
					classBodyDeclaration.enterRule(classListener);
				}
			}
			
			else if(ctx.typeDeclaration().interfaceDeclaration() != null) {
				String className = (this.clazz.readOnlyAnonymousCounter()) + ctx.typeDeclaration().interfaceDeclaration().Identifier().getText();
				Clazz innerClazz = new Clazz();
				innerClazz.setClassName(className);
				this.clazz.addAInnerClazz(innerClazz);
				ClassListener classListener = new ClassListener(innerClazz);
				for(JavaParser.InterfaceBodyDeclarationContext interfaceBodyDeclaration : ctx.typeDeclaration().interfaceDeclaration().interfaceBody().interfaceBodyDeclaration()) {
					interfaceBodyDeclaration.enterRule(classListener);
				}
			}
			
			else if(ctx.typeDeclaration().annotationTypeDeclaration() != null) {
				String className = (this.clazz.readOnlyAnonymousCounter()) + ctx.typeDeclaration().annotationTypeDeclaration().Identifier().getText();
				Clazz innerClazz = new Clazz();
				innerClazz.setClassName(className);
				this.clazz.addAInnerClazz(innerClazz);
				ClassListener classListener = new ClassListener(innerClazz);
				for(JavaParser.AnnotationTypeElementDeclarationContext annoCtx : ctx.typeDeclaration().annotationTypeDeclaration().annotationTypeBody().annotationTypeElementDeclaration()) {
					annoCtx.annotationTypeElementRest().enterRule(classListener);
				}
			}
		}

	}
	
	@Override
	public void enterStatement(JavaParser.StatementContext ctx) {
//		System.out.println("Enter statement " + ctx.start.getLine());
//		System.out.println(ctx.getText());
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
		
		if(ctx.expression() != null) {
//			ctx.expression().forEach(expression -> expression.enterRule(this));
			for(JavaParser.ExpressionContext expression : ctx.expression()) {
				expression.enterRule(this);
			}
		}
		
		if(ctx.parExpression() != null) {
			ctx.parExpression().expression().enterRule(this);
		}
		
		if(ctx.forControl() != null) {
			int startLine = ctx.start.getLine();
			int endLine = ctx.stop.getLine();
			if(IntInterval.match(startLine, endLine)) {
				Statement statement = new Statement(startLine);
				this.clazz.add(statement);
			}
		}
		
		if(ctx.catchClause() != null) {
//			ctx.catchClause().forEach(catchClause -> catchClause.enterRule(this));
			for(JavaParser.CatchClauseContext catchClause : ctx.catchClause()) {
				catchClause.enterRule(this);
			}
		}
		
		if(ctx.finallyBlock() != null) {
			int startLine = ctx.finallyBlock().start.getLine();
			int endLine = ctx.finallyBlock().stop.getLine();
			if(IntInterval.match(startLine, endLine)) {
				Statement statement = new Statement(startLine);
				this.clazz.add(statement);
			}
			ctx.finallyBlock().block().enterRule(this);
		}

		if(ctx.switchBlockStatementGroup() != null) {
//			ctx.switchBlockStatementGroup().forEach(switchBlockStatementGroup -> switchBlockStatementGroup.enterRule(this));
			for(JavaParser.SwitchBlockStatementGroupContext switchBlockStatementGroup : ctx.switchBlockStatementGroup()) {
				switchBlockStatementGroup.enterRule(this);
			}
		}
		
		if(ctx.switchLabel() != null) {
//			ctx.switchLabel().forEach(switchLabel -> switchLabel.enterRule(this));
			for(JavaParser.SwitchLabelContext switchLabel : ctx.switchLabel()) {
				switchLabel.enterRule(this);
			}
		}
		
		if(ctx.resourceSpecification() != null) {
			for(JavaParser.ResourceContext resource : ctx.resourceSpecification().resources().resource()) {
				int startLine = resource.start.getLine();
				int endLine = resource.stop.getLine();
				if(IntInterval.match(startLine, endLine)) {
					Statement statement = new Statement(startLine);
					this.clazz.add(statement);
				}
			}
		}
		
		int startLine = ctx.start.getLine();
		int endLine = ctx.stop.getLine();
		if(IntInterval.match(startLine, endLine)) {
			Statement statement = new Statement(startLine);
			this.clazz.add(statement);
		}
		
	}
	
	@Override
	public void enterStatementExpression(JavaParser.StatementExpressionContext ctx) {
		ctx.expression().enterRule(this);
	}
	
	@Override
	public void enterExpression(JavaParser.ExpressionContext ctx) {
//		System.out.println("Enter expression " + ctx.start.getLine() + "-" + ctx.stop.getLine() + " --- " + ctx.getText() );
		if(ctx.expression() == null) {
			int startLine = ctx.start.getLine();
			int endLine = ctx.stop.getLine();
			if(IntInterval.match(startLine, endLine)) {
				Statement statement = new Statement(ctx.start.getLine());
				this.clazz.add(statement);
			}
			
		}
		else {
//			ctx.expression().forEach(expression -> expression.enterRule(this));
			for(JavaParser.ExpressionContext expression : ctx.expression()) {
				expression.enterRule(this);
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
		}
	}
	
	@Override
	public void enterVariableDeclarator(JavaParser.VariableDeclaratorContext ctx) {
		int startLine = ctx.start.getLine();
		int endLine = ctx.stop.getLine();
		if(IntInterval.match(startLine, endLine)) {
			Statement statement = new Statement(ctx.start.getLine());
			this.clazz.add(statement);
		}
		
		if(ctx.variableInitializer() != null && ctx.variableInitializer().expression() != null) {
			MethodListener methodListener = new MethodListener(this.clazz);
			ctx.variableInitializer().expression().enterRule(methodListener);
		}
		
	}
	
	@Override
	public void enterCatchClause(JavaParser.CatchClauseContext ctx) {
		int startLine = ctx.start.getLine();
		int endLine = ctx.stop.getLine();
		if(IntInterval.match(startLine, endLine)) {
			Statement statement = new Statement(ctx.start.getLine());
			this.clazz.add(statement);
		}
		
		ctx.block().enterRule(this);
	}
	
	@Override
	public void enterSwitchBlockStatementGroup(JavaParser.SwitchBlockStatementGroupContext ctx) {
//		ctx.switchLabel().forEach(switchLabel -> switchLabel.enterRule(this));
//		ctx.blockStatement().forEach(blockStatement -> blockStatement.enterRule(this));
		
		for(JavaParser.SwitchLabelContext switchLabel : ctx.switchLabel()) {
			switchLabel.enterRule(this);
		}
		
		for(JavaParser.BlockStatementContext blockStatement : ctx.blockStatement()) {
			blockStatement.enterRule(this);
		}
	}
	
	@Override 
	public void enterSwitchLabel(JavaParser.SwitchLabelContext ctx) {
		int startLine = ctx.start.getLine();
		int endLine = ctx.stop.getLine();
		if(IntInterval.match(startLine, endLine)) {
			Statement statement = new Statement(ctx.start.getLine());
			this.clazz.add(statement);
		}
		
	}
}
