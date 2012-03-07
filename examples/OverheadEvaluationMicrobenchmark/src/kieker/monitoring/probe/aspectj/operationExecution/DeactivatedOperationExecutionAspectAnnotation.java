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

package kieker.monitoring.probe.aspectj.operationExecution;

import java.util.concurrent.ConcurrentHashMap;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.common.record.controlflow.OperationExecutionRecord;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * based upon OperationExecutionAspectAnnotation
 * 
 * @author Andre van Hoorn, Jan Waller
 */
@Aspect
public class DeactivatedOperationExecutionAspectAnnotation extends AbstractOperationExecutionAspect {
	private static final Log log = LogFactory.getLog(DeactivatedOperationExecutionAspectAnnotation.class);

	private static final ConcurrentHashMap<String, Boolean> deactivatedProbes = new ConcurrentHashMap<String, Boolean>();
	{
		final int mapSize = 10000;
		for (int i = 0; i < (mapSize / 2); i++) {
			DeactivatedOperationExecutionAspectAnnotation.deactivatedProbes.put(Long.toHexString(Double.doubleToLongBits(Math.random())), Boolean.TRUE);
		}
		DeactivatedOperationExecutionAspectAnnotation.deactivatedProbes.put("long kieker.evaluation.monitoredApplication.MonitoredClass.monitoredMethod(long, int)",
				Boolean.TRUE);
		for (int i = 0; i < (mapSize / 2); i++) {
			DeactivatedOperationExecutionAspectAnnotation.deactivatedProbes.put(Long.toHexString(Double.doubleToLongBits(Math.random())), Boolean.TRUE);
		}
	}

	@Pointcut("execution(@kieker.monitoring.annotation.OperationExecutionMonitoringProbe * *.*(..))")
	public void monitoredMethod() {}

	@Override
	@Around("monitoredMethod() && notWithinKieker()")
	public Object doBasicProfiling(final ProceedingJoinPoint thisJoinPoint) throws Throwable {
		if (!AbstractOperationExecutionAspect.CTRLINST.isMonitoringEnabled()
				|| DeactivatedOperationExecutionAspectAnnotation.deactivatedProbes.containsKey(thisJoinPoint.getStaticPart().getSignature().toString())) {
			return thisJoinPoint.proceed();
		}
		final OperationExecutionRecord execData = this.initExecutionData(thisJoinPoint);
		int eoi = 0; // this is executionOrderIndex-th execution in this trace
		int ess = 0; // this is the height in the dynamic call tree of this execution
		if (execData.isEntryPoint()) {
			AbstractOperationExecutionAspect.CFREGISTRY.storeThreadLocalEOI(0);
			// current execution's eoi is 0
			AbstractOperationExecutionAspect.CFREGISTRY.storeThreadLocalESS(1);
			// current execution's ess is 0
		} else {
			eoi = AbstractOperationExecutionAspect.CFREGISTRY.incrementAndRecallThreadLocalEOI(); // ess > 1
			ess = AbstractOperationExecutionAspect.CFREGISTRY.recallAndIncrementThreadLocalESS(); // ess >= 0
		}
		try {
			this.proceedAndMeasure(thisJoinPoint, execData);
			if ((eoi == -1) || (ess == -1)) {
				DeactivatedOperationExecutionAspectAnnotation.log.error("eoi and/or ess have invalid values:" + " eoi == " + eoi + " ess == " + ess);
				DeactivatedOperationExecutionAspectAnnotation.log.error("Terminating!");
				AbstractOperationExecutionAspect.CTRLINST.terminateMonitoring();
			}
		} catch (final Exception e) {
			throw e; // exceptions are forwarded
		} finally {
			/*
			 * note that proceedAndMeasure(...) even sets the variable name in
			 * case the execution of the joint point resulted in an exception!
			 */
			execData.setEoi(eoi);
			execData.setEss(ess);
			AbstractOperationExecutionAspect.CTRLINST.newMonitoringRecord(execData);
			if (execData.isEntryPoint()) {
				AbstractOperationExecutionAspect.CFREGISTRY.unsetThreadLocalEOI();
				AbstractOperationExecutionAspect.CFREGISTRY.unsetThreadLocalESS();
			} else {
				AbstractOperationExecutionAspect.CFREGISTRY.storeThreadLocalESS(ess);
			}
		}
		return execData.getRetVal();
	}
}