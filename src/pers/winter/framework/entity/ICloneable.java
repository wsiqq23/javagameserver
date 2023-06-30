/*
 * Copyright 2023 Winter Game Server
 *
 * The Winter Game Server licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
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
