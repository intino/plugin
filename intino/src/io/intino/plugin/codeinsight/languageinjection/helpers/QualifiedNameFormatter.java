package io.intino.plugin.codeinsight.languageinjection.helpers;

import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.Checker;
import io.intino.tara.Language;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.semantics.errorcollector.SemanticFatalException;

import static io.intino.plugin.codeinsight.languageinjection.helpers.Format.qualifiedName;

public class QualifiedNameFormatter {

	public static final String DOT = ".";

	private QualifiedNameFormatter() {
	}

	public static String qn(Node node, String workingPackage, boolean m0) {
		return workingPackage.toLowerCase() + DOT + qualifiedName().format(node.qualifiedName());
	}

	public static String cleanQn(String qualifiedName) {
		return qualifiedName.replace(Node.ANONYMOUS, "").replace("[", "").replace("]", "");
	}

	public static String qnOf(Valued valued) {
		final Node container = TaraPsiUtil.getContainerNodeOf(valued);
		if (container == null || valued == null) return "";
		if (valued.name() == null || valued.name().isEmpty()) resolve(valued);
		return container.qualifiedName() + "." + valued.name();
	}

	private static void resolve(Valued valued) {
		final Node node = TaraPsiUtil.getContainerNodeOf(valued);
		if (node != null) try {
			final Language language = TaraUtil.getLanguage(valued);
			if (language != null) new Checker(language).check(node.resolve());
		} catch (SemanticFatalException ignored) {
		}
	}


}
