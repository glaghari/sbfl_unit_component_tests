package myTracer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class MethodTrace {
	
	private Integer id;
	private List<List<Integer>> traces;
	private List<Integer> currentTrace;
	private boolean logging = false;
	
	
	public MethodTrace(Integer id, boolean logging) {
		this.id= id;
		this.logging = logging;
		this.traces = java.util.Collections.synchronizedList(new ArrayList<List<Integer>>());
		this.currentTrace = null;
	}
	
	public void addTrace(Integer id) {
		if(this.currentTrace == null) {
			this.currentTrace = java.util.Collections.synchronizedList(new ArrayList<Integer>());
			this.traces.add(this.currentTrace);
			//if(logging) log.info("[INIT] - " + this.id + "->" + id + " - " + this.currentTrace.toString());
		}
		synchronized (this.currentTrace) {
			this.currentTrace.add(id);
		}
//		this.currentTrace.add(id);
		//if(logging) log.info("[ENTRY] - " + this.id + "->" + id + " - " + this.currentTrace.toString());
	}
	
	public void closeTrace() {
		//if(logging) log.info("[TRACE] - " + this.id + "->" + this.currentTrace);
		if(this.currentTrace != null)
		synchronized (this.currentTrace) {
			this.currentTrace = null;
		}
	}
		
	public String toString() {
		return this.getTrace();
 	}
	
	public Integer getID() {
		return this.id;
	}
	
	public String getTrace() {
		StringBuilder stringTraceFinal = new StringBuilder();
		// Create a copy and then get Iterator over the List to avoid ConcurrentModificationException
		Iterator<List<Integer>> listIterator = this.traces.iterator();
		try {
			while(listIterator.hasNext()) {
				StringBuilder stringTrace = new StringBuilder();
				List<Integer> t = listIterator.next();
				Iterator<Integer> iterator = t.iterator();
				while(iterator.hasNext()) {
					stringTrace.append(iterator.next());
					stringTrace.append(" ");
				}
				stringTraceFinal.append(stringTrace.toString().trim());
				stringTraceFinal.append("\n");
			}
		} catch (Exception exception) {
			String message = "method = " + id;
			Utils.handleException(exception, message);
		}
		return stringTraceFinal.toString().trim();
	}

	public int size() {
		return this.traces.size();
	}

	public Iterator<List<Integer>> getTraceIterator() {
		return this.traces.iterator();
	}
	
}
