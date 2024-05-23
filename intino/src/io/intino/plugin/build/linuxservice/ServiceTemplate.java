package io.intino.plugin.build.linuxservice;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.allTypes;
import static io.intino.itrules.template.condition.predicates.Predicates.trigger;
import static io.intino.itrules.template.outputs.Outputs.*;

public class ServiceTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("service")).output(literal("[Unit]\nDescription=")).output(placeholder("artifact")).output(literal(" service\nAfter=syslog.target\nAfter=network.target\n\n[Service]\nType=forking\nUMask=077\nEnvironmentFile=/etc/sysconfig/")).output(placeholder("artifact")).output(literal("\nExecStart=/usr/sbin/daemonize -a -u $JAVA_USER -o $JAVA_STDOUT -e $JAVA_STDERR -c $JAVA_APPDIR $JAVA_BIN $ARG1 $ARG2 $ARG3\nExecStop=/bin/kill -TERM ")).output(placeholder("MAINPID")).output(literal("\nTimeoutSec=300\n")).output(expression().output(placeholder("restart", "firstUpperCase")).output(literal("=on-failure"))).output(literal("\n\n[Install]\nWantedBy=multi-user.target")));
		rules.add(rule().condition(allTypes("sysconfig")).output(literal("# Configz for java service\n\nJAVA_USER=\"")).output(placeholder("user")).output(literal("\"\nJAVA_STDOUT=\"/var/log/")).output(placeholder("user")).output(literal("/")).output(placeholder("artifact", "lowercase")).output(literal(".log\"\nJAVA_STDERR=\"/var/log/")).output(placeholder("user")).output(literal("/")).output(placeholder("artifact", "lowercase")).output(literal(".log\"\nJAVA_BIN=\"/usr/bin/java\"\nJAVA_APPDIR=\"/opt/")).output(placeholder("artifact")).output(literal("\"\nARG1=\"-Dfile.encoding=UTF-8 --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED\"\nARG2=\"")).output(expression().output(literal("-Xms")).output(placeholder("minMemory")).output(literal("m "))).output(expression().output(literal("-Xmx")).output(placeholder("maxMemory")).output(literal("m "))).output(literal("-Djava.io.tmpdir=/home/")).output(placeholder("user")).output(literal("/tmp -Dcom.sun.management.jmxremote -Djava.rmi.server.hostname=127.0.0.1 -Dcom.sun.management.jmxremote.port=")).output(placeholder("managementPort")).output(literal(" -Dcom.sun.management.jmxremote.rmi.port=")).output(placeholder("managementPort")).output(literal(" -Dcom.sun.management.jmxremote.local.only=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -XX:+StartAttachListener\"\nARG3=\"--class-path /opt/")).output(placeholder("artifact", "lowercase")).output(literal("/")).output(placeholder("artifact", "lowercase")).output(literal("-")).output(placeholder("version")).output(literal(".jar")).output(expression().output(placeholder("dependencies"))).output(literal(" ")).output(placeholder("mainClass")).output(literal(" ")).output(placeholder("parameter").multiple(" ")).output(literal("\"")));
		rules.add(rule().condition(trigger("parameter")).output(placeholder("name")).output(literal("=")).output(placeholder("value")));
		rules.add(rule().condition(trigger("dependencies")).output(literal(":/opt/")).output(placeholder("artifact", "lowercase")).output(literal("/")).output(expression().output(placeholder("directory"))).output(literal("/*")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}
}