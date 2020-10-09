import io.intino.plugin.IntinoException;
import io.intino.plugin.archetype.lang.antlr.ArchetypeParser;
import org.junit.Test;

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
}
