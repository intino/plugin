package io.intino.plugin.actions;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import io.intino.Configuration;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.Language;
import io.intino.tara.dsl.ProteoConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.plugin.lang.psi.impl.TaraUtil.graphPackage;

class TestClassCreator {


	static void creteTestClass(Module module, String dsl, String newName) {
		final PsiDirectory psiDirectory = testDirectory(module);
		if (psiDirectory == null) return;
		final Configuration conf = TaraUtil.configurationOf(module);
		final PsiClass aClass = JavaDirectoryService.getInstance().createClass(psiDirectory, newName + "Test", "Tara" + (dsl.equals(ProteoConstants.PROTEO) ? "Ontology" : "") + "Test", false, templateParameters(module, conf, dsl, newName));
		assert aClass != null;
		VfsUtil.markDirtyAndRefresh(true, true, true, psiDirectory.getVirtualFile());
	}

	private static Map<String, String> templateParameters(Module module, Configuration conf, String dsl, String newName) {
		Map<String, String> map = new HashMap<>();
		map.put("NAME", newName);
		final Language language = LanguageManager.getLanguage(module.getProject(), dsl);
		map.put("APPLICATION", dsl);
		map.put("WORKING_PACKAGE", graphPackage(module));
		if (language != null) map.put("PLATFORM", language.metaLanguage());
		return map;
	}

	private static PsiDirectory testDirectory(Module module) {
		final List<VirtualFile> sourceRoots = TaraUtil.getSourceRoots(module);
		for (VirtualFile sourceRoot : sourceRoots)
			if (sourceRoot.isDirectory() && "test".equals(sourceRoot.getName()))
				return PsiManager.getInstance(module.getProject()).findDirectory(sourceRoot);
		return null;
	}
}
