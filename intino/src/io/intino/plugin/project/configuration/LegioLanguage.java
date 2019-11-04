package io.intino.plugin.project.configuration;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import io.intino.legio.graph.Artifact;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.dsl.Meta;
import io.intino.tara.dsl.Proteo;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.lang.psi.TaraModel;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.tara.compiler.shared.TaraBuildConstants.WORKING_PACKAGE;

public class LegioLanguage implements Configuration.Model.ModelLanguage {
	private final Module module;
	private final VirtualFile legioFile;
	private final Artifact.Level.Model model;

	LegioLanguage(Module module, VirtualFile legioFile, Artifact.Level.Model model) {
		this.module = module;
		this.legioFile = legioFile;
		this.model = model;
	}

	@Override
	public String name() {
		return model != null ? model.language() : null;
	}

	@Override
	public String version() {
		return model != null ? model.version() : null;
	}

	@Override
	public String effectiveVersion() {
		return model != null ? model.effectiveVersion() : null;
	}

	@Override
	public void version(String version) {
		final Application application = ApplicationManager.getApplication();
		TaraModel psiFile = !application.isReadAccessAllowed() ?
				(TaraModel) application.runReadAction((Computable<PsiFile>) () -> PsiManager.getInstance(module.getProject()).findFile(legioFile)) :
				(TaraModel) PsiManager.getInstance(module.getProject()).findFile(legioFile);
		writeCommandAction(module.getProject(), legioFile()).run(() -> {
			model.version(version);
			final Node model = psiFile.components().get(0).components().stream().filter(f -> f.type().equals("Level.Model")).findFirst().orElse(null);
			if (model == null) return;
			final Parameter versionParameter = model.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(null);
			versionParameter.substituteValues(Collections.singletonList(version));
		});
	}


	public PsiFile legioFile() {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) return PsiManager.getInstance(module.getProject()).findFile(legioFile);
		return application.runReadAction((Computable<PsiFile>) () -> PsiManager.getInstance(module.getProject()).findFile(legioFile));
	}

	@Override
	public String generationPackage() {
		Attributes attributes = parameters();
		return attributes == null ? null : attributes.getValue(WORKING_PACKAGE.replace(".", "-"));
	}

	public Attributes parameters() {
		if (model == null) return null;
		if (isCoreLanguage()) return new Attributes();
		final File languageFile = LanguageManager.getLanguageFile(model.language(), model.effectiveVersion().isEmpty() ? model.version() : model.effectiveVersion());
		if (!languageFile.exists()) return null;
		try {
			Manifest manifest = new JarFile(languageFile).getManifest();
			return manifest == null ? null : manifest.getAttributes("tara");
		} catch (IOException e) {
			return null;
		}
	}

	private boolean isCoreLanguage() {
		return Proteo.class.getSimpleName().equalsIgnoreCase(name()) || Meta.class.getSimpleName().equalsIgnoreCase(name());
	}

}
