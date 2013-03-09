/***************************************************************************
 * Copyright 2013 Kieker Project (http://kieker-monitoring.net)
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

package kieker.test.tools.junit.writeRead.filesystem.unknownTypes;

import java.util.List;

import org.junit.Assert;

import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.common.configuration.Configuration;
import kieker.common.exception.MonitoringRecordException;
import kieker.common.logging.LogImplJUnit;
import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.writer.IMonitoringWriter;
import kieker.monitoring.writer.filesystem.AsyncFsWriter;

/**
 * 
 * @author Andre van Hoorn
 * 
 */
public class ContinueAfterUnknownTypeRegularFileTest extends AbstractUnknownTypeTest { // NOPMD NOCS (TestClassWithoutTestCases)
	@Override
	protected Class<? extends IMonitoringWriter> getTestedWriterClazz() {
		return AsyncFsWriter.class;
	}

	@Override
	protected void refineWriterConfiguration(final Configuration config, final int numRecordsWritten) {
		config.setProperty(this.getClass().getName() + "." + AsyncFsWriter.CONFIG_FLUSH, Boolean.TRUE.toString());
		LogImplJUnit.disableThrowable(MonitoringRecordException.class);
	}

	@Override
	protected void inspectRecords(final List<IMonitoringRecord> eventsPassedToController, final List<IMonitoringRecord> eventFromMonitoringLog) {
		// we expect that EVENT1_UNKNOWN_TYPE and EVENT3_UNKNOWN_TYPE are simply ignored
		Assert.assertEquals("Expected one record", 2, eventFromMonitoringLog.size());
		Assert.assertEquals("Unexpected record", EVENT0_KNOWN_TYPE, eventFromMonitoringLog.get(0));
		Assert.assertEquals("Unexpected record", EVENT2_KNOWN_TYPE, eventFromMonitoringLog.get(1));
		LogImplJUnit.reset();
	}

	/**
	 * Here, we make sure that the reader aborts on the first occurrence of an unknown type.
	 * 
	 * @param config
	 *            The configuration to modify.
	 */
	@Override
	protected void refineFSReaderConfiguration(final Configuration config) {
		config.setProperty(FSReader.CONFIG_PROPERTY_NAME_IGNORE_UNKNOWN_RECORD_TYPES, Boolean.TRUE.toString());
	}
}
