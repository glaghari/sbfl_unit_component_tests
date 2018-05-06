package myTracer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Trace {
	
	private Map<Integer, MethodTrace> traces;
	private String testCaseName;
	// total methods
	private Set<Integer> totalMethodsExecuted;
	private boolean logging = false;
		
	public Trace(boolean logging) {
		this.testCaseName = null;
		this.logging = logging;
		this.totalMethodsExecuted = new HashSet<Integer>();
		this.traces = new HashMap<Integer, MethodTrace>();
	}
	
	public void addTrace(Integer sourceID, Integer targetID) {
		MethodTrace methodTrace = traces.get(sourceID);
		if(methodTrace == null) {
			methodTrace = new MethodTrace(sourceID, this.logging);
			this.traces.put(methodTrace.getID(), methodTrace);
		}
		methodTrace.addTrace(targetID);
	}
	
	public void closeTrace(Integer sourceID) {
		MethodTrace methodTrace = this.traces.get(sourceID);
		if(methodTrace != null) {
			methodTrace.closeTrace();
//			if(logging) log.info("[CLOSING] - " + methodTrace.getID() + " - [" + methodTrace + "]");
		}
	}
	
	public void setTestCaseName(String testCaseName) {
		this.testCaseName = testCaseName;
	}
	
	public String getTestCaseName() {
		return this.testCaseName;
	}
	
	public void addMethodExecutedEntry(Integer targetMethod) {
		this.totalMethodsExecuted.add(targetMethod);
	}
	
	public Iterator<Entry<Integer, MethodTrace>> getTracesIterator() {
		return this.traces.entrySet().iterator();
	}
	
	public String getMethodExecuted() {
		Iterator<Integer> iterator = this.totalMethodsExecuted.iterator();
		StringBuffer executed = new StringBuffer();
		boolean removeSpace = false;
		while(iterator.hasNext()) {
			executed.append(iterator.next());
			executed.append(Utils.SEPARATOR);
			removeSpace = true;
		}
		if(removeSpace) executed.delete(executed.lastIndexOf(Utils.SEPARATOR), executed.length());
		executed.append("\nTotal methods executed=" + this.totalMethodsExecuted.size());
		return executed.toString();
	}
	
	public int getTotalMethodExecuted() {
		return this.totalMethodsExecuted.size();
	}

	public int getTracesSize() {
		return this.traces.size();
	}
	
}
