package org.lore.rankings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.lore.rankings.methods.sequences.PatternedSpectrum;
import org.lore.util.Utils;

public class FaultyMethodID {

	static ArrayList<String> faultyMethodIDList = null;
	static String bugID = null;
		
//	public static void processFaultyMethodFile(File logTracesDir) {
	public static void processFaultyMethodFile(File tracesDir, File groundTruthDir) {
		HashMap<String, String> DB = getDB(tracesDir);
		File faultyIDFile = new File(groundTruthDir.getAbsolutePath() + File.separator + Utils.FAULTY_METHODS_FILE);
		
		if(!faultyIDFile.exists())
			return;

		FileReader reader = null;
		try {
			reader = new FileReader(faultyIDFile);
		} catch (FileNotFoundException e) {
			System.err.println(faultyIDFile.getAbsolutePath() + " file not found!");
			System.err.println(Utils.getStackStrace(e));
		}
		BufferedReader buffer = new BufferedReader(reader);
		try {
			bugID = buffer.readLine();
			String faultyMethodName = null;
			StringBuilder faultyMethods = new StringBuilder();
			faultyMethodIDList = new ArrayList<>();
			while((faultyMethodName = buffer.readLine()) != null)
				faultyMethodIDList.add(DB.get(faultyMethodName));
		} catch (Exception e) {
			System.err.println("Error processing file! : " + faultyIDFile.getAbsolutePath());
			System.err.println(Utils.getStackStrace(e));
			faultyMethodIDList = null;
		}
		
		finally {
			if(buffer != null)
				try {
					buffer.close();
				} catch (IOException e) {
					System.err.println(Utils.getStackStrace(e));
				}
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					System.err.println(Utils.getStackStrace(e));
				}
		}
	}
	
	private static HashMap<String, String> getDB(File tracesDir) {
		HashMap<String, String> DB = new HashMap<>();
//		File DBFile = new File(logTracesDir.getAbsolutePath() + File.separator + "DB.csv"); // For Project/bugIDb/log/DB.csv
//		File DBFile = new File(logTracesDir.getParentFile().getParentFile().getAbsolutePath() + File.separator + "DB.csv"); // For Project/bugIDb/log/DB.csv
		File DBFile = new File(tracesDir.getParentFile().getAbsolutePath() + File.separator + Utils.DB_FILE); // For Project/DB.csv
		if(!DBFile.exists())
			return null;
		
		FileReader reader = null;
		try {
			reader = new FileReader(DBFile);
		} catch (FileNotFoundException e) {
			System.err.println(DBFile.getAbsolutePath() + " file not found!");
			System.err.println(e);
			return null;
		}
		BufferedReader buffer = new BufferedReader(reader);
		try {
			buffer.readLine();
			String DBEntry = null;
			while((DBEntry = buffer.readLine()) != null) {
				String lineSplit[] = DBEntry.split(",");
				DB.put(lineSplit[1], lineSplit[0]);
				
			}
		} catch (Exception e) {
			System.err.println("Error processing file! : " + DBFile.getAbsolutePath());
			System.err.println(Utils.getStackStrace(e));
			return null;
		}
		
		finally {
			if(buffer != null)
				try {
					buffer.close();
				} catch (IOException e) {
					System.err.println(Utils.getStackStrace(e));
				}
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					System.err.println(Utils.getStackStrace(e));
				}
		}
		return DB;
	}

	public static ArrayList<String> getFaultyMethodIDList() {
		return faultyMethodIDList;
	}
	
	public static String getBugID() {
		return bugID;
	}
}
