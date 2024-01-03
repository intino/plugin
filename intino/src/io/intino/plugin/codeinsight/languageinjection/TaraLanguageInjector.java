package io.intino.plugin.codeinsight.languageinjection;

import com.intellij.lang.Language;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.InjectedLanguagePlaces;
import com.intellij.psi.LanguageInjector;
import com.intellij.psi.PsiLanguageInjectionHost;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.plugin.lang.psi.Expression;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.Checker;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.model.Tag;
import io.intino.tara.language.model.Variable;
import io.intino.tara.language.semantics.errorcollector.SemanticFatalException;
import org.jetbrains.annotations.NotNull;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.getContainerByType;
import static io.intino.plugin.project.module.ModuleProvider.moduleOf;
import static io.intino.tara.language.model.Primitive.FUNCTION;

public class TaraLanguageInjector implements LanguageInjector {


	private static String defaultPrefix() {
		return "package org.sample;\n" +
				"public class Loading implements io.intino.magritte.framework.Function {" +
				"\tContainer $;" +
				"public void sample() {";
	}

	private static String suffix() {
		return "\n\t}\n\n" +
				"\tpublic void self(io.intino.magritte.framework.Layer context) {\n" +
				"\t}\n" +
				"\n" +
				"\tpublic Class<? extends io.intino.magritte.framework.Layer> selfClass() {\n" +
				"\t\treturn null;\n" +
				"\t}\n" +
				"}";
	}

	@Override
	public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost host, @NotNull InjectedLanguagePlaces injectionPlacesRegistrar) {
		if (!(host instanceof Expression) || !host.isValidHost()) return;
		final Language language = Language.findLanguageByID("JAVA");
		if (language == null) return;
		resolve(host);
		injectionPlacesRegistrar.addPlace(language,
				getRangeInsideHost((Expression) host),
				createPrefix((Expression) host),
				(isWithSemicolon((Expression) host) ? ";" : "") + suffix());
	}

	private void resolve(PsiLanguageInjectionHost host) {
		final Mogram mogram = TaraPsiUtil.getContainerNodeOf(host);
		if (mogram != null) try {
			final io.intino.tara.Language language = IntinoUtil.getLanguage(host);
			if (language != null) new Checker(language).check(mogram.resolve());
		} catch (SemanticFatalException ignored) {
		}
	}

	private boolean isWithSemicolon(@NotNull Expression host) {
		return !host.isMultiLine() && !host.getValue().trim().endsWith(";") && !host.getValue().trim().endsWith("}");
	}

	@NotNull
	private TextRange getRangeInsideHost(@NotNull Expression host) {
		return (!host.isMultiLine()) ? new TextRange(1, host.getTextLength() - 1) : getMultiLineBounds(host);
	}

	private TextRange getMultiLineBounds(Expression host) {
		final String value = host.getValue();
		final int i = host.getText().indexOf(value);
		return new TextRange(i, i + value.length());
	}

	private String createPrefix(Expression expression) {
		resolve(expression);
		final io.intino.tara.Language language = IntinoUtil.getLanguage(expression.getOriginalElement().getContainingFile());
		final Module module = moduleOf(expression);
		if (language == null || module == null) return "";
		Template template = new ExpressionInjectionTemplate();
		String prefix = template.render(buildFrame(expression, language, module));
		return prefix.isEmpty() ? defaultPrefix() : prefix;
	}

	private Frame buildFrame(Expression expression, io.intino.tara.Language language, Module module) {
		Valued valued = getContainerByType(expression, Valued.class);
		if (valued == null) return null;
		String workingPackage = IntinoUtil.modelPackage(expression).isEmpty() ? module.getName() : IntinoUtil.modelPackage(expression);
		FrameBuilder builder = new FrameBuilder()
				.put(Parameter.class, new NativeParameterAdapter(module, workingPackage, language))
				.put(Variable.class, new NativeVariableAdapter(module, workingPackage, language))
				.append(valued);
		return builder.add(isFunction(valued) ? valued.type().getName() : Tag.Reactive.name()).toFrame();
	}

	private boolean isFunction(Valued valued) {
		return FUNCTION.equals(valued.type());
	}
}
