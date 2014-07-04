/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
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

package kieker.tools.loggingTimestampConverter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.tools.AbstractCommandLineTool;
import kieker.tools.util.LoggingTimestampConverter;

/**
 * This tool can be used to convert timestamps.
 * 
 * @author Andre van Hoorn, Nils Christian Ehmke
 * 
 * @since 1.1
 */
public final class LoggingTimestampConverterTool extends AbstractCommandLineTool {

	private static final Log LOG = LogFactory.getLog(LoggingTimestampConverterTool.class);
	private static final char FLAG_TIMESTAMPS_PARAMETER = 't';

	private long[] timestampsLong;

	private LoggingTimestampConverterTool() {
		super(true);
	}

	public static void main(final String[] args) {
		new LoggingTimestampConverterTool().start(args);
	}

	@Override
	@SuppressWarnings("static-access")
	protected void addAdditionalOptions(final Options options) {
		options.addOption(OptionBuilder.withLongOpt("timestamps").withArgName("timestamp1 ... timestampN").hasArgs().isRequired()
				.withDescription("List of timestamps (UTC timezone) to convert").create(FLAG_TIMESTAMPS_PARAMETER));
	}

	@Override
	protected boolean readPropertiesFromCommandLine(final CommandLine commandLine) {
		final String[] timestampsStr = commandLine.getOptionValues(FLAG_TIMESTAMPS_PARAMETER);
		this.timestampsLong = new long[timestampsStr.length];

		for (int curIdx = 0; curIdx < timestampsStr.length; curIdx++) {
			try {
				this.timestampsLong[curIdx] = Long.parseLong(timestampsStr[curIdx]);
			} catch (final NumberFormatException ex) {
				LOG.error("Failed to parse timestamp: " + timestampsStr[curIdx], ex);
				return false;
			}
		}

		return true;
	}

	@Override
	protected boolean performTask() {
		final String lineSeperator = System.getProperty("line.separator");
		final int estimatedNumberOfChars = this.timestampsLong.length * 85;
		final StringBuilder stringBuilder = new StringBuilder(estimatedNumberOfChars);

		for (final long timestampLong : this.timestampsLong) {
			stringBuilder.append(timestampLong).append(": ").append(LoggingTimestampConverter.convertLoggingTimestampToUTCString(timestampLong));
			stringBuilder.append(" (").append(LoggingTimestampConverter.convertLoggingTimestampLocalTimeZoneString(timestampLong)).append(')').append(lineSeperator);
		}

		System.out.print(stringBuilder.toString()); // NOPMD (System.out)

		return true;
	}

}
