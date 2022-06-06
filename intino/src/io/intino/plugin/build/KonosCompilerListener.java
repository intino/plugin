package io.intino.plugin.build;

import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.compiler.server.CustomBuilderMessageHandler;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.konos.compiler.shared.KonosBuildConstants;
import io.intino.plugin.build.postcompileactions.PostCompileActionFactory;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.konos.compiler.shared.KonosBuildConstants.*;
import static io.intino.magritte.compiler.shared.TaraBuildConstants.REFRESH_BUILDER_MESSAGE_SEPARATOR;
import static io.intino.plugin.build.PostCompileAction.FinishStatus.RequiresReload;

public class KonosCompilerListener implements CustomBuilderMessageHandler {
	private static final String KONOS_PATTERN = "!?*.konos";
	private final Project project;
	private static final Logger Log = Logger.getInstance(KonosCompilerListener.class.getName());

	public KonosCompilerListener(Project project) {
		this.project = project;
		fillResourcePatterns(new CompilerConfigurationImpl(project));
	}

	private void fillResourcePatterns(CompilerConfigurationImpl configuration) {
		final List<String> patterns = Arrays.asList(configuration.getResourceFilePatterns());
		if (!patterns.contains(KONOS_PATTERN)) configuration.addResourceFilePattern(KONOS_PATTERN);
		configuration.convertPatterns();
	}

	@Override
	public void messageReceived(String builderId, String messageType, String messageText) {
		if (messageText.isEmpty()) return;
		if (KONOSC.equals(builderId) && KonosBuildConstants.ACTION_MESSAGE.equals(messageType)) {
			final List<String> messages = Arrays.asList(messageText.split(KonosBuildConstants.MESSAGE_ACTION_START));
			final Module[] module = {null};
			List<PostCompileAction.FinishStatus> finishStatus = messages.stream()
					.skip(1)
					.map(m -> createCompileAction(module, m))
					.map(a -> a != null ? a.execute() : null)
					.collect(Collectors.toList());
			if (finishStatus.contains(RequiresReload)) IntinoUtil.configurationOf(module[0]).reload();
		}
		if (KONOSC.equals(builderId) && REFRESH_MESSAGE.equals(messageType)) {
			System.out.println("Finishing compile...");
			final String[] parameters = messageText.split(REFRESH_BUILDER_MESSAGE_SEPARATOR);
			File directory = new File(parameters[parameters.length - 1]);
			refreshOut(directory);
			refreshDirectory(new File(directory.getParentFile(), "src"));
			refreshDirectory(new File(directory.getParentFile(), "res"));
		}
	}

	@Nullable
	private PostCompileAction createCompileAction(Module[] module, String m) {
		m = m.replace(KonosBuildConstants.MESSAGE_ACTION_END, "");
		List<String> split = List.of(m.split(SEPARATOR));
		return PostCompileActionFactory.get(module(module, split.get(0)), split.get(1), split.subList(2, split.size()));
	}

	private Module module(Module[] module, String name) {
		name = name.contains("#") ? name.substring(0, name.indexOf("#")) : name;
		return module[0] = findModule(name);
	}

	private void refreshOut(File file) {
		VirtualFile outDir = VfsUtil.findFileByIoFile(file, true);
		if (outDir == null || !outDir.isValid()) return;
		outDir.refresh(true, true);
	}

	private Module findModule(String module) {
		return Arrays.stream(ModuleManager.getInstance(project).getModules()).filter(m -> m.getName().equals(module)).findFirst().orElse(null);
	}

	private void refreshDirectory(File dir) {
		VirtualFile vDir = VfsUtil.findFileByIoFile(dir, true);
		if (vDir == null || !vDir.isValid()) return;
		Log.info("Refreshing " + dir.getName() + "...");
		System.out.println("Refreshing " + dir.getName() + "...");
		vDir.refresh(true, true);
		FileStatusManager.getInstance(project).fileStatusesChanged();
		FileDocumentManager.getInstance().reloadFiles(vDir);
		ProjectView.getInstance(project).refresh();
	}
}