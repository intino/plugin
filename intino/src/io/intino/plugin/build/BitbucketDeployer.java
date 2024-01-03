package io.intino.plugin.build;

import com.intellij.openapi.diagnostic.Logger;
import io.intino.Configuration;
import io.intino.alexandria.restaccessor.Response;
import io.intino.alexandria.restaccessor.exceptions.RestfulFailure;
import io.intino.plugin.dependencyresolution.MavenDependencyResolver;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.*;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.intino.plugin.project.Safe.safe;

public class BitbucketDeployer {
	private static final Logger logger = Logger.getInstance(BitbucketDeployer.class);
	private final Configuration.Distribution.BitBucketDistribution bitbucket;
	private final File jar;
	private final String bitbucketToken;

	public BitbucketDeployer(Configuration configuration, String bitbucketToken) {
		this.bitbucket = safe(() -> configuration.artifact().distribution().onBitbucket());
		this.jar = find(configuration.artifact().groupId() + ":" + configuration.artifact().name() + ":" + configuration.artifact().version());
		this.bitbucketToken = bitbucketToken;
	}

	public void execute() {
		if (jar == null) return;
		final URL url = url();
		try {
			HttpPost post = new HttpPost(url.toURI());
			post.addHeader("Authorization", "Basic " + bitbucketToken);
			post.setEntity(multipartEntityOf(jar));
			final Response response = executeMethod(url, post);
			System.out.println(response.content());
		} catch (URISyntaxException | RestfulFailure | FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private HttpEntity multipartEntityOf(File resource) throws FileNotFoundException {
		return MultipartEntityBuilder.create().
				setContentType(ContentType.MULTIPART_FORM_DATA).
				setMode(HttpMultipartMode.BROWSER_COMPATIBLE).
				setCharset(StandardCharsets.UTF_8).
				addPart(createContent(resource)).
				build();
	}

	private Response executeMethod(URL url, HttpRequestBase method) throws RestfulFailure {
		HttpResponse response;
		final CloseableHttpClient client = HttpClientBuilder.create().build();
		try {
			response = client.execute(method);
		} catch (IOException exception) {
			throw new RestfulFailure(exception.getMessage());
		}

		int status = response.getStatusLine().getStatusCode();
		if (status < 200 || status >= 300) {
			String errorMessage = response.containsHeader("error-message") ? response.getFirstHeader("error-message").getValue() : "";
			final String format = String.format("%s => %d - %s", url, status, response.getStatusLine().getReasonPhrase() + ". " + errorMessage);
			throw new RestfulFailure(String.valueOf(status), format);
		}
		return responseOf(response);
	}

	private FormBodyPart createContent(File resource) throws FileNotFoundException {
		final FormBodyPart part = FormBodyPartBuilder.create("files", new InputStreamBody(new FileInputStream(resource), ContentType.create("jar"), resource.getName())).build();
		part.getHeader().setField(new MinimalField("Content-Type", "multipart/form-data"));
		return part;
	}

	private URL url() {
		try {
			return new URL("https://api.bitbucket.org/2.0/repositories/" + bitbucket.owner() + "/" + bitbucket.slugName() + "/downloads");
		} catch (MalformedURLException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private Response responseOf(HttpResponse response) {
		return new Response() {

			@Override
			public int code() {
				return response.getStatusLine().getStatusCode();
			}

			@Override
			public String content() {
				try {
					if (response == null) return null;
					return stringContentOf(response.getEntity().getContent());
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					return null;
				}
			}

			@Override
			public Map<String, String> headers() {
				return Collections.emptyMap();
			}

			@Override
			public String contentType() {
				return null;
			}

			@Override
			public InputStream contentAsStream() {
				try {
					return response.getEntity().getContent();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					return null;
				}
			}

			private String stringContentOf(InputStream input) {
				BufferedReader buffer = null;
				StringBuilder sb = new StringBuilder();
				String line;

				try {
					buffer = new BufferedReader(new InputStreamReader(input));
					while ((line = buffer.readLine()) != null) sb.append(line);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				} finally {
					if (buffer != null) {
						try {
							buffer.close();
						} catch (IOException e) {
							logger.error(e.getMessage(), e);
						}
					}
				}

				return sb.toString();
			}
		};
	}

	private File find(String artifact) {
		MavenDependencyResolver resolver = new MavenDependencyResolver(Collections.emptyList());
		try {
			DependencyResult resolved = resolver.resolve(new DefaultArtifact(artifact.toLowerCase()), JavaScopes.COMPILE);
			List<Dependency> dependencies = MavenDependencyResolver.dependenciesFrom(resolved, false);
			return dependencies.get(0).getArtifact().getFile();
		} catch (DependencyResolutionException e) {
			return null;
		}
	}
}
