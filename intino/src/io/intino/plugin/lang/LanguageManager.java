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
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.Language;
import io.intino.tara.dsl.Meta;
import io.intino.tara.dsl.Proteo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.intino.plugin.project.Safe.safe;
import static io.intino.tara.dsl.ProteoConstants.META;
import static io.intino.tara.dsl.ProteoConstants.PROTEO;

public class LanguageManager {
	public static final String DSL = "dsl";
	public static final String TARA_USER = ".m2";
	public static final String TARA_LOCAL = ".intino/tara";
	public static final String JSON = ".json";
	@SuppressWarnings("WeakerAccess")
	public static final String DSL_GROUP_ID = "tara.dsl";
	private static final Map<Project, Map<String, Language>> languages = new HashMap<>();
	private static final Map<String, Language> core = new HashMap<>();
	private static final Map<String, Map<Project, Language>> auxiliarLanguages = new HashMap<>();
	private static final String INFO_JSON = "info" + JSON;
	private static final String LATEST = "LATEST";

	static {
		core.put(PROTEO, new Proteo());
		core.put(META, new Meta());
	}

	@Nullable
	public static Language getLanguage(@NotNull PsiFile file) {
		if (file.getFileType() instanceof TaraFileType) {
			final Configuration configuration = TaraUtil.configurationOf(file);
			final String dslName = ((TaraModel) file).dsl();
			if (dslName == null) return null;
			String v = safe(() -> configuration.artifact().model().language().version());
			final String version = v == null ? LATEST : safe(() -> configuration.artifact().model().language().version());
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
		if (auxiliarLanguages.containsKey(dsl)) return auxiliarLanguages.get(dsl).get(project);
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


	private static Language languageFrom(String dsl, Map<String, Language> languages) {
		for (String currentDSL : languages.keySet())
			if (currentDSL.equalsIgnoreCase(dsl))
				return languages.get(currentDSL);
		return null;
	}

	public static void register(Language language) {
		core.put(language.languageName(), language);
	}

	public static void registerAuxiliar(Project project, Language language) {
		if (!auxiliarLanguages.containsKey(language.languageName()))
			auxiliarLanguages.put(language.languageName(), new HashMap<>());
		auxiliarLanguages.get(language.languageName()).put(project, language);
	}

	@SuppressWarnings("WeakerAccess")
	public static boolean reloadLanguage(Project project, String dsl) {
		final File languageDirectory = getLanguageDirectory(dsl);
		if (!languageDirectory.exists()) return false;
		Language language = LanguageLoader.loadLatest(dsl, languageDirectory.getPath());
		if (language == null) return false;
		addLanguage(project, dsl, language);
		PsiManager.getInstance(project).dropResolveCaches();
		Notifications.Bus.notify(new Notification("Language Reload", "", "Language " + dsl + " reloaded", NotificationType.INFORMATION), project);
		return true;
	}

	@SuppressWarnings("WeakerAccess")
	public static boolean reloadLanguage(Project project, String dsl, String version) {
		final File languageDirectory = getLanguageDirectory(dsl);
		if (!languageDirectory.exists()) return false;
		Language language = LanguageLoader.load(dsl, version, languageDirectory.getPath());
		if (language == null) return false;
		addLanguage(project, dsl, language);
		PsiManager.getInstance(project).dropResolveCaches();
		Notifications.Bus.notify(new Notification("Language Reload", "", "Language " + dsl + " reloaded", NotificationType.INFORMATION), project);
		return true;
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
		return new File(getLanguagesDirectory().getPath(), DSL_GROUP_ID.replace(".", File.separator) + File.separator + dsl.toLowerCase());
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

	private static VirtualFile getTaraDirectory() {
		final File baseDir = new File(System.getProperty("user.home"));
		final File tara = new File(baseDir, TARA_USER);
		if (!tara.exists()) tara.mkdirs();
		return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(tara);
	}

	public static VirtualFile getLanguagesDirectory() {
		final VirtualFile taraDirectory = getTaraDirectory();
		final File dslDirectory = new File(taraDirectory.getPath(), "repository");
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
