package org.lore.testresultsanalyser;

import java.util.ArrayList;
import java.util.Iterator;

public class TestCasesReport {
	
	private ArrayList<String> failingTestCases = new ArrayList<String>();
	private ArrayList<String> passingTestCases = new ArrayList<String>();
	
	public ArrayList<String> getFailingTestCases() {
		return failingTestCases;
	}
	
	public ArrayList<String> getPassingTestCases() {
		return passingTestCases;
	}
	
	public void addFailingTestCases(ArrayList<String> failingTestCases) {
		this.failingTestCases.addAll(failingTestCases);
	}
	
	public void addPassingTestCases(ArrayList<String> passingTestCases) {
		this.passingTestCases.addAll(passingTestCases);
	}
	
	public int getNumberOfFailingTestCases() {
		return failingTestCases.size();
	}
	
	public int getNumberOfPassingTestCases() {
		return passingTestCases.size();
	}
	
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append("\nPassing test cases\n-----------------------------------");
		int i=0;
		Iterator<String> iterator = this.passingTestCases.iterator();
		while(iterator.hasNext()) {
			stringBuilder.append("\n" + ++i + ". " + iterator.next());
		}
		
		i=0;
		stringBuilder.append("\n\nFailing test cases\n-----------------------------------");
		iterator = this.failingTestCases.iterator();
		while(iterator.hasNext()) {
			stringBuilder.append("\n" + ++i + ". " + iterator.next());
		}
		return stringBuilder.toString();
	}

}
