/**
 */
package kieker.analysisteetime.model.analysismodel.deployment;

/**
 * @author S�ren Henning
 *
 * @since 1.13
 */
public interface IndexedDeployedComponent extends DeployedComponent {

	DeployedOperation getDeployedOperationByName(String name);

	boolean containsDeployedOperationByName(String name);

}
