package io.intino.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.intino.builder.BuildConstants.SEPARATOR;

public class PostCompileMethodActionMessage extends PostCompileActionMessage {
	private final boolean isStatic;
	private final List<String> parameters;
	private final String returnType;
	private final List<String> exceptions;

	public PostCompileMethodActionMessage(String module, File file, String name, boolean isStatic, List<String> parameters, String returnType) {
		super(module, file, ObjectType.METHOD, name);
		this.isStatic = isStatic;
		this.parameters = parameters;
		this.returnType = returnType;
		this.exceptions = new ArrayList<>();
	}


	public PostCompileMethodActionMessage(String module, File file, String name, boolean isStatic, List<String> parameters, String returnType, List<String> exceptions) {
		super(module, file, ObjectType.METHOD, name);
		this.isStatic = isStatic;
		this.parameters = parameters;
		this.returnType = returnType;
		this.exceptions = exceptions;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public List<String> parameters() {
		return parameters;
	}

	public String returnType() {
		return returnType;
	}

	public List<String> exceptions() {
		return exceptions;
	}

	@Override
	public String toString() {
		return super.toString() + SEPARATOR + isStatic + SEPARATOR + String.join(";", parameters()) + SEPARATOR + returnType + SEPARATOR + String.join(";", exceptions());
	}
}
