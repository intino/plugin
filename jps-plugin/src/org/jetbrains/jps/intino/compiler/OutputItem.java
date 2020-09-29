package org.jetbrains.jps.intino.compiler;

public class OutputItem {
	private final String outputPath;
	private final String sourcePath;

	public OutputItem(String outputPath, String sourceFileName) {
		this.outputPath = outputPath;
		sourcePath = sourceFileName;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	@Override
	public String toString() {
		return "OutputItem{" + "outputPath='" + outputPath + '\'' + ", sourcePath='" + sourcePath + '\'' + '}';
	}
}
