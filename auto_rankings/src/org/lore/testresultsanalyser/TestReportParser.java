package org.lore.testresultsanalyser;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.lore.rankings.FaultyMethodIDGenerator;
import org.xml.sax.SAXException;

public class TestReportParser {
	
	static Logger logger = Logger.getLogger(TestReportParser.class);
	
	private File testReportDir;
	private TestReportStatistics testReportStatistics;
	
	public TestReportParser(File testReportDir, TestReportStatistics testReportStatistics) {
		this.testReportStatistics = testReportStatistics;
		this.testReportDir = testReportDir;
	}
	
	public boolean parseTestReport() {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser;
	    try {
	        saxParser = saxParserFactory.newSAXParser();
	        TestReportAnalyseHandler handler = new TestReportAnalyseHandler(testReportStatistics);
	      //First check if there is a combined Test suite XML repot, if present parse that.
	    	// Otherwise parse all report files.
	    	File combinedTestReport = new File(testReportDir.getAbsolutePath()+"/TESTS-TestSuites.xml");
	    	if(combinedTestReport.exists()) {
	    		logger.debug("Processing test report file " + combinedTestReport.getAbsolutePath());
	    		saxParser.parse(combinedTestReport, handler);
	    	}
	    	else {
	    		for(File file:testReportDir.listFiles()) {
	            	// parse all test report files
	            	if(file.getName().endsWith(".xml")) {
	            		logger.debug("Processing test report file " + file.getAbsolutePath());
	            		saxParser.parse(file, handler);
	            	}
	            }
	    	}
	    	
	    } catch (IOException | SAXException | ParserConfigurationException e) {
	    	logger.error("Oops! Something went wrong while processing JUnit xml report files!", e);
	    	System.err.println("Oops! Something went wrong while processing JUnit xml report files!\n See Logs...");
	    	return false;
	    }
	    return true;
	}

	private boolean combinedTestReportExists() {
		File combinedTestReport = new File(testReportDir.getAbsolutePath()+"/TESTS-TestSuites.xml");
		return combinedTestReport.exists();
	}

}
