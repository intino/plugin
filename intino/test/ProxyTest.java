import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.config.hosts.HostConfigEntryResolver;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.forward.PortForwardingEventListener;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.TimeUnit;

public class ProxyTest {
	private static final File SSH_DIRECTORY = new File(System.getProperty("user.home") + File.separator + ".ssh");

	public static void main(String[] args) throws Exception {
		createNativeSession(null);
	}

	public static ClientSession createNativeSession(PortForwardingEventListener listener) throws Exception {
		SshClient client = SshClient.setUpDefaultClient();
		client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
		client.setHostConfigEntryResolver(HostConfigEntryResolver.EMPTY);
		client.setKeyPairProvider(KeyPairProvider.EMPTY_KEYPAIR_PROVIDER);
		PropertyResolverUtils.updateProperty(client, FactoryManager.WINDOW_SIZE, 2048);
		PropertyResolverUtils.updateProperty(client, FactoryManager.MAX_PACKET_SIZE, 256);
		client.setTcpipForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
		client.addPublicKeyIdentity(loadKeyPair(SSH_DIRECTORY.getAbsolutePath()));
		if (listener != null) client.addPortForwardingEventListener(listener);
		client.start();

		ClientSession session = client.connect("root", "proxy.monentia.es", 4037).verify(7L, TimeUnit.SECONDS).getSession();
		session.auth().verify(11L, TimeUnit.SECONDS);
		return session;
	}

	public static KeyPair loadKeyPair(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		File publicFile = new File(path, "id_rsa.pub");
		File privateFile = new File(path, "id_rsa");
		KeyFactory kf = KeyFactory.getInstance("RSA");
		final PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(Files.readAllBytes(publicFile.toPath())));
		final PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(privateFile.toPath())));
		return new KeyPair(publicKey, privateKey);
	}

}