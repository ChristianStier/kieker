/***************************************************************************
 * Copyright 2014 Kicker Project (http://kicker-monitoring.net)
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

package kicker.common.logging;

/**
 * This is an actual implementation of the logging interface used by the JDK 14 logger.
 * 
 * @author Jan Waller
 * 
 * @since 1.5
 */
public final class LogImplJDK14 implements Log {
	private final java.util.logging.Logger logger; // NOPMD (Implementation of an logger)
	private final String name;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param name
	 *            The name of the logger.
	 */
	protected LogImplJDK14(final String name) {
		this.name = name;
		this.logger = java.util.logging.Logger.getLogger(name);
	}

	private final void log(final java.util.logging.Level level, final String message, final Throwable t) {
		if (this.logger.isLoggable(level)) {
			final String sourceClass = this.name;
			final String sourceMethod;
			{ // NOCS detect calling class and method
				final StackTraceElement[] stackArray = new Throwable().getStackTrace(); // NOPMD (throwable)
				if (stackArray.length > 2) { // our stackDepth
					sourceMethod = stackArray[2].getMethodName();
				} else {
					sourceMethod = "unknown";
				}
			}
			if (t != null) {
				this.logger.logp(level, sourceClass, sourceMethod, message, t);
			} else {
				this.logger.logp(level, sourceClass, sourceMethod, message);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isDebugEnabled() {
		return this.logger.isLoggable(java.util.logging.Level.FINE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void debug(final String message) {
		this.log(java.util.logging.Level.FINE, message, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void debug(final String message, final Throwable t) {
		this.log(java.util.logging.Level.FINE, message, t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void info(final String message) {
		this.log(java.util.logging.Level.INFO, message, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void info(final String message, final Throwable t) {
		this.log(java.util.logging.Level.INFO, message, t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void warn(final String message) {
		this.log(java.util.logging.Level.WARNING, message, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void warn(final String message, final Throwable t) {
		this.log(java.util.logging.Level.WARNING, message, t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void error(final String message) {
		this.log(java.util.logging.Level.SEVERE, message, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void error(final String message, final Throwable t) {
		this.log(java.util.logging.Level.SEVERE, message, t);
	}
}
