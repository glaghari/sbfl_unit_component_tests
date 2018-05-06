package extract;

import java.util.*;

public class Clazz {
	private String packageName;
	private String className;
	private Set<Statement> statements;
    private Set<Method> methods;
    private Collection<Clazz> anonymousClasses;
    private Collection<Clazz> innerClasses;
    private int anonymousCounter = 1;
    
    public Clazz() {
    	this.statements = new LinkedHashSet<Statement>();
    	this.methods = new LinkedHashSet<Method>();
    	this.anonymousClasses = new LinkedList<Clazz>();
    	this.innerClasses = new LinkedList<Clazz>();
    }
    
    public Clazz(String packageName, String className) {
    	this();
    	this.packageName = packageName;
    	this.className = className;
    }
    
    public Clazz(String packageName) {
    	this(packageName, null);
    }
    
    public Clazz(int className) {
    	this(null, String.valueOf(className));
	}

	public Method add(Method method) {
    	this.methods.add(method);
    	return method;
    }
	
	public Statement add(Statement statement) {
    	this.statements.add(statement);
    	return statement;
    }

    
    public String toString() {
    	return 
    			"----------------------------Methods-------------------------\n" +
    			getMethods()
    			+ "\n\n----------------------------Statements-------------------------\n" +
    			getStatements();
    }
    
    public String getMethods() {
    	String className = getPackageName() + "." + getClassName();
    	return getMethods(this, className).toString();
    }

	public String getStatements() {
		String className = getPackageName() + "." + getClassName();
    	return getStatements(this, className).toString();
    }

	private StringBuilder getMethods(Clazz clazz, String className) {
    	StringBuilder methodNames = new StringBuilder();
    	for(Method method : clazz.methods) {
    		if(method == null) continue;
    		methodNames.append(className);
    		methodNames.append("#");
    		methodNames.append(method);
    		methodNames.append("\n");
    	}
    	
    	fillAnonymousClassMethods(clazz, methodNames, className);
    	fillInnerClassMethods(clazz, methodNames, className);
    	
    	return methodNames;
    }
    
    private void fillAnonymousClassMethods(Clazz clazz, StringBuilder methodNames, String className) {
    	for(Clazz anonymousClazz : clazz.anonymousClasses) {
    		String anonymousClassName = className + "$" + anonymousClazz.getClassName();
    		methodNames.append(getMethods(anonymousClazz, anonymousClassName));
    	}
    }

    private void fillInnerClassMethods(Clazz clazz, StringBuilder methodNames, String className) {
    	for(Clazz innerClazz : clazz.innerClasses) {
    		String innerClassName = className + "$" + innerClazz.getClassName();
    		methodNames.append(getMethods(innerClazz, innerClassName));
    	}
	}
    
    private StringBuilder getStatements(Clazz clazz, String className) {
		StringBuilder statementNames = new StringBuilder();
    	for(Statement statement : clazz.statements) {
    		if(statement == null) continue;
    		statementNames.append(className);
    		statementNames.append("#");
    		statementNames.append(statement);
    		statementNames.append("\n");
    	}
    	
    	fillAnonymousClassStatements(clazz, statementNames, className);
    	fillInnerClassStatements(clazz, statementNames, className);
    	return statementNames;
	}

	private void fillAnonymousClassStatements(Clazz clazz, StringBuilder statementNames, String className) {
		for(Clazz anonymousClazz : clazz.anonymousClasses) {
    		String anonymousClassName = className + "$" + anonymousClazz.getClassName();
    		statementNames.append(getStatements(anonymousClazz, anonymousClassName));
    	}
	}
	
	private void fillInnerClassStatements(Clazz clazz, StringBuilder statementNames, String className) {
		for(Clazz innerClazz : clazz.innerClasses) {
			String innerClassName = className + "$" + innerClazz.getClassName();
    		statementNames.append(getStatements(innerClazz, innerClassName));
    	}
	}

    
    private String getPackageName() {
		return this.packageName;
	}

    public String getClassName() {
    	return this.className;
    }

	public Clazz addAnonymousClass(Clazz clazz) {
		this.anonymousClasses.add(clazz);
		return clazz;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getAnonymousCounter() {
		return anonymousCounter++;
	}
	
	public int readOnlyAnonymousCounter() {
		return anonymousCounter ;
	}

	public void addAInnerClazz(Clazz innerClazz) {
		this.innerClasses.add(innerClazz);
	}
}