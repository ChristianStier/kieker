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

package kieker.tools.traceAnalysis.plugins.visualization.callTree;

import kieker.tools.traceAnalysis.systemModel.AllocationComponent;
import kieker.tools.traceAnalysis.systemModel.Operation;

/**
 *
 * @author Andre van Hoorn
 */
public class CallTreeOperationHashKey {
    private final AllocationComponent allocationComponent;
    private final Operation operation;

    private final int hashCode; // the final is computed once and never changes

    public CallTreeOperationHashKey(final AllocationComponent allocationComponent,
            final Operation operation) {
        this.allocationComponent = allocationComponent;
        this.operation = operation;
        this.hashCode =
                this.allocationComponent.hashCode()
                ^ this.operation.hashCode();
    }

    @Override
    public final int hashCode() {
        return this.hashCode;
    }

    @Override
    public final boolean equals(Object o){
        if (o == this) return true;
        if (! (o instanceof CallTreeOperationHashKey)) return false;
        CallTreeOperationHashKey k = (CallTreeOperationHashKey)o;

        return this.allocationComponent.equals(k.allocationComponent)
                && this.operation.equals(k.operation);
    }

    public final AllocationComponent getAllocationComponent() {
        return this.allocationComponent;
    }

    public final Operation getOperation() {
        return this.operation;
    }
}
