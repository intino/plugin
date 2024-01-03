package io.intino.plugin.codeinsight.languageinjection.helpers;

import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.Checker;
import io.intino.tara.Language;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.semantics.errorcollector.SemanticFatalException;

import static io.intino.plugin.codeinsight.languageinjection.helpers.Format.qualifiedName;

public class QualifiedNameFormatter {

	public static final String DOT = ".";

	private QualifiedNameFormatter() {
	}

	public static String qn(Mogram mogram, String workingPackage) {
		return (workingPackage == null ? "" : workingPackage.toLowerCase()) + (mogram == null ? "" : DOT + qualifiedName().format(mogram.qualifiedName()));
	}

	public static String cleanQn(String qualifiedName) {
		return qualifiedName.replace(Mogram.ANONYMOUS, "").replace("[", "").replace("]", "");
	}

	public static String qnOf(Valued valued) {
		final Mogram container = TaraPsiUtil.getContainerNodeOf(valued);
		if (container == null || valued == null) return "";
		if (valued.name() == null || valued.name().isEmpty()) resolve(valued);
		return container.qualifiedName() + "." + valued.name();
	}

	private static void resolve(Valued valued) {
		final Mogram mogram = TaraPsiUtil.getContainerNodeOf(valued);
		if (mogram != null) try {
			final Language language = IntinoUtil.getLanguage(valued);
			if (language != null) new Checker(language).check(mogram.resolve());
		} catch (SemanticFatalException ignored) {
		}
	}


}
