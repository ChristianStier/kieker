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

package kieker.common.record.factory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import kieker.common.exception.MonitoringRecordException;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.factory.old.RecordFactoryWrapper;

/**
 * @author Christian Wulf
 *
 * @since 1.10
 */
public class CachedRecordFactoryRepository {

	private final ConcurrentMap<String, IRecordFactory<? extends IMonitoringRecord>> recordFactories = new ConcurrentHashMap<String, IRecordFactory<? extends IMonitoringRecord>>();
	private final RecordFactoryRepository recordFactoryRepository;

	public CachedRecordFactoryRepository(final RecordFactoryRepository recordFactoryRepository) {
		this.recordFactoryRepository = recordFactoryRepository;
	}

	/**
	 * @param recordClassName
	 * @return a cached record factory instance of the record class indicated by <code>recordClassName</code>.
	 *         <ul>
	 *         <li>If the cache does not contain a record factory instance, a new one is searched and instantiated via Java's Reflection API.
	 *         <li>If there is no factory for the given <code>recordClassName</code>, a new {@code RecordFactoryWrapper} is created and stored for the given
	 *         <code>recordClassName</code>.
	 *         </ul>
	 * @hint This method uses convention over configuration when searching for a record factory class.
	 */
	public IRecordFactory<? extends IMonitoringRecord> get(final String recordClassName) {
		IRecordFactory<? extends IMonitoringRecord> recordFactory = this.recordFactories.get(recordClassName);
		if (null == recordFactory) {
			try {
				recordFactory = this.recordFactoryRepository.get(recordClassName);
			} catch (final MonitoringRecordException e) {
				recordFactory = new RecordFactoryWrapper(recordClassName);
			}
			this.recordFactories.putIfAbsent(recordClassName, recordFactory);
		}
		return recordFactory;
	}
}
