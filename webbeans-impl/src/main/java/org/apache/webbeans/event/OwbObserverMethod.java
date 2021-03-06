/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.event;

import java.util.Set;

import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ObserverMethod;

/**
 * This interface fixes an issue of the CDI API. See https://issues.jboss.org/browse/CDI-36
 */
public interface OwbObserverMethod<T> extends ObserverMethod<T>
{
    /**
     * Returns the {@link InjectionPoint}s for the parameters of this observer method.
     */
    Set<InjectionPoint> getInjectionPoints();

    /**
     * will actually call the underlying observer method with the specified event metadata
     */
    void notify(T event, EventMetadata metadata);

}
