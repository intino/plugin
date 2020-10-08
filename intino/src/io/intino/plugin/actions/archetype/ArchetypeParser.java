package io.intino.plugin.actions.archetype;

import io.intino.plugin.IntinoException;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static org.antlr.v4.runtime.CharStreams.fromString;

public class ArchetypeParser {
	private String text;

	public ArchetypeParser(File archetypeFile) {
		try {
			this.text = Files.readString(archetypeFile.toPath(), Charset.defaultCharset());
		} catch (IOException e) {
			text = "";
		}
	}

	public ArchetypeParser(String text) {
		this.text = text;
	}

	public ArchetypeGrammar.RootContext parse() throws IntinoException {
		try {
			ArchetypeLexer lexer = new ArchetypeLexer(fromString(text.trim()));
			lexer.reset();
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ArchetypeGrammar grammar = new ArchetypeGrammar(tokens);
			grammar.setErrorHandler(new ArchetypeErrorStrategy());
			grammar.addErrorListener(new GrammarErrorListener());
			return grammar.root();
		} catch (RuntimeException e) {
			throw new IntinoException(e.getMessage());
		}

	}
}
