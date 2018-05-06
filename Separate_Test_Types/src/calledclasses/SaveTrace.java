package calledclasses;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SaveTrace extends Thread {
	private TraceManager traceManager = null;
	
	public SaveTrace(TraceManager traceManager) {
		this.traceManager = traceManager;
		setName("SAVE TRACE");
	}
	
	public void save() {
		// If testCaseName is null it means the test case never executed
		// perhaps the trace exists because some other methods executed before test methods
		if(this.traceManager.getTestCaseName() == null)
			return;
		
		this.traceManager.doFinalDataPreparatorySteps();
		
//		Utils.log(this.traceManager.getTestCaseName(), Utils.LOG_FILE_NAME, true);
		
		try {
			this.saveCalledClasses();
			this.saveCalledMethods();
			this.writeTestsInfo();
		} catch (Throwable exception) {
			Utils.handleException(exception, "save()");
		}
	}

	private void saveCalledClasses() {
		File currentDir = new File(Utils.TESTS_INFO_DIR);
		currentDir = currentDir.getParentFile();
		
		File dir = new File(currentDir.getAbsolutePath() + File.separator + "called_classes" + File.separator);		
		if(!dir.exists())
			dir.mkdirs();
		
		File file = new File(dir, this.traceManager.getTestCaseName() + ".txt");
		StringBuilder contents = new StringBuilder("Classes Under Test:\n");
		contents.append(getDetails(this.traceManager.getClassesUnderTest()));
		contents.append("\n\nMock Classes:\n");
		contents.append(getDetails(this.traceManager.getMockClasses()));
		contents.append("\nOther Called Classes:\n");
		contents.append(getDetails(this.traceManager.getCalledClasses()));
		Utils.log(contents.toString(), file.getAbsolutePath(), false);
	}
	
	private void saveCalledMethods() {
		File currentDir = new File(Utils.TESTS_INFO_DIR);
		currentDir = currentDir.getParentFile();
		
		File dir = new File(currentDir.getAbsolutePath() + File.separator + "called_methods" + File.separator);		
		if(!dir.exists())
			dir.mkdirs();
		
		File file = new File(dir, this.traceManager.getTestCaseName() + ".txt");
		
		StringBuilder contents = new StringBuilder();
		contents.append(getMethodDetails(this.traceManager.getClassesUnderTest()));
		contents.append(getMethodDetails(this.traceManager.getMockClasses()));
		contents.append(getMethodDetails(this.traceManager.getCalledClasses()));
		Utils.log(contents.toString(), file.getAbsolutePath(), false);
	}

	private void writeTestsInfo() {
		File currentDir = new File(Utils.TESTS_INFO_DIR);
		currentDir = currentDir.getParentFile();
		
		String version = currentDir.getName();
		
		currentDir = currentDir.getParentFile();
		String project = currentDir.getName();
		
		File file = new File(currentDir + File.separator + "_" + project + "_tests_info.csv");
		StringBuilder line = new StringBuilder();
		line.append(version);
		line.append(",");
		line.append(this.traceManager.getTestCaseName());
		line.append(",");
		line.append(this.traceManager.getTestType());
		line.append(",");
		line.append(this.traceManager.getClassesUnderTest().size());
		line.append(",");
		line.append(this.traceManager.getMethodsCount());

		Utils.log(line.toString(), file.getAbsolutePath(), true);
	}
	
	private String getDetails(Collection<CalledClass> classes) {
		if(classes == null)
			return "";
		StringBuilder details = new StringBuilder();
		for(CalledClass calledClass:classes) {
			details.append(calledClass.getDetails());
			details.append("\n");
		}
		return details.toString().trim();
	}
	
	private Object getMethodDetails(Collection<CalledClass> classes) {
		if(classes == null)
			return "";
		StringBuilder details = new StringBuilder();
		for(CalledClass calledClass:classes) {
			details.append(calledClass.getMethodDetails());
		}
		return details.toString().trim();
	}

	@Override
	public void run() {
		save();
	}
	
}
