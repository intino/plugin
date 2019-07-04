package io.intino.plugin.codeinsight.notifications;

import com.google.common.collect.ImmutableSet;
import com.intellij.codeInsight.AttachSourcesProvider;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.psi.PsiFile;
import io.intino.legio.graph.Repository.Type;
import io.intino.plugin.dependencyresolution.*;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
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
		return ImmutableSet.of(new AttachSourcesAction() {
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
				List<Artifact> sources = resolveSources(orderEntries.get(0), configurations);
				if (!sources.isEmpty() && sources.get(0) != null)
					attachSources(orderEntries, resultWrapper, sources.get(0), psiFile);
				else notifyNotFound(resultWrapper, psiFile);
				return resultWrapper;
			}
		});
	}

	private void attachSources(List<LibraryOrderEntry> orderEntries, ActionCallback resultWrapper, Artifact artifact, PsiFile psiFile) {
		Application application = ApplicationManager.getApplication();
		application.runWriteAction(() -> {
			if (artifact == null) {
				notifyNotFound(resultWrapper, psiFile);
				return;
			}
			for (LibraryOrderEntry orderEntry : orderEntries) {
				DependencyCatalog.Dependency dependency = new DependencyCatalog.Dependency(artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion() + ":COMPILE", artifact.getFile(), true);
				new ProjectLibrariesManager(orderEntry.getOwnerModule().getProject()).registerSources(dependency);
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
	private List<Artifact> resolveSources(LibraryOrderEntry entry, List<LegioConfiguration> configurations) {
		Module module = configurations.get(0).module();
		final ImportsResolver resolver = new ImportsResolver(module, repositoryTypes(configurations), new DependencyAuditor(module), RepositoryPolicy.UPDATE_POLICY_ALWAYS, Collections.emptyList());
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
			Configuration configuration = TaraUtil.configurationOf(each.getOwnerModule());
			if (configuration instanceof LegioConfiguration) result.add((LegioConfiguration) configuration);
		}
		return result;
	}

	private List<Type> repositoryTypes(List<LegioConfiguration> configurations) {
		List<Type> types = new ArrayList<>();
		configurations.stream().map(LegioConfiguration::repositoryTypes).map(t -> filter(types, t)).forEach(types::addAll);
		return types;
	}

	private List<Type> filter(List<Type> types, List<Type> toAdd) {
		List<Type> filtered = new ArrayList<>();
		for (Type type : toAdd) if (!isInList(types, type)) filtered.add(type);
		return filtered;
	}

	private boolean isInList(List<Type> types, Type type) {
		return types.stream().anyMatch(added -> type.url().equals(added.url()));
	}
}
