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

package kieker.monitoring.core.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import kieker.common.configuration.Configuration;
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.monitoring.core.controller.MonitoringController;

/**
 * A ConfigurationFactory for kieker.monitoring
 * 
 * @author Andre van Hoorn, Jan Waller
 */
public final class ConfigurationFactory implements Keys {
	private static final Log LOG = LogFactory.getLog(ConfigurationFactory.class);

	private ConfigurationFactory() {}

	/*
	 * factory methods
	 */

	/**
	 * Creates the configuration for the singleton controller instance. Note
	 * that the {@link Properties} returned by this method are not a
	 * singleton instance, i.e., each call returns an equal but not same set of {@link Properties}.
	 * 
	 * @return the configuration for the singleton controller
	 */
	public static final Configuration createSingletonConfiguration() {
		if (ConfigurationFactory.LOG.isDebugEnabled()) {
			ConfigurationFactory.LOG.debug("Searching for JVM argument '" + Keys.CUSTOM_PROPERTIES_LOCATION_JVM + "' ...");
		}
		final Configuration defaultConfiguration = ConfigurationFactory.defaultConfiguration();
		// ignore default default-name and set to KIEKER-SINGLETON
		defaultConfiguration.setProperty(Keys.CONTROLLER_NAME, "KIEKER-SINGLETON");
		// Searching for configuration file location passed to JVM
		String configurationFile = System.getProperty(Keys.CUSTOM_PROPERTIES_LOCATION_JVM);
		final Configuration loadConfiguration;
		if (configurationFile != null) {
			ConfigurationFactory.LOG.info("Loading configuration from JVM-specified location: '" + configurationFile + "'"); // NOCS (MultipleStringLiteralsCheck)
			loadConfiguration = ConfigurationFactory.loadConfigurationFromFile(configurationFile, defaultConfiguration);
		} else {
			// No JVM property; Trying to find configuration file in classpath
			configurationFile = Keys.CUSTOM_PROPERTIES_LOCATION_CLASSPATH;
			ConfigurationFactory.LOG.info("Loading properties from properties file in classpath: '" + configurationFile + "'");
			loadConfiguration = ConfigurationFactory.loadConfigurationFromResource(configurationFile, defaultConfiguration);
		}
		// 1.JVM-params -> 2.properties file -> 3.default properties file
		return ConfigurationFactory.getSystemPropertiesStartingWith(Keys.PREFIX, loadConfiguration);
	}

	/**
	 * Returns an empty properties map with a fallback on the default configuration.
	 * 
	 * @return default configuration
	 */
	public static final Configuration createDefaultConfiguration() {
		return new Configuration(ConfigurationFactory.defaultConfiguration());
	}

	/**
	 * Creates a new configuration based on the given properties file with fallback on the default values.
	 * If the file does not exists, a warning is logged and an empty configuration with fallback on
	 * the default configuration is returned.
	 * 
	 * @param configurationFile
	 * @return the created Configuration
	 */
	public static final Configuration createConfigurationFromFile(final String configurationFile) {
		return ConfigurationFactory.loadConfigurationFromFile(configurationFile, ConfigurationFactory.defaultConfiguration());
	}

	/**
	 * Returns a properties map with the default configuration.
	 * 
	 * @return
	 */
	private static final Configuration defaultConfiguration() {
		return ConfigurationFactory.loadConfigurationFromResource(Keys.DEFAULT_PROPERTIES_LOCATION_CLASSPATH, null);
	}

	/**
	 * Returns the properties loaded from file propertiesFn with fallback on the default values.
	 * If the file does not exists, a warning is logged and an empty configuration with fallback on
	 * the default configuration is returned.
	 * 
	 * @param propertiesFn
	 * @param defaultValues
	 * @return
	 */
	private static final Configuration loadConfigurationFromFile(final String propertiesFn, final Configuration defaultValues) {
		final Configuration properties = new Configuration(defaultValues);
		InputStream is = null; // NOPMD
		try {
			try {
				is = new FileInputStream(propertiesFn);
			} catch (final FileNotFoundException ex) {
				// if not found as absolute path try within the classpath
				is = MonitoringController.class.getClassLoader().getResourceAsStream(propertiesFn);
				if (is == null) {
					ConfigurationFactory.LOG.warn("File '" + propertiesFn + "' not found"); // NOCS (MultipleStringLiteralsCheck)
					return new Configuration(defaultValues);
				}
			}
			properties.load(is);
			return properties;
		} catch (final Exception ex) { // NOCS (IllegalCatchCheck) // NOPMD
			ConfigurationFactory.LOG.error("Error reading file '" + propertiesFn + "'", ex); // NOCS (MultipleStringLiteralsCheck)
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final IOException ex) {
					ConfigurationFactory.LOG.warn("Failed to close FileInputStream", ex);
				}
			}
		}
		return new Configuration(defaultValues);
	}

	/**
	 * Returns the properties loaded from the resource name with fallback on the default values.
	 * If the file does not exists, a warning is logged and an empty configuration with fallback on
	 * the default configuration is returned.
	 * 
	 * @param propertiesFn
	 * @param defaultValues
	 * @return
	 */
	private static final Configuration loadConfigurationFromResource(final String propertiesFn, final Configuration defaultValues) {
		final InputStream is = MonitoringController.class.getClassLoader().getResourceAsStream(propertiesFn);
		if (is == null) {
			ConfigurationFactory.LOG.warn("File '" + propertiesFn + "' not found in classpath"); // NOCS (MultipleStringLiteralsCheck)
		} else {
			try {
				final Configuration properties = new Configuration(defaultValues);
				properties.load(is);
				return properties;
			} catch (final Exception ex) { // NOCS (IllegalCatchCheck) // NOPMD
				ConfigurationFactory.LOG.error("Error reading file '" + propertiesFn + "'", ex); // NOCS (MultipleStringLiteralsCheck)
			} finally {
				try {
					is.close();
				} catch (final IOException ex) {
					ConfigurationFactory.LOG.warn("Failed to close RessourceInputStream", ex);
				}
			}
		}
		return new Configuration(defaultValues);
	}

	/**
	 * Returns the system properties starting with prefix.
	 * 
	 * @param prefix
	 * @param defaultValues
	 * @return
	 */
	private static final Configuration getSystemPropertiesStartingWith(final String prefix, final Configuration defaultValues) {
		final Configuration configuration = new Configuration(defaultValues);
		final Properties properties = System.getProperties();
		final Enumeration<?> keys = properties.propertyNames();
		while (keys.hasMoreElements()) {
			final String property = (String) keys.nextElement();
			if (property.startsWith(prefix)) {
				configuration.setProperty(property, properties.getProperty(property));
			}
		}
		return configuration;
	}
}
