import io.intino.plugin.dependencyresolution.MavenDependencyResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.junit.Test;

import java.util.List;

public class DependencyResolverTest {

	@Test
	public void should_resolve() throws DependencyResolutionException {
		MavenDependencyResolver resolver = new MavenDependencyResolver( List.of());


		DefaultArtifact artifact = new DefaultArtifact("com.esotericsoftware:kryo:5.4.0");
		DependencyResult resolved = resolver.resolve(artifact, JavaScopes.COMPILE);
		for (ArtifactResult artifactResult : resolved.getArtifactResults()) {
			System.out.println(artifactResult.getArtifact().toString());
		}
	}
}
