package io.intino.plugin.lang;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import io.intino.Configuration;
import io.intino.plugin.dependencyresolution.Repositories;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.builders.BoxBuilderManager;
import io.intino.tara.Language;
import io.intino.tara.dsls.Meta;
import io.intino.tara.dsls.Proteo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.builders.BoxBuilderManager.BOX_LANGUAGE;
import static io.intino.tara.dsls.MetaIdentifiers.META;
import static io.intino.tara.dsls.MetaIdentifiers.PROTEO;

public class LanguageManager {
	public static final String DSL = "dsl";
	public static final String TARA_REPOSITORY = ".m2";
	public static final String TARA_LOCAL = ".intino/tara";
	public static final String JSON = ".json";
	@SuppressWarnings("WeakerAccess")
	public static final String DSL_GROUP_ID = "tara.dsl";
	private static final Map<Project, Map<String, Language>> languages = new HashMap<>();
	private static final Map<String, Language> core = new HashMap<>();
	private static final Map<String, Language> boxLanguages = new HashMap<>();
	private static final String INFO_JSON = "info" + JSON;
	private static final String LATEST = "LATEST";

	static {
		core.put(PROTEO, new Proteo());
		core.put(META, new Meta());
	}

	@Nullable
	public static Language getLanguage(@NotNull PsiFile file) {
		if (file.getFileType() instanceof TaraFileType) {
			final Configuration configuration = IntinoUtil.configurationOf(file);
			final String dslName = ((TaraModel) file).dsl();
			if (dslName == null) return null;
			String version;
			if (core.containsKey(dslName)) {
				version = safe(() -> configuration.artifact().model().language().version());
				if (version == null) version = LATEST;
			} else if (BOX_LANGUAGE.equals(dslName)) {
				version = safe(() -> configuration.artifact().box().version());
				if (version == null) version = BoxBuilderManager.MinimumVersion;
			} else {
				version = safe(() -> configuration.artifact().model().language().version());
				if (version == null) version = LATEST;
			}
			if (version == null) return null;
			return getLanguage(file.getProject(), dslName, version);
		} else return null;
	}

	@Nullable
	public static Language getLanguage(Project project, String dsl) {
		return getLanguage(project, dsl, LATEST);
	}

	@Nullable
	public static Language getLanguage(Project project, String dsl, String version) {
		if (dsl == null) return null;
		if (core.containsKey(dsl)) return core.get(dsl);
		if (boxLanguages.containsKey(dsl + "#" + version)) return boxLanguages.get(dsl + "#" + version);
		if (dsl.isEmpty()) return core.get(META);
		if (project == null) return null;
		return loadLanguage(project, dsl, version);
	}

	private static Language loadLanguage(Project project, String dsl, String version) {
		if (isLoaded(project, dsl)) return languages.get(project).get(dsl);
		else if (LATEST.equals(version)) reloadLanguage(project, dsl);
		else reloadLanguage(project, dsl, version);
		return languages.get(project) == null ? null : languages.get(project).get(dsl);
	}

	public static void register(Language language) {
		core.put(language.languageName(), language);
	}

	public static void registerBoxLanguage(Project project, Language language, String version) {
		boxLanguages.putIfAbsent(language.languageName() + "#" + version, language);
	}

	@SuppressWarnings("WeakerAccess")
	public static void reloadLanguage(Project project, String dsl) {
		final File languageDirectory = getLanguageDirectory(dsl);
		if (!languageDirectory.exists()) return;
		Language language = LanguageLoader.loadLatest(dsl, languageDirectory.getPath());
		if (language == null) return;
		addLanguage(project, dsl, language);
		PsiManager.getInstance(project).dropResolveCaches();
		Notifications.Bus.notify(new Notification("Intino", "", "Language " + dsl + " reloaded", NotificationType.INFORMATION), project);
	}

	@SuppressWarnings("WeakerAccess")
	public static void reloadLanguage(Project project, String dsl, String version) {
		final File languageDirectory = getLanguageDirectory(dsl);
		if (!languageDirectory.exists()) return;
		Language language = LanguageLoader.load(dsl, version, languageDirectory.getPath());
		if (language == null) return;
		addLanguage(project, dsl, language);
		PsiManager.getInstance(project).dropResolveCaches();
	}

	@SuppressWarnings("WeakerAccess")
	public static boolean silentReload(Project project, String dsl, String version) {
		final File languageDirectory = getLanguageDirectory(dsl);
		if (!languageDirectory.exists()) return false;
		Language language = LanguageLoader.load(dsl, version, languageDirectory.getPath());
		if (language == null) return false;
		addLanguage(project, dsl, language);
		PsiManager.getInstance(project).dropResolveCaches();
		return true;
	}

	@SuppressWarnings("unused")
	public static File getLanguageFile(String dsl, String version) {
		return LanguageLoader.composeLanguagePath(getLanguageDirectory(dsl).getPath(), dsl, version);
	}

	public static File getLanguageDirectory(String dsl) {
		return new File(getLanguagesRepository().getPath(), DSL_GROUP_ID.replace(".", File.separator) + File.separator + dsl.toLowerCase());
	}

	public static Map<String, Object> getImportedLanguageInfo(String dsl) {
		try {
			final File languageDirectory = getLanguageDirectory(dsl);
			Gson gson = new Gson();
			return gson.fromJson(new FileReader(new File(languageDirectory, INFO_JSON)), new TypeToken<Map<String, String>>() {
			}.getType());
		} catch (FileNotFoundException ignored) {
		}
		return Collections.emptyMap();
	}

	public static File getTaraLocalDirectory(Project project) {
		final VirtualFile baseDir = VfsUtil.findFileByIoFile(new File(project.getBasePath()), true);
		final VirtualFile tara = baseDir.findChild(TARA_LOCAL);
		return tara == null ? createTaraDirectory(baseDir) : new File(tara.getPath());
	}

	public static VirtualFile getLanguagesRepository() {
		final File dslDirectory = Repositories.LOCAL;
		dslDirectory.mkdirs();
		return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dslDirectory);
	}

	private static File createTaraDirectory(VirtualFile baseDir) {
		final File file = new File(baseDir.getPath(), TARA_LOCAL);
		file.mkdirs();
		return file;
	}

	private static boolean isLoaded(Project project, String language) {
		return languages.get(project) != null && languages.get(project).get(language) != null;
	}

	public static void remove(Project project) {
		languages.remove(project);
	}

	private static void addLanguage(Project project, String dsl, Language language) {
		if (!languages.containsKey(project)) languages.put(project, new HashMap<>());
		languages.get(project).put(dsl, language);
	}
}
