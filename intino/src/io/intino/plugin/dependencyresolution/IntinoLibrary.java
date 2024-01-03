package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTable.ModifiableModel;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class IntinoLibrary {
	public static final String INTINO = "Intino: ";
	private final LibraryTable table;
	private final ModifiableModel modifiableModel;

	public IntinoLibrary(Project project) {
		this.table = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
		this.modifiableModel = table.getModifiableModel();

	}

	public static String libraryLabelOf(Artifact artifact) {
		return INTINO + libraryIdentifierOf(artifact);
	}

	public static String libraryIdentifierOf(Artifact artifact) {
		return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
	}

	public static Artifact artifactOf(Library library, String scope) {
		return new DefaultArtifact(Objects.requireNonNull(library.getName()).replace(INTINO, "") + ":" + scope);
	}

	List<Library> libraries() {
		return Arrays.asList(table.getLibraries());
	}

	ModifiableModel model() {
		return modifiableModel;
	}

	public Library findLibrary(Artifact artifact) {
		String label = libraryLabelOf(artifact);
		return Arrays.stream(table.getLibraries())
				.filter(library -> label.equals(library.getName()))
				.findFirst()
				.orElse(null);
	}
}
