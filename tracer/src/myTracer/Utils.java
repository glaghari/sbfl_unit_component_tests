package myTracer;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.InitializerSignature;
import org.aspectj.lang.reflect.MethodSignature;

public class Utils {
	public static String TEST_FILE_NAME = "test.txt";
	public static String EXCEPTION_FILE_NAME = "exceptions.txt";
	
	// Commons
	public static final String TRACES = "patterned_coverage";
	public static final String TRACE_FILE = "raw_coverage.csv";
	public static final String TXT_FILE_EXTENSION = ".txt";
	public static final String FAILING_TESTS_FILE = "failing_tests.txt";
	public static final String TRACES_DIR = "traces_dir";
	public static final String DB_FILE = "DB.csv";
	public static final Object GROUND_TRUTH_DIR = "ground_truth_dir";
	public static final String SEPARATOR = ",";
	
//	public static Logger log = Logger.getLogger(TraceManager.class);
//	static {
//		File f = new File(System.getProperty("user.dir")).getParentFile().getParentFile().getParentFile();
//		f = new File(f, "tracer2/resources/log4j.properties");
//        PropertyConfigurator.configure(f.getAbsolutePath());
//	}

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
	
	public static String getSourceMethod(JoinPoint.StaticPart sourceJoinPoint) {
		Signature signature = sourceJoinPoint.getSignature();
		Class _class = signature.getDeclaringType();
		// If package is null it may be that the source method is of primitive type array 
		// Example -> Object double[].clone()
		// This also causes infinite recursion in double[].clone() in the test cases of following Test class 
		// Defects4J Math/6b org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest
		// We are not interested in such method so return null
		if(_class.isArray()) {
			//log.info("[GET SOURCE END WITH NULL] - IS ARRAY");
			return null;
		}
		// If source method is from java library return null. The trace for java library methods need not be collected
		else {
			String _className = _class.getName();
			//log.info("[GET SOURCE END WITH NULL] - JAVA CLASS");
			if(_className.startsWith("java.") || _className.startsWith("javax.") || _className.startsWith("sun."))
				return null;
		}
		
		// JUnit 3
		// If source method is from TestCase class return null. The trace for methods in TestCase classes need not be collected
		if(junit.framework.TestCase.class.isAssignableFrom(_class)) {
			//log.info("[GET SOURCE END WITH NULL] - TEST CLASS");
			return null;
		}

		//log.info("[SOURCE CHECK CONSTRUCTOR]");
		if(ConstructorSignature.class.isAssignableFrom(signature.getClass())) {
			ConstructorSignature cons = (ConstructorSignature) signature;
			//log.info("[GET SOURCE CONSTRUCTOR END]");
			return cons.getConstructor().toString();
		}		
		
		//log.info("[SOURCE CHECK METHOD SIGNATURE]");
		if(org.aspectj.lang.reflect.MethodSignature.class.isAssignableFrom(signature.getClass())) {
			Method method = ((org.aspectj.lang.reflect.MethodSignature) signature).getMethod();
			// JUnit 4
			// If source method is from TestCase class return null. The trace for methods in TestCase classes need not be collected
			//log.info("[SOURCE CHECK ANNOTATIONS]");
			Annotation[] annotations = method.getDeclaredAnnotations();
			for(Annotation annotation:annotations) {
				if(annotation instanceof org.junit.Before || annotation instanceof org.junit.Test || annotation instanceof org.junit.After) {
					//log.info("[GET SOURCE END WITH NULL] - ANNOTATIONS");
					return null;
				}
			}
			//log.info("[SOURCE GET METHOD]");
			if( method.getDeclaringClass().isInterface()) {
				// This conversion is required because we want the method signature with class name
				// even if it is called with reference of say an interface
				method = getMethod(_class, method);
			}
			
			if(method != null)
				return method.toString();
		}
		//log.info("[GET SOURCE END WITH NULL] - FINAL");
		return null;
	}
	
	public static String getTargetMethod(JoinPoint targetJoinPoint) {
		Signature signature = targetJoinPoint.getSignature();
		if(ConstructorSignature.class.isAssignableFrom(signature.getClass())) {
			//log.info("[GET TARGET END] - CONSTRUCTOR");
			return ((ConstructorSignature) signature).getConstructor().toString();
		}
		
		if( MethodSignature.class.isAssignableFrom(signature.getClass())) {
			try {
				//log.info("[GET TARGET START]");
				Method method = ((MethodSignature) signature).getMethod();
				if( method.getDeclaringClass().isInterface()) {
					// This conversion is required because we want the method signature with class name
					// even if it is called with reference of say an interface
					Object targetObject = targetJoinPoint.getTarget();
					Class _class = targetObject != null? targetObject.getClass() : signature.getDeclaringType();
					method = getMethod(_class, method);
				}

				if(method != null)
					return method.toString();

			}catch(ClassCastException exception) {
				String message = "executed targetMethod not resolved - " + signature.toLongString();
				handleException(exception, message);
				return null;
			}
		}
		//log.info("[GET TARGET END WITH NULL] - FINAL");
		return null;
	}
	
	@Deprecated
	public static String getSourceMethod(Signature signature) {
		//log.info("[GET SOURCE START]");
		//log.info("[SOURCE CHECK INITIALIZER SIGNATURE]");
		if(signature instanceof InitializerSignature) {
			//log.info("[GET SOURCE END WITH NULL] - INITIALIZER SIGNATURE");
			return null;
		}
		
		//log.info("[SOURCE CHECK JAVA CLASS]");
		// If source method is from java library return null. The trace for java library methods need not be collected		
		Class _class = signature.getDeclaringType();
		Package _package = _class.getPackage();
		String packageName = null;
		// If package is null it may be that the source method is of primitive type array 
		// Example -> Object double[].clone()
		// This also causes infinite recursion in double[].clone() in the test cases of following Test class 
		// Defects4J Math/6b org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest
		// We are not interested in such method so return null
		if(_package == null) {
			//log.info("[GET SOURCE END WITH NULL] - NO PACKAGAE");
			return null;
		}
		packageName = _package.getName();
//		if(packageName != null) {
			if(packageName.startsWith("java.") || packageName.startsWith("javax.") || packageName.startsWith("sun.")) {
				//log.info("[GET SOURCE END WITH NULL] - JAVA CLASS");
				return null;
			}
//		}
		//log.info("[SOURCE CHECK TEST CLASS]");
		// JUnit 3
		// If source method is from TestCase class return null. The trace for methods in TestCase classes need not be collected
//		if(signature.getDeclaringType().getSuperclass() == junit.framework.TestCase.class) {
		if(junit.framework.TestCase.class.isAssignableFrom(_class)) {
			//log.info("[GET SOURCE END WITH NULL] - TEST CLASS");
			return null;
		}
		
		//log.info("[SOURCE CHECK CONSTRUCTOR]");
		if(signature instanceof ConstructorSignature) {
			ConstructorSignature cons = (ConstructorSignature) signature;
			//log.info("[GET SOURCE CONSTRUCTOR END]");
			return cons.getConstructor().toString();
		}
		
		//log.info("[SOURCE CHECK METHOD SIGNATURE]");
		if(signature instanceof org.aspectj.lang.reflect.MethodSignature) {
			Method method = ((org.aspectj.lang.reflect.MethodSignature) signature).getMethod();
			// JUnit 4
			// If source method is from TestCase class return null. The trace for methods in TestCase classes need not be collected
			//log.info("[SOURCE CHECK ANNOTATIONS]");
			Annotation[] annotations = method.getDeclaredAnnotations();
			for(Annotation annotation:annotations) {
				if(annotation instanceof org.junit.Before || annotation instanceof org.junit.Test || annotation instanceof org.junit.After) {
					//log.info("[GET SOURCE END WITH NULL] - ANNOTATIONS");
					return null;
				}
			}
			//log.info("[SOURCE GET METHOD]");
			method = getMethod(signature, signature.getDeclaringType());
			if(method != null) {
				//log.info("[GET SOURCE END] - METHOD");
				return method.toString();
			}
		}
		//log.info("[GET SOURCE END WITH NULL] - FINAL");
		return null;
	}
		
	@Deprecated
	public static String getTargetMethod(Signature signature, Object targetObject) {
		try {
			//log.info("[GET TARGET START]");
			Class clazz = targetObject != null? targetObject.getClass() : null;
			Method method = getMethod(signature, clazz);
			if(method != null) {
				//log.info("[GET TARGET END] - METHOD NULL");
				return method.toString();
			}
			else {
				//log.info("[TARGET CHECK CONSTRUCTOR]");
				if(signature instanceof ConstructorSignature) {
					ConstructorSignature cons = (ConstructorSignature) signature;
					//log.info("[GET TARGET END] - CONSTRUCTOR");
					return cons.getConstructor().toString();
				}
			}
		}catch(ClassCastException exception) {
			String message = "executed targetMethod not resolved - " + signature.toLongString();
			handleException(exception, message);
			return null;
		}
		//log.info("[GET TARGET END WITH NULL] - FINAL");
		return null;
	}
	
	public static Method getMethod(Class _class, Method method) {
		//log.info("[GET METHOD START]");
		try {
			method = _class.getMethod(method.getName(), method.getParameterTypes());
			return method;
        } catch (final Exception exception) {
			String message = null;
			message = "[UNRESOLVED] Provided Class = " + _class.getCanonicalName() + " - " + method.toGenericString() + " - " + method.getDeclaringClass().getCanonicalName();
			handleException(exception, message);
        	return null;
        }
	}
	
	@Deprecated
	public static Method getMethod(Signature signature, Class clazz) {
		//log.info("[GET METHOD START]");
		if(signature instanceof org.aspectj.lang.reflect.MethodSignature) {
			org.aspectj.lang.reflect.MethodSignature methodSignature = (org.aspectj.lang.reflect.MethodSignature) signature;
			//log.info("[GET METHOD METHOD] - refelection");
			Method method = methodSignature.getMethod();
			//log.info("[METHOD CHECK INTERFACE]");
			if (method.getDeclaringClass().isInterface()) {
		        try {
//		        	method= clazz.getDeclaredMethod(signature.getName(),method.getParameterTypes());
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
					handleException(exception, message);
		        	return null;
		        }
		    }
			//log.info("[GET METHOD END] - METHOD");
			return method;
		}
		//log.info("[GET METHOD END WITH NULL] - FINAL");
		return null;
	}
	
	public static void handleException(Throwable exception, String where) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		exception.printStackTrace(ps);
		String stackTrace = new String(baos.toByteArray());
		String message = "[" + where + "]\n";
		message += "[" + exception.getLocalizedMessage() + "]\n";
		message += stackTrace;
		log(message, EXCEPTION_FILE_NAME, true);
	}

}
