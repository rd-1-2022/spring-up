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

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.up.config.TemplateRepository;
import org.springframework.up.config.UpCliProperties;
import org.springframework.util.StringUtils;

@ShellComponent
public class BootTemplateCommands {

	private UpCliProperties upCliProperties;

	@Autowired
	public BootTemplateCommands(UpCliProperties upCliProperties) {
		this.upCliProperties = upCliProperties;
	}

	@ShellMethod(key = "template list", value = "List templates available to create a new Spring Boot project")
	public Table templateList() {
		Stream<String[]> header = Stream.<String[]>of(new String[] { "Name", "Description", "Tags" });
		List<TemplateRepository> templateRepositories = this.upCliProperties.getTemplateRepositories();
		Stream<String[]> rows = templateRepositories.stream()
				.map(tr -> new String[] { tr.getName(), tr.getDescription(),
						StringUtils.collectionToCommaDelimitedString(tr.getTags()) });
		String[][] data = Stream.concat(header, rows).toArray(String[][]::new);

		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		return tableBuilder.addFullBorder(BorderStyle.fancy_light).build();
	}
}
