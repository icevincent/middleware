/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut f�r Graphische Datenverarbeitung 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package org.universAAL.middleware.context.impl;

import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.rdf.ContextEvent;
import org.universAAL.middleware.context.rdf.ContextEventPattern;
import org.universAAL.middleware.sodapop.AbstractBus;
import org.universAAL.middleware.sodapop.BusMember;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.sodapop.msg.MessageType;
import org.universAAL.middleware.util.Constants;


/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class ContextBusImpl extends AbstractBus implements ContextBus {
	
	public ContextBusImpl(SodaPop g) {
		super(Constants.uAAL_BUS_NAME_CONTEXT, new ContextStrategy(g), g);
		busStrategy.setBus(this);
	}
	
	public String register(BusMember member) {
		return null;
	}

	public String register(ContextPublisher publisher) {
		return Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + super.register(publisher);
	}

	public String register(ContextSubscriber subscriber, ContextEventPattern[] initialSubscriptions) {
		String id = super.register(subscriber);
		if (initialSubscriptions != null)
			((ContextStrategy) busStrategy).addRegParams(subscriber, initialSubscriptions);
		return Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + id;
	}
	
	public void sendMessage(String senderID, Message msg) {}

	public void sendMessage(String publisherID, ContextEvent msg) {
		Activator.assessContentSerialization(msg);
		if (publisherID != null
				&&  publisherID.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.sendMessage(
					publisherID.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					new Message(MessageType.event, msg));
	}

	public void unregister(String publisherID, ContextPublisher publisher) {
		if (publisherID != null
				&&  publisherID.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.unregister(
					publisherID.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					publisher);
	}

	public void unregister(String subscriberID, ContextSubscriber subscriber) {
		if (subscriberID != null
				&&  subscriberID.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
		super.unregister(
				subscriberID.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
				subscriber);
		((ContextStrategy) busStrategy).removeRegParams(subscriber);
	}
	
	public void unregister(String id, BusMember member) {}

	public void addNewRegParams(String subscriberID, ContextEventPattern[] newSubscriptions) {
		if (subscriberID != null
				&&  subscriberID.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(
					subscriberID.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
			if (o instanceof ContextSubscriber  &&  newSubscriptions != null)
				((ContextStrategy) busStrategy).addRegParams((ContextSubscriber) o, newSubscriptions);
		}
	}

	public void removeMatchingRegParams(String subscriberID,
			ContextEventPattern[] oldSubscriptions) {
		if (subscriberID != null
				&&  subscriberID.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(
					subscriberID.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
			if (o instanceof ContextSubscriber  &&  oldSubscriptions != null)
				((ContextStrategy) busStrategy).removeMatchingRegParams((ContextSubscriber) o, oldSubscriptions);
		}
	}
}