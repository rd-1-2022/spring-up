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
package org.springframework.up.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for cli.
 *
 * @author Janne Valkealahti
 */
@ConfigurationProperties(prefix = "spring.up")
public class UpCliProperties {

	private List<TemplateCatalog> templateCatalogs = new ArrayList<>();

	private List<TemplateRepository> templateRepositories = new ArrayList<>();

	private Defaults defaults;

	public List<TemplateCatalog> getTemplateCatalogs() {
		return templateCatalogs;
	}

	public void setTemplateCatalogs(List<TemplateCatalog> templateCatalogs) {
		this.templateCatalogs = templateCatalogs;
	}

	public List<TemplateRepository> getTemplateRepositories() {
		return templateRepositories;
	}

	public void setTemplateRepositories(List<TemplateRepository> templateRepositories) {
		this.templateRepositories = templateRepositories;
	}

	public Defaults getDefaults() {
		return defaults;
	}

	public void setDefaults(Defaults defaults) {
		this.defaults = defaults;
	}

	@Override
	public String toString() {
		return "UpCliProperties{" +
				"templateCatalogs=" + templateCatalogs +
				", templateRepositories=" + templateRepositories +
				", defaults=" + defaults +
				'}';
	}
}
