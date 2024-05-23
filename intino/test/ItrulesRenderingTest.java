import io.intino.itrules.TemplateReader;
import io.intino.itrules.parser.ITRulesSyntaxError;
import io.intino.itrules.template.Template;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ItrulesRenderingTest {

	@Test
	public void shouldRenderTemplate() throws FileNotFoundException, ITRulesSyntaxError {
		File file = new File("/Users/oroncal/workspace/infrastructure/intino-plugin/intino/src/io/intino/plugin/build/maven/Pom.itr");
		Template read = new TemplateReader(new FileInputStream(file)).read();
	}
}
