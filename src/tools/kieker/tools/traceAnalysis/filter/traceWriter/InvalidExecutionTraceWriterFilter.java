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

package kieker.tools.traceAnalysis.filter.traceWriter;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.annotation.RepositoryPort;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.tools.traceAnalysis.Constants;
import kieker.tools.traceAnalysis.filter.AbstractInvalidExecutionTraceProcessingFilter;
import kieker.tools.traceAnalysis.filter.AbstractTraceAnalysisFilter;
import kieker.tools.traceAnalysis.systemModel.InvalidExecutionTrace;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;

/**
 * 
 * @author Andre van Hoorn
 */
@Plugin(description = "A filter allowing to write the incoming InvalidExecutionTraces into a configured file",
		repositoryPorts = @RepositoryPort(name = AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, repositoryType = SystemModelRepository.class))
public class InvalidExecutionTraceWriterFilter extends AbstractInvalidExecutionTraceProcessingFilter {

	public static final String INPUT_PORT_NAME_INVALID_EXECUTION_TRACES = "invalidExecutionTraces";

	public static final String CONFIG_PROPERTY_NAME_OUTPUT_FN = "outputFn";

	private static final Log LOG = LogFactory.getLog(InvalidExecutionTraceWriterFilter.class);

	private static final String ENCODING = "UTF-8";

	private final String outputFn;
	private final BufferedWriter ps;

	public InvalidExecutionTraceWriterFilter(final Configuration configuration)
			throws IOException {
		super(configuration);
		this.outputFn = configuration.getStringProperty(CONFIG_PROPERTY_NAME_OUTPUT_FN);
		this.ps = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFn), ENCODING));
	}

	@Override
	public void printStatusMessage() {
		super.printStatusMessage();
		final int numTraces = this.getSuccessCount();
		this.stdOutPrintln("Wrote " + numTraces + " execution trace artifact" + (numTraces > 1 ? "s" : "") + " to file '" + this.outputFn + "'"); // NOCS
	}

	@Override
	public void terminate(final boolean error) {
		if (this.ps != null) {
			try {
				this.ps.close();
			} catch (final IOException ex) {
				LOG.error("IOException while terminating", ex);
			}
		}
	}

	@Override
	public String getInvalidExecutionTraceInputPortName() {
		return INPUT_PORT_NAME_INVALID_EXECUTION_TRACES;
	}

	@InputPort(
			name = INPUT_PORT_NAME_INVALID_EXECUTION_TRACES,
			description = "Receives the invalid execution traces to be written", eventTypes = { InvalidExecutionTrace.class })
	public void newInvalidExecutionTrace(final InvalidExecutionTrace et) {
		try {
			InvalidExecutionTraceWriterFilter.this.ps.append(et.getInvalidExecutionTraceArtifacts().toString()).append(AbstractFilterPlugin.SYSTEM_NEWLINE_STRING);
			InvalidExecutionTraceWriterFilter.this.reportSuccess(et.getInvalidExecutionTraceArtifacts().getTraceId());
		} catch (final IOException ex) {
			InvalidExecutionTraceWriterFilter.this.reportError(et.getInvalidExecutionTraceArtifacts().getTraceId());
			LOG.error("IOException", ex);
		}
	}

	@Override
	protected Configuration getDefaultConfiguration() {
		final DateFormat date = new SimpleDateFormat("yyyyMMdd'-'HHmmssSSS", Locale.US);
		date.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String dateStr = date.format(new java.util.Date()); // NOPMD (Date)
		final String defaultFn = Constants.INVALID_TRACES_FN_PREFIX + "-" + dateStr + ".txt";

		final Configuration configuration = new Configuration();
		configuration.setProperty(CONFIG_PROPERTY_NAME_OUTPUT_FN, defaultFn);
		return configuration;
	}

	public Configuration getCurrentConfiguration() {
		final Configuration configuration = new Configuration();

		configuration.setProperty(CONFIG_PROPERTY_NAME_OUTPUT_FN, this.outputFn);

		return configuration;
	}

}
