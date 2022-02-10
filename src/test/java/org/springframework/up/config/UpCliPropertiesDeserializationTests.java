/*
 * Copyright 2021 the original author or authors.
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


package org.springframework.up.config;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(UpCliProperties.class)
@ActiveProfiles("deser-test")
public class UpCliPropertiesDeserializationTests {

	@Autowired
	private UpCliProperties upCliProperties;

	@Test
	void deserializationWorked() {
		assertThat(this.upCliProperties.getTemplateCatalogs().size()).isEqualTo(2);
		TemplateCatalog templateCatalog = this.upCliProperties.getTemplateCatalogs().get(0);
		assertThat(templateCatalog.getUrl()).isEqualTo("https://github.com/rd-1-2022/template-catalog");

		templateCatalog = this.upCliProperties.getTemplateCatalogs().get(1);
		assertThat(templateCatalog.getUrl()).isEqualTo("https://github.com/rd-1-2022/template-catalog-2");

		List<TemplateRepository> templateRepositories = this.upCliProperties.getTemplateRepositories();
		assertThat(templateRepositories.size()).isEqualTo(1);
		assertThat(templateRepositories.get(0).getUrl()).isEqualTo("https://github.com/rd-1-2022/rpt-spring-data-jpa");
		assertThat(templateRepositories.get(0).getTags()).hasSize(2);

		Defaults defaults = this.upCliProperties.getDefaults();
		assertThat(defaults.getPackageName()).isEqualTo("com.xkcd");
		assertThat(defaults.getTemplateRepositoryName()).isEqualTo("jpa");

	}
}
