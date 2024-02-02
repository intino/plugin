package io.intino.plugin.codeinsight.annotators;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;

import static com.intellij.lang.annotation.HighlightSeverity.*;
import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public abstract class TaraAnnotator implements Annotator {

	public void analyzeAndAnnotate(AnnotationHolder holder, TaraAnalyzer analyzer) {
		analyzer.analyze();
		annotateAndFix(holder, analyzer.results());
	}

	public void annotateAndFix(AnnotationHolder holder, Map<PsiElement, AnnotateAndFix> annotations) {
		for (Map.Entry<PsiElement, AnnotateAndFix> entry : annotations.entrySet()) {
			final String message = entry.getValue().message();
			AnnotationBuilder builder = switch (entry.getValue().level()) {
				case INFO -> holder.newAnnotation(INFORMATION, message).range(entry.getKey());
				case WARNING -> holder.newAnnotation(WARNING, message).range(entry.getKey());
				case INSTANCE -> createDeclarationAnnotation(holder, entry.getKey(), message);
				default -> holder.newAnnotation(ERROR, message).range(entry.getKey());
			};
			if (entry.getValue().textAttributes() != null) builder.textAttributes(entry.getValue().attributes);
			for (IntentionAction action : entry.getValue().actions())
				builder.newFix(action).range(entry.getKey().getTextRange()).registerFix();
			builder.create();
		}
	}

	private AnnotationBuilder createDeclarationAnnotation(AnnotationHolder holder, PsiElement node, String message) {
		AnnotationBuilder builder = holder.newAnnotation(WARNING, message).range(node);
		TextAttributesKey key = createTextAttributesKey("node_instance");
		key.getDefaultAttributes().setFontType(Font.ITALIC);
		key.getDefaultAttributes().setEffectType(null);
		key.getDefaultAttributes().setEffectColor(null);
		return builder.textAttributes(key);
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