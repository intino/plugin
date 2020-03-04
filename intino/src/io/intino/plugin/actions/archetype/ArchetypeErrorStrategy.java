package io.intino.plugin.actions.archetype;

import org.antlr.v4.runtime.*;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class ArchetypeErrorStrategy implements ANTLRErrorStrategy {

	private static final Logger LOG = Logger.getGlobal();
	private static Token currentError;


	@Override
	public void reset(Parser recognizer) {

	}

	@Override
	public Token recoverInline(Parser recognizer) throws RecognitionException {
		reportError(recognizer, new InputMismatchException(recognizer));
		return null;
	}

	@Override
	public void recover(Parser recognizer, RecognitionException e) throws RecognitionException {

	}

	@Override
	public void sync(Parser recognizer) throws RecognitionException {

	}

	@Override
	public boolean inErrorRecoveryMode(Parser recognizer) {
		return false;
	}

	@Override
	public void reportMatch(Parser recognizer) {

	}

	@Override
	public void reportError(Parser recognizer, RecognitionException e) {
		throw new RuntimeException(printParameters(recognizer));
	}

	private String printParameters(Parser recognizer) {
		Token token = recognizer.getCurrentToken();
		if (currentError == token) return "";
		else currentError = token;
		String[] nameList = recognizer.getTokenNames();
		String message = message(recognizer, token, nameList);
		System.out.println(message);
		return message;
	}

	@NotNull
	private String message(Parser recognizer, Token token, String[] nameList) {
		return "Line: " + token.getLine() + "\n" +
				"Column: " + token.getCharPositionInLine() + "\n" +
				"Text Length: " + token.getText().length() + "\n" +
				(token.getType() > 0 ? "Token type: " + nameList[token.getType()] + "\n" : "") +
				"Expected tokens: " + recognizer.getExpectedTokens().toString(recognizer.getVocabulary()) + "\n" +
				"Text: " + token.getText().replace("\n", "\\n");
	}
}
