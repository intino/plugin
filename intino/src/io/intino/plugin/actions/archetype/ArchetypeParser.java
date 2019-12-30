package io.intino.plugin.actions.archetype;

import io.intino.alexandria.logger.Logger;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static org.antlr.v4.runtime.CharStreams.fromString;

public class ArchetypeParser {
	private final File archetypeFile;

	public ArchetypeParser(File archetypeFile) {
		this.archetypeFile = archetypeFile;
	}


	public ArchetypeGrammar.RootContext parse() {
		try {
			ArchetypeLexer lexer = new ArchetypeLexer(fromString(Files.readString(archetypeFile.toPath(), Charset.defaultCharset()).trim()));
			lexer.reset();
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ArchetypeGrammar grammar = new ArchetypeGrammar(tokens);
			grammar.setErrorHandler(new ArchetypeErrorStrategy());
			grammar.addErrorListener(new GrammarErrorListener());
			return grammar.root();
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}
}
