package io.intino.plugin.cesar;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import io.intino.Configuration;
import io.intino.cesar.box.schemas.ServerInfo;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CesarServerInfoDownloader {
	public void download(Project project) {
		CesarAccessor accessor = new CesarAccessor(project);
		List<Configuration> configurations = Arrays.stream(ModuleManager.getInstance(project).getModules()).map(IntinoUtil::configurationOf).filter(obj -> obj instanceof LegioConfiguration).collect(Collectors.toList());
		List<Configuration.Server> localServers = configurations.stream().map(Configuration::servers).flatMap(Collection::stream).filter(distinctByKey(Configuration.Server::name)).collect(Collectors.toList());
		List<ServerInfo> servers = accessor.servers();
		CesarInfo info = CesarInfo.getSafeInstance(project);
		info.serversInfo(localServers.stream()
				.filter(s -> servers.stream().anyMatch(s2 -> s2.alias().equals(s.name()) || s2.id().equals(s.name())))
				.collect(Collectors.toMap(Configuration.Server::name,
						s -> new CesarInfo.ServerInfo(s.name(), s.type().name(), accessor.processes(s.name())), (u, v) -> u, LinkedHashMap::new)));
	}

	public void download(Module module) {
		download(module.getProject(), IntinoUtil.configurationOf(module));
	}

	private void download(Project project, Configuration configuration) {
		CesarAccessor accessor = new CesarAccessor(project);
		CesarInfo info = CesarInfo.getSafeInstance(project);
		LinkedHashMap<String, CesarInfo.ServerInfo> collect = configuration.servers().stream()
				.filter(s -> accessor.server(s.name()) != null)
				.collect(Collectors.toMap(Configuration.Server::name,
						s -> new CesarInfo.ServerInfo(s.name(), s.type().name(), accessor.processes(s.name())), (u, v) -> v, LinkedHashMap::new));

		info.serversInfo(collect);
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

}
