/**
 */
package kieker.analysisteetime.model.analysismodel.deployment;

import kieker.analysisteetime.model.analysismodel.deployment.impl.IndexedDeploymentFactoryImpl;

/**
 * @author S�ren Henning
 *
 * @since 1.13
 */
public interface IndexedDeploymentFactory {

	IndexedDeploymentFactory INSTANCE = new IndexedDeploymentFactoryImpl();

	IndexedDeploymentRoot createDeploymentRoot();

	IndexedDeploymentContext createDeploymentContext();

	IndexedDeployedComponent createDeployedComponent();

}
