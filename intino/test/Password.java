import io.intino.plugin.IntinoException;
import io.intino.plugin.project.configuration.Version;
import org.junit.Test;

public class Password {
	public static final String PASSPHRASE = "ZZZZ";


	@Test
	public void name() {
		try {
			new Version("1.0.0-SNAPSHOT");
			new Version("1.0.0");
		} catch (IntinoException e) {
			e.printStackTrace();
		}
	}
}
