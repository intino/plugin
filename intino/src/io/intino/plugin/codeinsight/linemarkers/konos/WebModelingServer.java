package io.intino.plugin.codeinsight.linemarkers.konos;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class WebModelingServer {
	private static final Logger logger = Logger.getInstance(WebModelingServer.class);
	private static WebModelingServer instance;
	private final int port = 9999;
	private Map<String, File> processes;
	private HttpServer server;
	private String template;

	private WebModelingServer() {
		processes = new HashMap<>();
		try {
			template = IOUtils.toString(this.getClass().getResourceAsStream("/process_modeler.html"), "UTF-8");
			createServer();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	static WebModelingServer instance() {
		if (instance != null) return instance;
		return instance = new WebModelingServer();
	}

	private void createServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/process", req -> {
			if (req.getRequestMethod().equals("GET")) openDiagram(process(req), req);
			if (req.getRequestMethod().equals("POST")) saveDiagram(process(req), req);
		});
		server.createContext("/process/closed", req -> remove(process(req)));
		server.start();
	}

	private String process(HttpExchange req) {
		String[] split = req.getRequestURI().toString().split("\\?");
		return split[split.length - 1];
	}

	public void add(String process, File source) {
		processes.put(process, source);

	}

	void open(String processId) {
		try {
			BrowserUtil.browse(new URL("http://localhost:" + port + "/process?" + processId));
		} catch (MalformedURLException e) {
			logger.error(e);
		}
	}

	private void remove(String process) {
		processes.remove(process);
		if (processes.isEmpty()) restartServer();
	}

	private void restartServer() {
		server.stop(0);
		try {
			createServer();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private void saveDiagram(String process, HttpExchange req) {
		try {
			InputStream requestBody = req.getRequestBody();
			String xml = new String(IOUtils.readFully(requestBody, requestBody.available()), StandardCharsets.UTF_8);
			File file = processes.get(process);
			Files.write(file.toPath(), xml.getBytes(), CREATE, TRUNCATE_EXISTING);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private void openDiagram(String process, HttpExchange req) throws IOException {
		File file = processes.get(process);
		String diagram = file.exists() ? new String(Files.readAllBytes(file.toPath())).replace("\n", "\\n") : "";
		String html = template.replace("$diagram", diagram).replace("$process", process);
		req.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
		req.sendResponseHeaders(200, html.getBytes().length);
		OutputStream out = req.getResponseBody();
		out.write(html.getBytes());
		out.close();
	}
}
