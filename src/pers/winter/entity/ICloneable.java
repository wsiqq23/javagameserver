package pers.winter.entity;

/**
 * The {@code ICloneable} interface represents an object that supports deep cloning.
 * <p>Implementing classes should provide a mechanism to create a deep copy of the object.
 * The deepClone() method allows creating an independent copy of the object, including
 * its internal state, recursively cloning any referenced objects as well.
 * <p>Deep cloning ensures that changes made to the cloned object do not affect the original,\
 * allowing for independent usage or modification of the cloned instance.
 * <p>Note: The deepClone() method returns an {@code Object}, which should be cast to the appropriate
 * type by the caller, based on the actual implementation of the interface.
 * @author Winter
 */
public interface ICloneable {
    /**
     * @return A deep clone of the object, preserving the internal state and any referenced objects.
     */
    Object deepClone();
}
