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
import io.intino.plugin.dependencyresolution.ImportsResolver;
import io.intino.plugin.dependencyresolution.IntinoLibrary;
import io.intino.plugin.dependencyresolution.ProjectLibrariesManager;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.utils.MavenUtil;

import java.util.*;

import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS;

public class AttachSourcesFromExternalArtifactoryProvider implements AttachSourcesProvider {

	@Override
	public @NotNull Collection<? extends AttachSourcesAction> getActions(@NotNull List<? extends LibraryOrderEntry> list, @NotNull PsiFile psiFile) {
		List<ArtifactLegioConfiguration> configurations = configurations(psiFile);
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
			public @NotNull ActionCallback perform(@NotNull List<? extends LibraryOrderEntry> orderEntries) {
				List<ArtifactLegioConfiguration> configurations = configurations(psiFile);
				if (configurations.isEmpty()) return ActionCallback.REJECTED;
				final ActionCallback resultWrapper = new ActionCallback();
				var sources = resolveSources(orderEntries, configurations, psiFile);
				if (sources != null && sources.isResolved())
					attachSources(orderEntries, resultWrapper, sources.getArtifact(), psiFile);
				else notifyNotFound(resultWrapper, psiFile);
				return resultWrapper;
			}
		});
	}

	private ArtifactResult resolveSources(List<? extends LibraryOrderEntry> orderEntries, List<ArtifactLegioConfiguration> configurations, PsiFile psiFile) {
		try {
			return ProgressManager.getInstance().runProcessWithProgressSynchronously(((ThrowableComputable<ArtifactResult, Exception>) () -> resolveSources(orderEntries.get(0), configurations)), "Downloading Sources", false, psiFile.getProject());
		} catch (Exception e) {
			return null;
		}
	}

	private void attachSources(List<? extends LibraryOrderEntry> orderEntries, ActionCallback resultWrapper, Artifact a, PsiFile psiFile) {
		Application application = ApplicationManager.getApplication();
		application.runWriteAction(() -> {
			if (a == null) {
				notifyNotFound(resultWrapper, psiFile);
				return;
			}
			for (LibraryOrderEntry orderEntry : orderEntries) {
				new ProjectLibrariesManager(orderEntry.getOwnerModule().getProject()).registerSources(a);
			}
		});
		resultWrapper.setDone();
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
	private ArtifactResult resolveSources(LibraryOrderEntry entry, List<ArtifactLegioConfiguration> configurations) {
		Module module = configurations.get(0).module();
		final ImportsResolver resolver = new ImportsResolver(module, UPDATE_POLICY_ALWAYS, repositoryTypes(configurations), null);
		final String libraryName = Objects.requireNonNull(entry.getLibraryName()).replace(IntinoLibrary.INTINO, "");
		final String[] names = libraryName.split(":");
		return resolver.sourcesOf(names[0], names[1], names[2]);
	}

	private List<ArtifactLegioConfiguration> configurations(PsiFile psiFile) {
		Project project = psiFile.getProject();
		List<ArtifactLegioConfiguration> result = new ArrayList<>();
		for (OrderEntry each : ProjectRootManager.getInstance(project).getFileIndex().getOrderEntriesForFile(psiFile.getVirtualFile())) {
			Configuration configuration = IntinoUtil.configurationOf(each.getOwnerModule());
			if (configuration instanceof ArtifactLegioConfiguration)
				result.add((ArtifactLegioConfiguration) configuration);
		}
		return result;
	}

	private List<Repository> repositoryTypes(List<ArtifactLegioConfiguration> configurations) {
		List<Repository> types = new ArrayList<>();
		configurations.stream().map(ArtifactLegioConfiguration::repositories).map(t -> filter(types, t)).forEach(types::addAll);
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
