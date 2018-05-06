package myTracer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MethodIDManager {
	private static HashMap<String, MethodSignature> idList = new HashMap<String, MethodSignature>();
	private static HashMap<Integer, MethodSignature> idListInt = new HashMap<Integer, MethodSignature>();
	
	private static Integer lastUniqueID = 0;
	private static String dbFile = null;
	private static boolean listModified = false;
	
	static {
		// initialize db file path
		String dbDir = System.getenv().get(Utils.TRACES_DIR);
		if(dbDir == null)
			dbDir = System.getProperty("user.dir");
		
		dbFile = new File(dbDir).getParentFile().getAbsolutePath() + File.separator + Utils.DB_FILE;
		
		Runtime.getRuntime().addShutdownHook(
			new Thread() {
				public void run() {
					if(listModified)
						writeDBFile();
				}
			}
		);
		
		 try {
			 File f = new File(dbFile);
			if(f.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String s = null;
				lastUniqueID = Integer.valueOf(br.readLine());
				while((s = br.readLine()) !=null) {
					String triplet[] = s.split(Utils.SEPARATOR);
					if(idList.get(triplet[1]) == null) {
						MethodSignature ms = new MethodSignature(triplet);
							idList.put(ms.getMethod(), ms);
							idListInt.put(ms.getID(), ms);
					}
				} // while
				br.close();
			} // if
		  }catch(Exception e) {}
		} // end static block

	

	public static MethodSignature getMethodSignature(String methodName) {
		MethodSignature ms = idList.get(methodName);
		return ms;
	}
	
	public static MethodSignature getMethodSignature(Integer id) {
		MethodSignature ms = idListInt.get(id);
		return ms;
	}
	
	
	public static MethodSignature addMethodSignature(String methodName) {
		lastUniqueID++;
		MethodSignature ms = new MethodSignature(lastUniqueID, methodName);
		idList.put(ms.getMethod(), ms);
		idListInt.put(ms.getID(), ms);
//		saveToFile();
		listModified = true;
		return ms;
	}
	
	
//	public static void saveToFile() {
//		new Thread() {
//			public void run() {
//				writeDBFile();
//			}
//		}.start();
//	}

	private static void writeDBFile() {
		PrintStream ps = null;
//		File f = new File("log");
//		if(!f.exists())
//			f.mkdirs();
		
		try {
//			ps = new PrintStream(dbFile);
			ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(dbFile)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// Save to file the last used unique ID for a method
		ps.print(lastUniqueID.toString());
		
		// Save to a file the ID,method
		
		HashMap<String, MethodSignature> idListClone = new HashMap<String, MethodSignature>();
		synchronized (idList) {
			idListClone = (HashMap<String, MethodSignature>) idList.clone();
		}
				
		// Following code somehow solves java.util.ConcurrentModificationException
		for(Map.Entry<String, MethodSignature> entry : idListClone.entrySet()) {
			MethodSignature ms = entry.getValue();
			ps.println();
			ps.print(ms.getID() + Utils.SEPARATOR + ms.getMethod());
		}
				
		ps.close();
	}
}
