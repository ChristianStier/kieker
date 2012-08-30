/***************************************************************************
 * Copyright 2012 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.analysis.plugin.filter.select;

import java.util.LinkedList;
import java.util.List;

import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.OutputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;

/**
 * This filter has exactly one input port and one output port.
 * 
 * Only the specified objects are forwarded to the output port.
 * All other objects are forwarded to the output-not port.
 * 
 * @author Jan Waller
 */
@Plugin(description = "Filters incoming objects based on their type",
		outputPorts = {
			@OutputPort(name = TypeFilter.OUTPUT_PORT_NAME_TYPE_MATCH, eventTypes = { Object.class }, description = "Forwards events matching the configured types"),
			@OutputPort(name = TypeFilter.OUTPUT_PORT_NAME_TYPE_MISMATCH, eventTypes = {}, description = "Forwards events not matching the configured types")
		})
public final class TypeFilter extends AbstractFilterPlugin {

	public static final String INPUT_PORT_NAME_EVENTS = "events";

	public static final String OUTPUT_PORT_NAME_TYPE_MATCH = "eventsMatchingType";
	public static final String OUTPUT_PORT_NAME_TYPE_MISMATCH = "eventsNotMatchingType";

	public static final String CONFIG_PROPERTY_NAME_TYPES = "types";

	private static final Log LOG = LogFactory.getLog(TypeFilter.class);

	private final Class<?>[] acceptedClasses;

	public TypeFilter(final Configuration configuration) {
		super(configuration);
		final String[] classes = configuration.getStringArrayProperty(CONFIG_PROPERTY_NAME_TYPES);
		final List<Class<?>> listOfClasses = new LinkedList<Class<?>>();
		for (final String clazz : classes) {
			try {
				listOfClasses.add(Class.forName(clazz));
			} catch (final ClassNotFoundException ex) {
				LOG.warn("Failed to add class " + clazz + " to the filter.", ex);
			}
		}
		this.acceptedClasses = listOfClasses.toArray(new Class<?>[listOfClasses.size()]);
	}

	@Override
	protected final Configuration getDefaultConfiguration() {
		final Configuration configuration = new Configuration();
		configuration.setProperty(CONFIG_PROPERTY_NAME_TYPES, "java.lang.Object");
		return configuration;
	}

	public final Configuration getCurrentConfiguration() {
		final Configuration configuration = new Configuration();
		final String[] acceptedClassesConfig = new String[this.acceptedClasses.length];
		for (int i = 0; i < acceptedClassesConfig.length; i++) {
			acceptedClassesConfig[i] = this.acceptedClasses[i].getName();
		}
		configuration.setProperty(CONFIG_PROPERTY_NAME_TYPES, Configuration.toProperty(acceptedClassesConfig));
		return configuration;
	}

	@InputPort(name = INPUT_PORT_NAME_EVENTS, eventTypes = { Object.class }, description = "all objects with matching types are forwarded")
	public final void inputEvents(final Object event) {
		final Class<?> eventClass = event.getClass();
		for (final Class<?> clazz : this.acceptedClasses) {
			if (clazz.isAssignableFrom(eventClass)) {
				super.deliver(OUTPUT_PORT_NAME_TYPE_MATCH, event);
				break; // only deliver once!
			}
		}
		super.deliver(OUTPUT_PORT_NAME_TYPE_MISMATCH, event);
	}
}
