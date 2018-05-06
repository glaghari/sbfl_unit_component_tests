package runCharm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import charm.AlgoCharm_Bitset;
import charm.TransactionDatabase;

public class DoCharm {
	
	private boolean done = false;
	private String output;
	private TransactionDatabase database = null;
	private boolean verbose=false;
	
	public DoCharm(File dir, File inputFile, boolean verbose) {
		this.verbose = verbose;
		this.database = readAndPrepareDatabase(inputFile);
		this.output = getOutputFile(dir,inputFile);
//		start();
		run();
	}
	
	public DoCharm(File dir, File inputFile) {
		this(dir,inputFile,false);
	}
	
	
	private String getOutputFile(File dir, File inputFile) {
		if(!dir.exists()) {
			if(verbose) System.out.println("dir does not exist " + dir);
			System.exit(0);
		}
		
		String outputFileName = dir.getAbsolutePath() + "/" + inputFile.getName();
		File f = new File(outputFileName);
		if(f.exists()) {
			System.out.println("File already exists! " + outputFileName);
			System.exit(0);
		}
		return outputFileName;
	}
	
	
	public void run() {
		runCharm();
		done = true;
	}
	
	
	public boolean isDone() {
		return this.done;
	}

	private void runCharm() {
		try {
			if(verbose) System.out.println("Generate sequences for " + output);
			new AlgoCharm_Bitset().runAlgorithm(output, database, 0.1, false, 10000);
		} catch (IOException e) {
			System.out.println(output);
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private TransactionDatabase readAndPrepareDatabase(File clazz) {
		
		TransactionDatabase td = new TransactionDatabase();
		try {
			if(clazz.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(clazz));
				String s = null;
				while((s = br.readLine()) !=null) {
					String sequence;
					String methodID[] = s.split(" ");
						List<Integer> transaction = new ArrayList<Integer>();
						for(int i=0;i<methodID.length;i++) {
							Integer item = Integer.parseInt(methodID[i]);
							transaction.add(item);
						}
						td.addTransaction(transaction);
				} // while
				 
				br.close();
				return td;
			} // if
			
		  }catch(Exception e) { e.printStackTrace(); System.exit(0);}
		return null;
		
	}

}
