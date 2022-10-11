package io.intino.plugin.codeinsight.annotators;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.semantics.errorcollector.SemanticNotification.Level;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;

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
		AnnotationBuilder builder;
		for (Map.Entry<PsiElement, AnnotateAndFix> entry : annotations.entrySet()) {
			switch (entry.getValue().level()) {
				case INFO:
					builder = holder.newAnnotation(HighlightSeverity.INFORMATION, entry.getValue().message()).range(entry.getKey());
					break;
				case WARNING:
					builder = holder.newAnnotation(HighlightSeverity.WARNING, entry.getValue().message()).range(entry.getKey());
					break;
				case INSTANCE:
					builder = createDeclarationAnnotation(entry.getKey(), entry.getValue().message());
					break;
				default:
					builder = holder.newAnnotation(HighlightSeverity.ERROR, entry.getValue().message()).range(entry.getKey());
					break;
			}
			if (entry.getValue().textAttributes() != null) builder.textAttributes(entry.getValue().attributes);
			for (IntentionAction action : entry.getValue().actions()) {
				builder.newFix(action).range(entry.getKey().getTextRange()).registerFix();
			}
			builder.create();
		}
	}

	@SuppressWarnings("deprecation")
	private AnnotationBuilder createDeclarationAnnotation(PsiElement node, String message) {
		AnnotationBuilder builder = holder.newAnnotation(HighlightSeverity.WARNING, message).range(node);
		TextAttributesKey root = createTextAttributesKey("node_declaration", new TextAttributes(null, null, null, null, Font.ITALIC));
		return builder.textAttributes(root);
	}

	public static class AnnotateAndFix {
		private final Level level;
		private final String message;
		private IntentionAction[] actions;
		private final TextAttributesKey attributes;

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