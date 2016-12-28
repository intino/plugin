package io.intino.plugin.dependencyresolution;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.jcabi.aether.Aether;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import io.intino.tara.compiler.shared.Configuration;
import tara.dsl.ProteoConstants;
import io.intino.tara.plugin.lang.LanguageManager;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class LanguageImporter {

    private static final Logger LOG = Logger.getInstance(LanguageImporter.class.getName());

    private Module module;
    private final Configuration configuration;

    public LanguageImporter(Module module, Configuration configuration) {
        this.module = module;
        this.configuration = configuration;
    }

    public String importLanguage(String dsl, String version) {
        final String versionCode = LegioUtil.effectiveVersionOf(dsl, version, (LegioConfiguration) configuration);
        final boolean done = downloadLanguage(dsl, versionCode);
        if (done) {
            configuration.dslVersion(versionCode);
            reload(dsl, module.getProject());
            return versionCode;
        } else return "";
    }

    private boolean downloadLanguage(String name, String version) {
        try {
            if (name.equals(ProteoConstants.PROTEO) || name.equals(ProteoConstants.VERSO)) return true;
            final File languagesDirectory = new File(LanguageManager.getLanguagesDirectory().getPath());
            new Aether(repository(), languagesDirectory).resolve(new DefaultArtifact(LanguageManager.DSL_GROUP_ID, name, "jar", version), JavaScopes.COMPILE);
            return true;
        } catch (DependencyResolutionException e) {
            error(e);
            return false;
        }
    }

    @NotNull
    private List<RemoteRepository> repository() {
        return Collections.singletonList(new RemoteRepository(configuration.languageRepositoryId(), "default", configuration.languageRepository()));
    }

    private void reload(String fileName, Project project) {
        LanguageManager.reloadLanguage(project, FileUtil.getNameWithoutExtension(fileName));
    }

    private void error(Exception e) {
        Bus.notify(new Notification("Tara Language", "Error connecting with Artifactory.", e.getMessage(), NotificationType.ERROR));
    }
}
