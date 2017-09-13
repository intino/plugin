package io.intino.plugin.build;

import com.intellij.openapi.diagnostic.Logger;
import com.jcabi.aether.Aether;
import io.intino.konos.Resource;
import io.intino.konos.restful.RestfulApi;
import io.intino.konos.restful.exceptions.RestfulFailure;
import io.intino.legio.graph.Artifact.Distribution.OnBitbucket;
import io.intino.plugin.project.LegioConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.*;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

public class BitbucketDeployer {
	private static final Logger logger = Logger.getInstance(BitbucketDeployer.class);
	private final OnBitbucket bitbucket;
	private File jar;

	public BitbucketDeployer(LegioConfiguration configuration) {
		this.bitbucket = configuration.artifact().distribution().onBitbucket();
		this.jar = find(configuration.artifact().groupId() + ":" + configuration.artifact().name$() + ":" + configuration.artifact().version());
	}

	public void execute() {
		if (jar == null) return;
		final URL url = url();
		try {
			HttpPost post = new HttpPost(url.toURI());
			post.addHeader("Authorization", "Basic b2N0YXZpb3JvbmNhbDpxTXptMjgzeHR1RkJValNzVURZYQ=="); //octavioroncal:qMzm283xtuFBUjSsUDYa
			post.setEntity(multipartEntityOf(resource()));
			final RestfulApi.Response response = executeMethod(url, post);
			System.out.println(response.content());
		} catch (URISyntaxException | FileNotFoundException | RestfulFailure e) {
			logger.error(e.getMessage(), e);
		}
	}

	private HttpEntity multipartEntityOf(Resource resource) throws RestfulFailure {
		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().
				setContentType(ContentType.MULTIPART_FORM_DATA).
				setMode(HttpMultipartMode.BROWSER_COMPATIBLE).

				setCharset(Charset.forName("UTF-8"));
		addContent(entityBuilder, resource);
		return entityBuilder.build();
	}

	private RestfulApi.Response executeMethod(URL url, HttpRequestBase method) throws RestfulFailure {
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

	private void addContent(MultipartEntityBuilder builder, Resource resource) throws RestfulFailure {
		final FormBodyPart part = FormBodyPartBuilder.create(resource.name(), new InputStreamBody(resource.content(), ContentType.create(resource.contentType()), resource.fileName())).build();
		part.getHeader().setField(new MinimalField("Content-Type", "multipart/form-data"));
		builder.addPart(part);
	}

	@NotNull
	private Resource resource() throws FileNotFoundException {
		return new Resource("files", jar.getName(), "application/java-archive", new FileInputStream(jar));
	}

	private URL url() {
		try {
			return new URL("https://api.bitbucket.org/2.0/repositories/" + bitbucket.owner() + "/" + bitbucket.slugName() + "/downloads");
		} catch (MalformedURLException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private RestfulApi.Response responseOf(HttpResponse response) {
		return new RestfulApi.Response() {

			@Override
			public String content() {
				try {
					if (response == null)
						return null;

					return stringContentOf(response.getEntity().getContent());
				} catch (IOException e) {
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
					e.printStackTrace();
				} finally {
					if (buffer != null) {
						try {
							buffer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				return sb.toString();
			}
		};
	}

	private File find(String artifact) {
		Aether aether = new Aether(Collections.emptyList(), new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository"));
		try {
			final List<Artifact> resolve = aether.resolve(new DefaultArtifact(artifact.toLowerCase()), JavaScopes.COMPILE, (node, parents) -> true);
			return resolve.isEmpty() ? null : resolve.get(0).getFile();
		} catch (DependencyResolutionException e) {
			return null;
		}
	}
}
