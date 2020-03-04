package io.intino.plugin.codeinsight.languageinjection.helpers;

import io.intino.magritte.Checker;
import io.intino.magritte.Language;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.semantics.errorcollector.SemanticFatalException;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;

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
			final Language language = IntinoUtil.getLanguage(valued);
			if (language != null) new Checker(language).check(node.resolve());
		} catch (SemanticFatalException ignored) {
		}
	}


}
