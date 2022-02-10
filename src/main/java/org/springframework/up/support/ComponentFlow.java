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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;

import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.MultiItemSelector;
import org.springframework.shell.component.MultiItemSelector.MultiItemSelectorContext;
import org.springframework.shell.component.PathInput;
import org.springframework.shell.component.PathInput.PathInputContext;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.SingleItemSelector.SingleItemSelectorContext;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.StringInput.StringInputContext;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.style.TemplateExecutor;
import org.springframework.up.support.ComponentFlow.ComponentFlowResult;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Wizart providing an implementation which allows more polished way to ask various inputs
 * from a user using shell style components for simple text/path input, single select and
 * multi-select.
 *
 * NOTE: Experiment until moved to spring-shell
 *
 * @author Janne Valkealahti
 */
public interface ComponentFlow extends Wizard<ComponentFlowResult> {

	/**
	 * Run a wizard and returns a result from it.
	 *
	 * @return the input wizard result
	 */
	ComponentFlowResult run();

	/**
	 * Gets a new instance of an input wizard builder.
	 *
	 * @param terminal the terminal
	 * @return the input wizard builder
	 */
	public static Builder builder(Terminal terminal) {
		return new DefaultBuilder(terminal);
	}

	/**
	 * Results from a wizard run.
	 */
	interface ComponentFlowResult extends WizardResult {

		/**
		 * Gets a context.
		 *
		 * @return a context
		 */
		ComponentContext<?> getContext();
	}

	/**
	 * Enumeration of a modes instructing how {@code resultValue} is handled.
	 */
	public enum ResultMode {

		/**
		 * Blindly accept a given {@code resultValue} resulting component run to be
		 * skipped and value set as a result automatically.
		 */
		ACCEPT,

		/**
		 * Verify a given {@code resultValue} with a component run. It is up to a
		 * component to define how it's done but usually result value is set as a
		 * default value which allows user to just continue.
		 */
		VERIFY
	}

	/**
	 * Interface for string input spec builder.
	 */
	interface StringInputSpec {

		/**
		 * Sets a name.
		 *
		 * @param name the name
		 * @return a builder
		 */
		StringInputSpec name(String name);

		/**
		 * Sets a result value.
		 *
		 * @param resultValue the result value
		 * @return a builder
		 */
		StringInputSpec resultValue(String resultValue);

		/**
		 * Sets a result mode.
		 *
		 * @param resultMode the result mode
		 * @return a builder
		 */
		StringInputSpec resultMode(ResultMode resultMode);

		/**
		 * Sets a default value.
		 *
		 * @param defaultValue the defult value
		 * @return a builder
		 */
		StringInputSpec defaultValue(String defaultValue);

		/**
		 * Sets a renderer function.
		 *
		 * @param renderer the renderer
		 * @return a builder
		 */
		StringInputSpec renderer(Function<StringInputContext, List<AttributedString>> renderer);

		/**
		 * Sets a default renderer template location.
		 *
		 * @param location the template location
		 * @return a builder
		 */
		StringInputSpec template(String location);

		/**
		 * Adds a pre-run context handler.
		 *
		 * @param handler the context handler
		 * @return a builder
		 */
		StringInputSpec preHandler(Consumer<StringInputContext> handler);

		/**
		 * Adds a post-run context handler.
		 *
		 * @param handler the context handler
		 * @return a builder
		 */
		StringInputSpec postHandler(Consumer<StringInputContext> handler);

		/**
		 * Automatically stores result from a {@link StringInputContext} into
		 * {@link ComponentContext} with key given to builder. Defaults to {@code true}.
		 *
		 * @param store the flag if storing result
		 * @return a builder
		 */
		StringInputSpec storeResult(boolean store);

		/**
		 * Build and return parent builder.
		 *
		 * @return the parent builder
		 */
		Builder and();
	}

	/**
	 * Interface for path input spec builder.
	 */
	interface PathInputSpec {

		/**
		 * Sets a name.
		 *
		 * @param name the name
		 * @return a builder
		 */
		PathInputSpec name(String name);

		/**
		 * Sets a result value.
		 *
		 * @param resultValue the result value
		 * @return a builder
		 */
		PathInputSpec resultValue(String resultValue);

		/**
		 * Sets a result mode.
		 *
		 * @param resultMode the result mode
		 * @return a builder
		 */
		PathInputSpec resultMode(ResultMode resultMode);

		/**
		 * Sets a default value.
		 *
		 * @param defaultValue the defult value
		 * @return a builder
		 */
		PathInputSpec defaultValue(String defaultValue);

		/**
		 * Sets a renderer function.
		 *
		 * @param renderer the renderer
		 * @return a builder
		 */
		PathInputSpec renderer(Function<PathInputContext, List<AttributedString>> renderer);

		/**
		 * Sets a default renderer template location.
		 *
		 * @param location the template location
		 * @return a builder
		 */
		PathInputSpec template(String location);

		/**
		 * Adds a pre-run context handler.
		 *
		 * @param handler the context handler
		 * @return a builder
		 */
		PathInputSpec preHandler(Consumer<PathInputContext> handler);

		/**
		 * Adds a post-run context handler.
		 *
		 * @param handler the context handler
		 * @return a builder
		 */
		PathInputSpec postHandler(Consumer<PathInputContext> handler);

		/**
		 * Automatically stores result from a {@link StringInputContext} into
		 * {@link ComponentContext} with key given to builder. Defaults to {@code true}.
		 *
		 * @param store the flag if storing result
		 * @return a builder
		 */
		PathInputSpec storeResult(boolean store);

		/**
		 * Build and return parent builder.
		 *
		 * @return the parent builder
		 */
		Builder and();
	}

	/**
	 * Interface for single item selector spec builder.
	 */
	interface SingleItemSelectorSpec {

		/**
		 * Sets a name.
		 *
		 * @param name the name
		 * @return a builder
		 */
		SingleItemSelectorSpec name(String name);

		/**
		 * Sets a result value.
		 *
		 * @param resultValue the result value
		 * @return a builder
		 */
		SingleItemSelectorSpec resultValue(String resultValue);

		/**
		 * Sets a result mode.
		 *
		 * @param resultMode the result mode
		 * @return a builder
		 */
		SingleItemSelectorSpec resultMode(ResultMode resultMode);

		/**
		 * Adds a select item.
		 *
		 * @param name the name
		 * @param item the item
		 * @return a builder
		 */
		SingleItemSelectorSpec selectItem(String name, String item);

		/**
		 * Adds a map of select items.
		 *
		 * @param selectItems the select items
		 * @return a builder
		 */
		SingleItemSelectorSpec selectItems(Map<String, String> selectItems);

		/**
		 * Sets a {@link Comparator} for sorting items.
		 *
		 * @param comparator the item comparator
		 * @return a builder
		 */
		SingleItemSelectorSpec sort(Comparator<SelectorItem<String>> comparator);

		/**
		 * Sets a renderer function.
		 *
		 * @param renderer the renderer
		 * @return a builder
		 */
		SingleItemSelectorSpec renderer(Function<SingleItemSelectorContext<String, SelectorItem<String>>, List<AttributedString>> renderer);

		/**
		 * Sets a default renderer template location.
		 *
		 * @param location the template location
		 * @return a builder
		 */
		SingleItemSelectorSpec template(String location);

		/**
		 * Sets a maximum number of items in a selector list;
		 *
		 * @param max the maximum number of items
		 * @return a builder
		 */
		SingleItemSelectorSpec max(int max);

		/**
		 * Adds a pre-run context handler.
		 *
		 * @param handler the context handler
		 * @return a builder
		 */
		SingleItemSelectorSpec preHandler(Consumer<SingleItemSelectorContext<String, SelectorItem<String>>> handler);

		/**
		 * Adds a post-run context handler.
		 *
		 * @param handler the context handler
		 * @return a builder
		 */
		SingleItemSelectorSpec postHandler(Consumer<SingleItemSelectorContext<String, SelectorItem<String>>> handler);

		/**
		 * Automatically stores result from a {@link SingleItemSelectorContext} into
		 * {@link ComponentContext} with key given to builder. Defaults to {@code true}.
		 *
		 * @param store the flag if storing result
		 * @return a builder
		 */
		SingleItemSelectorSpec storeResult(boolean store);

		/**
		 * Build and return parent builder.
		 *
		 * @return the parent builder
		 */
		Builder and();
	}

	/**
	 * Interface for multi input spec builder.
	 */
	interface MultiItemSelectorSpec {

		/**
		 * Sets a name.
		 *
		 * @param name the name
		 * @return a builder
		 */
		MultiItemSelectorSpec name(String name);

		/**
		 * Sets a result values.
		 *
		 * @param resultValues the result values
		 * @return a builder
		 */
		MultiItemSelectorSpec resultValues(List<String> resultValues);

		/**
		 * Sets a result mode.
		 *
		 * @param resultMode the result mode
		 * @return a builder
		 */
		MultiItemSelectorSpec resultMode(ResultMode resultMode);

		/**
		 * Adds a list of select items.
		 *
		 * @param selectItems the select items
		 * @return a builder
		 */
		MultiItemSelectorSpec selectItems(List<SelectItem> selectItems);

		/**
		 * Sets a {@link Comparator} for sorting items.
		 *
		 * @param comparator the item comparator
		 * @return a builder
		 */
		MultiItemSelectorSpec sort(Comparator<SelectorItem<String>> comparator);

		/**
		 * Sets a renderer function.
		 *
		 * @param renderer the renderer
		 * @return a builder
		 */
		MultiItemSelectorSpec renderer(Function<MultiItemSelectorContext<String, SelectorItem<String>>, List<AttributedString>> renderer);

		/**
		 * Sets a default renderer template location.
		 *
		 * @param location the template location
		 * @return a builder
		 */
		MultiItemSelectorSpec template(String location);

		/**
		 * Sets a maximum number of items in a selector list;
		 *
		 * @param max the maximum number of items
		 * @return a builder
		 */
		MultiItemSelectorSpec max(int max);

		/**
		 * Adds a pre-run context handler.
		 *
		 * @param handler the context handler
		 * @return a builder
		 */
		MultiItemSelectorSpec preHandler(Consumer<MultiItemSelectorContext<String, SelectorItem<String>>> handler);

		/**
		 * Adds a post-run context handler.
		 *
		 * @param handler the context handler
		 * @return a builder
		 */
		MultiItemSelectorSpec postHandler(Consumer<MultiItemSelectorContext<String, SelectorItem<String>>> handler);

		/**
		 * Automatically stores result from a {@link MultiItemSelectorContext} into
		 * {@link ComponentContext} with key given to builder. Defaults to {@code true}.
		 *
		 * @param store the flag if storing result
		 * @return a builder
		 */
		MultiItemSelectorSpec storeResult(boolean store);

		/**
		 * Build and return parent builder.
		 *
		 * @return the parent builder
		 */
		Builder and();
	}

	/**
	 * Interface for a wizard builder.
	 */
	interface Builder {

		/**
		 * Gets a builder for string input.
		 *
		 * @param id the identifier
		 * @return builder for string input
		 */
		StringInputSpec withStringInput(String id);

		/**
		 * Gets a builder for path input.
		 *
		 * @param id the identifier
		 * @return builder for text input
		 */
		PathInputSpec withPathInput(String id);

		/**
		 * Gets a builder for single item selector.
		 *
		 * @param id the identifier
		 * @return builder for single item selector
		 */
		SingleItemSelectorSpec withSingleItemSelector(String id);

		/**
		 * Gets a builder for multi item selector.
		 *
		 * @param id the identifier
		 * @return builder for multi item selector
		 */
		MultiItemSelectorSpec withMultiItemSelector(String id);

		/**
		 * Sets a {@link ResourceLoader}.
		 *
		 * @param resourceLoader the resource loader
		 * @return a builder
		 */
		Builder resourceLoader(ResourceLoader resourceLoader);

		/**
		 * Sets a {@link TemplateExecutor}.
		 *
		 * @param templateExecutor the template executor
		 * @return a builder
		 */
		Builder templateExecutor(TemplateExecutor templateExecutor);

		/**
		 * Builds instance of input wizard.
		 *
		 * @return instance of input wizard
		 */
		ComponentFlow build();
	}

	static abstract class BaseBuilder implements Builder {

		private Terminal terminal;
		private final List<BaseStringInput> stringInputs = new ArrayList<>();
		private final List<BasePathInput> pathInputs = new ArrayList<>();
		private final List<BaseSingleItemSelector> singleItemSelectors = new ArrayList<>();
		private final List<BaseMultiItemSelector> multiItemSelectors = new ArrayList<>();
		private final AtomicInteger order = new AtomicInteger();
		private final HashSet<String> uniqueIds = new HashSet<>();
		private ResourceLoader resourceLoader;
		private TemplateExecutor templateExecutor;

		BaseBuilder(Terminal terminal) {
			this.terminal = terminal;
		}

		@Override
		public ComponentFlow build() {
			return new DefaultComponentFlow(terminal, resourceLoader, templateExecutor, stringInputs, pathInputs,
					singleItemSelectors, multiItemSelectors);
		}

		@Override
		public StringInputSpec withStringInput(String id) {
			return new DefaultStringInputSpec(this, id);
		}

		@Override
		public PathInputSpec withPathInput(String id) {
			return new DefaultPathInputSpec(this, id);
		}

		@Override
		public SingleItemSelectorSpec withSingleItemSelector(String id) {
			return new DefaultSingleInputSpec(this, id);
		}

		@Override
		public MultiItemSelectorSpec withMultiItemSelector(String id) {
			return new DefaultMultiInputSpec(this, id);
		}

		@Override
		public Builder resourceLoader(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
			return this;
		}

		@Override
		public Builder templateExecutor(TemplateExecutor templateExecutor) {
			this.templateExecutor = templateExecutor;
			return this;
		}

		void addStringInput(BaseStringInput input) {
			checkUniqueId(input.getId());
			input.setOrder(order.getAndIncrement());
			stringInputs.add(input);
		}

		void addPathInput(BasePathInput input) {
			checkUniqueId(input.getId());
			input.setOrder(order.getAndIncrement());
			pathInputs.add(input);
		}

		void addSingleItemSelector(BaseSingleItemSelector input) {
			checkUniqueId(input.getId());
			input.setOrder(order.getAndIncrement());
			singleItemSelectors.add(input);
		}

		void addMultiItemSelector(BaseMultiItemSelector input) {
			checkUniqueId(input.getId());
			input.setOrder(order.getAndIncrement());
			multiItemSelectors.add(input);
		}

		ResourceLoader getResourceLoader() {
			return resourceLoader;
		}

		TemplateExecutor getTemplateExecutor() {
			return templateExecutor;
		}

		private void checkUniqueId(String id) {
			if (uniqueIds.contains(id)) {
				throw new IllegalArgumentException(String.format("Component with id %s is already registered", id));
			}
			uniqueIds.add(id);
		}
	}

	static abstract class BaseInput implements Ordered {

		private final BaseBuilder builder;
		private final String id;
		private int order;

		BaseInput(BaseBuilder builder, String id) {
			this.builder = builder;
			this.id = id;
		}

		@Override
		public int getOrder() {
			return order;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		public BaseBuilder getBuilder() {
			return builder;
		}

		public String getId() {
			return id;
		}
	}

	static abstract class BaseStringInput extends BaseInput implements StringInputSpec {

		private String name;
		private String resultValue;
		private ResultMode resultMode;
		private String defaultValue;
		private Function<StringInputContext, List<AttributedString>> renderer;
		private List<Consumer<StringInputContext>> preHandlers = new ArrayList<>();
		private List<Consumer<StringInputContext>> postHandlers = new ArrayList<>();
		private boolean storeResult = true;
		private String templateLocation;

		public BaseStringInput(BaseBuilder builder, String id) {
			super(builder, id);
		}

		@Override
		public StringInputSpec name(String name) {
			this.name = name;
			return this;
		}

		@Override
		public StringInputSpec resultValue(String resultValue) {
			this.resultValue = resultValue;
			return this;
		}

		@Override
		public StringInputSpec resultMode(ResultMode resultMode) {
			this.resultMode = resultMode;
			return this;
		}

		@Override
		public StringInputSpec defaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		@Override
		public StringInputSpec renderer(Function<StringInputContext, List<AttributedString>> renderer) {
			this.renderer = renderer;
			return this;
		}

		@Override
		public StringInputSpec template(String location) {
			this.templateLocation = location;
			return this;
		}

		@Override
		public StringInputSpec preHandler(Consumer<StringInputContext> handler) {
			this.preHandlers.add(handler);
			return this;
		}

		@Override
		public StringInputSpec postHandler(Consumer<StringInputContext> handler) {
			this.postHandlers.add(handler);
			return this;
		}

		@Override
		public StringInputSpec storeResult(boolean store) {
			this.storeResult = store;
			return this;
		}

		@Override
		public Builder and() {
			getBuilder().addStringInput(this);
			return getBuilder();
		}

		public String getName() {
			return name;
		}

		public String getResultValue() {
			return resultValue;
		}

		public ResultMode getResultMode() {
			return resultMode;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public Function<StringInputContext, List<AttributedString>> getRenderer() {
			return renderer;
		}

		public String getTemplateLocation() {
			return templateLocation;
		}

		public List<Consumer<StringInputContext>> getPreHandlers() {
			return preHandlers;
		}

		public List<Consumer<StringInputContext>> getPostHandlers() {
			return postHandlers;
		}

		public boolean isStoreResult() {
			return storeResult;
		}
	}

	static class DefaultStringInputSpec extends BaseStringInput {

		public DefaultStringInputSpec(BaseBuilder builder, String id) {
			super(builder, id);
		}
	}

	static abstract class BasePathInput extends BaseInput implements PathInputSpec {

		private String name;
		private String resultValue;
		private ResultMode resultMode;
		private String defaultValue;
		private Function<PathInputContext, List<AttributedString>> renderer;
		private List<Consumer<PathInputContext>> preHandlers = new ArrayList<>();
		private List<Consumer<PathInputContext>> postHandlers = new ArrayList<>();
		private boolean storeResult = true;
		private String templateLocation;

		public BasePathInput(BaseBuilder builder, String id) {
			super(builder, id);
		}

		@Override
		public PathInputSpec name(String name) {
			this.name = name;
			return this;
		}

		@Override
		public PathInputSpec resultValue(String resultValue) {
			this.resultValue = resultValue;
			return this;
		}

		@Override
		public PathInputSpec resultMode(ResultMode resultMode) {
			this.resultMode = resultMode;
			return this;
		}

		@Override
		public PathInputSpec defaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		@Override
		public PathInputSpec renderer(Function<PathInputContext, List<AttributedString>> renderer) {
			this.renderer = renderer;
			return this;
		}

		@Override
		public PathInputSpec template(String location) {
			this.templateLocation = location;
			return this;
		}

		@Override
		public PathInputSpec preHandler(Consumer<PathInputContext> handler) {
			this.preHandlers.add(handler);
			return this;
		}

		@Override
		public PathInputSpec postHandler(Consumer<PathInputContext> handler) {
			this.postHandlers.add(handler);
			return this;
		}

		@Override
		public PathInputSpec storeResult(boolean store) {
			this.storeResult = store;
			return this;
		}

		@Override
		public Builder and() {
			getBuilder().addPathInput(this);
			return getBuilder();
		}

		public String getName() {
			return name;
		}

		public String getResultValue() {
			return resultValue;
		}

		public ResultMode getResultMode() {
			return resultMode;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public Function<PathInputContext, List<AttributedString>> getRenderer() {
			return renderer;
		}

		public String getTemplateLocation() {
			return templateLocation;
		}

		public List<Consumer<PathInputContext>> getPreHandlers() {
			return preHandlers;
		}

		public List<Consumer<PathInputContext>> getPostHandlers() {
			return postHandlers;
		}

		public boolean isStoreResult() {
			return storeResult;
		}
	}

	static class DefaultPathInputSpec extends BasePathInput {

		public DefaultPathInputSpec(BaseBuilder builder, String id) {
			super(builder, id);
		}
	}

	static abstract class BaseSingleItemSelector extends BaseInput implements SingleItemSelectorSpec {

		private String name;
		private String resultValue;
		private ResultMode resultMode;
		private Map<String, String> selectItems = new HashMap<>();
		private Comparator<SelectorItem<String>> comparator;
		private Function<SingleItemSelectorContext<String, SelectorItem<String>>, List<AttributedString>> renderer;
		private Integer maxItems;
		private List<Consumer<SingleItemSelectorContext<String, SelectorItem<String>>>> preHandlers = new ArrayList<>();
		private List<Consumer<SingleItemSelectorContext<String, SelectorItem<String>>>> postHandlers = new ArrayList<>();
		private boolean storeResult = true;
		private String templateLocation;

		public BaseSingleItemSelector(BaseBuilder builder, String id) {
			super(builder, id);
		}

		@Override
		public SingleItemSelectorSpec name(String name) {
			this.name = name;
			return this;
		}

		@Override
		public SingleItemSelectorSpec resultValue(String resultValue) {
			this.resultValue = resultValue;
			return this;
		}

		@Override
		public SingleItemSelectorSpec resultMode(ResultMode resultMode) {
			this.resultMode = resultMode;
			return this;
		}

		@Override
		public SingleItemSelectorSpec selectItem(String name, String item) {
			this.selectItems.put(name, item);
			return this;
		}

		@Override
		public SingleItemSelectorSpec selectItems(Map<String, String> selectItems) {
			this.selectItems.putAll(selectItems);
			return this;
		}

		@Override
		public SingleItemSelectorSpec sort(Comparator<SelectorItem<String>> comparator) {
			this.comparator = comparator;
			return this;
		}

		@Override
		public SingleItemSelectorSpec renderer(Function<SingleItemSelectorContext<String, SelectorItem<String>>, List<AttributedString>> renderer) {
			this.renderer = renderer;
			return this;
		}

		@Override
		public SingleItemSelectorSpec template(String location) {
			this.templateLocation = location;
			return this;
		}

		@Override
		public SingleItemSelectorSpec max(int max) {
			this.maxItems = max;
			return this;
		}

		@Override
		public SingleItemSelectorSpec preHandler(Consumer<SingleItemSelectorContext<String, SelectorItem<String>>> handler) {
			this.preHandlers.add(handler);
			return null;
		}

		@Override
		public SingleItemSelectorSpec postHandler(Consumer<SingleItemSelectorContext<String, SelectorItem<String>>> handler) {
			this.postHandlers.add(handler);
			return this;
		}

		@Override
		public SingleItemSelectorSpec storeResult(boolean store) {
			this.storeResult = store;
			return this;
		}

		@Override
		public Builder and() {
			getBuilder().addSingleItemSelector(this);
			return getBuilder();
		}

		public String getName() {
			return name;
		}

		public String getResultValue() {
			return resultValue;
		}

		public ResultMode getResultMode() {
			return resultMode;
		}

		public Map<String, String> getSelectItems() {
			return selectItems;
		}

		public Comparator<SelectorItem<String>> getComparator() {
			return comparator;
		}

		public Function<SingleItemSelectorContext<String, SelectorItem<String>>, List<AttributedString>> getRenderer() {
			return renderer;
		}

		public String getTemplateLocation() {
			return templateLocation;
		}

		public Integer getMaxItems() {
			return maxItems;
		}

		public List<Consumer<SingleItemSelectorContext<String, SelectorItem<String>>>> getPreHandlers() {
			return preHandlers;
		}

		public List<Consumer<SingleItemSelectorContext<String, SelectorItem<String>>>> getPostHandlers() {
			return postHandlers;
		}

		public boolean isStoreResult() {
			return storeResult;
		}
	}

	static class DefaultSingleInputSpec extends BaseSingleItemSelector {

		public DefaultSingleInputSpec(BaseBuilder builder, String id) {
			super(builder, id);
		}
	}

	public interface SelectItem {

		String name();
		String item();
		boolean enabled();

		public static SelectItem of(String name, String item) {
			return of(name, item, true);
		}

		public static SelectItem of(String name, String item, boolean enabled) {
			return new DefaultSelectItem(name, item, enabled);
		}
	}

	static class DefaultSelectItem implements SelectItem {

		private String name;
		private String item;
		private boolean enabled;

		public DefaultSelectItem(String name, String item, boolean enabled) {
			this.name = name;
			this.item = item;
			this.enabled = enabled;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String item() {
			return item;
		}

		@Override
		public boolean enabled() {
			return enabled;
		}
	}

	static abstract class BaseMultiItemSelector extends BaseInput implements MultiItemSelectorSpec {

		private String name;
		private List<String> resultValues = new ArrayList<>();
		private ResultMode resultMode;
		private List<SelectItem> selectItems = new ArrayList<>();
		private Comparator<SelectorItem<String>> comparator;
		private Function<MultiItemSelectorContext<String, SelectorItem<String>>, List<AttributedString>> renderer;
		private Integer maxItems;
		private List<Consumer<MultiItemSelectorContext<String, SelectorItem<String>>>> preHandlers = new ArrayList<>();
		private List<Consumer<MultiItemSelectorContext<String, SelectorItem<String>>>> postHandlers = new ArrayList<>();
		private boolean storeResult = true;
		private String templateLocation;

		public BaseMultiItemSelector(BaseBuilder builder, String id) {
			super(builder, id);
		}

		@Override
		public MultiItemSelectorSpec name(String name) {
			this.name = name;
			return this;
		}

		@Override
		public MultiItemSelectorSpec resultValues(List<String> resultValues) {
			this.resultValues.addAll(resultValues);
			return this;
		}

		@Override
		public MultiItemSelectorSpec resultMode(ResultMode resultMode) {
			this.resultMode = resultMode;
			return this;
		}

		@Override
		public MultiItemSelectorSpec selectItems(List<SelectItem> selectItems) {
			this.selectItems = selectItems;
			return this;
		}

		@Override
		public MultiItemSelectorSpec sort(Comparator<SelectorItem<String>> comparator) {
			this.comparator = comparator;
			return this;
		}

		@Override
		public MultiItemSelectorSpec renderer(Function<MultiItemSelectorContext<String, SelectorItem<String>>, List<AttributedString>> renderer) {
			this.renderer = renderer;
			return this;
		}

		@Override
		public MultiItemSelectorSpec template(String location) {
			this.templateLocation = location;
			return this;
		}

		@Override
		public MultiItemSelectorSpec max(int max) {
			this.maxItems = max;
			return this;
		}

		@Override
		public MultiItemSelectorSpec preHandler(Consumer<MultiItemSelectorContext<String, SelectorItem<String>>> handler) {
			this.preHandlers.add(handler);
			return this;
		}

		@Override
		public MultiItemSelectorSpec postHandler(Consumer<MultiItemSelectorContext<String, SelectorItem<String>>> handler) {
			this.postHandlers.add(handler);
			return this;
		}

		@Override
		public MultiItemSelectorSpec storeResult(boolean store) {
			this.storeResult = store;
			return this;
		}

		@Override
		public Builder and() {
			getBuilder().addMultiItemSelector(this);
			return getBuilder();
		}

		public String getName() {
			return name;
		}

		public List<String> getResultValues() {
			return resultValues;
		}

		public ResultMode getResultMode() {
			return resultMode;
		}

		public List<SelectItem> getSelectItems() {
			return selectItems;
		}

		public Comparator<SelectorItem<String>> getComparator() {
			return comparator;
		}

		public Function<MultiItemSelectorContext<String, SelectorItem<String>>, List<AttributedString>> getRenderer() {
			return renderer;
		}

		public String getTemplateLocation() {
			return templateLocation;
		}

		public Integer getMaxItems() {
			return maxItems;
		}

		public List<Consumer<MultiItemSelectorContext<String, SelectorItem<String>>>> getPreHandlers() {
			return preHandlers;
		}

		public List<Consumer<MultiItemSelectorContext<String, SelectorItem<String>>>> getPostHandlers() {
			return postHandlers;
		}

		public boolean isStoreResult() {
			return storeResult;
		}
	}

	static class DefaultMultiInputSpec extends BaseMultiItemSelector {

		public DefaultMultiInputSpec(BaseBuilder builder, String id) {
			super(builder, id);
		}
	}

	static class DefaultBuilder extends BaseBuilder {

		DefaultBuilder(Terminal terminal) {
			super(terminal);
		}
	}

	static class DefaultComponentFlowResult implements ComponentFlowResult {

		private ComponentContext<?> context;

		DefaultComponentFlowResult(ComponentContext<?> context) {
			this.context = context;
		}

		public ComponentContext<?> getContext() {
			return context;
		}
	}

	static class DefaultComponentFlow implements ComponentFlow {

		private final Terminal terminal;
		private final List<BaseStringInput> stringInputs;
		private final List<BasePathInput> pathInputs;
		private final List<BaseSingleItemSelector> singleInputs;
		private final List<BaseMultiItemSelector> multiInputs;
		private final ResourceLoader resourceLoader;
		private final TemplateExecutor templateExecutor;

		DefaultComponentFlow(Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor,
				List<BaseStringInput> stringInputs, List<BasePathInput> pathInputs,
				List<BaseSingleItemSelector> singleInputs, List<BaseMultiItemSelector> multiInputs) {
			this.terminal = terminal;
			this.resourceLoader = resourceLoader;
			this.templateExecutor = templateExecutor;
			this.stringInputs = stringInputs;
			this.pathInputs = pathInputs;
			this.singleInputs = singleInputs;
			this.multiInputs = multiInputs;
		}

		@Override
		public ComponentFlowResult run() {
			return runGetResults();
		}

		private DefaultComponentFlowResult runGetResults() {
			Stream<Function<ComponentContext<?>, ComponentContext<?>>> operations = Stream
					.of(stringInputsStream(), pathInputsStream(), singleItemSelectorsStream(), multiItemSelectorsStream())
					.flatMap(oio -> oio)
					.sorted(OrderComparator.INSTANCE)
					.map(oio -> oio.getOperation());
			ComponentContext<?> context = chain(ComponentContext.empty(), operations);
			return new DefaultComponentFlowResult(context);
		}

		private Stream<OrderedInputOperation> stringInputsStream() {
			return stringInputs.stream().map(input -> {
				Function<ComponentContext<?>, ComponentContext<?>> operation = (context) -> {
						if (input.getResultMode() == ResultMode.ACCEPT && input.isStoreResult()
								&& StringUtils.hasText(input.getResultValue())) {
							context.put(input.getId(), input.getResultValue());
							return context;
						}
						StringInput selector = new StringInput(terminal, input.getName(), input.getDefaultValue());
						selector.setResourceLoader(resourceLoader);
						selector.setTemplateExecutor(templateExecutor);
						if (StringUtils.hasText(input.getTemplateLocation())) {
							selector.setTemplateLocation(input.getTemplateLocation());
						}
						if (input.getRenderer() != null) {
							selector.setRenderer(input.getRenderer());
						}
						if (input.isStoreResult()) {
							if (input.getResultMode() == ResultMode.VERIFY && StringUtils.hasText(input.getResultValue())) {
								selector.addPreRunHandler(c -> {
									c.setDefaultValue(input.getResultValue());
								});
							}
							selector.addPostRunHandler(c -> {
								c.put(input.getId(), c.getResultValue());
							});
						}
						for (Consumer<StringInputContext> handler : input.getPreHandlers()) {
							selector.addPreRunHandler(handler);
						}
						for (Consumer<StringInputContext> handler : input.getPostHandlers()) {
							selector.addPostRunHandler(handler);
						}
						return selector.run(context);
				};
				return OrderedInputOperation.of(input.getOrder(), operation);
			});
		}

		private Stream<OrderedInputOperation> pathInputsStream() {
			return pathInputs.stream().map(input -> {
				Function<ComponentContext<?>, ComponentContext<?>> operation = (context) -> {
						if (input.getResultMode() == ResultMode.ACCEPT && input.isStoreResult()
								&& StringUtils.hasText(input.getResultValue())) {
							context.put(input.getId(), Path.of(input.getResultValue()));
							return context;
						}
						PathInput selector = new PathInput(terminal, input.getName());
						selector.setResourceLoader(resourceLoader);
						selector.setTemplateExecutor(templateExecutor);
						if (StringUtils.hasText(input.getTemplateLocation())) {
							selector.setTemplateLocation(input.getTemplateLocation());
						}
						if (input.getRenderer() != null) {
							selector.setRenderer(input.getRenderer());
						}
						if (input.isStoreResult()) {
							selector.addPostRunHandler(c -> {
								c.put(input.getId(), c.getResultValue());
							});
						}
						for (Consumer<PathInputContext> handler : input.getPreHandlers()) {
							selector.addPreRunHandler(handler);
						}
						for (Consumer<PathInputContext> handler : input.getPostHandlers()) {
							selector.addPostRunHandler(handler);
						}
						return selector.run(context);
				};
				return OrderedInputOperation.of(input.getOrder(), operation);
			});
		}

		private Stream<OrderedInputOperation> singleItemSelectorsStream() {
			return singleInputs.stream().map(input -> {
				Function<ComponentContext<?>, ComponentContext<?>> operation = (context) -> {
					if (input.getResultMode() == ResultMode.ACCEPT && input.isStoreResult()
							&& StringUtils.hasText(input.getResultValue())) {
						context.put(input.getId(), input.getResultValue());
						return context;
					}
					List<SelectorItem<String>> selectorItems = input.getSelectItems().entrySet().stream()
						.map(e -> SelectorItem.of(e.getKey(), e.getValue()))
						.collect(Collectors.toList());
					SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(terminal,
							selectorItems, input.getName(), input.getComparator());
					selector.setResourceLoader(resourceLoader);
					selector.setTemplateExecutor(templateExecutor);
					if (StringUtils.hasText(input.getTemplateLocation())) {
						selector.setTemplateLocation(input.getTemplateLocation());
					}
					if (input.getRenderer() != null) {
						selector.setRenderer(input.getRenderer());
					}
					if (input.getMaxItems() != null) {
						selector.setMaxItems(input.getMaxItems());
					}
					if (input.isStoreResult()) {
						selector.addPostRunHandler(c -> {
							c.getValue().ifPresent(v -> {
								c.put(input.getId(), v);
							});
						});
					}
					for (Consumer<SingleItemSelectorContext<String, SelectorItem<String>>> handler : input.getPreHandlers()) {
						selector.addPreRunHandler(handler);
					}
					for (Consumer<SingleItemSelectorContext<String, SelectorItem<String>>> handler : input.getPostHandlers()) {
						selector.addPostRunHandler(handler);
					}
					return selector.run(context);
				};
				return OrderedInputOperation.of(input.getOrder(), operation);
			});
		}

		private Stream<OrderedInputOperation> multiItemSelectorsStream() {
			return multiInputs.stream().map(input -> {
				Function<ComponentContext<?>, ComponentContext<?>> operation = (context) -> {
					if (input.getResultMode() == ResultMode.ACCEPT && input.isStoreResult()
							&& !ObjectUtils.isEmpty(input.getResultValues())) {
						context.put(input.getId(), input.getResultValues());
						return context;
					}
					List<SelectorItem<String>> selectorItems = input.getSelectItems().stream()
							.map(si -> SelectorItem.of(si.name(), si.item(), si.enabled()))
							.collect(Collectors.toList());
					MultiItemSelector<String, SelectorItem<String>> selector = new MultiItemSelector<>(terminal,
							selectorItems, input.getName(), input.getComparator());
					selector.setResourceLoader(resourceLoader);
					selector.setTemplateExecutor(templateExecutor);
					if (StringUtils.hasText(input.getTemplateLocation())) {
						selector.setTemplateLocation(input.getTemplateLocation());
					}
					if (input.getRenderer() != null) {
						selector.setRenderer(input.getRenderer());
					}
					if (input.getMaxItems() != null) {
						selector.setMaxItems(input.getMaxItems());
					}
					if (input.isStoreResult()) {
						selector.addPostRunHandler(c -> {
							c.put(input.getId(), c.getValues());
						});
					}
					for (Consumer<MultiItemSelectorContext<String, SelectorItem<String>>> handler : input.getPreHandlers()) {
						selector.addPreRunHandler(handler);
					}
					for (Consumer<MultiItemSelectorContext<String, SelectorItem<String>>> handler : input.getPostHandlers()) {
						selector.addPostRunHandler(handler);
					}
					return selector.run(context);
				};
				return OrderedInputOperation.of(input.getOrder(), operation);
			});
		}

		private static ComponentContext<?> chain(ComponentContext<?> context,
				Stream<Function<ComponentContext<?>, ComponentContext<?>>> operations) {
			for (Function<ComponentContext<?>, ComponentContext<?>> operation : operations.collect(Collectors.toList())) {
				context = operation.apply(context);
			}
			return context;
		}
	}

	static class OrderedInputOperation implements Ordered {

		private int order;
		private Function<ComponentContext<?>, ComponentContext<?>> operation;

		@Override
		public int getOrder() {
			return order;
		}

		public Function<ComponentContext<?>, ComponentContext<?>> getOperation() {
			return operation;
		}

		static OrderedInputOperation of(int order, Function<ComponentContext<?>, ComponentContext<?>> operation) {
			OrderedInputOperation oio = new OrderedInputOperation();
			oio.order = order;
			oio.operation = operation;
			return oio;
		}
	}
}
