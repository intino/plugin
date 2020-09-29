package io.intino.plugin.build;

import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.konos.compiler.shared.KonosBuildConstants;
import io.intino.magritte.compiler.shared.TaraBuildConstants;
import io.intino.plugin.build.postcompileactions.PostCompileActionFactory;
import io.intino.plugin.lang.psi.impl.IntinoUtil;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.konos.compiler.shared.KonosBuildConstants.KONOSC;
import static io.intino.konos.compiler.shared.KonosBuildConstants.SEPARATOR;
import static io.intino.plugin.build.PostCompileAction.FinishStatus.RequiresReload;

public class KonosCompilerListener implements com.intellij.compiler.server.CustomBuilderMessageHandler {
	private static final String KONOS_PATTERN = "!?*.konos";
	private final Project project;

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
			List<PostCompileAction.FinishStatus> finishSatus = messages.subList(1, messages.size()).stream().map(m -> {
				m = m.replace(KonosBuildConstants.MESSAGE_ACTION_END, "");
				List<String> split = List.of(m.split(SEPARATOR));
				return PostCompileActionFactory.get(module(module, split.get(0)), split.get(1), split.subList(2, split.size()));
			}).map(a -> a != null ? a.execute() : null).collect(Collectors.toList());
			if (finishSatus.contains(RequiresReload)) IntinoUtil.configurationOf(module[0]).reload();
		}
		if (KONOSC.equals(builderId) && KonosBuildConstants.REFRESH_MESSAGE.equals(messageType)) {
			final String[] parameters = messageText.split(TaraBuildConstants.REFRESH_BUILDER_MESSAGE_SEPARATOR);
			File directory = new File(parameters[parameters.length - 1]);
			refreshOut(directory);
			refreshDirectory(new File(directory.getParentFile(), "res"));
			refreshDirectory(new File(directory.getParentFile(), "src"));
		}
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

	private void refreshDirectory(File res) {
		VirtualFile resDir = VfsUtil.findFileByIoFile(res, true);
		if (resDir == null || !resDir.isValid()) return;
		resDir.refresh(true, true);
	}
}