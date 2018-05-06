package extract;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import antlr.JavaLexer;
import antlr.JavaParser;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.*;

public class ExtractEntityTool {
	
//	public static final String FAULTY_METHODS_DIR = "faulty_methods";
//	public static final String DIFF_DIR = "diff";
	public static final String EXTENSION = ".txt";

    public static void main(String[] args) throws Exception {
//    	parseFile2();
//    	System.exit(0);
    	
    	if ( args.length < 7 ) {
    		System.exit(-1);
    	}
    	
    	String projectSourceDir = args[0];
    	String groundTruthDir = args[1];
    	String project = args[2];
    	String bugID   = args[3];
    	String faultyLinesFile = args[4];
    	String outDirFaultyStatements = args[5];
    	String outDirFaultyMethods = args[6];
    	
//    	fileExitsCheck(groundTruthDir, project, bugID, outDirFaultyStatements);
//    	fileExitsCheck(groundTruthDir, project, bugID, outDirFaultyMethods);
        
        Map<String, List<Integer>> files = processFaultyLineFile(faultyLinesFile);
        for (Map.Entry<String, List<Integer>> entry : files.entrySet()) {   
			   String fileName = entry.getKey();
			   List<Integer> lineNumbers = entry.getValue();
			   fileName = projectSourceDir + File.separator + project + File.separator +  bugID + "b" + File.separator + fileName;
			   parseFile(fileName, lineNumbers, groundTruthDir, project, bugID, outDirFaultyStatements, outDirFaultyMethods);
        }
    }
    
    private static void fileExitsCheck(String groundTruthDir, String project, String bugID, String outDir) throws Exception {
    	String fileName = getFileName(groundTruthDir, project, bugID, outDir);
		File file = new File(fileName);
		if(file.exists())
			return;
		if(! file.getParentFile().exists())
			file.getParentFile().mkdirs();
		
		file.createNewFile();
	}

	public static String getFileName(String groundTruthDir, String project,
			String bugID, String outDir) {
		String fileName = groundTruthDir + File.separator + outDir + File.separator + project + File.separator + bugID + EXTENSION;
		return fileName;
	}

	private static Map<String, List<Integer>> processFaultyLineFile(String faultyLinesFile) throws Exception {
    	Map<String, List<Integer>> faultyClassesAndLines = new HashMap<String, List<Integer>>();
    	FileInputStream fis = new FileInputStream(new File(faultyLinesFile));
    	BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line = null;
		while ((line = br.readLine()) != null) {
			int firstCommaIndex = line.indexOf(",");
			String fileName = line.substring(0, firstCommaIndex);
			String lines = line.substring(firstCommaIndex + 1);
			String[] data = lines.split(",");
			List<Integer> lineNumbers = new ArrayList<Integer>();
			for(String l : data) {
				lineNumbers.add(Integer.valueOf(l));
			}
			faultyClassesAndLines.put(fileName, lineNumbers);
		}
 
		br.close();
		fis.close();
		
		return faultyClassesAndLines;
    }
    
    private static void parseFile(String fileName, List<Integer> lineNumbers, String groundTruthDir, String project, String bugID, String outDirFaultyStatements, String outDirFaultyMethods) throws Exception {
		CharStream input = CharStreams.fromFileName(fileName);
		JavaLexer lexer = new JavaLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JavaParser parser = new JavaParser(tokens);
		
		IntInterval.setLineNumbers(lineNumbers);
		
		CompilationUnitListener compilationUnitListener = new CompilationUnitListener();
		
		parser.compilationUnit().enterRule(compilationUnitListener);
//		compilationUnitListener.printClasses();
		writeContents(groundTruthDir, project, bugID, outDirFaultyMethods, compilationUnitListener.getMethods());
		writeContents(groundTruthDir, project, bugID, outDirFaultyStatements, compilationUnitListener.getStatements());
    }
    
    private static void writeContents(String groundTruthDir, String project, String bugID, String outDir, String contents) throws Exception {
    	if(contents.length() < 1)
    		return;

    	fileExitsCheck(groundTruthDir, project, bugID, outDir);
    	
    	BufferedOutputStream stream = null;
    	String fileName = getFileName(groundTruthDir, project, bugID, outDir); 
		stream = new BufferedOutputStream(new FileOutputStream(fileName, true));
		byte[] traceBytes = contents.getBytes();
		stream.write(traceBytes, 0, traceBytes.length);
		stream.flush();
		stream.close();
	}
}
