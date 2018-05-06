package calledclasses;

import java.io.File;

import org.aspectj.lang.JoinPoint;

public aspect CalledClassesTracer {
	
	private static TraceManager traceManager = null;
	private static Timeout timer = null;
	private static Long timeout = 1000l * 20l; // 20 seconds
	
	static {
		
		String dir = null;	
		dir  = System.getenv().get(Utils.TESTS_INFO_DIR);
		if(dir != null) {
			File d = new File(dir);
			if( ! d.exists())
				d.mkdirs();
			
			Utils.EXCEPTION_FILE_NAME = dir + File.separator + Utils.EXCEPTION_FILE_NAME;
			Utils.LOG_FILE_NAME = dir + File.separator + Utils.LOG_FILE_NAME;
			Utils.TEST_FILE_NAME = dir + File.separator + Utils.TEST_FILE_NAME;
			Utils.TESTS_INFO_DIR = dir + File.separator + Utils.TESTS_INFO_DIR;
		}
		else {
			Utils.log("NO TESTS INFO DIR PROPERTY" , Utils.EXCEPTION_FILE_NAME, true);
			System.exit(-1);
		}
	}
	
	// Trace all methods in project classes including those to JAVA library calls
	// Include tests packages if tests are outside project code.
	pointcut projectMethodCall() :
		(	
			// Call to any method within classes inside package mentioned  and its subpackages
			// Or call to any constructor within classes inside package mentioned and its subpackages
			// or may be call(* *..*.*(..))
			(call(* *(..)) || call(*.new(..)))
//			 Enable only one of the following
//			&& within(org.joda..*) // Jode-Time
//			&& within(org.jfree..*) // JFreeChart
			&& within(org.apache.commons..*) // Appache Commons Lang and Math
//			&& ( within(com.google.debugging..*) || within(com.google.javascript..*) ) // Google Closure
//			&& within(org.mockito*..*) // Mockito
		);

	pointcut projectMethodExecution() :
		(
			// Execution of any method within classes inside package mentioned and its subpackages
			// Or execution of any constructor within classes inside package mentioned and its subpackages
			// Enable only one of them
//			(execution(* org.jfree..*.*(..)) || execution(org.jfree..*.new(..))) // JFreeChart
//			(execution(* org.joda..*.*(..)) || execution(org.joda..*.new(..))) // Jode-Time
			(execution(* org.apache.commons..*.*(..)) || execution(org.apache.commons..*.new(..))) // Apache Commons Lang and Math
//			(execution(* com.google.debugging..*.*(..)) || execution(com.google.debugging..*.new(..)) || execution(* com.google.javascript..*.*(..)) || execution(com.google.javascript..*.new(..))) // Google Closure
//			(execution(* org.mockito*..*.*(..)) || execution(org.mockito*..*.new(..))) // Mockito
			
			// Try to disable this and before add method check if its not the test method
			
			&& !(@annotation(org.junit.Test) || @annotation(org.junit.Before) || @annotation(org.junit.After))
		 	&& !(execution(public void junit.framework.TestCase+.test*(..))
		 		|| execution(* junit.framework.TestCase+.setUp())
		 		|| execution(* junit.framework.TestCase+.tearDown())
		 		)
		 	&& !execution(junit.framework.TestCase+.new(..))
		);
	
	
//	static final String projectPackages[] = {"org.jfree"};
//	static final String projectPackages[] = {"org.joda"};
	static final String projectPackages[] = {"org.apache.commons"};
//	static final String projectPackages[] = {"com.google.debugging", "com.google.javascript"};
//	static final String projectPackages[] = {"org.mockito"};

	
	pointcut testMethod() :
		(
			execution(* *(..)) && @annotation(org.junit.Test)
			|| execution(* junit.framework.TestCase+.test*(..))
		);
						  
	pointcut setUpMethod() :
		(
			execution(* *()) && @annotation(org.junit.Before)
			|| execution(* junit.framework.TestCase+.setUp())
		);

	before() : testMethod() {
		
		String testCaseName = getTestCaseName(thisJoinPoint);
		
		//  Utils.log("[TEST METHOD] " + testCaseName, Utils.LOG_FILE_NAME, true);
		
		// if Trace Manager is null,
		// it means there is no setUp() method for the test
		if(traceManager == null) {
			//  Utils.log("[PREP TRACER] in test", Utils.LOG_FILE_NAME, true);
			prepareTracer();
		}
		
		if(! traceManager.isTestCaseNameSet()) {
			//  Utils.log("[START] " + testCaseName, Utils.LOG_FILE_NAME, true);
			traceManager.setTestCaseName(testCaseName);
		}
	}
	
	after() : testMethod() {
		String testCaseName = getTestCaseName(thisJoinPoint);
		
		
		if(traceManager !=null && ! traceManager.isTestCaseNameSame(testCaseName)) {
			return;
		}
		
		//  Utils.log("[END] " + testCaseName, Utils.LOG_FILE_NAME, true);
		
		traceManager = null;
		if(timer != null)
			timer.interrupt();
	}

	before() : setUpMethod() {
		if(traceManager == null) {
			//  Utils.log("[PREP TRACER] in setup", Utils.LOG_FILE_NAME, true);
			prepareTracer();
		}
	}
	
	before() : projectMethodExecution() {
		
		//  Utils.log("[UNCHECK] [EXEC] [TM NOT NULL?] " + (traceManager !=null) + " - " + thisJoinPoint.getSignature(), Utils.LOG_FILE_NAME, true);
		
		if(traceManager !=null) {
		  try {
			  Package _package = thisJoinPoint.getSignature().getDeclaringType().getPackage();
			  String calledClassPackage = "";
			  if(_package != null)
				  calledClassPackage = _package.getName();
			  
			  //			String calledClassPackage = thisJoinPoint.getSignature().getDeclaringType().getPackage().getName();
			  Class calledClass = thisJoinPoint.getSignature().getDeclaringType();
			  
			  if(isSkipableClass(calledClass))
					return;
			  
			  String calledMethod = thisJoinPoint.getSignature().getName();

			  //  Utils.log("[EXEC] " + calledClassPackage + " -p- " + calledClass + " -c- " + calledMethod, Utils.LOG_FILE_NAME, true);

			  CalledClass _calledClass = traceManager.addCalledClass(calledClassPackage, calledClass);  
			  if(_calledClass != null)
				  _calledClass.addMethod(calledMethod);
		  } catch (Throwable exception) {
				Utils.handleException(exception, "projectMethodExecution()");
			}
		}
	}
	
	before() : projectMethodCall() {
		if(traceManager !=null) {
			try {
				
				Class calledClass = thisJoinPoint.getTarget() != null? thisJoinPoint.getTarget().getClass():
					thisJoinPoint.getSignature().getDeclaringType();
				
				Package _package = calledClass.getPackage();
				
				if(_package == null)
					return;
				
				String calledClassPackage = "";
				calledClassPackage = _package.getName();
				
				if(isSkipableClass(calledClass))
					return;
				
//				  calledClass = Utils.getClass(thisJoinPoint.getSignature(), calledClass);
				
//				boolean isCallerTestMethod = false;
				if(thisEnclosingJoinPointStaticPart.getSignature() != null) {
					String testCaseName = thisEnclosingJoinPointStaticPart.getSignature().getDeclaringTypeName() + "." + thisEnclosingJoinPointStaticPart.getSignature().getName();
//					if(traceManager.getTestCaseName() != null) {
//						isCallerTestMethod = traceManager.getTestCaseName().equals(testCaseName);
//					}
				}
				
				String calledMethod = thisJoinPoint.getSignature().getName();
				
				//  Utils.log("[CALL] " + calledClassPackage + " -p- " + calledClass + " -c- " + calledMethod, Utils.LOG_FILE_NAME, true);
				
				CalledClass _calledClass = traceManager.addCalledClass(calledClassPackage, calledClass);
				if(_calledClass != null) {
					_calledClass.addMethod(calledMethod);
//					if(isCallerTestMethod)
//						traceManager.setCallerIsTestMethod(calledClassPackage, calledClass);
				}
			} catch (Throwable exception) {
				Utils.handleException(exception, "projectMethodCall()");
			}
		}
	}
	
	private boolean isSkipableClass(Class calledClass) {
		if(calledClass == null || calledClass.isPrimitive() || calledClass.isEnum())
			return true;
		
		String className = calledClass.getName();
		if(className == null)
			return true;
		
		// Java base classes and junit classes are to be skipped
		if(className.startsWith("java") || className.startsWith("javax") || className.startsWith("junit")) {
			return true;
		}
		
		// project packages classes are not to be skipped
		for(String projectPackage : projectPackages) {
			if(className.startsWith(projectPackage)) {
				return false;
			}
		}
		
		return true;
	}

	private void prepareTracer() {
		//  Utils.log("[PREP TRCR]", Utils.LOG_FILE_NAME, true);
		traceManager = new TraceManager();
		timer = new Timeout(timeout,this);
		timer.start();
	}

	private void stopTracing() {
		if(traceManager != null) {
			Utils.log("[INTERRUPTED] " + traceManager.getTestCaseName(), Utils.TEST_FILE_NAME, true);
			traceManager.removeShutdownHook();
		}
		traceManager = null;
		System.exit(-1);
	}

	public static class Timeout extends Thread {
		private Long maxTime = null;
		private CalledClassesTracer tracer = null;
		public Timeout(long maxMiliSecond, CalledClassesTracer tracer) {
			super();
			this.maxTime = maxMiliSecond;
			this.tracer = tracer;
		}

		public void run() {
			try {
				Thread.sleep(this.maxTime);
				this.tracer.stopTracing();
			} catch (Exception e) {}
		}
	}
	
	private String getTestCaseName(JoinPoint joinPoint) {
		Object targetObject = joinPoint.getTarget();
		Class _class = targetObject != null? targetObject.getClass() : joinPoint.getSignature().getDeclaringType();
		String testCaseName = _class.getName() + "." + joinPoint.getSignature().getName();
		return testCaseName;
	}

}
