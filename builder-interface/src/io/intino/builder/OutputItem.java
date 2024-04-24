package io.intino.builder;

public class OutputItem {
	private final String sourceFilePath;
	private final String myOutputPath;

	public OutputItem(String sourceFilePath, String outputFilePath) {
		this.sourceFilePath = sourceFilePath;
		myOutputPath = outputFilePath;
	}

	public String getOutputPath() {
		return myOutputPath;
	}

	public String getSourcePath() {
		return sourceFilePath;
	}
}