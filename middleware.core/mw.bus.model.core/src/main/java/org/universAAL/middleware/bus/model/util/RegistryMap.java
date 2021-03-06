/**
 * 
 *  OCO Source Materials 
 *      Copyright IBM Corp. 2012 
 *
 *      See the NOTICE file distributed with this work for additional 
 *      information regarding copyright ownership 
 *       
 *      Licensed under the Apache License, Version 2.0 (the "License"); 
 *      you may not use this file except in compliance with the License. 
 *      You may obtain a copy of the License at 
 *       	http://www.apache.org/licenses/LICENSE-2.0 
 *       
 *      Unless required by applicable law or agreed to in writing, software 
 *      distributed under the License is distributed on an "AS IS" BASIS, 
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *      See the License for the specific language governing permissions and 
 *      limitations under the License. 
 *
 */
package org.universAAL.middleware.bus.model.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.rdf.Resource;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 20, 2012
 * 
 */
// TODO: synchronize map and listener for concurrent access
public class RegistryMap extends Object implements IRegistry {

    protected Map<String, BusMember> map = new HashMap<String, BusMember>();
    protected List<IRegistryListener> listeners = new ArrayList<IRegistryListener>();

    public void addBusMember(String memberID, BusMember busMember) {
	map.put(memberID, busMember);
	for (int i = 0; i < listeners.size(); i++) {
	    listeners.get(i).busMemberAdded(busMember);
	}
    }

    public BusMember removeMemberByID(String memberID) {
	BusMember busMember = (BusMember) map.remove(memberID);
	if (busMember != null) {
	    for (int i = 0; i < listeners.size(); i++) {
		listeners.get(i).busMemberRemoved(busMember);
	    }
	}
	return busMember;
    }

    public BusMember[] getAllBusMembers() {
	return (BusMember[]) map.values().toArray(new BusMember[0]);
    }

    public String[] getAllBusMembersIds() {
	return (String[]) map.keySet().toArray(new String[0]);
    }

    public BusMember getBusMemberByID(String memberID) {
	return (memberID == null) ? null : (BusMember) map.get(memberID);
    }

    public String getBusMemberID(BusMember busMember) {
	String result = null;
	if (busMember != null) {
	    for (Iterator<String> i = map.keySet().iterator(); i.hasNext();) {
		String id = i.next();
		if (busMember.equals(map.get(id))) {
		    result = id;
		    break;
		}
	    }
	}
	return result;
    }

    public int getBusMembersCount() {
	return map.size();
    }

    public void reset() {
	map.clear();
	for (int i = 0; i < listeners.size(); i++) {
	    ((IRegistryListener) listeners.get(i)).busCleared();
	}
    }

    public boolean addRegistryListener(IRegistryListener listener) {
	if (listener == null)
	    throw new NullPointerException();
	return listeners.add(listener);
    }

    public boolean removeRegistryListener(IRegistryListener listener) {
	return listeners.remove(listener);
    }

    public void addRegParams(String busMemberID, Resource[] params) {
	// We don't do anything with the reg params, we just forward them to the
	// listeners.
	for (IRegistryListener listener : listeners) {
	    listener.regParamsAdded(busMemberID, params);
	}
    }

    public void removeRegParams(String busMemberID, Resource[] params) {
	// We don't do anything with the reg params, we just forward them to the
	// listeners.
	for (IRegistryListener listener : listeners) {
	    listener.regParamsRemoved(busMemberID, params);
	}
    }
}
