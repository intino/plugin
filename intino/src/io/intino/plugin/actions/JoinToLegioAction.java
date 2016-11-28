package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenUtil;
import org.siani.itrules.model.Frame;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.psi.impl.TaraUtil;
import tara.intellij.project.TaraModuleType;
import tara.intellij.project.configuration.ConfigurationManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static io.intino.plugin.MessageProvider.message;

public class JoinToLegioAction extends AnAction implements DumbAware {

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
		};
	}

	private void transformToLegio(Module module, MavenProject project) {
		write(legioFile(module), fromMavenFrame(module, project));
	}

	private void newLegio(Module module) {
		write(legioFile(module), newFrame(module));
	}

	private Frame newFrame(Module module) {
		Frame frame = new Frame().addTypes("legio").addSlot("groupId", "org.example").
				addSlot("artifactId", module.getName().toLowerCase()).addSlot("version", "1.0.0");
		if (TaraModuleType.isTara(module)) frame.addSlot("isIntino", "");
		return frame;
	}

	private Frame fromMavenFrame(Module module, MavenProject maven) {
		MavenId mavenId = maven.getMavenId();
		Frame frame = new Frame().addTypes("legio").addSlot("groupId", mavenId.getGroupId()).addSlot("artifactId", mavenId.getArtifactId()).addSlot("version", mavenId.getVersion());
		maven.getRemoteRepositories().stream().filter(r -> !r.getId().equals("central")).forEach(r ->
				frame.addSlot("repository", new Frame().addTypes(r.getSnapshotsPolicy() != null ? "snapshot" : "release").addSlot("url", r.getUrl()).addSlot("id", r.getId())));
		for (MavenArtifactNode node : maven.getDependencyTree()) {
			MavenArtifact artifact = node.getArtifact();
			frame.addSlot("dependency", new Frame().addTypes("dependency").addSlot("type", artifact.getScope() == null ? "compile" : artifact.getScope().toLowerCase()).
					addSlot("groupId", artifact.getGroupId()).
					addSlot("artifactId", artifact.getArtifactId()).
					addSlot("version", artifact.getVersion()));
		}
		if (TaraModuleType.isTara(module)) {
			frame.addSlot("isIntino", "");
			Configuration conf = TaraUtil.configurationOf(module);
			frame.addSlot("factory", new Frame().addTypes("factory").
					addSlot("level", notNull(conf.level().name())).
					addSlot("workingPackage", notNull(conf.workingPackage())).
					addSlot("dsl", notNull(conf.dsl())).
					addSlot("dslVersion", conf.dslVersion() == null ? "LATEST" : conf.dslVersion()));
		}
		return frame;
	}

	private String notNull(String name) {
		return name == null ? "" : name;
	}

	private Path write(File legioFile, Frame frame) {
		try {
			return Files.write(legioFile.toPath(), LegioFileTemplate.create().format(frame).getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
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
		return new File(moduleRoot, "configuration.legio");
	}
}
