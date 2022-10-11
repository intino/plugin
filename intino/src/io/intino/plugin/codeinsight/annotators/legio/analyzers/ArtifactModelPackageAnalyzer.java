package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiPackage;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.semantics.errorcollector.SemanticNotification.Level;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.project.configuration.LegioConfiguration;

import static io.intino.plugin.MessageProvider.message;

public class ArtifactModelPackageAnalyzer extends TaraAnalyzer {
	private final Node node;
	private final LegioConfiguration configuration;

	public ArtifactModelPackageAnalyzer(Node node, LegioConfiguration configuration) {
		this.node = node;
		this.configuration = configuration;
	}

	@Override
	public void analyze() {
		if (configuration == null || !configuration.inited()) return;
		String generationPackage = configuration.artifact().code().generationPackage();
		String modelPackage = configuration.artifact().code().modelPackage();
		JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(configuration.module().getProject());
		PsiPackage oldPackage = javaPsiFacade.findPackage(generationPackage + ".graph");
		if (oldPackage != null && !modelPackage.equals("graph"))
			results.put(((TaraNode) node).getSignature(), new TaraAnnotator.AnnotateAndFix(Level.WARNING, message("warning.model.in.old.package")));
	}
}