package myTracer;

public class MethodSignature {
	
	private Integer id;
	private String method;
	
	public MethodSignature(Integer id, String methodName) {
		this.id= id;
		this.method = methodName.replace(',', ';');
	}
	
	public MethodSignature(String[] triplet) {
		this.id= Integer.valueOf(triplet[0]);
		this.method = triplet[1];
	}
	
	
	public String toString() {
		return this.id + ":" + this.method;
 	}
	
	public Integer getID() {
		return this.id;
	}
		
	public String getMethod() {
		return this.method;
	}
	
	public static MethodSignature getMethodSignature(String method) {
		method = method.replace(',', ';');
		MethodSignature ms = MethodIDManager.getMethodSignature(method);
		if (ms == null) {
			ms = MethodIDManager.addMethodSignature(method);
		}
		return ms;
	}

}
