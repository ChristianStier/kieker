/**
 */
package kieker.analysisteetime.model.analysismodel.deployment;

/**
 * @author S�ren Henning
 *
 * @since 1.13
 */
public interface IndexedDeploymentContext extends DeploymentContext {

	DeployedComponent getDeployedComponentByName(String name);

	boolean containsDeployedComponentByName(String name);
}
