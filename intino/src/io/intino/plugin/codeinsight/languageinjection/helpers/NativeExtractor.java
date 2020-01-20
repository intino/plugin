package io.intino.plugin.codeinsight.languageinjection.helpers;

public class NativeExtractor {

	private final String interfaceName;
	private final String variableName;
	private final String returnValue;
	private final String methodName;
	private final String parameters;

	public NativeExtractor(String interfaceName, String variableName, String methodSignature) {
		this.interfaceName = interfaceName;
		this.variableName = variableName;
		this.returnValue = getReturn(clean(methodSignature));
		this.methodName = getMethod(clean(methodSignature));
		this.parameters = getParameters(clean(methodSignature));
	}

	private String clean(String methodSignature) {
		return methodSignature.trim().replaceAll("\\s+", " ").replace(" (", "(");
	}

	private static String getReturn(String methodSignature) {
		final String returnValue = methodSignature.split(" (\\S)*\\(")[0];
		return returnValue.startsWith("public ") ? returnValue.replaceFirst("public ", "") : returnValue;
	}

	private String getMethod(String methodSignature) {
		String[] substring = methodSignature.substring(0, methodSignature.indexOf('(')).split(" ");
		return substring[substring.length - 1];
	}

	private static String getParameters(String methodSignature) {
		return methodSignature.substring(methodSignature.indexOf('(') + 1, methodSignature.length() - 1);
	}

	public String interfaceName() {
		return interfaceName;
	}

	public String variableName() {
		return variableName;
	}

	public String returnValue() {
		return returnValue;
	}

	public String methodName() {
		return methodName;
	}

	public String parameters() {
		return parameters;
	}

}
