package calledclasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class CalledClass implements Comparable<CalledClass>{
	private Class _class = null;
	private String classPackage = null;
//	private boolean callerIsTestMethod;
	private TestCase testCase = null;
	private Set<String> methods = null;

	public CalledClass(String classPackage, Class _class, TestCase testCase) {
		this._class = _class;
		this.classPackage = classPackage;
		this.testCase = testCase;
//		this.callerIsTestMethod = false;
		this.methods = new HashSet<>();
	}
	
	private double getTestCaseNameMatch() {
		// Remove the "Test" in the beginning or at the end even if "Test" is followed by "s" 
		if(this.testCase == null) {
			Utils.handleException(new IllegalAccessError("NO TEST CASE " + this.testCase), "getTestCaseNameMatch()");
			return 0;
		}
		
		String testClass = this.testCase.getTestClassName();
		
		if(testClass == null) {
			Utils.handleException(new IllegalAccessError("NO TEST CASE " + this.testCase.getName()), "getTestCaseNameMatch()");
			return 0;
		}
			
		testClass = testClass.replaceAll("^Test|Test+s?$", "");
		
		double packageMatchScore = this.getNameSimilarity(this.classPackage, this.testCase.getPackage());
		double classMatchScore = this.getNameSimilarity(getClassName(), testClass);
		double methodMatchScore = this.getMethodNameSimilarity();
		
//		double classCalledFromTestMethodScore = this.callerIsTestMethod?1d:0d;
//		double classExactMatchScore = testClass.equals(getClassName())?1d:0d;
		
		double matchScore =   classMatchScore
							+ packageMatchScore
//							+ classExactMatchScore
//							+ classCalledFromTestMethodScore
							+ methodMatchScore
							;
		return matchScore;
	}
	
	private String getMatchScores() {
		StringBuilder scores = new StringBuilder();
		
		String testClass = this.testCase.getTestClassName().replaceAll("^Test|Test+s?$", "");
		double packageMatchScore = this.getNameSimilarity(this.classPackage, this.testCase.getPackage());
		double classMatchScore = this.getNameSimilarity(getClassName(), testClass);
		double methodMatchScore = this.getMethodNameSimilarity();
//		double classCalledFromTestMethodScore = this.callerIsTestMethod?1d:0d;
//		double classExactMatchScore = testClass.equals(getClassName())?1d:0d;
		
		scores.append("[p:" + String.format("%.2f", packageMatchScore));
		scores.append(" c:" + String.format("%.2f", classMatchScore));
//		scores.append(" e:" + String.format("%.2f", classExactMatchScore));
		scores.append(" m:" + String.format("%.2f", methodMatchScore));
//		scores.append(" tm:" + String.format("%.2f", classCalledFromTestMethodScore));
		scores.append("]");
		return scores.toString();
	}

	private double getMethodNameSimilarity() {
		String testMethod = this.testCase.getTestMethod().replaceAll("^test", "");
		ArrayList<Double> methodSimilarityScores = new ArrayList<>();
		for(String method:this.methods) {
			double methodSimilarity = getNameSimilarity(method, testMethod);
			methodSimilarityScores.add(methodSimilarity);
		}
		if(methodSimilarityScores.size() == 0)
			return 0d;
		return Collections.max(methodSimilarityScores);
	}

	private double getNameSimilarity(String source, String criterion) {
		String[] sourcePieces = StringUtils.splitByCharacterTypeCamelCase(source);
		String[] criterionPieces = StringUtils.splitByCharacterTypeCamelCase(criterion);
		Set<String> sourcePiecesSet = new HashSet<>();
		for(String sourcePiece:sourcePieces)
			sourcePiecesSet.add(sourcePiece.toLowerCase());
		
//		Set<String> sourcePiecesSet = new HashSet<>(Arrays.asList(sourcePieces));
		sourcePiecesSet.remove(".");
		
		Set<String> criterionPiecesSet = new HashSet<>();
		for(String criterionPiece:criterionPieces)
			criterionPiecesSet.add(criterionPiece.toLowerCase());
		
//		Set<String> criterionPiecesSet = new HashSet<>(Arrays.asList(criterionPieces));
		criterionPiecesSet.remove(".");
		
		// Find the union
		Set<String> unionSet = new HashSet<>(criterionPiecesSet);
		unionSet.addAll(sourcePiecesSet);
		
		// Find the intersection
		criterionPiecesSet.retainAll(sourcePiecesSet); // After this call criterionPiecesSet contains the intersection of the two 
		
		return (double) criterionPiecesSet.size() / unionSet.size();
	}
	
	public double getTestMatchScore() {
		return getTestCaseNameMatch();
	}

	public String getClassName() {
		return _class.getSimpleName();
	}
	
	public boolean isSuperClass(CalledClass superClass) {
		return superClass._class.isAssignableFrom(this._class);
	}
	
	public String getClassPackage() {
		return classPackage;
	}
	
	@Override
	public int compareTo(CalledClass otherCalledClass) {
		return (int) (otherCalledClass.getTestMatchScore()*100 - this.getTestMatchScore()*100);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
	        return false;
	    }
		
		if (!CalledClass.class.isAssignableFrom(obj.getClass()))
		        return false;
		 
		final CalledClass otherCalledClass = (CalledClass) obj;
		return this.getClassName().equals(otherCalledClass.getClassName()) && this.getClassPackage().equals(otherCalledClass.getClassPackage());
	}
	
	@Override
	public int hashCode() {
	    int hash = 31;
	    hash = 31 * hash + this.getClassName().hashCode();
	    hash = 31 * hash + this.getClassPackage().hashCode();
	    return hash;
	}

	public boolean isMock() {
		String className = this.getClassName().toLowerCase();
		if(className.contains("mock"))
			return true;
		return false;
	}

	public void addMethod(String calledMethod) {
		this.methods.add(calledMethod);
	}
	
	public String toString() {
		return this.classPackage + "." + this.getClassName();
	}
	
	public String getDetails() {
		StringBuilder details = new StringBuilder();
		if(this.isMock())
			details.append(getMatchScores());
		if(!this.isMock())
			details.append("[" + String.format("%.2f",getTestCaseNameMatch()) + "] ");
		details.append(this.classPackage);
		details.append(".");
		details.append(getClassName());
		details.append(" extends ");
		details.append(this._class.getSuperclass() !=null?this._class.getSuperclass().getName():"");
		return details.toString();
	}

	public String getMethodDetails() {
		StringBuilder details = new StringBuilder();
		if(this.methods != null) {
			for(String method : this.methods) {
				details.append(this.getCanonicalName());
				details.append(".");
				details.append(method);
				details.append("\n");
			}
		}
		return details.toString();
	}

	private String getCanonicalName() {
		return this.getClassPackage() + "." + this.getClassName();
	}

	public int getMethodCount() {
		return this.methods.size();
	}

	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}

}
