package io.intino.plugin.lang;

import com.intellij.lang.Commenter;

public class TaraCommenter implements Commenter {

	public String getLineCommentPrefix() {
		return "//";
	}

	public String getBlockCommentPrefix() {
		return "/*";
	}

	public String getBlockCommentSuffix() {
		return "*/";
	}

	public String getCommentedBlockCommentPrefix() {
		return "/*";
	}

	public String getCommentedBlockCommentSuffix() {
		return "*/";
	}
}
