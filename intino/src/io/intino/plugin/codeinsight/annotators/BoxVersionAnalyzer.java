package io.intino.plugin.codeinsight.annotators;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import io.intino.plugin.project.builders.InterfaceBuilderLoader;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.semantics.errorcollector.SemanticNotification;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.tara.plugin.lang.psi.TaraNode;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import java.util.ArrayList;
import java.util.List;

import static io.intino.plugin.MessageProvider.message;

public class BoxVersionAnalyzer extends TaraAnalyzer {
	private final Module module;
	private final Node interfaceNode;

	BoxVersionAnalyzer(Module module, Node node) {
		this.module = module;
		this.interfaceNode = node;
	}

	@Override
	public void analyze() {
		if (interfaceNode.parameters().isEmpty()) return;
		Parameter parameter = interfaceNode.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(null);
		if (parameter == null) {
			if (interfaceNode.parameters().size() > 1)
				parameter = interfaceNode.parameters().get(1);
			else return;
		}
		final String version = parameter.values().get(0).toString();
		if (!InterfaceBuilderLoader.exists(version))
			results.put(((TaraNode) interfaceNode).getSignature(),
					new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, message("error.interface.version.not.found", version)));
		else if (boxVersionOfOtherModules().stream().anyMatch(s -> !s.equalsIgnoreCase(version))) {
			results.put(((TaraNode) interfaceNode).getSignature(),
					new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.WARNING, message("warn.interface.version.differ.in.project", version)));
		}
	}

	private List<String> boxVersionOfOtherModules() {
		List<String> versions = new ArrayList<>();
		for (Module m : ModuleManager.getInstance(module.getProject()).getModules()) {
			if (m.equals(this.module)) continue;
			final Configuration configuration = TaraUtil.configurationOf(m);
			if (configuration != null) {
				String version = configuration.boxVersion();
				if (version != null && !version.isEmpty()) versions.add(version);
			}
		}
		return versions;
	}
}
