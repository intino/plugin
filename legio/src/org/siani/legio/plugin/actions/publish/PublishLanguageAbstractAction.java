package org.siani.legio.plugin.actions.publish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.*;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.siani.legio.plugin.build.LanguagePublisher;
import tara.intellij.lang.LanguageManager;
import tara.intellij.lang.psi.impl.TaraUtil;
import tara.intellij.project.configuration.Configuration;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipOutputStream;

import static com.intellij.util.io.ZipUtil.addFileToZip;
import static org.jetbrains.idea.maven.execution.MavenExecutionOptions.LoggingLevel.ERROR;
import static tara.intellij.actions.utils.ExportationPomCreator.createPom;
import static tara.intellij.lang.LanguageManager.DSL;
import static tara.intellij.lang.LanguageManager.FRAMEWORK;
import static tara.intellij.messages.MessageProvider.message;

abstract class PublishLanguageAbstractAction extends AnAction implements DumbAware {
	private static final String TEMP_POM_XML = "_pom.xml.itr";
	private static final String INFO_JSON = "info.json";
	private static final Logger LOG = Logger.getInstance(PublishLanguageAbstractAction.class.getName());
	private static final String JAR_EXTENSION = ".jar";
	private static final String JSON_EXTENSION = ".json";

	List<String> errorMessages = new ArrayList<>();
	List<String> successMessages = new ArrayList<>();

	boolean publish(final Module module, String outputDsl) {
		final File dstFile = new File(module.getProject().getBasePath() + File.separator + outputDsl + LanguageManager.LANGUAGE_EXTENSION);
		publish(dstFile, module);
		return clearReadOnly(module.getProject(), dstFile);
	}

	private void publish(File zipFile, Module module) {
		final MavenProject project = MavenProjectsManager.getInstance(module.getProject()).findProject(module);
		if (project == null) return;
		MavenGeneralSettings generalSettings = new MavenGeneralSettings();
		generalSettings.setOutputLevel(ERROR);
		generalSettings.setPrintErrorStackTraces(false);
		generalSettings.setFailureBehavior(MavenExecutionOptions.FailureMode.AT_END);
		MavenRunnerSettings runnerSettings = MavenRunner.getInstance(module.getProject()).getSettings().clone();
		runnerSettings.setSkipTests(false);
		runnerSettings.setRunMavenInBackground(true);
		MavenRunnerParameters parameters = new MavenRunnerParameters(true, new File(project.getPath()).getParent(), Arrays.asList(ParametersList.parse("deploy")), Collections.<String>emptyList());
		MavenRunConfigurationType.runConfiguration(module.getProject(), parameters, generalSettings, runnerSettings, descriptor -> deployLanguage(zipFile, module, FileUtil.getNameWithoutExtension(zipFile)));
	}

	private boolean deployLanguage(File zipFile, Module module, String languageName) {
		return ProgressManager.getInstance().runProcessWithProgressSynchronously(() ->
			deployLanguage(zipFile, module, languageName, createProgressIndicator()), message("export.language", languageName), false, module.getProject());
	}

	private void deployLanguage(File zipFile, Module module, String languageName, ProgressIndicator indicator) {
		try {
			createArtifact(zipFile, module, languageName, indicator);
			LocalFileSystem.getInstance().refreshIoFiles(Collections.singleton(zipFile), true, false, null);
			final int i = new LanguagePublisher(module, languageName, zipFile).export();
			if (i != 201) throw new IOException("Error uploading language. Code: " + i);
			zipFile.delete();
			successMessages.add(message("saved.message", languageName));
		} catch (final IOException e) {
			LOG.info(e.getMessage(), e);
			errorMessages.add(e.getMessage() + "\n(" + FileUtil.getNameWithoutExtension(zipFile) + ")");
		}
	}

	private void createArtifact(File zipFile, Module module, String languageName, ProgressIndicator progressIndicator) throws IOException {
		zipAll(zipFile, languageName, module, createPom(module, languageName), progressIndicator);
	}

	@Nullable
	private ProgressIndicator createProgressIndicator() {
		final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
		if (progressIndicator != null) {
			progressIndicator.setText(message("prepare.for.deployment.common"));
			progressIndicator.setIndeterminate(true);
		}
		return progressIndicator;
	}

	private void zipAll(final File zipFile, final String languageName, Module module, File pom,
						final ProgressIndicator progressIndicator) throws IOException {
		if (FileUtil.ensureCanCreateFile(zipFile)) {
			ZipOutputStream zos = null;
			try {
				zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
				addLanguage(zos, languageName);
				addPom(zos, pom);
				addInfo(zos, module, languageName, progressIndicator);
			} finally {
				if (zos != null) zos.close();
			}
		}
	}

	private void addInfo(ZipOutputStream zos, Module module, String languageName, ProgressIndicator progressIndicator) throws IOException {
		File file = createInfo(module);
		if (file == null) return;
		final String entryPath = "/" + DSL + "/" + languageName + "/" + INFO_JSON;
		addFileToZip(zos, file, entryPath, new HashSet<>(), createFilter(progressIndicator, FileTypeManager.getInstance()));
	}

	private File createInfo(Module module) {
		Map<String, Object> values = new HashMap<>();
		final Configuration conf = TaraUtil.configurationOf(module);
		for (Method method : conf.getClass().getMethods()) {
			if (method.isDefault() || !method.isAccessible() || method.getParameters().length > 0) continue;
			try {
				values.put(method.getName(), method.invoke(conf));
			} catch (IllegalAccessException | InvocationTargetException ignored) {
			} finally {
				method.setAccessible(false);
			}
		}
		values.put("framework", conf.groupId() + ":" + conf.artifactId() + ":" + conf.modelVersion());
		return saveInfo(values);
	}


	private File saveInfo(Map<String, Object> values) {
		try {
			final File info = FileUtil.createTempFile("info", ".json", true);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			final String txt = gson.toJson(values);
			if (!info.exists()) info.getParentFile().mkdirs();
			Files.write(info.toPath(), txt.getBytes());
			return info;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void addLanguage(ZipOutputStream zos, String languageName) throws IOException {
		File taraDirectory = LanguageManager.getLanguageDirectory(languageName);
		if (!taraDirectory.exists()) throw new IOException("Language file not found");
		String entryPath = "/" + DSL + "/" + languageName + "/" + languageName + JAR_EXTENSION;
		final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
		addFileToZip(zos, new File(taraDirectory.getPath(), languageName + JAR_EXTENSION), entryPath, new HashSet<>(), createFilter(progressIndicator, FileTypeManager.getInstance()));
		final File refactors = new File(taraDirectory.getPath(), "refactors" + JSON_EXTENSION);
		if (refactors.exists()) {
			entryPath = "/" + FRAMEWORK + "/" + languageName + "/" + "refactors" + JSON_EXTENSION;
			addFileToZip(zos, refactors, entryPath, new HashSet<>(), createFilter(progressIndicator, FileTypeManager.getInstance()));
		}
	}

	private void addPom(ZipOutputStream zos, File pom) throws IOException {
		final File dest = new File(pom.getParent(), TEMP_POM_XML);
		dest.delete();
		pom.renameTo(dest);
		final String entryPath = "/" + TEMP_POM_XML;
		final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
		addFileToZip(zos, dest, entryPath, new HashSet<>(), createFilter(progressIndicator, FileTypeManager.getInstance()));
	}


	private FileFilter createFilter(final ProgressIndicator progressIndicator, @Nullable final FileTypeManager fileTypeManager) {
		return pathName -> {
			if (progressIndicator != null) progressIndicator.setText2("");
			return fileTypeManager == null || !fileTypeManager.isFileIgnored(FileUtil.toSystemIndependentName(pathName.getName()));
		};
	}

	private boolean clearReadOnly(final Project project, final File dstFile) {
		final URL url;
		FileUtil.delete(dstFile);
		try {
			url = dstFile.toURI().toURL();
		} catch (MalformedURLException e) {
			return true;
		}
		final VirtualFile vfile = VfsUtil.findFileByURL(url);
		return !dstFile.exists() || vfile == null || !ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(vfile).hasReadonlyFiles();
	}

}
