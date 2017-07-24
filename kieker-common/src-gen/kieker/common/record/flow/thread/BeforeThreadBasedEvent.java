package kieker.common.record.flow.thread;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

import kieker.common.record.flow.thread.AbstractThreadBasedEvent;
import kieker.common.record.io.*;
import kieker.common.util.registry.IRegistry;


/**
 * @author Christian Wulf
 * 
 * @since 1.13
 */
public class BeforeThreadBasedEvent extends AbstractThreadBasedEvent  {
	private static final long serialVersionUID = 5925125427812994180L;

	/** Descriptive definition of the serialization size of the record. */
	public static final int SIZE = TYPE_SIZE_LONG // IEventRecord.timestamp
			 + TYPE_SIZE_LONG // IThreadBasedRecord.threadId
			 + TYPE_SIZE_INT // IThreadBasedRecord.orderIndex
			 + TYPE_SIZE_STRING // IOperationSignature.operationSignature
			 + TYPE_SIZE_STRING // IClassSignature.classSignature
	;
	
	public static final Class<?>[] TYPES = {
		long.class, // IEventRecord.timestamp
		long.class, // IThreadBasedRecord.threadId
		int.class, // IThreadBasedRecord.orderIndex
		String.class, // IOperationSignature.operationSignature
		String.class, // IClassSignature.classSignature
	};
	
	/** user-defined constants */
	
	/** default constants */
	
	/** property name array. */
	private static final String[] PROPERTY_NAMES = {
		"timestamp",
		"threadId",
		"orderIndex",
		"operationSignature",
		"classSignature",
		"cause",
	};
	
	/** property declarations */
	
	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param timestamp
	 *            timestamp
	 * @param threadId
	 *            threadId
	 * @param orderIndex
	 *            orderIndex
	 * @param operationSignature
	 *            operationSignature
	 * @param classSignature
	 *            classSignature
	 */
	public BeforeThreadBasedEvent(final long timestamp, final long threadId, final int orderIndex, final String operationSignature, final String classSignature) {
		super(timestamp, threadId, orderIndex, operationSignature, classSignature);
	}

	/**
	 * This constructor converts the given array into a record.
	 * It is recommended to use the array which is the result of a call to {@link #toArray()}.
	 * 
	 * @param values
	 *            The values for the record.
	 */
	public BeforeThreadBasedEvent(final Object[] values) { // NOPMD (direct store of values)
		super(values, TYPES);
	}

	/**
	 * This constructor uses the given array to initialize the fields of this record.
	 * 
	 * @param values
	 *            The values for the record.
	 * @param valueTypes
	 *            The types of the elements in the first array.
	 */
	protected BeforeThreadBasedEvent(final Object[] values, final Class<?>[] valueTypes) { // NOPMD (values stored directly)
		super(values, valueTypes);
	}
	
	/**
	 * @param deserializer
	 *            The deserializer to use
	 *
	 * @throws BufferUnderflowException
	 *             if buffer not sufficient
	 */
	public BeforeThreadBasedEvent(final IValueDeserializer deserializer) throws BufferUnderflowException {
		super(deserializer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() {
		return new Object[] {
			this.getTimestamp(),
			this.getThreadId(),
			this.getOrderIndex(),
			this.getOperationSignature(),
			this.getClassSignature()
		};
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerStrings(final IRegistry<String> stringRegistry) {	// NOPMD (generated code)
		stringRegistry.get(this.getOperationSignature());
		stringRegistry.get(this.getClassSignature());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(final IValueSerializer serializer) throws BufferOverflowException {
		super.serialize(serializer);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] getValueTypes() {
		return TYPES; // NOPMD
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSize() {
		return SIZE;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This record uses the {@link kieker.common.record.IMonitoringRecord.Factory} mechanism. Hence, this method is not implemented.
	 */
	@Override
	@Deprecated
	public void initFromArray(final Object[] values) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (obj.getClass() != this.getClass()) return false;
		
		final BeforeThreadBasedEvent castedRecord = (BeforeThreadBasedEvent) obj;
		if (this.getLoggingTimestamp() != castedRecord.getLoggingTimestamp()) return false;
		if (this.getTimestamp() != castedRecord.getTimestamp()) return false;
		if (this.getThreadId() != castedRecord.getThreadId()) return false;
		if (this.getOrderIndex() != castedRecord.getOrderIndex()) return false;
		if (!this.getOperationSignature().equals(castedRecord.getOperationSignature())) return false;
		if (!this.getClassSignature().equals(castedRecord.getClassSignature())) return false;
		return true;
	}
	
	@Override
	public String[] getValueNames() {
		return PROPERTY_NAMES;	// NOPMD
	}	
	
}
