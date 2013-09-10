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

package kieker.common.record.flow.trace.operation.object;

import kieker.common.record.flow.ICallObjectRecord;
import kieker.common.record.flow.trace.operation.CallOperationEvent;
import kieker.common.util.Bits;

/**
 * @author Jan Waller
 * 
 * @since 1.6
 */
public class CallOperationObjectEvent extends CallOperationEvent implements ICallObjectRecord {
	private static final long serialVersionUID = 5099289901643589844L;
	public static final Class<?>[] TYPES = {
		long.class, // Event.timestamp
		long.class, // TraceEvent.traceId
		int.class, // TraceEvent.orderIndex
		String.class, // OperationEvent.operationSiganture
		String.class, // OperationEvent.classSignature
		String.class, // CallOperationEvent.calleeOperationSignature
		String.class, // CallOperationEvent.calleeClassSiganture
		int.class, // Caller objectId
		int.class, // Callee objectId
	};

	private final int callerObjectId;
	private final int calleeObjectId;

	/**
	 * This constructor uses the given parameters to initialize the fields of this record.
	 * 
	 * @param timestamp
	 *            The timestamp of this record.
	 * @param traceId
	 *            The trace ID.
	 * @param orderIndex
	 *            The order index.
	 * @param callerOperationSignature
	 *            The caller operation signature. This parameter can be null.
	 * @param callerClassSignature
	 *            The caller class signature. This parameter can be null.
	 * @param calleeOperationSignature
	 *            The callee operation signature. This parameter can be null.
	 * @param calleeClassSignature
	 *            The callee class signature. This parameter can be null.
	 * @param callerObjectId
	 *            The ID of the caller object.
	 * @param calleeObjectId
	 *            The ID of the callee object.
	 */
	public CallOperationObjectEvent(final long timestamp, final long traceId, final int orderIndex,
			final String callerOperationSignature, final String callerClassSignature,
			final String calleeOperationSignature, final String calleeClassSignature,
			final int callerObjectId, final int calleeObjectId) {
		super(timestamp, traceId, orderIndex, callerOperationSignature, callerClassSignature, calleeOperationSignature, calleeClassSignature);
		this.callerObjectId = callerObjectId;
		this.calleeObjectId = calleeObjectId;
	}

	/**
	 * 
	 * Creates a new instance of this class using the given array. The array should be the one resulting in a call to {@link #toArray()}.
	 * 
	 * @param values
	 *            The array containing the values.
	 */
	public CallOperationObjectEvent(final Object[] values) { // NOPMD (values stored directly)
		super(values, TYPES); // values[0..6]
		this.callerObjectId = (Integer) values[7];
		this.calleeObjectId = (Integer) values[8];
	}

	/**
	 * 
	 * Creates a new instance of this class using the given array but with modified types.
	 * 
	 * @param values
	 *            The array containing the values.
	 * @param types
	 *            The types of the array objects.
	 */
	protected CallOperationObjectEvent(final Object[] values, final Class<?>[] types) { // NOPMD (values stored directly)
		super(values, types); // values[0..6]
		this.callerObjectId = (Integer) values[7];
		this.calleeObjectId = (Integer) values[8];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() {
		return new Object[] { this.getTimestamp(), this.getTraceId(), this.getOrderIndex(),
			this.getCallerOperationSignature(), this.getCallerClassSignature(),
			this.getCalleeOperationSignature(), this.getCalleeClassSignature(),
			this.getCallerObjectId(), this.getCalleeObjectId(), };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] toByteArray() {
		final byte[] arr = new byte[8 + 8 + 4 + 8 + 8 + 8 + 8 + 4 + 4];
		Bits.putLong(arr, 0, this.getTimestamp());
		Bits.putLong(arr, 8, this.getTraceId());
		Bits.putInt(arr, 8 + 8, this.getOrderIndex());
		Bits.putString(arr, 8 + 8 + 4, this.getCallerOperationSignature());
		Bits.putString(arr, 8 + 8 + 4 + 8, this.getCallerClassSignature());
		Bits.putString(arr, 8 + 8 + 4 + 8 + 8, this.getCalleeOperationSignature());
		Bits.putString(arr, 8 + 8 + 4 + 8 + 8 + 8, this.getCalleeClassSignature());
		Bits.putInt(arr, 8 + 8 + 4 + 8 + 8 + 8 + 8, this.getCallerObjectId());
		Bits.putInt(arr, 8 + 8 + 4 + 8 + 8 + 8 + 8 + 4, this.getCalleeObjectId());
		return arr;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] getValueTypes() {
		return TYPES; // NOPMD
	}

	/**
	 * Delivers the ID of the caller object.
	 * 
	 * @return The ID of the caller.
	 */
	public final int getObjectId() {
		return this.callerObjectId;
	}

	/**
	 * Delivers the ID of the caller object.
	 * 
	 * @return The ID of the caller.
	 */
	public final int getCallerObjectId() {
		return this.callerObjectId;
	}

	/**
	 * Delivers the ID of the callee object.
	 * 
	 * @return The ID of the callee.
	 */
	public final int getCalleeObjectId() {
		return this.calleeObjectId;
	}

	// Currently, we do not override this method to include a comparison of object ids. So, these events stay downward compatible and just provide additional
	// information.
	// @Override
	// public final boolean callsReferencedOperationOf(final IOperationRecord record) {
	// return this.getCalleeOperationSignature().equals(record.getOperationSignature()) && this.getCalleeClassSignature().equals(record.getClassSignature());
	// }
}
