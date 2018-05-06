package calledclasses;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

public class Utils {
	public static String TEST_FILE_NAME = "test.txt";
	public static String EXCEPTION_FILE_NAME = "exceptions.txt";
	public static String LOG_FILE_NAME = "log_traces.txt";
	public static String TESTS_INFO_DIR = "tests_info_dir";
	public static final String FAILING_TESTS_FILE = "failing_tests.txt";
	public static final Object GROUND_TRUTH_DIR = "ground_truth_dir";
	
	public static void log(final String message, final String fileName, final boolean append) {
		BufferedOutputStream stream = null;
		try {
			stream = new BufferedOutputStream(new FileOutputStream(fileName, append));
			byte[] traceBytes = message.getBytes();
			stream.write(traceBytes, 0, traceBytes.length);
			stream.write('\n');
		} catch (FileNotFoundException e) {
			System.err.println(e.getStackTrace());
		} catch (IOException e) {
			System.err.println(e.getStackTrace());
		}
		finally {try { stream.flush(); stream.close();} catch (IOException e) {}}
	}
	
	public static void handleException(Throwable exception, String where) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		exception.printStackTrace(ps);
		String stackTrace = new String(baos.toByteArray());
		String message = "[" + where + "]\n";
		message += "[" + exception.getLocalizedMessage() + "]\n";
		message += stackTrace;
		Utils.log(message, EXCEPTION_FILE_NAME, true);
	}
	
	public static Class getClass(Signature signature, Class clazz) {
		if(signature instanceof MethodSignature) {
			MethodSignature methodSignature = (MethodSignature) signature;
			Method method = methodSignature.getMethod();
			if (method.getDeclaringClass().isInterface()) {
		        try {
		        	if(clazz == null)
		        		clazz = signature.getDeclaringType();
		            method= clazz.getMethod(signature.getName(), method.getParameterTypes());
		        } catch (final Exception exception) {
					String message = null;
					try {
						message = signature.toLongString() + " - " + signature.getDeclaringTypeName() + " - " + signature.getDeclaringType().getMethod(signature.getName(), method.getParameterTypes());
					} catch (NoSuchMethodException | SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Utils.handleException(exception, message);
		        	return null;
		        }
		    }
			return method.getDeclaringClass();
		}
		return clazz;
	}

}
