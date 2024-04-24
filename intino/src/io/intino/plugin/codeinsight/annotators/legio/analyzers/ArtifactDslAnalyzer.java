package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiElement;
import io.intino.Configuration.Artifact.Dsl;
import io.intino.plugin.IntinoException;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.DslBuilderManager;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import io.intino.plugin.project.configuration.model.LegioDsl;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import org.apache.commons.io.IOUtils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class ArtifactDslAnalyzer extends TaraAnalyzer {
	private final Mogram dslMogram;
	private final ArtifactLegioConfiguration configuration;
	private final Module module;

	public ArtifactDslAnalyzer(Mogram mogram, Module module) {
		this.dslMogram = mogram;
		this.module = module;
		this.configuration = (ArtifactLegioConfiguration) IntinoUtil.configurationOf(module);
	}

	@Override
	public void analyze() {
		if (configuration == null || dslMogram == null) return;
		final Parameter languageNameParameter = dslMogram.parameters().stream().filter(p -> p.name().equals("name")).findFirst().orElse(null);
		if (languageNameParameter == null) return;
		final String dslName = languageNameParameter.values().get(0).toString();
		if (dslName == null) return;
		String version = version();
		Dsl dsl = safe(() -> configuration.artifact().dsl(dslName));
		if (dsl == null) {
			results.put((PsiElement) this.dslMogram, new AnnotateAndFix(ERROR, message("dsl.not.found")));
			return;
		}
		if ("LATEST".equals(version)) version = dsl.effectiveVersion();
		if (version == null || version.isEmpty()) return;
		if (((LegioDsl) dsl).attributes() == null) {
			results.put((PsiElement) this.dslMogram, new AnnotateAndFix(ERROR, message("dsl.not.found")));
			return;
		}
		if (!checkDsl(dslName, version)) return;
		checkRuntime(dsl);
		checkBuilder(dsl, safe(dsl::builder));
	}

	private boolean checkDsl(String dslName, String version) {
		boolean dslExists = LanguageManager.getLanguage(module.getProject(), dslName, version) == null && !LanguageManager.silentReload(module.getProject(), dslName, version);
		if (dslExists) results.put((PsiElement) this.dslMogram, new AnnotateAndFix(ERROR, message("dsl.not.found")));
		return dslExists;
	}

	private void checkRuntime(Dsl dsl) {
		if (!existRuntime(dsl.runtime().groupId() + ":" + dsl.runtime().artifactId() + ":" + dsl.runtime().version()))
			results.put(((TaraMogram) this.dslMogram).getSignature(), new AnnotateAndFix(ERROR, message("runtime.cannot.downloaded")));
	}

	private void checkBuilder(Dsl dsl, Dsl.Builder builder) {
		try {
			if (builder == null) throw new IntinoException("builder not found");
			String builderVersion = builder.version();
			if (builderVersion == null) return;
			new Version(builderVersion);
			String version = IOUtils.readLines(Objects.requireNonNull(this.getClass().getResourceAsStream("/minimum_model_sdk.info")), Charset.defaultCharset()).get(0);
			if (builderVersion.compareTo(version) < 0)
				results.put(((TaraMogram) this.dslMogram).getSignature(), new AnnotateAndFix(ERROR, message("builder.minimum.version", version)));
			if (!new DslBuilderManager(module, configuration.repositories(), dsl).exists(builderVersion))
				results.put(((TaraMogram) this.dslMogram).getSignature(), new AnnotateAndFix(ERROR, message("sdk.version.not.found")));
		} catch (IntinoException e) {
			results.put(((TaraMogram) this.dslMogram).getSignature(), new AnnotateAndFix(ERROR, message("sdk.version.not.found")));
		}
	}

	private boolean existRuntime(String framework) {
		return Arrays.stream(ModuleRootManager.getInstance(module).getOrderEntries())
				.filter(e -> e instanceof LibraryOrderEntry)
				.map(e -> ((LibraryOrderEntry) e).getLibrary())
				.anyMatch(library -> library != null && library.getName() != null && library.getName().endsWith(framework));
	}

	private String version() {
		return dslMogram.parameters().stream()
				.filter(parameter -> parameter.name().equals("version"))
				.findFirst()
				.map(parameter -> parameter.values().get(0).toString())
				.orElse(null);
	}
}