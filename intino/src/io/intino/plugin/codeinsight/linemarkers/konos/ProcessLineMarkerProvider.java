package io.intino.plugin.codeinsight.linemarkers.konos;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.JavaLineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.LineMarkerNavigator;
import com.intellij.codeInsight.daemon.impl.MarkerType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.file.KonosFileType;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ProcessLineMarkerProvider extends JavaLineMarkerProvider {
	private static final Logger logger = Logger.getInstance(ProcessLineMarkerProvider.class);
	private final MarkerType markerType = new MarkerType("ProcessEditor", element -> {
		if (!isProcessElement(element)) return null;
		InputStream stream = this.getClass().getResourceAsStream("/process_modeler.html");
		if (stream == null) return null;
		close(stream);
		return "Edit process...";
	}, new LineMarkerNavigator() {
		@Override
		public void browse(MouseEvent e, PsiElement element) {
			browseProcess(element);
		}
	});


	@Override
	public LineMarkerInfo<?> getLineMarkerInfo(@NotNull final PsiElement element) {
		if (!(element instanceof Mogram)) return super.getLineMarkerInfo(element);
		if (isProcessElement(element)) {
			final MarkerType type = markerType;
			final PsiElement leaf = leafOf(element);
			return new LineMarkerInfo<>(leaf, element.getTextRange(), IntinoIcons.BOX_PROCESS, type.getTooltip(),
					type.getNavigationHandler(), GutterIconRenderer.Alignment.LEFT, leaf::getText);
		} else return super.getLineMarkerInfo(element);
	}


	@NotNull
	@Override
	public String getName() {
		return "Edit process";
	}

	@Nullable
	@Override
	public Icon getIcon() {
		return IntinoIcons.BOX_PROCESS;
	}

	private void browseProcess(PsiElement element) {
		InputStream stream = this.getClass().getResourceAsStream("/process_modeler.html");
		if (stream == null) return;
		if (DumbService.isDumb(element.getProject())) {
			DumbService.getInstance(element.getProject()).showDumbModeNotification("Navigation to process editor is not possible during index update");
			return;
		}
		updateWebServer(processId(element), ModuleProvider.moduleOf(element), new File(filePath(element)));
	}

	private String filePath(PsiElement element) {
		Mogram mogram = mogram(element);
		if (mogram == null) return "process.bpmn";
		return mogram.parameters().isEmpty() ?
				mogram.name() :
				mogram.parameters().get(0).values().get(0).toString();
	}

	private void updateWebServer(String processId, Module module, File file) {
		WebModelingServer.open(processId, module, file);
	}

	private String processId(PsiElement element) {
		Module module = ModuleProvider.moduleOf(element);
		return module.getProject().getName() + "-" + module.getName() + "-" + mogram(element).name();
	}

	private PsiElement leafOf(@NotNull PsiElement element) {
		PsiElement leaf = element;
		while (leaf.getFirstChild() != null) leaf = leaf.getFirstChild();
		return leaf;
	}

	private boolean isProcessElement(PsiElement e) {
		Mogram mogram = mogram(e);
		FileType fileType = e.getContainingFile().getFileType();
		return (fileType.equals(KonosFileType.instance()) || fileType.equals(TaraFileType.instance())) && mogram != null &&
			   ("Process".equals(mogram.type()) || "Workflow.Process".equals(mogram.type()));
	}

	@Nullable
	private Mogram mogram(PsiElement e) {
		return e instanceof Mogram ? (Mogram) e : TaraPsiUtil.getContainerNodeOf(e);
	}

	private void close(InputStream stream) {
		try {
			stream.close();
		} catch (IOException e) {
			logger.error(e);
		}
	}
}