package io.intino.plugin.project.configuration.model;

import com.intellij.openapi.application.ApplicationManager;
import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;

import java.util.Collections;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public class LegioBox implements Configuration.Artifact.Box {
	private final LegioArtifact artifact;
	private final TaraMogram node;
	private String version;

	public LegioBox(LegioArtifact artifact, Mogram node) {
		this.artifact = artifact;
		this.node = (TaraMogram) node;
	}

	@Override
	public String language() {
		return parameterValue(node, "language", 0);
	}

	@Override
	public String version() {
		return version == null ? version = parameterValue(node, "version", 1) : version;
	}

	@Override
	public String effectiveVersion() {
		//TODO
		return "";
	}

	public void version(String newVersion) {
		writeCommandAction(node.getProject(), node.getContainingFile()).run(() -> {
			Parameter version = node.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(node.parameters().get(1));
			if (version != null) version.substituteValues(Collections.singletonList(newVersion));
		});
		ApplicationManager.getApplication().invokeAndWait(() -> IntinoUtil.commitDocument(node.getContainingFile()));
	}


	@Override
	public void effectiveVersion(String s) {

	}

	@Override
	public String targetPackage() {
		String targetPackage = parameterValue(node, "targetPackage");
		return targetPackage == null ? artifact.code().generationPackage() + "." + artifact.code().boxPackage() : targetPackage;
	}

	@Override
	public Configuration root() {
		return artifact.root();
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return artifact;
	}
}
