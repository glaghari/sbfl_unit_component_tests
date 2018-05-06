package calledclasses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class TraceManager {
	private Map<String, CalledClass> calledClasses = null;
	private Map<String, CalledClass> mockClasses = null;
	private Thread saveTrace;
	private TestCase testCase = null;
	
	public TraceManager() {
		this.calledClasses = new HashMap<>();
		this.mockClasses = new HashMap<>();
	}
	
	public void setTestCaseName(String testCaseName) {
		this.testCase = new TestCase(testCaseName);
		
		// Since we know now that there is the test case to claim the collected trace,
		// Initialise trace save object
		this.saveTrace = new SaveTrace(this);
		Runtime.getRuntime().addShutdownHook(saveTrace);
	}
	
	public String getTestCaseName() {
		if(this.testCase == null)
			return null;
		return this.testCase.getName();
	}
	
	public boolean isTestCaseNameSet() {
		// If testCaseName is not null it means testCaseName is set
		return this.testCase != null;
	}

	public boolean isTestCaseNameSame(String testCaseName) {
		if(this.testCase != null)
			return this.testCase.match(testCaseName);
		return false;
	}

	public List<CalledClass> getCalledClasses() {
		List<CalledClass> classesUnderTest = this.getClassesUnderTest();
		List<CalledClass> calledClasses = new ArrayList<>(this.calledClasses.values());
		
		// Remove classes under test
		calledClasses.removeAll(classesUnderTest);
		
		// If any of the remaining classes is supr class of class under test remove this also
		for(CalledClass calledClass : new ArrayList<>(calledClasses)) {
			for(CalledClass classUnderTest : classesUnderTest) {
				if(classUnderTest.isSuperClass(calledClass)) {
					calledClasses.remove(calledClass);
				}
			}
		}
		
		return calledClasses;
	}

	private List<CalledClass> getSortedCalledClasses() {
		ArrayList<CalledClass> calledClassesList = new ArrayList<>(this.calledClasses.values());
		Collections.sort(calledClassesList);
		return  calledClassesList;
	}

	public CalledClass addCalledClass(String calledClassPackage, Class calledClass) {
		CalledClass _calledClass = getCalledClass(calledClassPackage, calledClass);
		return _calledClass;
	}

	private CalledClass getCalledClass(String calledClassPackage, Class calledClass) {
		
		if(Exception.class.isAssignableFrom(calledClass))
			return null;
		
		CalledClass _calledClass = new CalledClass(calledClassPackage, calledClass, testCase);
		String key = _calledClass.toString();
		if(_calledClass.isMock())
			this.mockClasses.put(key, _calledClass);
		else {
			CalledClass _calledClassExisting = this.calledClasses.get(key);
			if(_calledClassExisting == null)
				this.calledClasses.put(key, _calledClass);
			else
				_calledClass = _calledClassExisting;
		}
		return _calledClass;
	}

	public List<CalledClass>  getClassesUnderTest() {
		Iterator<CalledClass> calledClassesIterator = getSortedCalledClasses().iterator();
		ArrayList<CalledClass> classesUnderTest = new ArrayList<>();
		if(calledClassesIterator.hasNext()) {
			classesUnderTest.add(calledClassesIterator.next());
		}
		// Exclude all called classes other than class under test.
		while(calledClassesIterator.hasNext()) {
			CalledClass calledClass = calledClassesIterator.next();
			if(classesUnderTest.get(0).getTestMatchScore() <= calledClass.getTestMatchScore())
				classesUnderTest.add(calledClass);
			else
				break;
		}
		
		return classesUnderTest;
	}

	public Collection<CalledClass> getMockClasses() {
		return this.mockClasses.values();
	}

	public String getTestType() {
		return this.getCalledClasses().size() > 0? "CT": "UT";
	}

	public void removeShutdownHook() {
		Runtime.getRuntime().removeShutdownHook(this.saveTrace);
	}

	public int getMethodsCount() {
		int methodsCount = 0;
		for(CalledClass calledClass : this.calledClasses.values()) {
			methodsCount += calledClass.getMethodCount();
		}
		return methodsCount;
	}

	public void doFinalDataPreparatorySteps() {
		removeTestClass();
		setTestCaseName();
		removeUtilClasses();
	}
	
	private void removeTestClass() {
		String testClassName = this.testCase.getTestClassName();
		for(String key : this.calledClasses.keySet()) {
			// CalledClass calledClass : calledClasses) {
			CalledClass calledClass = this.calledClasses.get(key);
			if(calledClass.getClassName().equals(testClassName)) {
				this.calledClasses.remove(key);
				break;
			}
		}	
	}
	
	private void setTestCaseName() {
		Collection<CalledClass> calledClasses = this.calledClasses.values();
		for(CalledClass calledClass : calledClasses) {
			calledClass.setTestCase(this.testCase);
		}
	}
	
	private void removeUtilClasses() {
		Collection<CalledClass> calledClasses = this.getCalledClasses();
		for(CalledClass calledClass : calledClasses) {
			boolean isUtilityClass = calledClass.getClassName().contains("Util") || calledClass.getClassPackage().contains("util");
			if(isUtilityClass) {
				String key = calledClass.toString();
				this.calledClasses.remove(key);
			}
		}
	}
}