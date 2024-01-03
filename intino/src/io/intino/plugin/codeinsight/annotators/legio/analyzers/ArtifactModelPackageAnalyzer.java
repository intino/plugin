package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiPackage;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level;

import static io.intino.plugin.MessageProvider.message;

public class ArtifactModelPackageAnalyzer extends TaraAnalyzer {
	private final Mogram node;
	private final ArtifactLegioConfiguration configuration;

	public ArtifactModelPackageAnalyzer(Mogram node, ArtifactLegioConfiguration configuration) {
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
			results.put(((TaraMogram) node).getSignature(), new TaraAnnotator.AnnotateAndFix(Level.WARNING, message("warning.model.in.old.package")));
	}
}