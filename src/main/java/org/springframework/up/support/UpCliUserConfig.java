/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.up.support;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.up.config.UpCliProperties;
import org.springframework.up.support.configfile.UserConfig;

/**
 * Cli access point for user level stored settings.
 *
 * @author Janne Valkealahti
 */
public class UpCliUserConfig {

	/**
	 * Optional env variable for {@code SpringUp} configuration dir.
	 */
	public final static String SPRINGUP_CONFIG_DIR = "SPRINGUP_CONFIG_DIR";

	/**
	 * {@code hosts.yml} stores authentication spesific info for hosts.
	 */
	public final static String HOSTS = "hosts.yml";

	public final static String UP_CLI_PROPERTIES = "springup.yml";

	/**
	 * Base directory name we store our config files.
	 */
	private final static String SPRINGUP_CONFIG_NAME = "springup";

	/**
	 * Keeps auth tokens per hostname.
	 */
	private final UserConfig<Hosts> hostsConfigFile;

	private final UserConfig<UpCliProperties> upCliPropertiesUserConfig;

	public UpCliUserConfig() {
		this(null);
	}

	public UpCliUserConfig(Function<String, Path> pathProvider) {
		this.hostsConfigFile = new UserConfig<>(HOSTS, Hosts.class, SPRINGUP_CONFIG_DIR, SPRINGUP_CONFIG_NAME);
		if (pathProvider != null) {
			this.hostsConfigFile.setPathProvider(pathProvider);
		}
		this.upCliPropertiesUserConfig = new UserConfig<>(UP_CLI_PROPERTIES, UpCliProperties.class, SPRINGUP_CONFIG_DIR, SPRINGUP_CONFIG_NAME);
		if (pathProvider != null) {
			this.upCliPropertiesUserConfig.setPathProvider(pathProvider);
		}
	}

	public UpCliProperties getUpCliProperties() {
		return this.upCliPropertiesUserConfig.getConfig();
	}

	/**
	 * Gets hosts.
	 *
	 * @return mappings for hosts
	 */
	public Map<String, Host> getHosts() {
		Hosts hosts = hostsConfigFile.getConfig();
		return hosts != null ? hosts.getHosts() : null;
	}

	/**
	 * Sets hosts.
	 *
	 * @param hosts
	 */
	public void setHosts(Hosts hosts) {
		hostsConfigFile.setConfig(hosts);
	}

	/**
	 * Update a single host.
	 *
	 * @param key the host key
	 * @param host the host
	 */
	public void updateHost(String key, Host host) {
		Map<String, Host> hostsMap = null;
		Hosts hosts = hostsConfigFile.getConfig();
		if (hosts != null) {
			hostsMap = hosts.getHosts();
		}
		else {
			hosts = new Hosts();
		}
		if (hostsMap == null) {
			hostsMap = new HashMap<>();
		}
		hostsMap.put(key, host);
		hosts.setHosts(hostsMap);
		setHosts(hosts);
	}

	public static class Hosts {

		private Map<String, Host> hosts = new HashMap<>();

		public Map<String, Host> getHosts() {
			return hosts;
		}

		public void setHosts(Map<String, Host> hosts) {
			this.hosts = hosts;
		}
	}

	public static class Host {

		private String oauthToken;
		private String user;

		public Host() {
		}

		public Host(String oauthToken, String user) {
			this.oauthToken = oauthToken;
			this.user = user;
		}

		public static Host of(String oauthToken, String user) {
			return new Host(oauthToken, user);
		}

		public String getOauthToken() {
			return oauthToken;
		}

		public void setOauthToken(String oauthToken) {
			this.oauthToken = oauthToken;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}
	}
}
