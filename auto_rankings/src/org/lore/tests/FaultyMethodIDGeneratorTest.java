package org.lore.tests;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lore.rankings.FaultyMethodIDGenerator;

public class FaultyMethodIDGeneratorTest {

	File logDir = new File("test_data/log");
	FaultyMethodIDGenerator faultyMethodIDGenerator = new FaultyMethodIDGenerator(logDir);
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testDbFileExisits() {
		assertTrue("DB File not found", faultyMethodIDGenerator.dbFileExisits());
	}
	
	@Test
	public void testReadIDs() {
		assertTrue("DB File not found", faultyMethodIDGenerator.readIDs());
	}
	
	@Test
	public void testGenerate128() {
		String[] methIDPair = {"128", "jester.tests.MutationsListTest.3(MutationsListTest; MutationMaker)"}; 
		String methodName = methIDPair[1];
		assertTrue("DB File not found", faultyMethodIDGenerator.generate(methodName));
		assertTrue("Method ID pair does not match", checkEquals(methIDPair, faultyMethodIDGenerator.getMethIDPair()));
	}
	
	@Test
	public void testGenerate170() {
		String[] methIDPair = {"170", "public final void com.sun.proxy.$Proxy4.setText(java.lang.String)"}; 
		String methodName = methIDPair[1];
		assertTrue("DB File not found", faultyMethodIDGenerator.generate(methodName));
		assertTrue("Method ID pair does not match", checkEquals(methIDPair, faultyMethodIDGenerator.getMethIDPair()));
	}
	
	@Test
	public void testGenerate237() {
		String[] methIDPair = {"237", "public final void com.sun.proxy.$Proxy5.iterate(jester.ClassTestTester) throws jester.SourceChangeException"}; 
		String methodName = methIDPair[1];
		assertTrue("DB File not found", faultyMethodIDGenerator.generate(methodName));
		assertTrue("Method ID pair does not match", checkEquals(methIDPair, faultyMethodIDGenerator.getMethIDPair()));
	}
	
	@Test
	public void testGenerate164() {
		String[] methIDPair = {"164", "private jester.Report jester.tests.ReportTest.newRealReport() throws jester.SourceChangeException"}; 
		String methodName = methIDPair[1];
		assertTrue("DB File not found", faultyMethodIDGenerator.generate(methodName));
		assertTrue("Method ID pair does not match", checkEquals(methIDPair, faultyMethodIDGenerator.getMethIDPair()));
	}
	
	@Test
	public void testGenerateRandom() {
		String[] methIDPair = {"---", "IgnoreListDocument.indexOf(java.lang.String;int)"}; 
		String methodName = methIDPair[1];
		assertTrue("DB File not found", faultyMethodIDGenerator.generate(methodName));
	}
	
	@Test
	public void testGenerateRandom2() {
		String[] methIDPair = {"---", "IgnoreListDocument.calculateIgnoreRegions(j"}; 
		String methodName = methIDPair[1];
		assertTrue("DB File not found", faultyMethodIDGenerator.generate(methodName));
	}
	
	private boolean checkEquals(String[] methIDPair, String[] methIDPair2) {
		
		if(methIDPair2 == null)
			return false;
		if(methIDPair.length != methIDPair2.length)
			return false;
		
		for(int i=0;i<methIDPair.length;i++) {
			if(!methIDPair[i].equals(methIDPair2[i]))
				return false;
		}
		
		return true;
	}
	
}
