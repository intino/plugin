package io.intino.plugin.findusage;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TaraNodeFindUsagesHandler extends FindUsagesHandler {
	private final TaraMogram node;

	public TaraNodeFindUsagesHandler(@NotNull Mogram node) {
		super((PsiElement) node);
		this.node = (TaraMogram) node;
	}

	@NotNull
	@Override
	public PsiElement[] getPrimaryElements() {
		return new PsiElement[]{node.getSignature().getIdentifier()};
	}

	@NotNull
	@Override
	public PsiElement[] getSecondaryElements() {
		return getInstancesOfElement();
	}

	private PsiElement[] getInstancesOfElement() {
		if (node.type() == null) return PsiElement.EMPTY_ARRAY;
		Project project = node.getProject();
		List<? extends PsiElement> conceptList = new ArrayList();
		Map<Module, List<TaraModel>> childModules = new HashMap<>();
		Module moduleForFile = ModuleProvider.moduleOf(node.getContainingFile().getOriginalFile());
		if (moduleForFile == null) return PsiElement.EMPTY_ARRAY;
		for (Module module : ModuleManager.getInstance(project).getModules()) {
			List<TaraModel> taraFilesOfModule = IntinoUtil.getTaraFilesOfModule(module);
			if (taraFilesOfModule.isEmpty()) continue;
			if ((project.getName() + "." + moduleForFile.getName()).equals(taraFilesOfModule.get(0).dsl()))
				childModules.put(module, taraFilesOfModule);
		}
		for (Map.Entry<Module, List<TaraModel>> moduleEntry : childModules.entrySet())
			conceptList.addAll(collectChildConceptsByType(moduleEntry.getValue()));
		return conceptList.toArray(new PsiElement[0]);
	}

	private Collection collectChildConceptsByType(List<TaraModel> files) {
		List<Mogram> list = new ArrayList();
		for (TaraModel file : files)
			list.addAll(IntinoUtil.getMainNodesOfFile(file).stream().
					filter(cpt -> node.name().equals(cpt.type())).
					collect(Collectors.toList()));
		return list;
	}


	@Override
	protected boolean isSearchForTextOccurrencesAvailable(@NotNull PsiElement psiElement, boolean isSingleFile) {
		return true;
	}
}