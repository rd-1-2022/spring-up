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

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UpCliUserConfigTests {

	private FileSystem fileSystem;
	private Function<String, Path> pathProvider;

	@BeforeEach
	public void setupTests() {
		fileSystem = Jimfs.newFileSystem();
		pathProvider = (path) -> fileSystem.getPath(path);
	}

	@Test
	public void test() {
		UpCliUserConfig config = new UpCliUserConfig(pathProvider);
		UpCliUserConfig.Hosts hosts = new UpCliUserConfig.Hosts();
		Map<String, UpCliUserConfig.Host> hostsMap = new HashMap<>();
		hostsMap.put("github.com", new UpCliUserConfig.Host("faketoken", "user"));
		hosts.setHosts(hostsMap);
		config.setHosts(hosts);
		assertThat(config.getHosts()).isNotNull();
		assertThat(config.getHosts().get("github.com")).isNotNull();
		assertThat(config.getHosts().get("github.com").getOauthToken()).isEqualTo("faketoken");
	}
}
