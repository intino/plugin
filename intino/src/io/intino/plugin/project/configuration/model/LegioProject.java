package io.intino.plugin.project.configuration.model;

import com.intellij.openapi.application.ApplicationManager;
import io.intino.Configuration;
import io.intino.ProjectConfiguration;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraMogramImpl;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.configuration.ProjectLegioConfiguration;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.MogramContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.componentsOfType;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;
import static java.util.stream.Collectors.toList;

public class LegioProject implements ProjectConfiguration.Project {
	private static final com.intellij.openapi.diagnostic.Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(LegioProject.class.getName());

	public static final String NL = "\n";
	private final ProjectLegioConfiguration root;
	private final TaraMogram mogram;
	private String name;

	public LegioProject(ProjectLegioConfiguration root, TaraMogram mogram) {
		this.root = root;
		this.mogram = mogram;
	}

	@Override
	public String name() {
		if (mogram == null) return null;
		return name == null ? name = mogram.name() : name;
	}

	@Override
	public void name(String newName) {
		writeCommandAction(mogram.getProject(), mogram.getContainingFile()).run(() -> mogram.name(newName));
		ApplicationManager.getApplication().invokeAndWait(() -> IntinoUtil.commitDocument(mogram.getContainingFile()));
	}

	@Override
	public String description() {
		return parameterValue(mogram, "description", 2);
	}

	@Override
	public String url() {
		return parameterValue(mogram, "url", 2);
	}


	@Override
	public Scm scm() {
		final Mogram scm = TaraPsiUtil.componentOfType(mogram, "Scm");
		return (scm == null) ? null : new Scm() {
			final Mogram scmNode = scm;

			@Override
			public String url() {
				return scmNode == null ? null : parameterValue(scmNode, "url", 0);
			}

			@Override
			public String connection() {
				return scmNode == null ? null : parameterValue(scmNode, "connection", 1);
			}

			@Override
			public String developerConnection() {
				return connection();
			}

			@Override
			public String tag() {
				return name();
			}
		};
	}

	@Override
	public List<Developer> developers() {
		final List<Mogram> developers = TaraPsiUtil.componentsOfType(mogram, "Developer");
		return developers.stream().map(d -> new Developer() {
			@Override
			public String name() {
				return parameterValue(d, "name", 0);
			}

			@Override
			public String email() {
				return parameterValue(d, "email", 1);
			}

			@Override
			public String organization() {
				return parameterValue(d, "organization", 2);
			}

			@Override
			public String organizationUrl() {
				return parameterValue(d, "organizationUrl", 3);
			}
		}).collect(toList());
	}

	@Override
	public List<Configuration.Server> servers() {
		return componentsOfType(mogram, "Server").stream().map(n -> new LegioServer((TaraMogram) n)).collect(Collectors.toList());
	}

	@Override
	public List<Configuration.Repository> repositories() {
		return componentsOfType(mogram, "Repository").stream().
				map(MogramContainer::components).
				flatMap(Collection::stream).
				map(this::repository).
				collect(Collectors.toList());
	}

	private Configuration.Repository repository(Mogram r) {
		if (((TaraMogramImpl) r).simpleType().equals("Release"))
			return new LegioRepository.LegioReleaseRepository(null, (TaraMogram) r);
		if (((TaraMogramImpl) r).simpleType().equals("Snapshot"))
			return new LegioRepository.LegioSnapshotRepository(null, (TaraMogram) r);
		return null;
	}


	@NotNull
	public ProjectLegioConfiguration root() {
		return root;
	}

	@Override
	public ProjectConfiguration.ConfigurationNode owner() {
		return null;
	}
}
