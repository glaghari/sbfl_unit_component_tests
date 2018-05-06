package org.lore.testresultsanalyser;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author Gulsher Laghari
 * @version 1.0
 * This is a SAX handler that analyses JUnit test reports.
 * It reports weather the test suite passed with success or not.
 *
 */
public class TestReportAnalyseHandler extends DefaultHandler {

	static Logger logger = Logger.getLogger(TestReportParser.class);
	private TestReportStatistics testReportStatistics;
	private String testCaseClass = null;
	private String testCaseMethod = null;
	// if we dont change it in the code it means the test is passing
	private TestCaseStatus testStatus = TestCaseStatus.SUCCESS;
	private String testSuiteName = null;
	
	public TestReportAnalyseHandler(TestReportStatistics testReportStatistics) {
		this.testReportStatistics = testReportStatistics;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// If we see <testsuite> tag we have begun a testsuite.
		if(qName.equalsIgnoreCase("testsuite")) {
			//Lets add a testsuite
			testSuiteName  = attributes.getValue("name");
			int totalTestCases = Integer.valueOf(attributes.getValue("tests"));
			int testCaseFailures = Integer.valueOf(attributes.getValue("failures"));
			int testCaseErrors = Integer.valueOf(attributes.getValue("errors"));
			
			testReportStatistics.addTestSuite(testSuiteName);
			
			// First add total tests in the testsuite?
			testReportStatistics.addTotalTestCases(testSuiteName, totalTestCases);
						
			// Lets add if there is any failures in the testsuite?
			testReportStatistics.addTestCaseFailures(testSuiteName, testCaseFailures);
						
			// Lets add if there is any errors in the testsuite?
			testReportStatistics.addTestCaseErrors(testSuiteName, testCaseErrors);
			logger.debug("Test Suite: " + testSuiteName + "[T=" + totalTestCases + ", E=" + testCaseErrors
					+ ", F=" + testCaseFailures + "]");
			
		}
		
		//If wee see testcase tag we have seen a new test case
		if(qName.equalsIgnoreCase("testcase")) {
			testCaseClass  = attributes.getValue("classname");
			testCaseMethod = attributes.getValue("name");
		}
		
		// if we see error tag and testCaseClass or testCaseMethod is not null
		// it means we are processing test case tag
		
		if(qName.equalsIgnoreCase("error")) {
			testStatus = TestCaseStatus.ERROR;
		}
		
		// if we see error tag and testCaseClass or testCaseMethod is not null
		// it means we are processing test case tag
		if(qName.equalsIgnoreCase("failure")) {
			testStatus = TestCaseStatus.FAIL;
		}
 	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		// If we have processed testcase tag, lets add it in the testsuite. 
		if(qName.equalsIgnoreCase("testcase")) {
			
			//Lets make sure that we don't add test case if any of the one (class/method) is missing
			if(testCaseClass == null || testCaseMethod == null) return;
			
			//First we make it sure that we have processed testsuite tag
			//Although it shoud be true, since we are only here, once we have processed testsuite tag.
			if(testSuiteName !=null) {
				testReportStatistics.addTestCase(testSuiteName, testCaseClass, testCaseMethod, testStatus);
				logger.debug("Adding Test Case [" + testStatus + " - " + testCaseClass + "."+ testCaseMethod);
				
				//Now we set them back to original state so that the next test is not affected
				testCaseClass = null;
				testCaseMethod= null;
				// Be default every test should be set passed if we dont change the status variable above.
				testStatus = TestCaseStatus.SUCCESS;
			}
		}
		
		// Once we finish the processing of testsuite tag we set the name null 
		if(qName.equalsIgnoreCase("testsuite")) {
			testSuiteName = null;
		}
		
	}

	
}
