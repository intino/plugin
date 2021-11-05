package io.intino.plugin.codeinsight.notifications;

import com.intellij.codeInsight.AttachSourcesProvider;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.PsiFile;
import io.intino.Configuration;
import io.intino.Configuration.Repository;
import io.intino.plugin.dependencyresolution.*;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.utils.MavenUtil;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RepositoryPolicy;

import java.util.*;

public class AttachSourcesFromExternalArtifactoryProvider implements AttachSourcesProvider {

	@NotNull
	@Override
	public Collection<AttachSourcesAction> getActions(List<LibraryOrderEntry> orderEntries, PsiFile psiFile) {
		List<LegioConfiguration> configurations = configurations(psiFile);
		if (configurations.isEmpty()) return Collections.emptyList();
		return List.of(new AttachSourcesAction() {
			@Override
			public String getName() {
				return "Download Sources";
			}

			@Override
			public String getBusyText() {
				return "Downloading sources...";
			}

			@Override
			public ActionCallback perform(List<LibraryOrderEntry> orderEntries) {
				List<LegioConfiguration> configurations = configurations(psiFile);
				if (configurations.isEmpty()) return ActionCallback.REJECTED;
				final ActionCallback resultWrapper = new ActionCallback();
				List<Artifact> sources = resolveSources(orderEntries, configurations, psiFile);
				if (!sources.isEmpty() && sources.get(0) != null)
					attachSources(orderEntries, resultWrapper, sources.get(0), psiFile);
				else notifyNotFound(resultWrapper, psiFile);
				return resultWrapper;
			}
		});
	}

	private List<Artifact> resolveSources(List<LibraryOrderEntry> orderEntries, List<LegioConfiguration> configurations, PsiFile psiFile) {
		try {
			return ProgressManager.getInstance().runProcessWithProgressSynchronously(((ThrowableComputable<List<Artifact>, Exception>) () -> resolveSources(orderEntries.get(0), configurations)), "Downloading Sources", false, psiFile.getProject());
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private void attachSources(List<LibraryOrderEntry> orderEntries, ActionCallback resultWrapper, Artifact a, PsiFile psiFile) {
		Application application = ApplicationManager.getApplication();
		application.runWriteAction(() -> {
			if (a == null) {
				notifyNotFound(resultWrapper, psiFile);
				return;
			}
			for (LibraryOrderEntry orderEntry : orderEntries) {
				DependencyCatalog.Dependency dependency = new DependencyCatalog.Dependency(a.getGroupId() + ":" + a.getArtifactId() + classifier(a) + ":" + a.getVersion() + ":COMPILE", a.getFile(), true);
				new ProjectLibrariesManager(orderEntry.getOwnerModule().getProject()).registerSources(dependency);
			}
		});
		resultWrapper.setDone();
	}

	private String classifier(Artifact a) {
		return a.getClassifier().trim().isEmpty() ? "" : ":" + a.getClassifier();
	}

	private void notifyNotFound(ActionCallback resultWrapper, PsiFile psiFile) {
		Notifications.Bus.notify(new Notification(MavenUtil.MAVEN_NOTIFICATION_GROUP,
						"Cannot download sources",
						"<html>Sources not found" + "</html>",
						NotificationType.WARNING),
				psiFile.getProject());
		resultWrapper.setRejected();
	}

	@NotNull
	private List<Artifact> resolveSources(LibraryOrderEntry entry, List<LegioConfiguration> configurations) {
		Module module = configurations.get(0).module();
		final ImportsResolver resolver = new ImportsResolver(module, new DependencyAuditor(module), RepositoryPolicy.UPDATE_POLICY_ALWAYS, repositoryTypes(configurations));
		List<Artifact> artifacts = new ArrayList<>();
		final String libraryName = Objects.requireNonNull(entry.getLibraryName()).replace(IntinoLibrary.INTINO, "");
		final String[] names = libraryName.split(":");
		artifacts.add(resolver.sourcesOf(names[0], names[1], names[2]));
		return artifacts;
	}

	private List<LegioConfiguration> configurations(PsiFile psiFile) {
		Project project = psiFile.getProject();
		List<LegioConfiguration> result = new ArrayList<>();
		for (OrderEntry each : ProjectRootManager.getInstance(project).getFileIndex().getOrderEntriesForFile(psiFile.getVirtualFile())) {
			Configuration configuration = IntinoUtil.configurationOf(each.getOwnerModule());
			if (configuration instanceof LegioConfiguration) result.add((LegioConfiguration) configuration);
		}
		return result;
	}

	private List<Repository> repositoryTypes(List<LegioConfiguration> configurations) {
		List<Repository> types = new ArrayList<>();
		configurations.stream().map(LegioConfiguration::repositories).map(t -> filter(types, t)).forEach(types::addAll);
		return types;
	}

	private List<Repository> filter(List<Repository> types, List<Repository> toAdd) {
		List<Repository> filtered = new ArrayList<>();
		for (Repository type : toAdd) if (!isInList(types, type)) filtered.add(type);
		return filtered;
	}

	private boolean isInList(List<Repository> repositories, Repository repository) {
		return repositories.stream().anyMatch(added -> repository.url().equals(added.url()));
	}
}
