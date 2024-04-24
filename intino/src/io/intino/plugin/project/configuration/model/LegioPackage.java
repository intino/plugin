package io.intino.plugin.project.configuration.model;

import com.intellij.psi.PsiFile;
import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraFacetApply;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.TaraFacetApplyImpl;
import io.intino.plugin.lang.psi.impl.TaraMogramImpl;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Facet;
import io.intino.tara.language.model.Mogram;

import java.util.List;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.*;

public class LegioPackage implements Configuration.Artifact.Package {
	private final LegioArtifact artifact;
	private final TaraMogram mogram;

	public LegioPackage(LegioArtifact artifact, TaraMogram mogram) {
		this.artifact = artifact;
		this.mogram = mogram;
	}

	@Override
	public Mode mode() {
		String mode = read(() -> parameterValue(mogram, "mode", 0));
		return mode == null ? Mode.ModulesAndLibrariesLinkedByManifest : Mode.valueOf(mode);
	}

	@Override
	public boolean isRunnable() {
		return mainClass() != null;
	}

	@Override
	public boolean createMavenPom() {
		return Boolean.parseBoolean(read(() -> parameterValue(mogram, "createMavenPom", 1)));
	}

	@Override
	public boolean attachSources() {
		return Boolean.parseBoolean(read(() -> parameterValue(mogram, "attachSources", 3)));
	}

	@Override
	public List<String> mavenPlugins() {
		List<Mogram> mavenPlugins = TaraPsiUtil.componentsOfType(mogram, "MavenPlugin");
		return mavenPlugins.stream().map(n -> read(() -> parameterValue(n, "code", 0).replace("=", ""))).toList();
	}

	@Override
	public boolean attachDoc() {
		String attachDoc = read(() -> parameterValue(mogram, "attachDoc", 4));
		return Boolean.parseBoolean(attachDoc);
	}

	@Override
	public boolean includeTests() {
		return Boolean.parseBoolean(read(() -> parameterValue(mogram, "includeTests", 5)));
	}

	@Override
	public boolean signArtifactWithGpg() {
		return Boolean.parseBoolean(read(() -> parameterValue(mogram, "signArtifactWithGpg", 6)));
	}

	@Override
	public String classpathPrefix() {
		return read(() -> parameterValue(mogram, "classpathPrefix", 7));
	}

	@Override
	public String finalName() {
		return read(() -> parameterValue(mogram, "finalName", 8));
	}

	@Override
	public String defaultJVMOptions() {
		return read(() -> parameterValue(mogram, "defaultJVMOptions", 9));
	}

	@Override
	public String mainClass() {
		if (mogram == null) return null;
		List<Facet> aspects = mogram.appliedFacets();
		if (aspects == null || aspects.isEmpty()) return null;
		Facet runnable = aspects.stream().filter(a -> a.type().contains("Runnable")).findFirst().orElse(null);
		String mainClass = parameterValue(runnable, "mainClass", 0);
		return mainClass == null ? read(() -> parameterValue(mogram, "mainClass")) : mainClass;
	}

	public void mainClass(String qualifiedName) {
		PsiFile containingFile = mogram.getContainingFile();
		writeCommandAction(mogram.getProject(), containingFile).run(() -> {
			if (this.mogram.appliedFacets().isEmpty()) ((TaraMogramImpl) this.mogram).applyFacet("Runnable");
		});
		writeCommandAction(mogram.getProject(), containingFile).run(() -> {
			TaraFacetApply runnable = (TaraFacetApply) this.mogram.appliedFacets().get(0);
			((TaraFacetApplyImpl) runnable).addParameter("mainClass", 0, List.of(qualifiedName));
		});
	}

	@Override
	public MacOs macOsConfiguration() {
		//TODO
		return null;
	}

	@Override
	public Windows windowsConfiguration() {//TODO
		return null;
	}

	@Override
	public LinuxService linuxService() {
		if (mogram == null) return null;
		List<Facet> aspects = mogram.appliedFacets();
		if (aspects == null || aspects.isEmpty()) return null;
		Facet linuxService = aspects.stream().filter(a -> a.type().contains("LinuxService")).findFirst().orElse(null);
		if (linuxService == null) return null;
		return new LegioPackageAsLinuxService(linuxService, artifact);
	}

	public static class LegioPackageAsLinuxService implements LinuxService {
		private final Facet node;
		private final Configuration.Artifact artifact;

		public LegioPackageAsLinuxService(Facet node, Configuration.Artifact artifact) {
			this.node = node;
			this.artifact = artifact;
		}

		@Override
		public String user() {
			return read(() -> parameterValue(node, "user", 0));
		}

		@Override
		public Configuration.RunConfiguration runConfiguration() {
			Mogram runConfiguration = read(() -> referenceParameterValue(node.parameters(), "runConfiguration", 1));
			if (runConfiguration == null) return null;
			return new LegioRunConfiguration((LegioArtifact) artifact, runConfiguration);
		}

		@Override
		public boolean restartOnFailure() {
			return Boolean.parseBoolean(read(() -> parameterValue(node, "restartOnFailure", 2)));
		}

		@Override
		public int managementPort() {
			return Integer.parseInt(read(() -> parameterValue(node, "managementPort", 3)));
		}
	}
}
