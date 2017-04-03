import com.jcraft.jsch.Channel;
import io.intino.plugin.deploy.SOCKSTunnel;

public class TunnelTest {

	public static void main(String[] args) {

		final SOCKSTunnel tunnel = new SOCKSTunnel("root", Password.PASSPHRASE, "proxy.server.monentia.es", 4037, 7776);
		final Channel channel = tunnel.connect();
	}
}
