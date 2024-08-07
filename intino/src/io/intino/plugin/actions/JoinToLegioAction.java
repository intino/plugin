package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.ConfigurationManager;
import io.intino.plugin.project.configuration.LegioFileTemplate;
import io.intino.plugin.project.module.IntinoModuleType;
import io.intino.plugin.toolwindows.IntinoTopics;
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
import static io.intino.plugin.file.LegioFileType.ARTIFACT_LEGIO;

public class JoinToLegioAction extends AnAction implements DumbAware {
	private static final com.intellij.openapi.diagnostic.Logger logger = com.intellij.openapi.diagnostic.Logger.getInstance(JoinToLegioAction.class);

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}

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
			ConfigurationManager.register(module, new ArtifactLegioConfiguration(module)).init();
			final VirtualFile moduleFile = ProjectUtil.guessModuleDir(module);
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
		for (MavenArtifactNode mogram : maven.getDependencyTree()) {
			MavenArtifact artifact = mogram.getArtifact();
			builder.add("dependency", new FrameBuilder("dependency").add("type", artifact.getScope() == null ? "compile" : artifact.getScope().toLowerCase()).
					add("groupId", artifact.getGroupId()).
					add("artifactId", artifact.getArtifactId()).
					add("version", artifact.getVersion()).toFrame());
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
		final Module module = ApplicationManager.getApplication().runReadAction((Computable<? extends Module>) () -> e.getData(LangDataKeys.MODULE));
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
		return new File(IntinoUtil.moduleRoot(module), ARTIFACT_LEGIO);
	}
}
