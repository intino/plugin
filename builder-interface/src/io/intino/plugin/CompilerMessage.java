package io.intino.plugin;

public record CompilerMessage(String category, String message, String url, int lineNum, int columnNum) {
	public static final String REBUILD_NEED = "rebuild_needed";
	public static final String ERROR = "error";
	public static final String WARNING = "warning";
	public static final String INFORMATION = "information";

	public CompilerMessage(String category, String message) {
		this(category, message, null, -1, -1);
	}

	public String getCategoryLabel() {
		return category.equals(REBUILD_NEED) ? ERROR : category;
	}


}
