/**
 */
package kieker.analysisteetime.model.analysismodel.architecture;

/**
 * @author S�ren Henning
 *
 * @since 1.13
 */
public interface IndexedArchitectureRoot extends ArchitectureRoot {

	// List<IndexedComponentType> getIndexedComponentTypes();

	ComponentType getComponentTypeByName(String name);

	boolean containsComponentTypeByName(String name);

}
