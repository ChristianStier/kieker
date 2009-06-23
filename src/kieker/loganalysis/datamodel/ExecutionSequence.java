package kieker.loganalysis.datamodel;

import java.util.SortedSet;
import java.util.TreeSet;
import kieker.tpmon.monitoringRecord.executions.KiekerExecutionRecord;

/**
 * kieker.loganalysis.datamodel.ExecutionSequence
 *
 * ==================LICENCE=========================
 * Copyright 2009 Kieker Project
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
 * ==================================================
 *
 * @author Andre van Hoorn
 */
public class ExecutionSequence {
    private long traceId = -1; // convenience field. All executions have this traceId.
    private SortedSet<KiekerExecutionRecord> sequence = new TreeSet<KiekerExecutionRecord>();

    public long getTraceId() {
        return traceId;
    }

    public void add(KiekerExecutionRecord record){
        this.sequence.add(record);
    }

    public void toMessageSequence(){
        throw new UnsupportedOperationException("");
    }
}
