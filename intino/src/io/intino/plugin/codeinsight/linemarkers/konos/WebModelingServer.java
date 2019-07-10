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
			server = createServer();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	static WebModelingServer instance() {
		if (instance != null) return instance;
		return instance = new WebModelingServer();
	}

	private HttpServer createServer() throws IOException {
		return HttpServer.create(new InetSocketAddress(port), 0);
	}

	public void add(String processId, File source) {
		processes.put(processId, source);
		restartServer();
	}

	void open(String processId) {
		try {
			BrowserUtil.browse(new URL("http://localhost:" + port + "/" + processId));
		} catch (MalformedURLException e) {
			logger.error(e);
		}
	}

	private void restartServer() {
		for (String process : processes.keySet()) {
			server.createContext("/" + process, req -> {
				if (req.getRequestMethod().equals("GET")) openDiagram(process, req);
				else if (req.getRequestMethod().equals("POST")) saveDiagram(process, req);
			});
			server.createContext("/" + process + "/closed", req -> removeAndStop(process));
		}
		server.start();
	}

	private void removeAndStop(String process) {
		processes.remove(process);
		if (processes.isEmpty()) {
			server.stop(0);
			try {
				server = createServer();
			} catch (IOException e) {
				logger.error(e);
			}
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
		String html = template.replace("$diagram", new String(Files.readAllBytes(file.toPath())).replace("\n", "\\n")).replace("$process", process);
		req.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
		req.sendResponseHeaders(200, html.getBytes().length);
		OutputStream out = req.getResponseBody();
		out.write(html.getBytes());
		out.close();
	}
}
