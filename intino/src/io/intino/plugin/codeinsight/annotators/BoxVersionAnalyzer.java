package io.intino.plugin.codeinsight.annotators;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import io.intino.Configuration;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.magritte.lang.semantics.errorcollector.SemanticNotification.Level;
import io.intino.plugin.annotator.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.builders.InterfaceBuilderManager;

import java.util.ArrayList;
import java.util.List;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safe;

public class BoxVersionAnalyzer extends TaraAnalyzer {
	private final Module module;
	private final Node interfaceNode;

	BoxVersionAnalyzer(Module module, Node node) {
		this.module = module;
		this.interfaceNode = node;
	}

	@Override
	public void analyze() {
		if (module == null || interfaceNode.parameters().isEmpty()) return;
		Parameter parameter = interfaceNode.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(null);
		if (parameter == null) {
			if (interfaceNode.parameters().size() > 1)
				parameter = interfaceNode.parameters().get(1);
			else return;
		}
		final String version = parameter.values().get(0).toString();
		if (version.compareTo(InterfaceBuilderManager.minimunVersion) < 0)
			results.put(((TaraNode) interfaceNode).getSignature(), new AnnotateAndFix(Level.ERROR, message("error.interface.version.not.compatible", version)));
		else if (!version.equals("LATEST") && !InterfaceBuilderManager.exists(version))
			results.put(((TaraNode) interfaceNode).getSignature(),
					new AnnotateAndFix(Level.ERROR, message("error.interface.version.not.found", version)));
		else if (boxVersionOfOtherModules().stream().anyMatch(s -> !s.equalsIgnoreCase(version)))
			results.put(((TaraNode) interfaceNode).getSignature(),
					new AnnotateAndFix(Level.WARNING, message("warn.interface.version.differ.in.project", version)));
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