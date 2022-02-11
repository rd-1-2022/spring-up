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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.java.ChangePackage;
import org.openrewrite.java.Java11Parser;
import org.openrewrite.java.JavaParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.up.UpException;
import org.springframework.up.config.TemplateRepository;
import org.springframework.up.config.UpCliProperties;
import org.springframework.up.git.SourceRepositoryService;
import org.springframework.up.util.FileTypeCollectingFileVisitor;
import org.springframework.up.util.IoUtils;
import org.springframework.up.util.ResultsExecutor;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

@ShellComponent
public class BootCommands {

	private static final Logger logger = LoggerFactory.getLogger(BootCommands.class);

	private UpCliProperties upCliProperties;

	private final SourceRepositoryService sourceRepositoryService;

	@Autowired
	public BootCommands(UpCliProperties upCliProperties,
			SourceRepositoryService sourceRepositoryService) {
		this.upCliProperties = upCliProperties;
		this.sourceRepositoryService = sourceRepositoryService;
	}

	@ShellMethod(key = "boot new", value = "Create a new Spring Boot project from a template")
	public void bootNew(
			@ShellOption(help = "Name of the new project", defaultValue = ShellOption.NULL) String projectName,
			@ShellOption(help = "Name of runnable project template", defaultValue = ShellOption.NULL) String templateName,
			@ShellOption(help = "URL of runnable project template repository",defaultValue = ShellOption.NULL) String url,
			@ShellOption(help = "Package name for the new project", defaultValue = ShellOption.NULL) String packageName) {


		String projectNameToUse = getProjectName(projectName);
		validate(templateName, url);
		if (StringUtils.hasText(url)) {
			generateFromUrl(projectNameToUse, url, packageName);
		} else {
			generateFromTemplateName(projectNameToUse, templateName, packageName);
		}

	}

	private String getProjectName(String projectName) {
		if (StringUtils.hasText(projectName)) {
			return projectName;
		}
		String defaultProjectName = this.upCliProperties.getDefaults().getProjectName();
		if (StringUtils.hasText(defaultProjectName)) {
			return defaultProjectName;
		}
		// The last resort default project name
		return "demo";
	}

	private void generateFromTemplateName(String projectName, String templateName, String packageName) {
		//TODO does not take into account catalogs
		Optional<String> url = getTemplateRepositoryUrl(templateName);
		if (url.isPresent()) {
			this.generateFromUrl(projectName, url.get(), packageName);
		} else {
			throw new UpException("Could not find a template repository given name = " + templateName);
		}
	}

	@Nullable
	private Optional<String> getTemplateRepositoryUrl(String templateName) {
		// If provided template name on the command line
		if (StringUtils.hasText(templateName)) {
			return findTemplateUrl(templateName);
		} else {
			// otherwise fall back to application defaults
			String defaultTemplateName = this.upCliProperties.getDefaults().getTemplateRepositoryName();
			if (StringUtils.hasText(defaultTemplateName)) {
				return findTemplateUrl(defaultTemplateName);
			} else {
				return Optional.empty();
			}
		}
	}

	@Nullable
	private Optional<String> findTemplateUrl(String templateName) {
		List<TemplateRepository> templateRepositories = this.upCliProperties.getTemplateRepositories();
		for (TemplateRepository templateRepository : templateRepositories) {
			if (templateName.trim().equalsIgnoreCase(templateRepository.getName().trim())) {
				// match - get url
				String url = templateRepository.getUrl();
				if (StringUtils.hasText(url)) {
					return Optional.of(url);
				}
				break;
			}
		}
		return Optional.empty();
	}

	private void generateFromUrl(String projectName, String url, String packageName) {

		Path repositoryContentsPath = sourceRepositoryService.retrieveRepositoryContents(url);
		if (!StringUtils.hasText(packageName))  {
			packageName = this.upCliProperties.getDefaults().getPackageName();
		}
		if (StringUtils.hasText(packageName)) {
			refactorPackage(packageName, repositoryContentsPath);
		}
		File projectDirectory = createProjectDirectory(projectName);
		try {
			FileSystemUtils.copyRecursively(repositoryContentsPath.toFile(), projectDirectory);
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new UpException("Could not copy files.", e);
		}

		AttributedStringBuilder sb = new AttributedStringBuilder();
		sb.append("Project " + projectName + " created in " + repositoryContentsPath)
				.style(sb.style().foreground(AttributedStyle.GREEN));
		System.out.println(sb.toAnsi());
	}

	private File createProjectDirectory(String projectName) {
		File workingDirectory = IoUtils.getWorkingDirectory();
		String projectNameToUse = projectName.replaceAll(" ", "_");
		File projectDirectory = new File(workingDirectory, projectNameToUse);
		IoUtils.createDirectory(projectDirectory);
		logger.debug("Created directory " + projectDirectory);
		return projectDirectory;
	}

	private void refactorPackage(String targetPackageName, Path workingPath) {
		logger.debug("Refactoring to package name " + targetPackageName);
		JavaParser javaParser = new Java11Parser.Builder().build();
		FileTypeCollectingFileVisitor collector = new FileTypeCollectingFileVisitor(".java");
		try {
			Files.walkFileTree(workingPath, collector);
		}
		catch (IOException e) {
			throw new UpException("Failed reading files in " + workingPath, e);
		}
		List<? extends SourceFile> compilationUnits = javaParser.parse(collector.getMatches(), null, null);
		ResultsExecutor container = new ResultsExecutor();

		//TODO derive fromPackage using location of existing @SpringBootApplication class, hardcode for now
		String fromPackage = "com.example.up";
		Recipe recipe = new ChangePackage(fromPackage, targetPackageName, true);
		container.addAll(recipe.run(compilationUnits));
		try {
			container.execute();
		}
		catch (IOException e) {
			throw new UpException("Error performing refactoring", e);
		}

		//TODO change groupId and artifactId
	}

	private void validate(String templateName, String url) {
		if (StringUtils.hasText(templateName) && StringUtils.hasText(url)) {
			System.err.println("templateName and url option can not be specified together");
		}
	}
}
