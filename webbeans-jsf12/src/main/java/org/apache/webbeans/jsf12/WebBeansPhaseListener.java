/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.webbeans.jsf12;

import javax.enterprise.context.BusyConversationException;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.NonexistentConversationException;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.context.ConversationContext;
import org.apache.webbeans.conversation.ConversationImpl;
import org.apache.webbeans.conversation.ConversationManager;
import org.apache.webbeans.logger.WebBeansLogger;

/**
 * Conversation related phase listener.
 * 
 * @version $Rev: 943786 $ $Date: 2010-05-13 07:06:16 +0300 (Thu, 13 May 2010) $
 *
 */
public class WebBeansPhaseListener implements PhaseListener
{
    private static final long serialVersionUID = 1L;

    /**Logger instance*/
    private static final WebBeansLogger logger = WebBeansLogger.getLogger(WebBeansPhaseListener.class);
    
    /**
     * {@inheritDoc}
     */
    public void afterPhase(PhaseEvent phaseEvent)
    {
        if (phaseEvent.getPhaseId().equals(PhaseId.RENDER_RESPONSE) ||
                JSFUtil.getCurrentFacesContext().getResponseComplete())
        {
        	ConversationManager conversationManager = ConversationManager.getInstance();
        	Conversation conversation = conversationManager.getConversationBeanReference();
        	
            if (conversation.isTransient())
            {
                logger.debug(OWBLogConst.INFO_0014, new Object[]{conversation.getId()});
                ContextFactory.destroyConversationContext();                                                    
            }
            else
            {
                //Conversation must be used by one thread at a time
                ConversationImpl owbConversation = (ConversationImpl)conversation;
                owbConversation.updateTimeOut();
                //Other threads can now access propogated conversation.
                owbConversation.setInUsed(false);                
            }            
        }
    }

    /**
     * {@inheritDoc}
     */
    public void beforePhase(PhaseEvent phaseEvent)
    {
        if (phaseEvent.getPhaseId().equals(PhaseId.RESTORE_VIEW))
        {
            //It looks for cid parameter in the JSF request.
            //If request contains cid, then it must restore conversation
            //Otherwise create NonexistentException
        	ConversationManager conversationManager = ConversationManager.getInstance();
        	Conversation conversation = conversationManager.getConversationBeanReference();
        	String cid = JSFUtil.getConversationId();
        	
			if (conversation.isTransient())
			{
				logger.debug(OWBLogConst.INFO_0016, new Object[]{conversation.getId()});
				ContextFactory.initConversationContext(null);
				
	            //Not restore, throw exception
				if(cid != null && !cid.equals(""))
				{
				    throw new NonexistentConversationException("Propogated conversation with cid=" + cid + " is not restored. It creates a new transient conversation.");
				}
			}
			else
			{
				logger.debug(OWBLogConst.INFO_0015, new Object[]{conversation.getId()});
				
				//Conversation must be used by one thread at a time
				ConversationImpl owbConversation = (ConversationImpl)conversation;
				if(!owbConversation.getInUsed().compareAndSet(false, true))
				{				    
				    ContextFactory.initConversationContext(null);
				    //Throw Busy exception
				    throw new BusyConversationException("Propogated conversation with cid=" + cid + " is used by other request. It creates a new transient conversation");
				}
				else
				{
	               ConversationContext conversationContext = conversationManager.getConversationContext(conversation);
	               ContextFactory.initConversationContext(conversationContext);
				}				
			}
        }
    }

    public PhaseId getPhaseId()
    {
        return PhaseId.ANY_PHASE;
    }
}