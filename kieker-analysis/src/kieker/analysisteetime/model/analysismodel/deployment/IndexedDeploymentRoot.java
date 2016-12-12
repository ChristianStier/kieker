/**
 */
package kieker.analysisteetime.model.analysismodel.deployment;

/**
 * @author S�ren Henning
 *
 * @since 1.13
 */
public interface IndexedDeploymentRoot extends DeploymentRoot {

	DeploymentContext getDeploymentContextByName(String name);

	boolean containsDeploymentContextByName(String name);
}
