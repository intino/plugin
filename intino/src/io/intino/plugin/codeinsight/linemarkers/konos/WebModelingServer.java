package io.intino.plugin.codeinsight.linemarkers.konos;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.builtInWebServer.BuiltInServerOptions;
import org.jetbrains.ide.RestService;
import org.jetbrains.io.Responses;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class WebModelingServer extends RestService {
	private static final Logger logger = Logger.getInstance(WebModelingServer.class);
	private static Map<String, File> processes = new HashMap<>();
	private static String template;

	static {
		try {
			template = IOUtils.toString(WebModelingServer.class.getResourceAsStream("/process_modeler.html"), StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	static void open(String processId, Module module, File file) {
		try {
			BrowserUtil.browse(new URL("http://localhost:" + BuiltInServerOptions.getInstance().getEffectiveBuiltInServerPort() + "/process?process=" + processId + "&file=" + file.getAbsolutePath()));
		} catch (MalformedURLException e) {
			logger.error(e);
		}
	}

	@NotNull
	@Override
	protected String getServiceName() {
		return "process-modeler";
	}

	@Override
	protected boolean isHostTrusted(@NotNull FullHttpRequest request, @NotNull QueryStringDecoder urlDecoder) {
		return true;
	}

	@Override
	protected boolean isMethodSupported(@NotNull HttpMethod method) {
		return method == HttpMethod.GET || method == HttpMethod.POST;
	}

	@Override
	public boolean isSupported(@NotNull FullHttpRequest request) {
		String path = path(request);
		return path.equalsIgnoreCase("/process") || path.equalsIgnoreCase("/process/closed");
	}

	private String path(FullHttpRequest request) {
		return request.uri().split("\\?")[0];
	}

	@Nullable
	@Override
	public String execute(@NotNull QueryStringDecoder decoder, @NotNull FullHttpRequest req, @NotNull ChannelHandlerContext context) throws IOException {
		Map<String, List<String>> parameters = decoder.parameters();
		String process = process(parameters);
		if (process == null) return null;
		if (decoder.path().endsWith("/process")) {
			if (!processes.containsKey(process) && file(parameters) != null) add(process, file(parameters));
			if (req.method().name().equalsIgnoreCase("GET")) openDiagram(process, req, context);
			else {
				saveDiagram(process, req);
//				reload()
			}
		} else remove(process);
		return null;
	}

	private File file(Map<String, List<String>> parameters) {
		if (!parameters.containsKey("file")) return null;
		return new File(parameters.get("file").get(0));
	}

	private String process(Map<String, List<String>> parameters) {
		if (!parameters.containsKey("process")) return null;
		return parameters.get("process").get(0);
	}

	public void add(String process, File source) {
		processes.put(process, source);
	}

	private void remove(String process) {
		processes.remove(process);
	}

	private void openDiagram(String process, FullHttpRequest req, ChannelHandlerContext context) throws IOException {
		File file = processes.get(process);
		String diagram = file.exists() ? new String(Files.readAllBytes(file.toPath())).replace("\n", "") : "";
		String html = template.replace("$diagram", diagram).replace("$process", process);
		Responses.send(Responses.response("text/html", Unpooled.copiedBuffer(html, StandardCharsets.UTF_8)).setProtocolVersion(req.protocolVersion()).setStatus(HttpResponseStatus.OK), context.channel(), req);
	}

	private void saveDiagram(String process, FullHttpRequest req) {
		try {
			ByteBuf buf = req.content();
			byte[] bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			String xml = new String(bytes, StandardCharsets.UTF_8);
			File file = processes.get(process);
			if (file != null) {
				if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
				Files.write(file.toPath(), xml.getBytes(), CREATE, TRUNCATE_EXISTING);
			}
		} catch (IOException e) {
			logger.error(e);
		}
	}
}
