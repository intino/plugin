package io.intino.plugin.build.postcompileactions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import io.intino.plugin.CompilerConfiguration.Phase;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.build.AbstractArtifactFactory;
import io.intino.plugin.build.PostCompileAction;
import io.intino.plugin.build.maven.MavenRunner;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.intellij.notification.NotificationType.INFORMATION;

public class MavenInvokeAction extends PostCompileAction {
	private static final Logger LOG = Logger.getInstance(AbstractArtifactFactory.class.getName());

	private final List<String> parameters;

	public MavenInvokeAction(Module module, List<String> parameters) {
		super(module);
		this.parameters = parameters;
	}

	@Override
	public FinishStatus execute() {
		File pom = new File(parameters.get(0));
		String[] coors = parameters.get(1).split(":");
		Phase phase = Phase.valueOf(parameters.get(2));
		try {
			mvn(pom, phase, coors);
			return FinishStatus.Done;
		} catch (IOException e) {
			return FinishStatus.Error;
		}
	}

	private void mvn(File pom, Phase phase, String[] coors) throws IOException {
		final MavenRunner.InvocationResult result = new MavenRunner(module).invokeMavenWithConfiguration(pom, mavenPhase(phase));
		String gerund = (phase.equals(Phase.INSTALL)) ? "installing" : "distributing";
		if (result != null && result.getExitCode() != 0) {
			if (result.getExecutionException() != null)
				throw new IOException("Failed " + gerund + " artifact.", result.getExecutionException());
			else throw new IOException("Failed " + gerund + " artifact. Exit code: " + result.getExitCode());
		} else if (result == null) throw new IOException("Failed generating accessor. Maven HOME not found");
		notifySuccess(coors, phase);
	}

	@NotNull
	private static String mavenPhase(Phase phase) {
		if (phase.equals(Phase.DISTRIBUTE)) return "deploy";
		return phase.name().toLowerCase();
	}

	private void notifySuccess(String[] coors, Phase goal) {
		NotificationGroup.findRegisteredGroup("Intino")
				.createNotification("Artifact " + coors[1] + (goal == Phase.INSTALL ? " installed" : " distributed"), "", INFORMATION)
				.setImportant(true)
				.setIcon(IntinoIcons.ICON_13)
				.addAction(new NotificationAction("Copy maven dependency") {
					@Override
					public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
						StringSelection selection = new StringSelection(newDependency(coors));
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(selection, selection);
					}
				}).notify(module.getProject());
	}

	@NotNull
	private String newDependency(String[] coors) {
		return "<dependency>\n" +
				"    <groupId>" + coors[0] + "</groupId>\n" +
				"    <artifactId>" + coors[1] + "</artifactId>\n" +
				"    <version>" + coors[2] + "</version>\n" +
				"</dependency>";
	}
}