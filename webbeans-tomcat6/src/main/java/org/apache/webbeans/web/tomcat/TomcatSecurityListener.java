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
package org.apache.webbeans.web.tomcat;

import java.security.Principal;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

public class TomcatSecurityListener implements ServletRequestListener
{
    public static ThreadLocal<Principal> principal = new ThreadLocal<Principal>();
    
    public TomcatSecurityListener()
    {
        
    }
    
    @Override
    public void requestDestroyed(ServletRequestEvent event)
    {
        if(principal.get() != null)
        {
            principal.remove();   
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent event)
    {
        ServletRequest request = event.getServletRequest();
        if(request instanceof HttpServletRequest)
        {
            Principal p = ((HttpServletRequest)request).getUserPrincipal();
            if(p != null)
            {
                principal.set(p);
            }
        }
        
    }

}
