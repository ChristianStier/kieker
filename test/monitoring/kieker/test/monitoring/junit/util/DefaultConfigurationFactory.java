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

package kieker.test.monitoring.junit.util;

import kieker.common.configuration.Configuration;
import kieker.monitoring.core.configuration.ConfigurationFactory;
import kieker.monitoring.writer.DummyWriter;

/**
 * @author Andre van Hoorn, Jan Waller
 * 
 */
public final class DefaultConfigurationFactory {

	public static final String WRITER_NAME = DummyWriter.class.getName();

	private DefaultConfigurationFactory() {}

	public static Configuration createDefaultConfigurationWithDummyWriter() {
		final Configuration configuration = ConfigurationFactory.createDefaultConfiguration();
		configuration.setProperty(ConfigurationFactory.CONTROLLER_NAME, "jUnit");
		configuration.setProperty(ConfigurationFactory.WRITER_CLASSNAME, DefaultConfigurationFactory.WRITER_NAME);
		configuration.setProperty(ConfigurationFactory.PREFIX + "jUnit", "true");
		configuration.setProperty(DefaultConfigurationFactory.WRITER_NAME + ".jUnit", "true");
		return configuration;
	}
}
