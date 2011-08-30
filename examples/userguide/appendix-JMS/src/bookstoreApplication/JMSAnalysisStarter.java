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

package bookstoreApplication;

import kieker.analysis.AnalysisController;
import kieker.analysis.plugin.IMonitoringRecordConsumerPlugin;
import kieker.analysis.plugin.MonitoringRecordConsumerException;
import kieker.analysis.reader.IMonitoringReader;
import kieker.analysis.reader.JMSReader;
import kieker.analysis.reader.MonitoringReaderException;

/**
 *
 * @author Andre van Hoorn
 */
public class JMSAnalysisStarter {

    private static final long MAX_RT_NANOS = 1700;

    private final static String CONNECTION_FACTORY_TYPE__OPEN_JMS = "org.exolab.jms.jndi.InitialContextFactory";
    //private final static String CONNECTION_FACTORY_TYPE__ACTIVE_MQ = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
    
    public static void main(final String[] args) throws MonitoringReaderException, MonitoringRecordConsumerException {
        final AnalysisController analysisInstance = new AnalysisController();
        final IMonitoringReader logReader =
				new JMSReader("tcp://127.0.0.1:3035/", "queue1",
						JMSAnalysisStarter.CONNECTION_FACTORY_TYPE__OPEN_JMS);
        analysisInstance.setReader(logReader);
        final IMonitoringRecordConsumerPlugin consumer =
                new Consumer(JMSAnalysisStarter.MAX_RT_NANOS);
        analysisInstance.registerPlugin(consumer);

        analysisInstance.run();
    }
}
