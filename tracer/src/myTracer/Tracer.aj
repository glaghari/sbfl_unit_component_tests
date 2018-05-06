package myTracer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public aspect Tracer {
	
	private static Set<String> failingTestCases = null;
	static {
		String dir = null;

		
		dir  = System.getenv().get(Utils.TRACES_DIR);
		if(dir != null) {
			File d = new File(dir);
			if( ! d.exists())
				d.mkdirs();
			
			Utils.EXCEPTION_FILE_NAME = dir + File.separator + Utils.EXCEPTION_FILE_NAME;
			Utils.TEST_FILE_NAME = dir + File.separator + Utils.TEST_FILE_NAME;
		}
		else {
			Utils.log(Utils.TRACES_DIR + " not set", "tracer_exit.txt", true);
			System.exit(-1);
		}
	}

	private static TraceManager traceManager = null;
	private static boolean immediateFlush = true; // Enable for Closure project if needed
//	private static boolean immediateFlush = false;
	private static boolean logging = false;
//	private static boolean serial = false;
	private static Timeout timer = null;
	private static Long timeout = 1000l * 20l; // 20 seconds
//	private static Set<String> failingTestCases = null;
	
	// Trace all methods in project classes including those to JAVA library calls
	// Include tests packages if tests are outside project code.
	pointcut projectMethodCall() :
		(	
			// Call to any method within classes inside package mentioned  and its subpackages
			// Or call to any constructor within classes inside package mentioned and its subpackages
			// or may be call(* *..*.*(..))
			(call(* *(..)) || call(*.new(..)))
//			 Enable only one of the following
//			&& within(org.jfree..*) // JFreeChart
//			&& within(org.joda..*) // Jode-Time
//			&& within(org.apache.commons..*) // Appache Commons Lang and Math
//			&& ( within(com.google.debugging..*) || within(com.google.javascript..*) ) // Google Closure
			&& within(org.mockito*..*) // Mockito
		);

	
	pointcut projectMethodExecution() :
		(
			// Execution of any method within classes inside package mentioned and its subpackages
			// Or execution of any constructor within classes inside package mentioned and its subpackages
			// Enable only one of them
//			(execution(* *(..)) || execution(*.new(..))) // JFreeChart
//			(execution(* org.jfree..*.*(..)) || execution(org.jfree..*.new(..))) // JFreeChart
//			(execution(* org.joda..*.*(..)) || execution(org.joda..*.new(..))) // Jode-Time
//			(execution(* org.apache.commons..*.*(..)) || execution(org.apache.commons..*.new(..))) // Apache Commons Lang and Math
//			(execution(* com.google.debugging..*.*(..)) || execution(com.google.debugging..*.new(..)) || execution(* com.google.javascript..*.*(..)) || execution(com.google.javascript..*.new(..))) // Google Closure
			(execution(* org.mockito*..*.*(..)) || execution(org.mockito*..*.new(..))) // Mockito
			
			&& !(@annotation(org.junit.Test) || @annotation(org.junit.Before) || @annotation(org.junit.After))
		 	&& !(execution(public void junit.framework.TestCase+.test*(..))
		 		|| execution(* junit.framework.TestCase+.setUp())
		 		|| execution(* junit.framework.TestCase+.tearDown())
		 		)
		 	&& !execution(junit.framework.TestCase+.new(..))
		);
	
	
	// Trace all methods in Test classes that are annotated with
	// Before = setUp() Test=test*() After=tearDown()
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
		
		// if Trace Manager is null,
		// it means there is no setUp() method for the test
		if(traceManager == null)
			prepareTracer();
		
		if(!traceManager.isTestCaseNameSet()) {
//			Utils.log(testCaseName, Utils.TEST_FILE_NAME, true);
			setTestCaseName(testCaseName);
		}
	}

	after() : testMethod() {
		String testCaseName = getTestCaseName(thisJoinPoint);
//		log.info("[TEST-END] - " + testCaseName);
		
		if(traceManager != null && ! traceManager.isTestCaseNameSame(testCaseName)) {
			//	log.info("[TEST-CLOSE] - Attempted! - " + testCaseName);
			return;
		}
		
//		Utils.log("[" + new Date().toString() + "] [TEST-END] - " + testCaseName, Utils.TEST_FILE_NAME, true);
//		log.info("[TEST-END] - " + testCaseName);
		
		if(traceManager != null) {
			if(timer != null)
				timer.interrupt();
			
			traceManager.closeTrace();
			
			if(immediateFlush)
				traceManager.printTraces();
		}
		traceManager = null;
	}

	before() : setUpMethod() {
//		if(logging) log.info("[SETUP] - " + thisJoinPoint.getSignature().toLongString());
		if(traceManager == null)
			prepareTracer();
	}

	before() : projectMethodCall() {
//		if(logging) log.info("[TRACER CALL TRACE] - " +  thisEnclosingJoinPointStaticPart.getSignature().toLongString() + "->" + thisJoinPoint.getSignature().toLongString());
		if(traceManager != null) {
			traceManager.addCall(thisEnclosingJoinPointStaticPart, thisJoinPoint);
		}
	}
	
	before() : projectMethodExecution() {
//		if(logging) log.info("[TRACER EXEC TRACE] - " +  thisJoinPoint.getSignature().toLongString());
		if(traceManager != null) {
			traceManager.addExecutionBefore(thisJoinPoint);
		}
	}
	
	after() : projectMethodExecution() {
//		if(logging) log.info("[TRACER EXEC TRACE] - " +  thisJoinPoint.getSignature().toLongString());
		if(traceManager != null) {
			traceManager.addExecutionAfter(thisJoinPoint);
		}
	}

	private void prepareTracer() {
		traceManager = new TraceManager(logging);
		timer = new Timeout(timeout,this);
		timer.start();
	}

	private void setTestCaseName(String testCaseName) {
		if(traceManager != null) {
			traceManager.setTestCaseName(testCaseName);
		}
	}

	private String getTestCaseName(JoinPoint joinPoint) {
		Object targetObject = joinPoint.getTarget();
		Class _class = targetObject != null? targetObject.getClass() : joinPoint.getSignature().getDeclaringType();
		String testCaseName = _class.getName() + "." + joinPoint.getSignature().getName();
		return testCaseName;
	}

	private static void initFailingTestCases(File failingTestsFile) {
		BufferedReader reader = null; 
		try {
			 if(failingTestsFile.exists()) {
				failingTestCases = new HashSet<String>();
				reader = new BufferedReader(new FileReader(failingTestsFile));
				String testCaseName = null;
				while((testCaseName = reader.readLine()) !=null) {
					failingTestCases.add(testCaseName.replace("::", "."));
				} // while
			} // if
		  }catch(Exception e) {}
		  finally {
			  try {
				  reader.close();
				  } catch (IOException e) {e.printStackTrace();}
		  }
	}

	private void stopTracing() {
		if(traceManager !=null) {			
			traceManager.removeShutdownHook();
			traceManager.closeTrace();
			Utils.log("[INTERRUPTED] " + traceManager.getTestCaseName(), Utils.TEST_FILE_NAME, true);
			traceManager = null;
		}
	}

	public static class Timeout extends Thread {
		private Long maxTime = null;
		private Tracer tracer = null;
		public Timeout(long maxMiliSecond, Tracer tracer) {
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
	
}
