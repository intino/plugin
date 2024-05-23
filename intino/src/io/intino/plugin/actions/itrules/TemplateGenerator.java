package io.intino.plugin.actions.itrules;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import io.intino.itrules.TemplateReader;
import io.intino.itrules.parser.ITRulesSyntaxError;
import io.intino.itrules.serializer.TemplateSerializer;
import io.intino.itrules.template.Template;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class TemplateGenerator extends Task.Modal {
	public static final Logger LOG = Logger.getInstance("RunItrulesOnRulesFile");
	private final Module module;
	private final File destination;
	private final String aPackage;
	private final VirtualFile rulesFile;
	private ProgressIndicator indicator;

	public TemplateGenerator(VirtualFile rulesFile, Module module, String title, File destination, String aPackage) {
		super(module.getProject(), title, true);
		this.rulesFile = rulesFile;
		this.module = module;
		this.destination = destination;
		this.aPackage = aPackage;
	}

	public void run(@NotNull ProgressIndicator indicator) {
		this.indicator = indicator;
		this.indicator.setIndeterminate(true);
		if (this.rulesFile == null) return;
		LOG.info("itrules(\"" + this.rulesFile.getPath() + "\")");
		task();
	}

	private void task() {
		try {
			toJava(rules());
			addFileToEncodings();
		} catch (Throwable e) {
			error(this.module.getProject(), e.getMessage());
			indicator.setText(e.getMessage());
			indicator.cancel();
		}
	}

	@NotNull
	private Template rules() throws IOException, ITRulesSyntaxError {
		return new TemplateReader(this.rulesFile.getInputStream()).read(rulesFile.getCharset());
	}

	private void toJava(Template template) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(this.destination), StandardCharsets.UTF_8);
		String content = new TemplateSerializer(simpleFileName(), aPackage, Locale.ENGLISH, Template.Configuration.LineSeparator.LF).toJava(template);
		writer.write(content);
		writer.close();
	}

	private void addFileToEncodings() {
		final VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(destination);
		new Runnable() {
			public void run() {
				EncodingManager.getInstance().setEncoding(virtualFile, StandardCharsets.UTF_8);
			}
		};
	}

	@NotNull
	private String simpleFileName() {
		return rulesFile.getName().substring(0, rulesFile.getName().lastIndexOf("."));
	}

	private void error(Project project, String message) {
		Notifications.Bus.notify(new Notification("Intino", "Error generating template", message, NotificationType.ERROR), project);
	}
}

