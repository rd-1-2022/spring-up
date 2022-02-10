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


package org.springframework.up.command;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.up.config.TemplateRepository;
import org.springframework.up.config.UpCliProperties;
import org.springframework.up.util.JavaBeanTableCreator;

@ShellComponent
public class BootTemplateCommands {

	private UpCliProperties upCliProperties;

	private JavaBeanTableCreator javaBeanTableCreator;

	@Autowired
	public BootTemplateCommands(UpCliProperties upCliProperties, JavaBeanTableCreator javaBeanTableCreator) {
		this.upCliProperties = upCliProperties;
		this.javaBeanTableCreator = javaBeanTableCreator;
	}

	@ShellMethod(key = "template list", value = "List templates available to create a new Spring Boot project")
	public void templateList() {
		// TODO Handle the catalog later
		List<TemplateRepository> templateRepositories = this.upCliProperties.getTemplateRepositories();
		LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
		headers.put("name", "Name");
		headers.put("description", "Description");
		headers.put("tags", "Tags");
		this.javaBeanTableCreator.createTable(templateRepositories, headers);
	}
}
