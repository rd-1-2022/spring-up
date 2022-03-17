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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.up.support.UpCliUserConfig;
import org.springframework.up.support.UpCliUserConfig.TemplateRepositories;
import org.springframework.up.support.UpCliUserConfig.TemplateRepository;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@ShellComponent
public class BootTemplateCommands {

	private final UpCliUserConfig upCliUserConfig;

	@Autowired
	public BootTemplateCommands(UpCliUserConfig upCliUserConfig) {
		this.upCliUserConfig = upCliUserConfig;
	}

	@ShellMethod(key = "template add", value = "Add template")
	public void templateAdd(
		@ShellOption(help = "Template name") String name,
		@ShellOption(help = "Template url") String url,
		@ShellOption(help = "Template description", defaultValue = ShellOption.NULL) String description,
		@ShellOption(help = "Template tags", defaultValue = ShellOption.NULL) List<String> tags
	) {
		List<TemplateRepository> templateRepositories = upCliUserConfig.getTemplateRepositoriesConfig().getTemplateRepositories();
		templateRepositories.add(TemplateRepository.of(name, description, url, tags));
		TemplateRepositories templateRepositoriesConfig = new TemplateRepositories();
		templateRepositoriesConfig.setTemplateRepositories(templateRepositories);
		upCliUserConfig.setTemplateRepositoriesConfig(templateRepositoriesConfig);
	}

	@ShellMethod(key = "template list", value = "List templates available to create a new Spring Boot project")
	public Table templateList() {
		Stream<String[]> header = Stream.<String[]>of(new String[] { "Name", "Description", "Tags" });
		Collection<TemplateRepository> templateRepositories = upCliUserConfig.getTemplateRepositoriesConfig().getTemplateRepositories();
		Stream<String[]> rows = null;
		if (templateRepositories != null) {
			rows = templateRepositories.stream()
				.map(tr -> new String[] { tr.getName(), tr.getDescription(),
					StringUtils.collectionToCommaDelimitedString(tr.getTags()) });
		}
		else {
			rows = Stream.empty();
		}
		String[][] data = Stream.concat(header, rows).toArray(String[][]::new);
		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		return tableBuilder.addFullBorder(BorderStyle.fancy_light).build();
	}

	@ShellMethod(key = "template remove", value = "Remove template")
	public void templateRemove(
		@ShellOption(help = "Template name") String name
	) {
		List<TemplateRepository> templateRepositories = upCliUserConfig.getTemplateRepositoriesConfig().getTemplateRepositories();
		templateRepositories = templateRepositories.stream()
			.filter(tc -> !ObjectUtils.nullSafeEquals(tc.getName(), name))
			.collect(Collectors.toList());
		TemplateRepositories templateRepositoriesConfig = new TemplateRepositories();
		templateRepositoriesConfig.setTemplateRepositories(templateRepositories);
		upCliUserConfig.setTemplateRepositoriesConfig(templateRepositoriesConfig);
	}
}
