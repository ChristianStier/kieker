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
package kieker.test.tools.junit.bridge;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import kieker.common.configuration.Configuration;
import kieker.monitoring.core.configuration.ConfigurationFactory;
import kieker.monitoring.writer.filesystem.AbstractAsyncFSWriter;
import kieker.monitoring.writer.filesystem.AsyncFsWriter;
import kieker.tools.bridge.ServiceContainer;
import kieker.tools.bridge.connector.ConnectorDataTransmissionException;

import kieker.test.common.junit.AbstractKiekerTest;

/**
 * @author Pascale Brandt
 * @since 1.8
 */
public class ConnectorTest extends AbstractKiekerTest {

	/**
	 * Folder for temporary data. Is used to delete every generated file after the test completed.
	 */
	@Rule
	public final TemporaryFolder tmpFolder = new TemporaryFolder(); // NOCS recommends that this is private. JUnit test wants this public.

	/**
	 * Nothing to initialize.
	 */
	public ConnectorTest() {
		// Why do we need this?
	}

	/**
	 * Test the ServiceContainer.
	 * 
	 * @throws IOException
	 *             if an IO error occurs
	 * @throws ConnectorDataTransmissionException
	 *             if an exception occurs in ServiceContainer or the TestServiceConnector
	 */
	@Test
	public void testServiceContainer() throws IOException, ConnectorDataTransmissionException {

		/**
		 * 1st parameter: Configuration is in CLIServerMain
		 * next steps stays in CLIServerMain//
		 * First element is a default configuration like in CLIServerMain(),
		 * the second part starts a new record which is written in the TestServiceConnector class.
		 * 
		 */

		final File path = this.tmpFolder.getRoot();

		final Configuration configuration = ConfigurationFactory.createDefaultConfiguration();

		final String writer = AsyncFsWriter.class.getName();
		configuration.setProperty(writer + '.' + AbstractAsyncFSWriter.CONFIG_MAXENTRIESINFILE, "1");
		// The maximal size of the log file must be greater than the expected number of log entries to ensure, that the framework allows to write more records, which
		// we then can detect as failures. Otherwise writing more than expected records would be hindered by the framework itself.
		configuration.setProperty(writer + '.' + AbstractAsyncFSWriter.CONFIG_MAXLOGFILES, String.valueOf(TestServiceConnector.SEND_NUMBER_OF_RECORDS * 2));
		configuration.setProperty(writer + '.' + AbstractAsyncFSWriter.CONFIG_MAXLOGSIZE, "-1");
		configuration.setProperty(writer + '.' + AbstractAsyncFSWriter.CONFIG_TEMP, "false");
		configuration.setProperty(writer + '.' + AbstractAsyncFSWriter.CONFIG_PATH, path.getCanonicalPath());

		// Create the service container and deploy the TestServiceConnector.
		final ServiceContainer serviceContainer = new ServiceContainer(configuration,
				new TestServiceConnector(), false);

		// Run the service
		serviceContainer.run();

		// Check number of written records.
		// logDirs should contain one Kieker records folders.
		final File[] logDirs = new File(this.tmpFolder.getRoot().getCanonicalPath()).listFiles();
		Assert.assertEquals("Should contain one folder.", 1, logDirs.length);
		Assert.assertTrue("The first entry must be the kieker directory.", logDirs[0].isDirectory());

		// enter that folder and count the number of files.
		final int numberOfLogFiles = new File(logDirs[0].getCanonicalPath()).listFiles().length;

		// The result contains 20 data records, 1 record containing the version field and 1 kieker map file
		Assert.assertEquals("The number of send records is not equal to TestServiceConnector.SEND_NUMBER_OF_RECORDS",
				TestServiceConnector.SEND_NUMBER_OF_RECORDS + 2, numberOfLogFiles);

		// now dump the temporary folder and all its content
		this.tmpFolder.delete();

		// Assert that the tempfolder does NOT exist (hey this is just for the paranoid, actually we are testing here Java API)
		Assert.assertFalse("Directory is not cleaned.", this.tmpFolder.getRoot().exists());
	}

}
