package org.lore.sequencegenerator;

import java.io.File;

import org.lore.util.Utils;

import runCharm.DoCharm;

public class SequenceGenerator {
	
	private int minSequenceSize = 1;
	private File tracesDir ; 
	private boolean verbose = false;
	
	public SequenceGenerator(File tracesDir, int minSequenceSize, boolean verbose) {
		this.verbose = verbose;
		this.tracesDir = tracesDir;
		this.minSequenceSize = minSequenceSize;
	}
	
	public void generateSequencesWithCharm() {
		// Lets first get the traces directory where all testcase foldes are contained.
//		File tracesDir = new File(tracesDir.getAbsolutePath() + "/traces");

		// Now we have all the testcase directories. We go through each.
		// All tests folders
		for(File dir:tracesDir.listFiles()) {
			// Single test
			if(dir.isDirectory()) {
				if(verbose) System.out.println(Utils.NEW_LINE + dir.getName());
				// Search for hashes and patterns folders
				for(File d:dir.listFiles()) {
					if(d.isDirectory()) {
						if (d.getName().equals(Utils.METHOD_TRACES)) {
							File patternsDir = new File(d.getParent() + File.separator + Utils.PATTERNS_DIR);
							if(verbose) System.out.println("Delete " + patternsDir.getAbsolutePath());
							deleteDirectory(patternsDir);
							if(verbose) System.out.println("Create " + patternsDir.getAbsolutePath());
							patternsDir.mkdirs();
							// Generate patterns
							for(File f:d.listFiles()) {
								if(f.getName().endsWith(Utils.TXT_EXTENSION)) {
									new DoCharm(patternsDir,f, this.minSequenceSize, verbose);
								}
							}
						}
					}
				} // for hashes and patterns folders
				
			} // if single test
			
		} // for all tests
	}
	
	public boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}

}
