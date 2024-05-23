import io.intino.plugin.IntinoException;
import io.intino.plugin.archetype.lang.antlr.ArchetypeParser;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ArchetypeTest {


	@Test
	public void checkArchetypeParsing() {
		try {
			String TEST_1 = "+ repository in \"documentos\"\n" +
					"    - comprobantesfiscales\n" +
					"    - getComprobanteFiscal(periodo as timetag:month, id) in \"comprobantesfiscales/{periodo}/{id}.zip\"\n" +
					"- identities in \"cfe.zif\"\n" +
					"+ pacs\n" +
					"    + forms \"forms\"\n" +
					"        - infile\n" +
					"        - outfile\n" +
					"        - XMLS\n" +
					"        - logs";
			new ArchetypeParser(TEST_1).parse();
		} catch (IntinoException e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void name() throws IOException {
		final byte[] bytes = Files.readAllBytes(Path.of("/Users/oroncal/Downloads/save_black_24dp.svg"));
		System.out.println(new String(Base64.getMimeEncoder().encode((bytes))));
	}
}
