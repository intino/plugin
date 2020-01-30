package io.intino.plugin.build;

import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.compiler.server.CustomBuilderMessageHandler;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.konos.compiler.shared.KonosBuildConstants;
import io.intino.plugin.build.postcompileactions.PostCompileActionFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static io.intino.konos.compiler.shared.KonosBuildConstants.KONOSC;
import static io.intino.konos.compiler.shared.KonosBuildConstants.SEPARATOR;

public class KonosCompilerListener implements ProjectComponent {
	private static final String KONOS_PATTERN = "!?*.konos";
	private final Project project;

	private MessageBusConnection messageBusConnection;

	public KonosCompilerListener(Project project) {
		this.project = project;
	}

	@Override
	public void initComponent() {
		messageBusConnection = project.getMessageBus().connect();
		messageBusConnection.subscribe(CustomBuilderMessageHandler.TOPIC, new PostCompileListener(this.project));
		fillResourcePatterns(new CompilerConfigurationImpl(project));
	}

	private void fillResourcePatterns(CompilerConfigurationImpl configuration) {
		final List<String> patterns = Arrays.asList(configuration.getResourceFilePatterns());
		if (!patterns.contains(KONOS_PATTERN)) configuration.addResourceFilePattern(KONOS_PATTERN);
		configuration.convertPatterns();
	}

	@Override
	public void disposeComponent() {
		messageBusConnection.disconnect();
	}

	private static class PostCompileListener implements CustomBuilderMessageHandler {
		private final Project project;

		public PostCompileListener(Project project) {
			this.project = project;
		}

		@Override
		public void messageReceived(String builderId, String messageType, String messageText) {
			if (KONOSC.equals(builderId) && KonosBuildConstants.ACTION_MESSAGE.equals(messageType)) {
				final String[] messages = messageText.split(KonosBuildConstants.MESSAGE_ACTION_SEPARATOR);
				Arrays.stream(messages).map(m -> {
					List<String> split = List.of(m.split(SEPARATOR));
					return PostCompileActionFactory.get(findModule(split.get(0)), split.get(1), split.subList(2, split.size()));
				}).forEach(PostCompileAction::execute);
//				refreshDirectory(new File(new File(parameters[parameters.length - 1]).getParentFile(), "test-res"));
//				refreshDirectory(new File(new File(parameters[parameters.length - 1]).getParentFile(), "res"));
//				refreshDirectory(new File(new File(parameters[parameters.length - 1]).getParentFile(), "src"));
//				refreshDirectory(new File(new File(parameters[parameters.length - 1]).getParentFile(), "test"));
			}
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
}

