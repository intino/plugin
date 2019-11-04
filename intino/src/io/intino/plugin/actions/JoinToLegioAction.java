package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.LegioFileTemplate;
import io.intino.plugin.toolwindows.output.IntinoTopics;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.plugin.project.IntinoModuleType;
import io.intino.tara.plugin.project.configuration.ConfigurationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.file.legio.LegioFileType.LEGIO_FILE;

public class JoinToLegioAction extends AnAction implements DumbAware {
	private static final com.intellij.openapi.diagnostic.Logger logger = com.intellij.openapi.diagnostic.Logger.getInstance(JoinToLegioAction.class);


	@Override
	public void actionPerformed(AnActionEvent e) {
		Module module = e.getData(LangDataKeys.MODULE);
		if (module == null) return;
		MavenProject mavenProject = MavenProjectsManager.getInstance(module.getProject()).findProject(module);
		Runnable runnable = run(module, mavenProject);
		if (ApplicationManager.getApplication().isDispatchThread()) runnable.run();
		else
			MavenUtil.runInBackground(module.getProject(), message("join.to.legio"), false, indicator -> runnable.run()).waitFor();
	}

	@NotNull
	private Runnable run(Module module, MavenProject mavenProject) {
		return () -> {
			if (mavenProject != null) transformToLegio(module, mavenProject);
			else newLegio(module);
			if (mavenProject != null)
				MavenProjectsManager.getInstance(module.getProject()).removeManagedFiles(Collections.singletonList(mavenProject.getFile()));
			ConfigurationManager.register(module, new LegioConfiguration(module)).init();
			final VirtualFile moduleFile = VfsUtil.findFileByIoFile(new File(module.getModuleFilePath()), true);
			if (moduleFile != null) VfsUtil.markDirtyAndRefresh(true, true, false, moduleFile.getParent());
			publish(module);
		};
	}

	private void publish(Module module) {
		final MessageBus messageBus = module.getProject().getMessageBus();
		final LegioListener legioListener = messageBus.syncPublisher(IntinoTopics.LEGIO);
		legioListener.moduleJoinedToLegio(module.getName());
		final MessageBusConnection connect = messageBus.connect();
		connect.deliverImmediately();
		connect.disconnect();
	}

	private void transformToLegio(Module module, MavenProject project) {
		write(legioFile(module), fromMavenFrame(module, project));
	}

	private void newLegio(Module module) {
		write(legioFile(module), newFrame(module));
	}

	private Frame newFrame(Module module) {
		FrameBuilder builder = new FrameBuilder("legio").add("groupId", "org.example").
				add("artifactId", module.getName().toLowerCase()).add("version", "1.0.0");
		if (IntinoModuleType.isIntino(module)) builder.add("isIntino", "");
		return builder.toFrame();
	}

	private Frame fromMavenFrame(Module module, MavenProject maven) {
		MavenId mavenId = maven.getMavenId();
		FrameBuilder builder = new FrameBuilder("legio").add("groupId", mavenId.getGroupId()).add("artifactId", mavenId.getArtifactId()).add("version", mavenId.getVersion());
		maven.getRemoteRepositories().stream().filter(r -> !r.getId().equals("central")).forEach(r ->
				builder.add("repository", new FrameBuilder(r.getSnapshotsPolicy() != null ? "snapshot" : "release").add("url", r.getUrl()).add("id", r.getId())).toFrame());
		for (MavenArtifactNode node : maven.getDependencyTree()) {
			MavenArtifact artifact = node.getArtifact();
			builder.add("dependency", new FrameBuilder("dependency").add("type", artifact.getScope() == null ? "compile" : artifact.getScope().toLowerCase()).
					add("groupId", artifact.getGroupId()).
					add("artifactId", artifact.getArtifactId()).
					add("version", artifact.getVersion()).toFrame());
		}
		if (IntinoModuleType.isIntino(module)) {
			builder.add("isIntino", "");
			Configuration conf = TaraUtil.configurationOf(module);
			if (conf.model() != null)
				builder.add("factory", new FrameBuilder("factory").
						add("level", notNull(conf.model().level().name())).
						add("workingPackage", notNull(conf.workingPackage())).
						add("dsl", notNull(conf.model().language().name())).
						add("dslVersion", conf.model().language().version() == null ? "LATEST" : conf.model().language().version()).toFrame());
		}
		return builder.toFrame();
	}

	private String notNull(String name) {
		return name == null ? "" : name;
	}

	private void write(File legioFile, Frame frame) {
		try {
			Files.write(legioFile.toPath(), new LegioFileTemplate().render(frame).getBytes());
		} catch (IOException e) {
			logger.error(e);
		}
	}

	@Override
	public void update(AnActionEvent e) {
		final Module module = e.getData(LangDataKeys.MODULE);
		boolean enabled = module != null && !hasLegioFile(module);
		e.getPresentation().setVisible(enabled);
		e.getPresentation().setEnabled(enabled);
		e.getPresentation().setIcon(IntinoIcons.LEGIO_16);
	}

	private boolean hasLegioFile(Module module) {
		File file = legioFile(module);
		return file.exists();
	}

	@NotNull
	private File legioFile(Module module) {
		File moduleRoot = new File(module.getModuleFilePath()).getParentFile();
		return new File(moduleRoot, LEGIO_FILE);
	}
}
