/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
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

package kieker.tools.traceAnalysis.plugins.executionFilter;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import kieker.analysis.plugin.AbstractAnalysisPlugin;
import kieker.analysis.plugin.port.AbstractInputPort;
import kieker.analysis.plugin.port.OutputPort;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.systemModel.Execution;

/**
 * Allows to filter Execution objects based on their traceId.<br>
 * 
 * This class has exactly one input port named "in" and one output ports named
 * "out". It receives only objects inheriting from the class {@link Execution}.
 * If the received object contains the defined traceID, the object is delivered
 * unmodified to the output port.
 * 
 * @author Andre van Hoorn
 */
public class TraceIdFilter extends AbstractAnalysisPlugin {

	public static final String CONFIG_SELECTED_TRACES = TraceIdFilter.class.getName() + ".selectedTraces";
	private final Set<Long> selectedTraces;

	private final OutputPort executionOutputPort = new OutputPort("Execution output", new CopyOnWriteArrayList<Class<?>>(new Class<?>[] { Execution.class }));

	public TraceIdFilter(final Configuration configuration) {
		super(configuration);
		// TODO: Initialize from the variable.
		this.selectedTraces = null;

		super.registerInputPort("in", this.executionInputPort);
		super.registerOutputPort("out", this.executionOutputPort);
	}

	/**
	 * Creates a filter instance that only passes Execution objects <i>e</i>
	 * whose traceId (<i>e.traceId</i>) is element of the set <i>selectedTraces</i>.
	 * 
	 * @param selectedTraces
	 */
	public TraceIdFilter(final Set<Long> selectedTraces) {
		super(new Configuration(null));
		this.selectedTraces = selectedTraces;

		super.registerOutputPort("out", this.executionOutputPort);
	}

	public AbstractInputPort getExecutionInputPort() {
		return this.executionInputPort;
	}

	private final AbstractInputPort executionInputPort = new AbstractInputPort("Execution input",
			Collections.unmodifiableCollection(new CopyOnWriteArrayList<Class<?>>(
					new Class<?>[] { Execution.class }))) {

		@Override
		public void newEvent(final Object obj) {
			TraceIdFilter.this.newExecution((Execution) obj);
		}
	};

	public OutputPort getExecutionOutputPort() {
		return this.executionOutputPort;
	}

	private void newExecution(final Execution execution) {
		if ((this.selectedTraces != null) && !this.selectedTraces.contains(execution.getTraceId())) {
			// not interested in this trace
			return;
		}
		this.executionOutputPort.deliver(execution);
	}

	@Override
	public boolean execute() {
		return true; // do nothing
	}

	@Override
	public void terminate(final boolean error) {
		// do nothing
	}

	@Override
	protected Configuration getDefaultConfiguration() {
		final Configuration defaultConfiguration = new Configuration();
		// TODO: Provide default properties.
		return defaultConfiguration;
	}

	@Override
	public Configuration getCurrentConfiguration() {
		final Configuration configuration = new Configuration(null);

		if (this.selectedTraces != null) {
			final String selectedTracesArr[] = new String[this.selectedTraces.size()];
			final Iterator<Long> iter = this.selectedTraces.iterator();
			int i = 0;
			while (iter.hasNext()) {
				selectedTracesArr[i++] = iter.next().toString();
			}
			configuration.setProperty(TraceIdFilter.CONFIG_SELECTED_TRACES, Configuration.toProperty(selectedTracesArr));
		}
		return configuration;
	}
}
