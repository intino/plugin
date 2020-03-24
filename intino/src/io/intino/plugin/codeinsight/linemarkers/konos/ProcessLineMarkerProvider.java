package io.intino.plugin.codeinsight.linemarkers.konos;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.JavaLineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.LineMarkerNavigator;
import com.intellij.codeInsight.daemon.impl.MarkerType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.file.konos.KonosFileType;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class ProcessLineMarkerProvider extends JavaLineMarkerProvider {
	private static final Logger logger = Logger.getInstance(ProcessLineMarkerProvider.class);
	private final MarkerType markerType = new MarkerType("ProcessEditor", element -> {
		if (!isProcessElement(element)) return null;
		InputStream stream = this.getClass().getResourceAsStream("/process_modeler.html");
		if (stream == null) return null;
		return "Edit process...";
	}, new LineMarkerNavigator() {
		@Override
		public void browse(MouseEvent e, PsiElement element) {
			browseProcess(element);
		}
	});


	@Override
	public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
		if (!(element instanceof Node)) return super.getLineMarkerInfo(element);
		if (isProcessElement(element)) {
			final Icon icon = IntinoIcons.BOX_PROCESS;
			final MarkerType type = markerType;
			return new LineMarkerInfo<>(leafOf(element), element.getTextRange(), icon, type.getTooltip(),
					type.getNavigationHandler(), GutterIconRenderer.Alignment.LEFT);
		} else return super.getLineMarkerInfo(element);
	}

	public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
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
		Node node = node(element);
		if (node == null) return "process.bpmn";
		if (node.parameters().isEmpty()) return node.name();
		return node.parameters().get(0).values().get(0).toString();
	}

	private void updateWebServer(String processId, Module module, File file) {
		WebModelingServer.open(processId, module, file);
	}

	private String processId(PsiElement element) {
		Module module = ModuleProvider.moduleOf(element);
		return module.getProject().getName() + "-" + module.getName() + "-" + node(element).name();
	}

	private PsiElement leafOf(@NotNull PsiElement element) {
		PsiElement leaf = element;
		while (leaf.getFirstChild() != null) leaf = leaf.getFirstChild();
		return leaf;
	}

	private boolean isProcessElement(PsiElement e) {
		Node node = node(e);
		return e.getContainingFile().getFileType().equals(KonosFileType.instance()) && node != null &&
				("Process".equals(node.type()) || "Workflow.Process".equals(node.type()));
	}

	@Nullable
	private Node node(PsiElement e) {
		return e instanceof Node ? (Node) e : TaraPsiUtil.getContainerNodeOf(e);
	}
}