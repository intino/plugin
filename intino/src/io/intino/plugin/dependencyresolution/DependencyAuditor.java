package io.intino.plugin.dependencyresolution;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiFile;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.module.ModuleProvider;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class DependencyAuditor {
	private static final com.intellij.openapi.diagnostic.Logger Logger = com.intellij.openapi.diagnostic.Logger.getInstance(DependencyAuditor.class.getName());
	private static final DependencyItem defaultItem = new DependencyItem("");
	private final Module module;
	private final PsiFile legioFile;
	private Map<Integer, DependencyItem> map = new HashMap<>();

	public DependencyAuditor(Module module, PsiFile legioFile) {
		this.module = module;
		this.legioFile = legioFile;
		reload();
	}

	public boolean isModified(TaraNode node) {
		if (node == null) return true;
		int hashcode = node.getSignature().hashcode();
		return !map.containsKey(hashcode);
	}

	public boolean isResolved(TaraNode node) {
		return map.getOrDefault(node.getSignature().hashcode(), defaultItem).resolved;
	}

	public void isResolved(TaraNode node, boolean resolved) {
		int code = node.getSignature().hashcode();
		if (!map.containsKey(code)) map.put(code, new DependencyItem(""));
		map.get(code).resolved = resolved;
	}

	public boolean isToModule(TaraNode node) {
		return map.getOrDefault(node.getSignature().hashcode(), defaultItem).toModule;
	}

	public void isToModule(TaraNode node, boolean toModule) {
		int code = node.getSignature().hashcode();
		if (!map.containsKey(code)) map.put(code, new DependencyItem(""));
		map.get(code).toModule = toModule;
	}

	public String effectiveVersion(TaraNode node) {
		return map.getOrDefault(node.getSignature().hashcode(), defaultItem).effectiveVersion;
	}

	public void effectiveVersion(TaraNode node, String version) {
		int code = node.getSignature().hashcode();
		if (!map.containsKey(code)) map.put(code, new DependencyItem(""));
		map.get(code).effectiveVersion = version;
	}

	public void reload() {
		map = loadDependencyMap();
	}

	private Map<Integer, DependencyItem> loadDependencyMap() {
		Gson gson = new Gson();
		try {
			File file = auditionFile();
			if (auditionFile().exists()) {
				Map<Integer, DependencyItem> map = gson.fromJson(new FileReader(file), new TypeToken<Map<Integer, DependencyItem>>() {
				}.getType());
				return map == null ? new HashMap<>() : map;
			}
			return new HashMap<>();
		} catch (Exception e) {
			return new HashMap<>();
		}
	}

	public void save() {
		Gson gson = new Gson();
		try {
			Files.write(auditionFile().toPath(), gson.toJson(map).getBytes());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void invalidate(TaraNode node) {
		map.remove(node.getSignature().hashcode());
	}

	public void invalidate(String identifier) {
		map.remove(identifier.hashCode());
	}

	private File auditionFile() {
		return new File(IntinoDirectory.auditDirectory(legioFile.getProject()), ModuleProvider.moduleOf(legioFile).getName());
	}

	private static class DependencyItem {

		public DependencyItem(String identifier) {
			this.identifier = identifier;
		}

		public String identifier;
		boolean resolved = false;
		boolean toModule = false;
		String effectiveVersion = "";
	}
}
