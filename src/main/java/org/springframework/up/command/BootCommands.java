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

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.up.config.UpCliProperties;
import org.springframework.up.git.SourceRepositoryService;
import org.springframework.util.StringUtils;

@ShellComponent
public class BootCommands {

	private UpCliProperties upCliProperties;

	private final SourceRepositoryService sourceRepositoryService;

	@Autowired
	public BootCommands(UpCliProperties upCliProperties,
			SourceRepositoryService sourceRepositoryService) {
		this.upCliProperties = upCliProperties;
		this.sourceRepositoryService = sourceRepositoryService;
	}

	@ShellMethod(key = "boot new", value = "Create a new Spring Boot project from a template")
	public String bootNew(
			@ShellOption(help = "Name of the new project") String projectName,
			@ShellOption(help = "Name of runnable project template") String templateName,
			@ShellOption(help = "URL of runnable project template repository") String url) {

		validate(projectName, templateName, url);
		if (StringUtils.hasText(url)) {
			generateFromUrl(projectName, url);
		} else {
			generateFromTemplateName(projectName, templateName);
		}

		System.out.println("hello world.  Let's make the project " + projectName);
		return String.format("Project generated in %s", "/home/john/ + projectName");
	}

	private void generateFromTemplateName(String projectName, String templateName) {
	}

	private void generateFromUrl(String projectName, String url) {

		Path tempDownloadedGeneratorPath = sourceRepositoryService.retrieveRepositoryContents(url);

		System.out.println("Downloaded contents to " + tempDownloadedGeneratorPath);
	}

	private void validate(String projectName, String templateName, String url) {
		if (StringUtils.hasText(projectName)) {
			System.err.println("projectName option is required");
		}
		if (StringUtils.hasText(templateName) && StringUtils.hasText(url)) {
			System.err.println("templateName and url option can not be specified together");
		}
	}
}
