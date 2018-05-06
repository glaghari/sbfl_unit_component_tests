package myTracer;

import java.io.File;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;


public class TraceManager {
	
	
	private Trace trace = null;
	private Thread saveTrace;
	private boolean logging = false;
//	private QueueConsumer queueConsumer = null;
	private boolean traceColsed = false;
	
	public TraceManager(boolean logging) {
		this.logging = logging;
		this.trace = new Trace(this.logging);
//		log.info("[SERIAL TRACE MANAGER INIT]");
	}
	
	public void setTestCaseName(String testCaseName) {
		this.trace.setTestCaseName(testCaseName);
		
		// Since we know now that there is the test case to claim the collected trace,
		// Initialise trace save object
		this.saveTrace = new SaveTraceSimple(this.trace);
		Runtime.getRuntime().addShutdownHook(this.saveTrace);
	}
	
	public String getTestCaseName() {
		return this.trace.getTestCaseName();
	}
	
	public void addTrace(String sourceMethod, String targetMethod) {
		MethodSignature msSource = MethodSignature.getMethodSignature(sourceMethod);
		MethodSignature msTarget = MethodSignature.getMethodSignature(targetMethod);
//		if(logging) log.info("[MANAGER ENTRY] - [" + this.getTestCaseName() + "] - " + msSource.getID() + " -> " + msTarget.getID());
		this.trace.addTrace(msSource.getID(), msTarget.getID());
	}

	private void addMethodExecutedEntry(String targetMethod) {
		MethodSignature msTarget = MethodSignature.getMethodSignature(targetMethod);
		int targetMethodID = msTarget.getID();
//		if(logging) log.info("[EXECUTED] [TOTAL] - " + this.trace.getTotalMethodExecuted() + " - [" + this.getTestCaseName() + "] - " + targetMethodID);
		this.trace.addMethodExecutedEntry(targetMethodID);
	}
	
	private void addMethodCloseEntry(String targetMethod) {
		MethodSignature msTarget = MethodSignature.getMethodSignature(targetMethod);
		int targetMethodID = msTarget.getID();
//		if(logging) log.info("[CLOSED] - [" + this.getTestCaseName() + "] - " + targetMethodID);
		this.trace.closeTrace(targetMethodID);
	}

	public boolean isTestCaseNameSet() {
		// If testCaseName is not null it means testCaseName is set
		return this.trace.getTestCaseName() != null;
	}

	public boolean isTestCaseNameSame(String testCaseName) {
		try {
			return this.trace.getTestCaseName().equals(testCaseName);
		}catch(NullPointerException npe) {}
		return false;
	}

	public void printTraces() {
		removeShutdownHook();
		this.saveTrace.start();
		try {
			this.saveTrace.join();
		} catch (InterruptedException exception) {
			String message = "[SAVE TRACE]\nTest Case = " + trace.getTestCaseName();
			Utils.handleException(exception, message);
		}
	}

	public Trace getTrace() {
		return this.trace;
	}

	@Deprecated
	public void addCall(Signature sourceSignature, Signature targetSignature, Object targetObject) {
		if(traceColsed )
			return;
		
//		if(logging) log.info("[CALL TRACE ] - " +  sourceSignature.toLongString() + "->" + targetSignature.toLongString());
		try {
			String targetMethod = null;
			String sourceMethod = null;
			sourceMethod = Utils.getSourceMethod(sourceSignature);
			if(sourceMethod == null)
				return;
			targetMethod = Utils.getTargetMethod(targetSignature, targetObject);
			if(targetMethod == null)
				return;
			addTrace(sourceMethod, targetMethod);
		}catch(Exception exception) {
			Utils.handleException(exception, "EXECEPTION IN CALL");
		}
	}
	
	public void addCall(JoinPoint.StaticPart sourceJoinPoint, JoinPoint targetJoinPoint) {
		if(traceColsed )
			return;
		
//		if(logging) log.info("[CALL TRACE ] - " +  sourceSignature.toLongString() + "->" + targetSignature.toLongString());
		try {
			String targetMethod = null;
			String sourceMethod = null;
//			sourceMethod = Utils.getSourceMethod(sourceSignature);
			sourceMethod = Utils.getSourceMethod(sourceJoinPoint);
			if(sourceMethod == null)
				return;
			targetMethod = Utils.getTargetMethod(targetJoinPoint);
			if(targetMethod == null)
				return;
			addTrace(sourceMethod, targetMethod);
		}catch(Exception exception) {
			Utils.handleException(exception, "EXECEPTION IN CALL" + sourceJoinPoint.getSignature().toLongString());
		}
	}
	
	public void addExecutionBefore(JoinPoint targetJoinPoint) {
		if(traceColsed )
			return;
		
		try {
			String targetMethod = null;
			targetMethod = Utils.getTargetMethod(targetJoinPoint);
			if(targetMethod !=null)
				addMethodExecutedEntry(targetMethod);
		}catch(Exception exception) {
			Utils.handleException(exception, "EXECEPTION IN EXEC");
		}
	}

	public void addExecutionAfter(JoinPoint targetJoinPoint) {
//		if(logging) log.info("[EXEC TRACE ] - " +  targetSignature.toLongString());
		try {
			String targetMethod = null;
			targetMethod = Utils.getTargetMethod(targetJoinPoint);
			if(targetMethod !=null)
				addMethodCloseEntry(targetMethod);
		}catch(Exception exception) {
			Utils.handleException(exception, "EXECEPTION IN EXEC");
		}
	}

	@Deprecated
	public void addExecutionBefore(Signature targetSignature, Object targetObject) {
		try {
			String targetMethod = null;
			targetMethod = Utils.getTargetMethod(targetSignature, targetObject);
			if(targetMethod !=null)
				addMethodExecutedEntry(targetMethod);
		}catch(Exception exception) {
			Utils.handleException(exception, "EXECEPTION IN EXEC");
		}
	}

	@Deprecated
	public void addExecutionAfter(Signature targetSignature, Object targetObject) {
//		if(logging) log.info("[EXEC TRACE ] - " +  targetSignature.toLongString());
		try {
			String targetMethod = null;
			targetMethod = Utils.getTargetMethod(targetSignature, targetObject);
			if(targetMethod !=null)
				addMethodCloseEntry(targetMethod);
		}catch(Exception exception) {
			Utils.handleException(exception, "EXECEPTION IN EXEC");
		}
	}

	public void removeShutdownHook() {
		Runtime.getRuntime().removeShutdownHook(this.saveTrace);
	}

	public void closeTrace() {
		this.traceColsed = true;
	}
}