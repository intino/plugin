package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import io.intino.Configuration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.builders.BoxBuilderManager;
import io.intino.plugin.project.configuration.Version;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level;

import java.util.ArrayList;
import java.util.List;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safe;

public class BoxVersionAnalyzer extends TaraAnalyzer {
	private final Module module;
	private final Mogram boxNode;

	public BoxVersionAnalyzer(Module module, Mogram node) {
		this.module = module;
		this.boxNode = node;
	}

	@Override
	public void analyze() {
		if (module == null || boxNode.parameters().isEmpty()) return;
		Parameter parameter = boxNode.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(null);
		if (parameter == null) {
			if (boxNode.parameters().size() > 1)
				parameter = boxNode.parameters().get(1);
			else return;
		}
		final String version = parameter.values().get(0).toString();
		if (isNotSuitableVersion(version))
			results.put(((TaraMogram) boxNode).getSignature(), new AnnotateAndFix(Level.ERROR, message("error.box.version.not.compatible", version)));
		else if (!version.equals("LATEST") && !BoxBuilderManager.exists(version))
			results.put(((TaraMogram) boxNode).getSignature(),
					new AnnotateAndFix(Level.ERROR, message("error.box.version.not.found", version)));
		else if (boxVersionOfOtherModules().stream().anyMatch(s -> !s.equalsIgnoreCase(version)))
			results.put(((TaraMogram) boxNode).getSignature(),
					new AnnotateAndFix(Level.WARNING, message("warn.box.version.differ.in.project", version)));
	}

	private boolean isNotSuitableVersion(String version) {
		try {
			return new Version(version).compareTo(new Version(BoxBuilderManager.MinimumVersion)) < 0;
		} catch (IntinoException e) {
			return false;
		}
	}

	private List<String> boxVersionOfOtherModules() {
		List<String> versions = new ArrayList<>();
		if (module == null) return versions;
		ModuleManager instance = ModuleManager.getInstance(module.getProject());
		if (instance == null) return versions;
		for (Module m : instance.getModules()) {
			if (m.equals(this.module)) continue;
			final Configuration configuration = IntinoUtil.configurationOf(m);
			Configuration.Artifact.Box box = safe(() -> configuration.artifact().box());
			if (box != null) {
				String version = box.version();
				if (version != null && !version.isEmpty()) versions.add(version);
			}
		}
		return versions;
	}
}