/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.webbeans.web.lifecycle.test;

import java.util.Properties;

import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.lifecycle.StandaloneLifeCycle;
import org.apache.webbeans.spi.se.DefaultScannerService;

/**
 * Ease the writing of the tests. Simulates container
 * startup and stop functionality. This will scan all
 * classes on the classpaths which have a beans.xml file. 
 * @version $Rev: 892509 $ $Date: 2009-12-19 23:47:15 +0200 (Sat, 19 Dec 2009) $
 */
public class EnterpriseTestLifeCycle extends StandaloneLifeCycle
{
    private MockServletContextEvent servletContextEvent;
    
    private MockHttpSession mockHttpSession;

    public EnterpriseTestLifeCycle()
    {
        super();
    }
    
    @Override
    public void beforeStartApplication(Object object)
    {
        this.mockHttpSession = new MockHttpSession();
        this.servletContextEvent = new MockServletContextEvent();
        
        ContextFactory.initRequestContext(null);
        ContextFactory.initSessionContext(mockHttpSession);
        ContextFactory.initConversationContext(null);
        ContextFactory.initApplicationContext(this.servletContextEvent.getServletContext());        
    }
    
    @Override
    public void initApplication(Properties properties)
    {
        this.scannerService = new DefaultScannerService();        
    }
        
    @Override
    public void beforeStopApplication(Object endObject)
    {
        ContextFactory.destroyRequestContext(null);
        ContextFactory.destroySessionContext(this.mockHttpSession);
        ContextFactory.destroyConversationContext();        
        ContextFactory.destroyApplicationContext(this.servletContextEvent.getServletContext());   
    }

}