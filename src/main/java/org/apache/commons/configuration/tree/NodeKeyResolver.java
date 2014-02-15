/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration.tree;

import java.util.List;

/**
 * <p>
 * Definition of an interface which allows resolving a (property) key for
 * different manipulating operations.
 * </p>
 * <p>
 * This interface is used when interacting with a node model. It is an
 * abstraction over a concrete {@link ExpressionEngine} instance. It also
 * implements some functionality for creating special helper objects for the
 * processing of complex update operations.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public interface NodeKeyResolver
{
    /**
     * Performs a query for the specified key on the given root node. This is a
     * thin wrapper over the {@code query()} method of an
     * {@link ExpressionEngine}.
     *
     * @param root the root node
     * @param key the key to be resolved
     * @param handler the {@code NodeHandler}
     * @param <T> the type of the nodes involved in this operation
     * @return a list with query results
     */
    <T> List<QueryResult<T>> resolveKey(T root, String key,
            NodeHandler<T> handler);

    /**
     * Resolves a key of an add operation. Result is a {@code NodeAddData}
     * object containing all information for actually performing the add
     * operation at the specified key.
     *
     * @param root the root node
     * @param key the key to be resolved
     * @param handler the {@code NodeHandler}
     * @param <T> the type of the nodes involved in this operation
     * @return a {@code NodeAddData} object to be used for the add operation
     */
    <T> NodeAddData<T> resolveAddKey(T root, String key, NodeHandler<T> handler);

    /**
     * Resolves a key for an update operation. Result is a
     * {@code NodeUpdateData} object containing all information for actually
     * performing the update operation at the specified key using the provided
     * new value object.
     *
     * @param root the root node
     * @param key the key to be resolved
     * @param newValue the new value for the key to be updated; this can be a
     *        single value or a container for multiple values
     * @param handler the {@code NodeHandler}
     * @param <T> the type of the nodes involved in this operation
     * @return a {@code NodeUpdateData} object to be used for this update
     *         operation
     */
    <T> NodeUpdateData<T> resolveUpdateKey(T root, String key, Object newValue,
            NodeHandler<T> handler);
}
