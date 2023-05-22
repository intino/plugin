package io.intino.plugin.build.linuxservice;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class ServiceTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("service"))).output(literal("[Unit]\nDescription=")).output(mark("artifact")).output(literal(" service\nAfter=syslog.target\nAfter=network.target\n\n[Service]\nType=forking\nUMask=077\nEnvironmentFile=/etc/sysconfig/")).output(mark("artifact")).output(literal("\nExecStart=/usr/sbin/daemonize -a -u $JAVA_USER -o $JAVA_STDOUT -e $JAVA_STDERR -c $JAVA_APPDIR $JAVA_BIN $ARG1 $ARG2 $ARG3\nExecStop=/bin/kill -TERM ")).output(mark("MAINPID")).output(literal("\nTimeoutSec=300\n")).output(expression().output(mark("restart", "firstUpperCase")).output(literal("=on-failure"))).output(literal("\n\n[Install]\nWantedBy=multi-user.target")),
			rule().condition((type("sysconfig"))).output(literal("# Configz for java service\n\nJAVA_USER=\"")).output(mark("user")).output(literal("\"\nJAVA_STDOUT=\"/var/log/")).output(mark("user")).output(literal("/")).output(mark("artifact", "lowercase")).output(literal(".log\"\nJAVA_STDERR=\"/var/log/")).output(mark("user")).output(literal("/")).output(mark("artifact", "lowercase")).output(literal(".log\"\nJAVA_BIN=\"/usr/bin/java\"\nJAVA_APPDIR=\"/opt/")).output(mark("artifact")).output(literal("\"\nARG1=\"-Dfile.encoding=UTF-8 --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED\"\nARG2=\"")).output(expression().output(literal("-Xms")).output(mark("minMemory")).output(literal("m "))).output(expression().output(literal("-Xmx")).output(mark("maxMemory")).output(literal("m "))).output(literal("-Djava.io.tmpdir=/home/")).output(mark("user")).output(literal("/tmp -Dcom.sun.management.jmxremote -Djava.rmi.server.hostname=127.0.0.1 -Dcom.sun.management.jmxremote.port=")).output(mark("managementPort")).output(literal(" -Dcom.sun.management.jmxremote.rmi.port=")).output(mark("managementPort")).output(literal(" -Dcom.sun.management.jmxremote.local.only=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -XX:+StartAttachListener\"\nARG3=\"--class-path /opt/")).output(mark("artifact", "lowercase")).output(literal("/")).output(mark("artifact", "lowercase")).output(literal("-")).output(mark("version")).output(literal(".jar")).output(expression().output(mark("dependencies"))).output(literal(" ")).output(mark("mainClass")).output(literal(" ")).output(mark("parameter").multiple(" ")).output(literal("\"")),
			rule().condition((trigger("parameter"))).output(mark("name")).output(literal("=")).output(mark("value")),
			rule().condition((trigger("dependencies"))).output(literal(":/opt/")).output(mark("artifact", "lowercase")).output(literal("/")).output(expression().output(mark("directory")).next(expression().output(literal("dependency")))).output(literal("/*"))
		);
	}
}