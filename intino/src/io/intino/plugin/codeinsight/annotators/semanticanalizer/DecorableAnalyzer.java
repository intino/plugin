package io.intino.plugin.codeinsight.annotators.semanticanalizer;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import io.intino.Configuration.Artifact.Dsl.Builder;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.fix.SyncDecorableClassIntention;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.messages.MessageProvider;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Tag;

import java.util.Arrays;
import java.util.Objects;

import static com.intellij.psi.search.GlobalSearchScope.moduleScope;
import static io.intino.plugin.codeinsight.languageinjection.helpers.Format.firstUpperCase;
import static io.intino.plugin.codeinsight.languageinjection.helpers.Format.javaValidName;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class DecorableAnalyzer extends TaraAnalyzer {
	private final TaraMogram mogram;
	private final String modelPackage;

	public DecorableAnalyzer(TaraMogram mogram) {
		this.mogram = mogram;
		modelPackage = IntinoUtil.dslGenerationPackage(mogram).toLowerCase();
	}

	@Override
	public void analyze() {
		if (mogram.isAnonymous()) return;
		if (!mogram.is(Tag.Decorable)) return;
		Module module = ModuleProvider.moduleOf(mogram);
		if (module == null) return;
		Builder builder = safe(() -> IntinoUtil.dsl(mogram).builder());
		if (builder != null && !builder.groupId().contains("magritte")) return;
		PsiClass aClass = JavaPsiFacade.getInstance(mogram.getProject()).findClass(modelPackage + "." + format(mogram.name()), moduleScope(module));
		if (aClass == null) {
			results.put(mogram.getSignature(), new TaraAnnotator.AnnotateAndFix(ERROR, MessageProvider.message("error.link.to.decorable"), collectFixes()));
			return;
		}
		checkTree(aClass, mogram);
	}

	private void checkTree(PsiClass aClass, Mogram mogram) {
		if (!results.isEmpty()) return;
		for (Mogram component : mogram.components()) {
			if (component.isReference()) continue;
			String name = format(component.name());
			PsiClass inner = Arrays.stream(aClass.getInnerClasses()).filter(cl -> Objects.equals(cl.getName(), name)).findFirst().orElse(null);
			if (inner == null) {
				results.put(this.mogram.getSignature(), new TaraAnnotator.AnnotateAndFix(ERROR, MessageProvider.message("error.inner.classes.decorable"), collectFixes()));
				return;
			}
			checkTree(inner, component);
		}
	}

	private String format(String name) {
		return firstUpperCase().format(javaValidName().format(name)).toString();
	}

	private IntentionAction[] collectFixes() {
		if (mogram == null) return new IntentionAction[0];
		return new IntentionAction[]{new SyncDecorableClassIntention(mogram, modelPackage)};
	}
}
