package io.intino.plugin.project;

import org.siani.itrules.*;

import java.util.Locale;

import static org.siani.itrules.LineSeparator.*;

public class CesarFileTemplate extends Template {

	protected CesarFileTemplate(Locale locale, LineSeparator separator) {
		super(locale, separator);
	}

	public static Template create() {
		return new CesarFileTemplate(Locale.ENGLISH, LF).define();
	}

	public Template define() {
		add(
			rule().add((condition("type", "project"))).add(literal("dsl Cesar\n\nProject ")).add(mark("name")).add(literal("\n    ")).add(expression().add(literal("alias = ")).add(mark("alias")).add(literal("\n")).add(literal("    "))).add(literal("servers = ")).add(expression().add(mark("servers")).or(expression().add(literal("0")))).add(literal("\n    devices = ")).add(expression().add(mark("devices")).or(expression().add(literal("0")))).add(literal("\n    server-processes = ")).add(expression().add(mark("server-processes")).or(expression().add(literal("0")))).add(literal("\n    device-processes = ")).add(expression().add(mark("device-processes")).or(expression().add(literal("0")))).add(literal("\n    datalakes = ")).add(expression().add(mark("datalakes")).or(expression().add(literal("0")))).add(literal("\n    datamarts = ")).add(expression().add(mark("datamarts")).or(expression().add(literal("0")))).add(expression().add(literal("\n")).add(literal("\n")).add(literal("    has ")).add(mark("device", "nameValue").multiple("\n has "))).add(expression().add(literal("\n")).add(literal("\n")).add(literal("    has ")).add(mark("process", "nameValue").multiple("\n has "))).add(literal("\n\n    ")).add(mark("server").multiple("\n")).add(literal("\n\n    ")).add(mark("process").multiple("\n")),
			rule().add((condition("trigger", "nameValue"))).add(mark("name")),
			rule().add((condition("trigger", "server"))).add(literal("Server ")).add(mark("name")).add(literal("\n    id = \"")).add(mark("id")).add(literal("\"\n    status = ")).add(mark("status")).add(literal("\n    ")).add(expression().add(literal("architecture = \"")).add(mark("architecture")).add(literal("\""))).add(expression().add(literal("\n")).add(literal("    cores = ")).add(mark("cores"))).add(literal("\n    ")).add(expression().add(literal("os = \"")).add(mark("os")).add(literal("\""))).add(literal("\n    ")).add(expression().add(literal("jvm = \"")).add(mark("jvm")).add(literal("\""))).add(literal("\n\n    ")).add(expression().add(literal("UpTime(\"")).add(mark("boot")).add(literal("\")"))).add(literal("\n    ")).add(expression().add(literal("temperature = ")).add(mark("temperature")).add(literal("°"))).add(literal("\n    ")).add(expression().add(mark("serverCpu"))).add(literal("\n    ")).add(expression().add(mark("serverMemory"))).add(literal("\n    //Swap(size = 2.29GB, used = 1.65GB, free = 28%, pagination = 18faults/second)\n    ")).add(expression().add(mark("fileSystem"))).add(literal("\n\n    Network(ip = \"")).add(mark("ip")).add(literal("\")\n        inbound = 150 Mb/s\n        outbound = 1430 Mb/s\n        Ports\n            //Port(nginx, 80 443)\n            //Port(ness, \"0xFEED\")\n\n        Connections(44)\n            Client(\"205.234.11.204\", \"Canada\")\n            Client(\"203.13.121.205\", \"Mexico\")\n\n    Rule(\"CPU.usage > 30%\") as Warning\n    Rule(\"Physical-Memory.usage > 80%\") as Alert\n    Rule(\"Swap-Memory usage > 80%\") as Alert\n\n    //has hss.consul hss.cesar\n    //has hss.ness hss.nginx\n    //has hss.federation hss.dashboard")),
			rule().add((condition("trigger", "serverCpu"))).add(literal("CPU(")).add(expression().add(literal("usage = ")).add(mark("usage")).add(literal("%"))).add(expression().add(literal(", processes = ")).add(mark("processes"))).add(expression().add(literal(", threads = ")).add(mark("threads"))).add(literal(")")),
			rule().add((condition("trigger", "serverMemory"))).add(literal("Memory(size = ")).add(mark("size")).add(literal(" MB")).add(expression().add(literal(", used = ")).add(mark("used")).add(literal(" MB"))).add(expression().add(literal(", free = ")).add(mark("free")).add(literal(" %"))).add(literal(")")),
			rule().add((condition("trigger", "fileSystem"))).add(literal("Filesystem(size = ")).add(mark("size")).add(literal(" MB")).add(expression().add(literal(", used = ")).add(mark("used")).add(literal(" %"))).add(literal(")")),
			rule().add((condition("trigger", "process"))),
			rule().add((condition("attribute", "true")), (condition("trigger", "status"))).add(literal("active")),
			rule().add((condition("attribute", "false")), (condition("trigger", "status"))).add(literal("inactive"))
		);
		return this;
	}
}