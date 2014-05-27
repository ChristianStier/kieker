package kieker.examples.livedemo.analysis.display;

import java.util.HashMap;
import java.util.Map;

import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;

import kieker.analysis.IProjectContext;
import kieker.common.configuration.Configuration;
import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.common.util.signature.ClassOperationSignaturePair;

public class MethodFlowDisplayFilter extends AbstractNonAggregatingDisplayFilter<OperationExecutionRecord, DefaultTagCloudModel> {

	private final Map<String, DefaultTagCloudItem> map = new HashMap<String, DefaultTagCloudItem>();

	public MethodFlowDisplayFilter(final Configuration configuration, final IProjectContext projectContext) {
		super(configuration, projectContext);
	}

	@Override
	protected DefaultTagCloudModel createChartModel(final int numberOfEntries) {
		return new DefaultTagCloudModel();
	}

	@Override
	protected void fillChartModelWithRecordData(final DefaultTagCloudModel chartModel, final OperationExecutionRecord record, final String minutesAndSeconds,
			final int numberOfEntries) {
		final String shortClassName = ClassOperationSignaturePair.splitOperationSignatureStr(record.getOperationSignature()).getSimpleClassname();
		final String methodName = shortClassName + '.' + this.extractMethodName(record.getOperationSignature());

		if (!this.map.containsKey(methodName)) {
			final DefaultTagCloudItem item = new DefaultTagCloudItem(methodName, 0);
			this.map.put(methodName, item);
			chartModel.addTag(item);
		}

		final DefaultTagCloudItem item = this.map.get(methodName);
		item.setStrength(item.getStrength() + 1);
	}

	private String extractMethodName(final String operationSignature) {
		final String operationSignatureWithoutParameters = operationSignature.replaceAll("\\(.*\\)", "");
		final int lastPointPos = operationSignatureWithoutParameters.lastIndexOf('.');
		final String methodName = operationSignatureWithoutParameters.substring(lastPointPos + 1);

		return methodName;
	}

}
