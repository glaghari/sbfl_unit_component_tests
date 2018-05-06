package myTracer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

//import myTracer.TraceManager.QueueConsumer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class SaveTraceSimple extends Thread {
	private Trace trace = null;
	
	private String tracesDir = "";
	
	public SaveTraceSimple(Trace trace) {
		this.trace = trace;
		setName("SAVE TRACE");
		String dir  = System.getenv().get(Utils.TRACES_DIR);
		if(dir != null)
			tracesDir = dir;
	}

	public void save() {
		// If testCaseName is null it means the test case never executed
		// perhaps the trace exists because some other methods executed before test methods
		if(this.trace.getTestCaseName() == null || this.trace.getTotalMethodExecuted() < 1)
			return;
		
//		log.info("[SAVE TRACE START] - " + this.trace.getTestCaseName());
		
//		log.info("[TRACE SAVING] - [" + this.trace.getTracesSize() + "] - " + this.trace.getTestCaseName());
//		log.info("[TRACES] - " + this.trace.getTracesSize() + " [METHODS EXECUTED] - " + this.trace.getTotalMethodExecuted() + " [TEST] - " + this.trace.getTestCaseName());
//		log.info("[TEST] - " + this.trace.getTestCaseName() + " - [METHODS EXECUTED] - " + this.trace.getTotalMethodExecuted());
		saveTraceIDs();
		saveTraces();
//		log.info("[TRACE SAVED] - " + this.trace.getTestCaseName());
	}
	
	private void saveTraces() {
		Iterator<Entry<Integer, MethodTrace>> traces = this.trace.getTracesIterator();
		
		if(!traces.hasNext())
			return;
		
		File dir = new File(tracesDir + File.separator + this.trace.getTestCaseName() + File.separator + Utils.TRACES + File.separator);		
		if(!dir.exists())
			dir.mkdirs();
		synchronized (traces) {
			while(traces.hasNext()) {
				BufferedOutputStream stream = null;
				String fileName = null;
				try {
					Entry<Integer, MethodTrace> methodEntry = traces.next();
					fileName = methodEntry.getKey().toString() + Utils.TXT_FILE_EXTENSION;
					File file = new File(dir, fileName);
					stream = new BufferedOutputStream(new FileOutputStream(file));
					// Loop through each and write
					Iterator<List<Integer>> listIterator = methodEntry.getValue().getTraceIterator();
					synchronized (listIterator) {
						while(listIterator.hasNext()) {
							List<Integer> t = listIterator.next();
							fillStreamCharByChar(stream, t);
							if(listIterator.hasNext())
								stream.write('\n');
						}
					}
					stream.flush();
					stream.close();
				} catch (Throwable exception) {
					Utils.handleException(exception, "saveTraces2() [METHOD] - " + fileName + " [IN] " + this.trace.getTestCaseName());
				}
				finally {try { if(stream!= null) {stream.flush(); stream.close();}} catch (IOException e) {}}
			}
		
		}
	}

	private void fillStreamCharByChar(BufferedOutputStream stream, List<Integer> t) throws IOException {
		synchronized (t) {
			Iterator<Integer> iterator = t.iterator();
			while(iterator.hasNext()) {
				stream.write(iterator.next().toString().getBytes());
				if(iterator.hasNext())
					stream.write(' ');
			}
		}
	}

	private void saveTraceIDs() {
		if(this.trace.getTotalMethodExecuted() < 1 )
			return;
		
		BufferedOutputStream stream = null;
		try {
			File dir = new File(tracesDir + File.separator + this.trace.getTestCaseName());		
			if(!dir.exists())
				dir.mkdirs();
			File file = new File(dir.getAbsolutePath() + File.separator + Utils.TRACE_FILE);
			stream = new BufferedOutputStream(new FileOutputStream(file));
			stream.write(this.trace.getMethodExecuted().getBytes());
			stream.flush();
		} catch (Exception exception) {
			Utils.handleException(exception, "saveTraceIDs()");
		}
		finally {try { if(stream!= null) {stream.flush(); stream.close();}} catch (IOException e) {}}
	}
	
	@Override
	public void run() {
		save();
	}
	
}