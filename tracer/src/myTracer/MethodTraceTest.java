package myTracer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MethodTraceTest {

	private MethodTrace trace;
	@Before
	public void setUp() throws Exception {
		this.trace = new MethodTrace(100, true);
	}

	@Test
	public void testAddTrace() {
		addTraces();
		assertEquals("[" + 100 + "]", "[" + this.trace.getID() + "]");
	}

	@Test
	public void testCloseTrace() {
		addTraces();
		this.trace.closeTrace();
		assertNotNull(this.trace.getTrace());
	}

	@Test
	public void testToString() {
		addTraces();
		addTraces();
		addTraces();
		String expected = "1 2 3 4 5\n1 2 3 4 5\n1 2 3 4 5";
		assertEquals(expected, this.trace.toString());
	}

	@Test
	public void testGetTrace() {
		addTraces();
		addTraces();
		String expected = "1 2 3 4 5\n1 2 3 4 5";
		assertEquals(expected, this.trace.getTrace());
	}
	
	private void addTraces() {
		this.trace.addTrace(1);
		this.trace.addTrace(2);
		this.trace.addTrace(3);
		this.trace.addTrace(4);
		this.trace.addTrace(5);
		this.trace.closeTrace();
	}

}
