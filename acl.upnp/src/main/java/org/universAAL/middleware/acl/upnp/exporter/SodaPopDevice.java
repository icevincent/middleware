/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.universAAL.middleware.acl.upnp.exporter;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPIcon;
import org.osgi.service.upnp.UPnPService;
import org.universAAL.middleware.acl.upnp.exporter.services.SodaPopPeerService;

import org.universAAL.middleware.acl.SodaPopPeer;

/* 
* @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
*/

public class SodaPopDevice implements UPnPDevice{

	public final static String TYPE = "urn:schemas-upnp-org:device:SodaPop-ACL-UPnP:1";
	private SodaPopPeerService sodapopPeerService;
	private UPnPService[] services;
	private Dictionary dictionary; // <String,Object>
	
	public SodaPopDevice(SodaPopPeer localPeer) {
		sodapopPeerService = new SodaPopPeerService(localPeer);
		services = new UPnPService[]{sodapopPeerService};
		setupDeviceProperties(localPeer);
	}


	private void setupDeviceProperties(SodaPopPeer localPeer){
		
		final  String DEVICE_ID =  "uuid:" +  localPeer.getID();

		dictionary =  new Hashtable();	// <String,Object>
		dictionary.put(UPnPDevice.UPNP_EXPORT,"");
		dictionary.put(
		        org.osgi.service.device.Constants.DEVICE_CATEGORY,
	        	new String[]{UPnPDevice.DEVICE_CATEGORY}
	        );
		dictionary.put(UPnPDevice.FRIENDLY_NAME,"SodaPop Peer");
		dictionary.put(UPnPDevice.MANUFACTURER,"ISTI-CNR (Persona Project)");
		dictionary.put(UPnPDevice.MANUFACTURER_URL,"http://www.isti.cnr.it/ResearchUnits/Labs/wn-lab/");
		dictionary.put(UPnPDevice.MODEL_DESCRIPTION,"A Sodapop Peer Proxy");
		dictionary.put(UPnPDevice.MODEL_NAME,"SodaPop ACL UPnP Connector");
		dictionary.put(UPnPDevice.MODEL_NUMBER,"1.0");
		dictionary.put(UPnPDevice.MODEL_URL,"http://gforge.aal-persona.org/projects/persona-middlew/");
		dictionary.put(UPnPDevice.SERIAL_NUMBER,DEVICE_ID);
		dictionary.put(UPnPDevice.TYPE,SodaPopDevice.TYPE);
		dictionary.put(UPnPDevice.UDN,DEVICE_ID);
		dictionary.put(UPnPDevice.UPC,DEVICE_ID);
	}
	
	
	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPDevice#getService(java.lang.String)
	 */
	public UPnPService getService(String serviceId) {
		if  (serviceId.equals(sodapopPeerService.getId())) return sodapopPeerService;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPDevice#getServices()
	 */
	public UPnPService[] getServices() {
		return services;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPDevice#getIcons(java.lang.String)
	 */
	public UPnPIcon[] getIcons(String locale) {
		UPnPIcon icon = new universAALIcon();
		return new UPnPIcon[]{icon} ;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPDevice#getDescriptions(java.lang.String)
	 */
	public Dictionary getDescriptions(String locale) {
		return dictionary;
	}

	
}