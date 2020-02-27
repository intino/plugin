package io.intino.plugin.annotator;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.semantics.errorcollector.SemanticNotification.Level;
import io.intino.plugin.annotator.semanticanalizer.TaraAnalyzer;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public abstract class TaraAnnotator implements Annotator {

	protected AnnotationHolder holder = null;

	public void analyzeAndAnnotate(TaraAnalyzer analyzer) {
		analyzer.analyze();
		annotateAndFix(analyzer.results());
	}

	public void annotateAndFix(Map<PsiElement, AnnotateAndFix> annotations) {
		Annotation annotation;
		for (Map.Entry<PsiElement, AnnotateAndFix> entry : annotations.entrySet()) {
			switch (entry.getValue().level()) {
				case INFO:
					annotation = holder.createInfoAnnotation(entry.getKey().getNode(), entry.getValue().message());
					break;
				case WARNING:
					annotation = holder.createWarningAnnotation(entry.getKey().getNode(), entry.getValue().message());
					break;
				case INSTANCE:
					annotation = addDeclarationAnnotation(entry.getKey().getNode(), entry.getValue().message());
					break;
				default:
					annotation = holder.createErrorAnnotation(entry.getKey().getNode(), entry.getValue().message());
					break;
			}
			if (entry.getValue().textAttributes() != null) annotation.setTextAttributes(entry.getValue().attributes);
			for (IntentionAction action : entry.getValue().actions()) annotation.registerFix(action);
		}
	}

	@SuppressWarnings("deprecation")
	private Annotation addDeclarationAnnotation(ASTNode node, String message) {
		TextAttributesKey root = createTextAttributesKey("node_declaration", new TextAttributes(null, null, null, null, Font.ITALIC));
		final Annotation declaration = holder.createInfoAnnotation(node, message);
		declaration.setTextAttributes(root);
		return declaration;
	}

	public static class AnnotateAndFix {
		private Level level;
		private String message;
		private IntentionAction[] actions = IntentionAction.EMPTY_ARRAY;
		private TextAttributesKey attributes;

		public AnnotateAndFix(Level level, String message, IntentionAction... actions) {
			this(level, message, null, actions);
		}

		public AnnotateAndFix(Level level, String message, TextAttributesKey attributes, IntentionAction... actions) {
			this.level = level;
			this.message = message;
			this.attributes = attributes;
			this.actions = actions;
		}

		public String message() {
			return message;
		}

		public IntentionAction[] actions() {
			return Arrays.copyOf(actions, actions.length);
		}

		public Level level() {
			return level;
		}

		TextAttributesKey textAttributes() {
			return attributes;
		}

		public void setActions(IntentionAction[] actions) {
			this.actions = actions.clone();
		}

	}
}