package io.intino.plugin.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.messages.MessageBus;
import io.intino.legio.graph.level.LevelArtifact;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.builders.ModelBuilderManager;
import io.intino.plugin.toolwindows.output.IntinoTopics;
import io.intino.plugin.toolwindows.output.MavenListener;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.compiler.shared.TaraBuildConstants;
import io.intino.tara.plugin.lang.file.TaraFileType;
import io.intino.tara.plugin.lang.psi.TaraModel;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.jps.cmdline.ClasspathBootstrap;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.plugin.project.Safe.safe;
import static io.intino.tara.compiler.shared.TaraBuildConstants.*;
import static java.util.Arrays.asList;

public class IntinoGenerationAction extends IntinoAction {
	private static final char NL = '\n';
	private static final String ENCODING = "UTF-8";
	private boolean isConnected = false;
	private Set<PsiFile> pendingFiles = new HashSet<>();

	@Override
	public void actionPerformed(AnActionEvent e) {
		Module module = e.getData(LangDataKeys.MODULE);
		execute(module);
	}

	@Override
	public void execute(Module module) {
		if (module == null) return;
		konos(module);
		model(module);
		pendingFiles.clear();
	}

	public void force(Module module) {
		if (module == null) return;
		forceKonos(module);
//		model(module);
	}


	private void forceKonos(Module module) {
		final InterfaceGenerationAction action = (InterfaceGenerationAction) ActionManager.getInstance().getAction("InterfaceGeneration");
		action.force(module);
	}

	private void konos(Module module) {
		final InterfaceGenerationAction action = (InterfaceGenerationAction) ActionManager.getInstance().getAction("InterfaceGeneration");
		action.execute(module);
	}

	private void model(Module module) {
		if (modelsModified()) buildModels(module);
	}

	private boolean modelsModified() {
		return pendingFiles.stream().anyMatch(f -> TaraFileType.instance().equals(f.getFileType()));
	}

	private void buildModels(Module module) {
		final List<TaraModel> models = TaraUtil.getFilesOfModuleByFileType(module, TaraFileType.instance());
		FileDocumentManager.getInstance().saveAllDocuments();
		if (models.isEmpty()) return;
		withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Generating Code", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
					 @Override
					 public void run(@NotNull ProgressIndicator indicator) {
						 Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
						 runCompiler(module, TaraUtil.configurationOf(module), models);
						 VirtualFileManager.getInstance().asyncRefresh(null);
					 }
				 }
		);
	}

	private void runCompiler(Module module, Configuration configuration, List<TaraModel> models) {
		try {
			String argsFile = createArgsFile(module, models);
			final List<String> libraries = taraCompilerClasspath(module, (LegioConfiguration) configuration);
			List<String> commandParameters = new ArrayList<>();
			commandParameters.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
			commandParameters.addAll(asList("-Dfile.encoding=UTF-8", "-cp", String.join(":", libraries)));
			commandParameters.add("io.intino.tara.TaracRunner");
			commandParameters.add(argsFile);
			final Process process = new ProcessBuilder(commandParameters).redirectErrorStream(true).start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String text = "a";
			while (process.isAlive() && !text.isEmpty()) {
				text = read(reader);
				sendMessage(module, text);
			}
			process.destroy();
			new File(argsFile).delete();
		} catch (Throwable e) {
			Bus.notify(new Notification("Tara Language", "Error occurred", "Exception: " + e.getMessage(), NotificationType.ERROR), null);
		}
	}


	private List<String> taraCompilerClasspath(Module module, LegioConfiguration configuration) {
		LevelArtifact.Model model = safe(() -> configuration.graph().artifact().asLevel().model());
		if (model == null) return Collections.emptyList();
		return new ModelBuilderManager(module.getProject(), model).resolveBuilder();
	}

	private String read(BufferedReader reader) {
		try {
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}
			return builder.toString();
		} catch (IOException e) {
		}
		return "";
	}

	private void sendMessage(Module module, String value) {
		final MessageBus messageBus = module.getProject().getMessageBus();
		final MavenListener mavenListener = messageBus.syncPublisher(IntinoTopics.MAVEN);
		mavenListener.println(value);
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}

	private String createArgsFile(Module module, List<TaraModel> models) throws IOException {
		final Path argsFile = Files.createTempFile("args_", "");
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(argsFile.toFile()), Charset.forName(ENCODING)))) {
			writer.write(SRC_FILE + NL);
			for (TaraModel model : models) writer.write(model.getVirtualFile().getPath() + "#true" + NL);
			writer.write(NL);
			writer.write(PROJECT + NL + module.getProject().getName() + NL);
			writer.write(MODULE + NL + module.getName() + NL);
			writePaths(writer, module);
			writer.write(MAKE + NL + false + NL);
			writer.write(TEST + NL + false + NL);
			writer.write(TaraBuildConstants.ENCODING + NL + ENCODING + NL);
			writer.close();
		}
		return argsFile.toFile().getPath();
	}

	private void writePaths(Writer writer, Module module) throws IOException {
		if (module == null) throw new IOException("Module is null");
		final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
		writer.write(SEMANTIC_LIB + NL + getTaraJar(ClasspathBootstrap.getResourceFile(TaraUtil.class)).getAbsolutePath() + NL);
		writer.write(OUTPUTPATH + NL + genDirectory(module) + NL);
		String compilerOutputPath = new URL(extension.getCompilerOutputUrl()).getFile();
		if (compilerOutputPath == null)
			compilerOutputPath = new URL(CompilerProjectExtension.getInstance(module.getProject()).getCompilerOutputUrl()).getFile();
		writer.write(FINAL_OUTPUTPATH + NL + compilerOutputPath + NL);
		final List<String> resources = resourceDirectories(module);
		if (!resources.isEmpty()) writer.write(RESOURCES + NL + resources.get(0) + NL);
		writer.write(TARA_PATH + NL + new File(MavenProjectsManager.getInstance(module.getProject()).getLocalRepository().getPath()).getParent() + NL);
		writer.write(TARA_PROJECT_PATH + NL + module.getProject().getBasePath() + File.separator + ".tara" + NL);
		writer.write(SRC_PATH + NL);
		for (String sourceDirectory : srcDirectories(module)) writer.write(sourceDirectory + NL);
		writer.write(NL);
	}

	private List<String> srcDirectories(Module module) {
		final ArrayList<String> sources = new ArrayList<>();
		ApplicationManager.getApplication().runReadAction(() -> {
			final ModuleRootManager manager = ModuleRootManager.getInstance(module);
			final List<VirtualFile> sourceRoots = manager.getModifiableModel().getSourceRoots(JavaSourceRootType.SOURCE);
			sources.addAll(sourceRoots.stream().filter(s -> s.getName().equals("src")).map(VirtualFile::getPath).collect(Collectors.toList()));
		});
		return sources;
	}

	private List<String> resourceDirectories(Module module) {
		final ArrayList<String> sources = new ArrayList<>();
		ApplicationManager.getApplication().runReadAction(() -> {
			final ModuleRootManager manager = ModuleRootManager.getInstance(module);
			final List<VirtualFile> sourceRoots = manager.getSourceRoots(JavaResourceRootType.RESOURCE);
			sources.addAll(sourceRoots.stream().map(VirtualFile::getPath).collect(Collectors.toList()));
		});
		return sources;
	}

	private String genDirectory(Module module) {
		final ArrayList<String> sources = new ArrayList<>();
		ApplicationManager.getApplication().runReadAction(() -> {
			final ModuleRootManager manager = ModuleRootManager.getInstance(module);
			final List<VirtualFile> sourceRoots = manager.getModifiableModel().getSourceRoots(JavaSourceRootType.SOURCE);
			final String[] directories = sourceRoots.stream().filter(s -> s.getName().equals("gen")).map(VirtualFile::getPath).toArray(String[]::new);
			sources.add(directories.length == 0 ?
					new File(new File(srcDirectories(module).get(0)).getParent(), "gen").getAbsolutePath() :
					directories[0]);
		});
		return sources.get(0);
	}

	@NotNull
	private File getTaraJar(File root) {
		return new File(root.getParentFile(), "tara-plugin.jar");
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		final Project project = e.getProject();
		if (!isConnected && project != null) {
			final MessageBus messageBus = project.getMessageBus();
			messageBus.connect().subscribe(IntinoTopics.FILE_MODIFICATION, file -> {
				final VirtualFile vFile = VfsUtil.findFileByIoFile(new File(file), true);
				if (vFile == null) return;
				pendingFiles.add(PsiManager.getInstance(project).findFile(vFile));
			});
			isConnected = true;
		}
		e.getPresentation().setVisible(!pendingFiles.isEmpty());
		e.getPresentation().setIcon(IntinoIcons.GENARATION_16);
		e.getPresentation().setText("Generate intino code");
	}
}
