package org.lore.rankings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.lore.rankings.methods.sequences.PatternedSpectrum;

public class FaultyMethodIDGenerator {

//	static Logger logger = Logger.getLogger(FaultyMethodIDGenerator.class);
	
	private File logTracesDir;
	private int totalMethods;
	private HashMap<String, String> methodIDs;
	private String[] methIDPair = null;
	
	public FaultyMethodIDGenerator(File logDir) {
		this.logTracesDir = logDir;
		methodIDs = new HashMap<String, String>();
	}
	
	/**
	 * 
	 * @param method exact method name as returned by java.lang.Method.getName()
	 * @return weather the faulty method ID file was generated or not
	 */
	public boolean generate(String method) {
		
		if(!readIDs())
			return false;
		if(!findID(method))
			return false;
		return writefaultyIDFile();
		
	}
	
	public boolean generate(File methodFile) {
		
		FileReader reader;
		try {
			reader = new FileReader(methodFile);
		} catch (FileNotFoundException e) {
//			logger.error("Faulty method name file not found! See logs.", e);
			System.err.println("Faulty method name file not found! See logs");
			System.err.println(e);
			return false;
		}
		BufferedReader buffer = new BufferedReader(reader);
		try {
			String method = buffer.readLine();
			return generate(method);
		} catch (Exception e) {
//			logger.error("Faulty method ID can't be generated!. Terminating... See logs.", e);
			System.err.println("Faulty method ID can't be generated!. Terminating...");
			System.err.println(e);
			return false;
		}
	}

	/**
	 * @param method
	 * @return
	 */
	private boolean findID(String method) {
		boolean found = false;
		boolean foundDuplicate = false;
		String methodID = null;
		Iterator<String> keysIterator = this.methodIDs.keySet().iterator();
		while(keysIterator.hasNext()) {
			String id = keysIterator.next();
			String methodName = this.methodIDs.get(id);
			
			if(methodName.contains(method)) {
				// If we already have seen the method then its a duplicate entry
				if(found) {
					foundDuplicate = true;
				}
				found = true;
				methodID = id;
			}
		}
		
		if(found && !foundDuplicate) {
			this.methIDPair = new String[2];
			this.methIDPair[0] = methodID;
			this.methIDPair[1] = this.methodIDs.get(methodID);
			return true;
		}
		
		return false;
	}
	
	public String getFaultyMethodID() {
		return this.methIDPair[0];
	}

	public boolean dbFileExisits() {
		File dbFile = new File(logTracesDir.getAbsolutePath() + "/DB.csv");
		boolean fileExists = dbFile.exists();
		if(!fileExists) {
//			logger.error("DB.csv file not found! See logs.");
			System.err.println("DB.csv file not found! See Logs");
		}
		return fileExists;
	}
	
	public boolean readIDs() {
		if(!dbFileExisits())
			return false;
		
		File dbFile = new File(logTracesDir.getAbsolutePath() + "/DB.csv");
		FileReader reader;
		try {
			reader = new FileReader(dbFile);
		} catch (FileNotFoundException e) {
//			logger.error("DB.csv file not found! See logs.", e);
			System.err.println("DB.csv file not found! See Logs");
			System.err.println(e);
			return false;
		}
		BufferedReader buffer = new BufferedReader(reader);
		try {
			String line = buffer.readLine();
			if(line.endsWith(","))
				line = line.substring(0, line.length()-1);
			totalMethods = Integer.parseInt(line);
			while((line = buffer.readLine()) !=null) {
				String[] methodIDPair = line.split(",");
				this.methodIDs.put(methodIDPair[0], methodIDPair[1]);
			}
			if(totalMethods != this.methodIDs.size())
				return false;
		} catch (Exception e) {
//			logger.error("Error processing DB.csv file! See logs.", e);
			System.err.println("Error processing DB.csv file! See logs.");
			System.err.println(e);
			return false;
		}
		
		return true;
	}
	
	public String[] getMethIDPair() {
		return methIDPair;
	}
	
	 private boolean writefaultyIDFile() {
			try {
				PrintStream out = new PrintStream(logTracesDir.getAbsolutePath() + "/faultyMethodID.txt");
				StringBuilder contents = new StringBuilder();
				contents.append(this.methIDPair[0]);
				contents.append("\n");
				contents.append(this.methIDPair[1]);
				out.print(contents.toString());
				out.flush();
				out.close();
			} catch (FileNotFoundException e) {
//				logger.error("Could'nt write " + logTracesDir.getAbsolutePath() + "/faultyMethodID.txt", e);
				System.err.println("Could'nt write " + logTracesDir.getAbsolutePath() + "/faultyMethodID.txt See logs..");
				System.err.println(e);
				return false;
			}
			return true;
		}
}
