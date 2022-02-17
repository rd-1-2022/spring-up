/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.up.util;

import java.util.LinkedHashMap;

import org.jline.terminal.Terminal;

import org.springframework.shell.table.BeanListTableModel;
import org.springframework.shell.table.BorderSpecification;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.CellMatchers;
import org.springframework.shell.table.SimpleHorizontalAligner;
import org.springframework.shell.table.SimpleVerticalAligner;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.shell.table.Tables;
import org.springframework.stereotype.Component;

/**
 * Given a list of JavaBean objects, print and ASCII art table based on the JavaBean
 * properties.
 *
 * @author Ilayaperumal Gopinathan
 * @author Eric Bottard
 * @author Janne Valkealahti
 * @author Mark Pollack
 */
@Component
public class JavaBeanTableCreator {

	private Terminal terminal;

	/**
	 * Construct a new ListTableCreator, given the JLine Terminal singleton instance so
	 * that the width of the current terminal can be determined.
	 * @param terminal the terminal to obtain width information from
	 */
	public JavaBeanTableCreator(Terminal terminal) {
		this.terminal = terminal;
	}

	/**
	 * Given a List of JavaBean objects and a Map of JavaBean property names as keys and a
	 * description of the property name as a value, create an ASCII table and print it to
	 * the terminal.
	 * @param list A list of JavaBean object
	 * @param headers A map of JavaBean property names and column header description
	 * @param <T> JavaBean object type
	 */
	public <T> void createTable(Iterable<T> list, LinkedHashMap<String, Object> headers) {
		TableModel model = new BeanListTableModel<>(list, headers);
		TableBuilder tableBuilder = new TableBuilder(model);
		Table table = applyStyle(tableBuilder).build();
	}

	/**
	 * Customize the given TableBuilder with the following common features (these choices
	 * can always be overridden by applying later customizations) :
	 * <ul>
	 * <li>double border around the whole table and first row</li>
	 * <li>vertical space (air) borders, single line separators between rows</li>
	 * <li>first row is assumed to be a header and is centered horizontally and
	 * vertically</li>
	 * <li>cells containing Map values are rendered as {@literal key = value} lines,
	 * trying to align on equal signs</li>
	 * </ul>
	 * @param builder the table builder to use
	 * @return the configured table builder
	 */
	public TableBuilder applyStyle(TableBuilder builder) {
		builder.addOutlineBorder(BorderStyle.fancy_double)
				.paintBorder(BorderStyle.air, BorderSpecification.INNER_HORIZONTAL).fromTopLeft().toBottomRight()
				.paintBorder(BorderStyle.fancy_light, BorderSpecification.INNER_VERTICAL).fromTopLeft().toBottomRight()
				.addHeaderBorder(BorderStyle.fancy_double).on(CellMatchers.row(0))
				.addAligner(SimpleVerticalAligner.middle).addAligner(SimpleHorizontalAligner.center);
		return Tables.configureKeyValueRendering(builder, " = ");
	}

	/**
	 * Customize the given TableBuilder with almost same way than
	 * {@link #applyStyle(TableBuilder)} but do not use any header styling.
	 * @param builder the table builder to use
	 * @return the configured table builder
	 */
	public TableBuilder applyStyleNoHeader(TableBuilder builder) {
		builder.addOutlineBorder(BorderStyle.fancy_double)
				.paintBorder(BorderStyle.air, BorderSpecification.INNER_VERTICAL).fromTopLeft().toBottomRight()
				.paintBorder(BorderStyle.fancy_light, BorderSpecification.INNER_VERTICAL).fromTopLeft().toBottomRight();
		return Tables.configureKeyValueRendering(builder, " = ");
	}

}
