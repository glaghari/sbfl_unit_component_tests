package extract;

import java.util.Collection;
import java.util.LinkedList;
import antlr.JavaBaseListener;
import antlr.JavaParser;


public class CompilationUnitListener extends JavaBaseListener {
	String packageName = null;
	private Collection<Clazz> classes;
	
	public CompilationUnitListener() {
		this.classes = new LinkedList<Clazz>();
	}
	
	@Override public void enterCompilationUnit(JavaParser.CompilationUnitContext ctx) {
		ctx.packageDeclaration().enterRule(this);
		
		if(ctx.typeDeclaration() != null) {
			//		ctx.typeDeclaration().forEach(typeDeclaration -> typeDeclaration.enterRule(this)); // for java 8
			for(JavaParser.TypeDeclarationContext typeDeclaration : ctx.typeDeclaration()) {
				typeDeclaration.enterRule(this);
			}
		}
	}

	@Override
    public void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
    	packageName = ctx.qualifiedName().getText();
    }
	
	@Override public void enterTypeDeclaration(JavaParser.TypeDeclarationContext ctx) {
		if(ctx.classDeclaration() != null) {
			ctx.classDeclaration().enterRule(this);
		}
		
		else if(ctx.enumDeclaration() != null) {
			ctx.enumDeclaration().enterRule(this);
		}
		
		else if(ctx.interfaceDeclaration()!= null) {
			ctx.interfaceDeclaration().enterRule(this);
		}
		
		else if(ctx.annotationTypeDeclaration() != null) {
			ctx.annotationTypeDeclaration().enterRule(this);
		}
	}

	@Override
    public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
		Clazz clazz = new Clazz(packageName);
		this.classes.add(clazz);
		ClassListener classListener = new ClassListener(clazz);
		ctx.enterRule(classListener);
    }
	
	@Override
    public void enterInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
		Clazz clazz = new Clazz(packageName);
		this.classes.add(clazz);
		ClassListener classListener = new ClassListener(clazz);
		ctx.enterRule(classListener);
    }

	@Override
    public void enterAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) {
		Clazz clazz = new Clazz(packageName);
		this.classes.add(clazz);
		ClassListener classListener = new ClassListener(clazz);
		ctx.enterRule(classListener);
    }

	@Override
    public void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
		Clazz clazz = new Clazz(packageName);
		this.classes.add(clazz);
		ClassListener classListener = new ClassListener(clazz);
		ctx.enterRule(classListener);
    }

	public Collection<Clazz> getClasses() {
		return this.classes;
	}
	
	public void printClasses() {
		for(Clazz clazz : this.getClasses()) {
			System.out.print(clazz);
		}
	}
	
	public String getMethods() {
		StringBuilder methods = new StringBuilder();
		for(Clazz clazz : this.getClasses()) {
			methods.append(clazz.getMethods());
		}
		return methods.toString();
	}
	
	public String getStatements() {
		StringBuilder methods = new StringBuilder();
		for(Clazz clazz : this.getClasses()) {
			methods.append(clazz.getStatements());
		}
		return methods.toString();
	}
}
