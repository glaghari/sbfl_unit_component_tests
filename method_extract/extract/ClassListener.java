package extract;

import java.util.HashMap;
import java.util.Map;

import antlr.JavaBaseListener;
import antlr.JavaParser;


public class ClassListener extends JavaBaseListener {
	private static Map<String, String> imports = new HashMap<String, String>();
	private Clazz clazz = null;
	
	public ClassListener(Clazz clazz) {
		this.clazz = clazz;
	}
	    
    @Override
    public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
    	String className = ctx.Identifier().getText();
    	this.clazz.setClassName(className);
//    	ctx.classBody().classBodyDeclaration().forEach(classBodyDeclaration -> classBodyDeclaration.enterRule(this)); // Java8
    	for(JavaParser.ClassBodyDeclarationContext classBodyDeclaration : ctx.classBody().classBodyDeclaration()) {
    		classBodyDeclaration.enterRule(this);
    	}
    }
    
    @Override
    public void enterInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
    	String className = ctx.Identifier().getText();
    	this.clazz.setClassName(className);
    	for(JavaParser.InterfaceBodyDeclarationContext interfaceBodyDeclaration : ctx.interfaceBody().interfaceBodyDeclaration()) {
    		interfaceBodyDeclaration.enterRule(this);
    	}
    }
    
    @Override
    public void enterAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) {
    	String className = ctx.Identifier().getText();
    	this.clazz.setClassName(className);
    	for(JavaParser.AnnotationTypeElementDeclarationContext annoCtx : ctx.annotationTypeBody().annotationTypeElementDeclaration())
			annoCtx.annotationTypeElementRest().enterRule(this);
    }
    
    @Override
    public void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
    	String className = ctx.Identifier().getText();
    	this.clazz.setClassName(className);
    	
    	if(ctx.enumConstants() != null){
    		StatementListener statementListener = new StatementListener(clazz);
    		ctx.enumConstants().enterRule(statementListener);
    		
    		for(JavaParser.EnumConstantContext enumConstantContext : ctx.enumConstants().enumConstant()) {
    			if(enumConstantContext.classBody() != null) {
    				Clazz anonymousClazz = new Clazz(this.clazz.getAnonymousCounter());
    				this.clazz.addAnonymousClass(anonymousClazz);
    				ClassListener classListener = new ClassListener(anonymousClazz);
    				for(JavaParser.ClassBodyDeclarationContext classBodyDeclaration : enumConstantContext.classBody().classBodyDeclaration()) {
    					classBodyDeclaration.enterRule(classListener);
    				}
    			}
    		}
    	}
    	
    	if(ctx.enumBodyDeclarations() != null && ctx.enumBodyDeclarations().classBodyDeclaration() != null) {
    		//    	ctx.enumBodyDeclarations().classBodyDeclaration().forEach(classBodyDeclaration -> classBodyDeclaration.enterRule(this)); // Java8
    		for(JavaParser.ClassBodyDeclarationContext classBodyDeclaration : ctx.enumBodyDeclarations().classBodyDeclaration()) {
    			classBodyDeclaration.enterRule(this);
    		}
    	}
    }
    
    @Override public void enterClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
//    	System.out.println("Enter classBodyDeclaration " + ctx.start.getLine());

    	// Only for statements
    	if(ctx.block() != null) {
    		StatementListener statementListener = new StatementListener(this.clazz);
    		ctx.block().enterRule(statementListener);
    	}

    	else if(ctx.memberDeclaration() == null) {
    		return;
    	}
    	
    	else if(ctx.memberDeclaration().methodDeclaration() != null && ctx.memberDeclaration().methodDeclaration().methodBody() != null) {
//    		System.out.println("method dec");
    		MethodListener methodListener = new MethodListener(this.clazz);
    		methodListener.addMethod(ctx.memberDeclaration().methodDeclaration());
    		ctx.memberDeclaration().methodDeclaration().methodBody().enterRule(methodListener);
    	}
    	
    	else if(ctx.memberDeclaration().genericMethodDeclaration() != null && ctx.memberDeclaration().genericMethodDeclaration().methodDeclaration().methodBody() != null) {
//    		System.out.println("method dec");
    		MethodListener methodListener = new MethodListener(this.clazz);
    		methodListener.addMethod(ctx.memberDeclaration().genericMethodDeclaration());
    		ctx.memberDeclaration().genericMethodDeclaration().methodDeclaration().methodBody().enterRule(methodListener);
    	}

    	else if(ctx.memberDeclaration().constructorDeclaration() != null) {
//    		System.out.println("cons dec");
    		MethodListener methodListener = new MethodListener(this.clazz);
    		ctx.memberDeclaration().constructorDeclaration().constructorBody().enterRule(methodListener);
    	}

    	else if(ctx.memberDeclaration().genericConstructorDeclaration() != null) {
//    		System.out.println("cons dec");
    		MethodListener methodListener = new MethodListener(this.clazz);
    		ctx.memberDeclaration().genericConstructorDeclaration().constructorDeclaration().constructorBody().enterRule(methodListener);
    	}
    	
    	else if(ctx.memberDeclaration().classDeclaration() != null) {
//    		System.out.println("Enter inner class");
			Clazz innerClazz = new Clazz();
			this.clazz.addAInnerClazz(innerClazz);
			ClassListener classListener = new ClassListener(innerClazz);
			ctx.memberDeclaration().classDeclaration().enterRule(classListener);
    	}
    	
    	else if(ctx.memberDeclaration().interfaceDeclaration() != null) {
//    		System.out.println("Enter inner class");
			Clazz innerClazz = new Clazz();
			this.clazz.addAInnerClazz(innerClazz);
			ClassListener classListener = new ClassListener(innerClazz);
			ctx.memberDeclaration().interfaceDeclaration().enterRule(classListener);
    	}
    	
    	else if(ctx.memberDeclaration().enumDeclaration() != null) {
//    		System.out.println("Enter inner class");
			Clazz innerClazz = new Clazz();
			this.clazz.addAInnerClazz(innerClazz);
			ClassListener classListener = new ClassListener(innerClazz);
			ctx.memberDeclaration().enumDeclaration().enterRule(classListener);
    	}
    	
    	else if(ctx.memberDeclaration().annotationTypeDeclaration() != null) {
    		Clazz innerClazz = new Clazz();
			this.clazz.addAInnerClazz(innerClazz);
			ClassListener classListener = new ClassListener(innerClazz);
    		ctx.memberDeclaration().annotationTypeDeclaration().enterRule(classListener);
    	}
    	
    	else if(ctx.memberDeclaration().fieldDeclaration() != null) {
    		MethodListener methodListener = new MethodListener(this.clazz);
    		StatementListener statementListener = new StatementListener(this.clazz);
//			ctx.memberDeclaration().fieldDeclaration().variableDeclarators().variableDeclarator().forEach(variableDeclarator -> variableDeclarator.enterRule(statementListener));
			for(JavaParser.VariableDeclaratorContext variableDeclarator : ctx.memberDeclaration().fieldDeclaration().variableDeclarators().variableDeclarator()) {
				variableDeclarator.enterRule(statementListener);
				JavaParser.VariableInitializerContext variableInitializerContext = variableDeclarator.variableInitializer();
				if (variableInitializerContext != null) {
					methodListener.addMethod(variableDeclarator);
					if(variableInitializerContext.expression() != null)
						variableInitializerContext.expression().enterRule(methodListener);
				}
			}
    	}
    	
    }
    
    @Override
    public void enterInterfaceBodyDeclaration(JavaParser.InterfaceBodyDeclarationContext ctx) {
    	JavaParser.InterfaceMemberDeclarationContext memberCtx = ctx.interfaceMemberDeclaration(); 
    	if(memberCtx == null) {
    		return;
    	}

       	else if(memberCtx.interfaceMethodDeclaration() != null) {
//    		System.out.println("Enter inner class");
       		MethodListener methodListener = new MethodListener(this.clazz);
    		methodListener.addMethod(memberCtx.interfaceMethodDeclaration());
    	}

       	else if(memberCtx.genericInterfaceMethodDeclaration()!= null) {
//    		System.out.println("Enter inner class");
       		MethodListener methodListener = new MethodListener(this.clazz);
    		methodListener.addMethod(memberCtx.genericInterfaceMethodDeclaration());
    	}
    	
       	else if(memberCtx.classDeclaration() != null) {
//    		System.out.println("Enter inner class");
			Clazz innerClazz = new Clazz();
			this.clazz.addAInnerClazz(innerClazz);
			ClassListener classListener = new ClassListener(innerClazz);
			memberCtx.classDeclaration().enterRule(classListener);
    	}
    	
    	else if(memberCtx.interfaceDeclaration() != null) {
//    		System.out.println("Enter inner class");
			Clazz innerClazz = new Clazz();
			this.clazz.addAInnerClazz(innerClazz);
			ClassListener classListener = new ClassListener(innerClazz);
			memberCtx.interfaceDeclaration().enterRule(classListener);
    	}
    	
    	else if(memberCtx.enumDeclaration() != null) {
//    		System.out.println("Enter inner class");
			Clazz innerClazz = new Clazz();
			this.clazz.addAInnerClazz(innerClazz);
			ClassListener classListener = new ClassListener(innerClazz);
			memberCtx.enumDeclaration().enterRule(classListener);
    	}
    	
    	else if(memberCtx.annotationTypeDeclaration() != null) {
    		Clazz innerClazz = new Clazz();
			this.clazz.addAInnerClazz(innerClazz);
			ClassListener classListener = new ClassListener(innerClazz);
    		memberCtx.annotationTypeDeclaration().enterRule(classListener);
    	} 	
    }
    
    @Override
    public void enterAnnotationTypeElementRest(JavaParser.AnnotationTypeElementRestContext ctx) {
    	
    	if(ctx.classDeclaration() != null) {
//    		System.out.println("Enter inner class");
			Clazz innerClazz = new Clazz();
			this.clazz.addAInnerClazz(innerClazz);
			ClassListener classListener = new ClassListener(innerClazz);
			ctx.classDeclaration().enterRule(classListener);
    	}
    	
    	else if(ctx.interfaceDeclaration() != null) {
//    		System.out.println("Enter inner class");
			Clazz innerClazz = new Clazz();
			this.clazz.addAInnerClazz(innerClazz);
			ClassListener classListener = new ClassListener(innerClazz);
			ctx.interfaceDeclaration().enterRule(classListener);
    	}
    	
    	else if(ctx.enumDeclaration() != null) {
//    		System.out.println("Enter inner class");
			Clazz innerClazz = new Clazz();
			this.clazz.addAInnerClazz(innerClazz);
			ClassListener classListener = new ClassListener(innerClazz);
			ctx.enumDeclaration().enterRule(classListener);
    	}
    	
    	else if(ctx.annotationTypeDeclaration() != null) {
    		Clazz innerClazz = new Clazz();
			this.clazz.addAInnerClazz(innerClazz);
			ClassListener classListener = new ClassListener(innerClazz);
    		ctx.annotationTypeDeclaration().enterRule(classListener);
    	}
    }


}
