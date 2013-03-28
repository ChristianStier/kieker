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

package kieker.monitoring.writer.filesystem;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;

import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.util.filesystem.FSUtil;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.writer.AbstractAsyncWriter;
import kieker.monitoring.writer.filesystem.async.AbstractFsWriterThread;
import kieker.monitoring.writer.filesystem.map.MappingFileWriter;

/**
 * @author Matthias Rohr, Robert von Massow, Andre van Hoorn, Jan Waller
 * 
 * @since 1.5
 */
public abstract class AbstractAsyncFSWriter extends AbstractAsyncWriter {
	public static final String CONFIG_PATH = "customStoragePath";
	public static final String CONFIG_TEMP = "storeInJavaIoTmpdir";
	public static final String CONFIG_MAXENTRIESINFILE = "maxEntriesInFile";
	public static final String CONFIG_MAXLOGSIZE = "maxLogSize"; // in MiB
	public static final String CONFIG_MAXLOGFILES = "maxLogFiles";

	private final String configPath;
	private final int configMaxEntriesInFile;
	private final int configMaxlogSize;
	private final int configMaxLogFiles;

	protected AbstractAsyncFSWriter(final Configuration configuration) {
		super(configuration);
		final String prefix = this.getClass().getName() + '.';
		// Determine path
		String path;
		if (configuration.getBooleanProperty(prefix + CONFIG_TEMP)) {
			path = System.getProperty("java.io.tmpdir");
		} else {
			path = configuration.getStringProperty(prefix + CONFIG_PATH);
		}
		if (!(new File(path)).isDirectory()) {
			throw new IllegalArgumentException("'" + path + "' is not a directory.");
		}
		this.configPath = path;
		// get number of entries per file
		this.configMaxEntriesInFile = configuration.getIntProperty(prefix + CONFIG_MAXENTRIESINFILE);
		if (this.configMaxEntriesInFile < 1) {
			throw new IllegalArgumentException(prefix + CONFIG_MAXENTRIESINFILE + " must be greater than 0 but is '" + this.configMaxEntriesInFile + "'");
		}
		// get values for size limitations
		this.configMaxlogSize = configuration.getIntProperty(prefix + CONFIG_MAXLOGSIZE);
		this.configMaxLogFiles = configuration.getIntProperty(prefix + CONFIG_MAXLOGFILES);

	}

	/**
	 * {@inheritDoc} Make sure that the required properties always have default values!
	 */
	@Override
	protected Configuration getDefaultConfiguration() {
		final Configuration configuration = new Configuration(super.getDefaultConfiguration());
		final String prefix = this.getClass().getName() + "."; // can't use this.prefix, maybe uninitialized
		configuration.setProperty(prefix + CONFIG_PATH, ".");
		configuration.setProperty(prefix + CONFIG_TEMP, "true");
		configuration.setProperty(prefix + CONFIG_MAXENTRIESINFILE, "25000");
		configuration.setProperty(prefix + CONFIG_MAXLOGSIZE, "-1");
		configuration.setProperty(prefix + CONFIG_MAXLOGFILES, "-1");
		return configuration;
	}

	@Override
	protected final void init() throws Exception {
		// Determine directory for files
		final String ctrlName = super.monitoringController.getHostname() + "-" + super.monitoringController.getName();
		final DateFormat date = new SimpleDateFormat("yyyyMMdd'-'HHmmssSSS", Locale.US);
		date.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String dateStr = date.format(new java.util.Date()); // NOPMD (Date)
		final StringBuffer sb = new StringBuffer(this.configPath.length() + FSUtil.FILE_PREFIX.length() + ctrlName.length() + 26);
		sb.append(this.configPath).append(File.separatorChar).append(FSUtil.FILE_PREFIX).append('-').append(dateStr).append("-UTC-").append(ctrlName)
				.append(File.separatorChar);
		final String path = sb.toString();
		final File f = new File(path);
		if (!f.mkdir()) {
			throw new IllegalArgumentException("Failed to create directory '" + path + "'");
		}
		// Mapping file
		final MappingFileWriter mappingFileWriter = new MappingFileWriter(path);
		// Create writer thread
		this.addWorker(this.initWorker(super.monitoringController, this.blockingQueue, mappingFileWriter, path, this.configMaxEntriesInFile, this.configMaxlogSize,
				this.configMaxLogFiles));
	}

	protected abstract AbstractFsWriterThread initWorker(final IMonitoringController monitoringController, final BlockingQueue<IMonitoringRecord> writeQueue,
			final MappingFileWriter mappingFileWriter, final String path, final int maxEntiresInFile, final int maxlogSize, final int maxLogFiles);
}
