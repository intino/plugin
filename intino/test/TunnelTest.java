import com.intellij.openapi.diagnostic.Logger;
import io.intino.konos.alexandria.schema.Resource;
import io.intino.konos.restful.RestfulApi;
import io.intino.konos.restful.exceptions.RestfulFailure;
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
import org.junit.Test;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class TunnelTest {
	private static final Logger logger = Logger.getInstance(TunnelTest.class);


	@Test
	public void testBitbucket() throws Exception {
		final URL url = url();
		HttpPost post = new HttpPost(url.toURI());
		post.addHeader("Authorization", "Basic b2N0YXZpb3JvbmNhbDpxTXptMjgzeHR1RkJValNzVURZYQ==");
		post.setEntity(multipartEntityOf(resource()));
		final RestfulApi.Response response = executeMethod(url, post);
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
		final FormBodyPart part = FormBodyPartBuilder.create(resource.id(), new InputStreamBody(resource.data(), ContentType.create(resource.contentType()), resource.id())).build();
		part.getHeader().setField(new MinimalField("Content-Type", "multipart/form-data"));
		builder.addPart(part);
	}

	@NotNull
	private Resource resource() throws FileNotFoundException {
		return new Resource("files").data(new FileInputStream(jar()));
	}

	private File jar() {
		return new File("/Users/oroncal/.m2/repository/io/intino/cesar/2.0.1/cesar-2.0.1.jar");
	}

	@NotNull
	private URL url() {
		try {
			return new URL("https://api.bitbucket.org/2.0/repositories/intino/cesar/downloads");
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
}
