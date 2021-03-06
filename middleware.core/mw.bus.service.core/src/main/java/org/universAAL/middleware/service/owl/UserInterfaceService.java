/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
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
package org.universAAL.middleware.service.owl;

import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
// TODO: check the methods, especially getUIServiceInfo; there seems to be
// something wrong (using MY_URI as property path??)
public class UserInterfaceService extends Service {
    public static final String SERVICE_REQUEST_URI_PREFIX_INFO = uAAL_SERVICE_NAMESPACE
	    + "UIServiceInfo";
    public static final String SERVICE_REQUEST_URI_PREFIX_START = uAAL_SERVICE_NAMESPACE
	    + "UIServiceStart";
    public static final String OUTPUT_INSTANCE_INFO = uAAL_SERVICE_NAMESPACE
	    + "uiServiceInfo";

    public static final String MY_URI;
    public static final String PROP_CORRELATED_SERVICE_CLASS;
    public static final String PROP_DESCRIPTION;
    public static final String PROP_HAS_INFO_RETRIEVAL_PROCESS;
    public static final String PROP_HAS_VENDOR;
    static {
	MY_URI = uAAL_SERVICE_NAMESPACE + "UserInterfaceService";

	PROP_CORRELATED_SERVICE_CLASS = uAAL_SERVICE_NAMESPACE
		+ "correlatedServiceClass";
	PROP_DESCRIPTION = uAAL_SERVICE_NAMESPACE + "description";
	PROP_HAS_INFO_RETRIEVAL_PROCESS = uAAL_SERVICE_NAMESPACE
		+ "infoRetrievalProcess";
	PROP_HAS_VENDOR = uAAL_SERVICE_NAMESPACE + "vendor";
    }

    /**
     * Creates and returns an appropriate {@link ServiceProfile} for a UI
     * service. This service is called from the main menu when the UI service is
     * selected. Typically, the UI service that registers this ServiceProfile
     * will then send a UI request to present the main dialog of the UI service.
     * 
     * @param serviceClassURI
     *            the URI of the service class from an underlying ontology, e.g.
     *            the value of <code>Lighting.MY_URI</code> from the lighting
     *            example.
     * @param vendor
     *            the URL of the partner home page that provides the UI e.g.
     *            <code>"http://www.igd.fraunhofer.de"</code>
     * @param description
     *            describes what the service does, e.g. <code>"The main screen
     *            of a lighting service component by Fraunhofer-IGD that allows
     *            human users to view and change the states of light sources
     *            found on a connected KNX bus. Special visualization techniques,
     *            such as usage of 3D scenes and moving the camera from the
     *            current viewpoint of the user all the way towards the location of
     *            a currently selected light source, lead to a unique user
     *            experience in controlling light sources."</code>
     * @param startServiceURI
     *            a URI identifying this service. When the UI service is called,
     *            i.e. when the method
     *            {@link ServiceCallee#handleCall(ServiceCall)} is called, the
     *            {@link ServiceCall} will have this URI as process URI and can
     *            be retrieved by calling {@link ServiceCall#getProcessURI()}.
     * @return The created service profile that can be used to register the UI
     *         service at the service bus.
     */
    public static final ServiceProfile createServiceProfile(
	    String serviceClassURI, String vendor, String description,
	    String startServiceURI) {
	return createServiceProfile(new UserInterfaceService(startServiceURI),
		serviceClassURI, vendor, description, startServiceURI);
    }

    /**
     * Same method with different arguments. This time a User Interface Service
     * will be required to create a service profile for a UI.
     * 
     * @return The created service profile that can be used to register the UI
     *         service at the service bus.
     */
    protected static ServiceProfile createServiceProfile(
	    UserInterfaceService uis, String serviceClassURI, String vendor,
	    String description, String startServiceURI) {
	uis.setProperty(PROP_CORRELATED_SERVICE_CLASS, new Resource(
		serviceClassURI, true));
	uis.setProperty(PROP_DESCRIPTION, description);
	uis.setProperty(PROP_HAS_VENDOR, new Resource(vendor, true));
	return uis.myProfile;
    }

    /**
     * @param serviceClassURI
     *            the URI of the service class from an underlying ontology, e.g.
     *            the value of <code>Lighting.MY_URI</code> from the lighting
     *            example.
     * @param vendor
     *            the URL of the partner home page that provides the UI e.g.
     *            <code>"http://www.igd.fraunhofer.de"</code>
     * @param theCaller
     *            instance of {@link ServiceCaller} that will be used to issue
     *            the service request to get the information.
     * @return The description of the service profile.
     */
    public static final String getUIServiceDescription(String serviceClassURI,
	    String vendor, ServiceCaller theCaller) {
	return getUIServiceDescription(new UserInterfaceService(),
		serviceClassURI, vendor, theCaller);
    }

    /**
     * Gets the UI service description of the service.
     * 
     * @param requestedService
     * @param serviceClassURI
     *            the URI of the service class from an underlying ontology, e.g.
     *            the value of <code>Lighting.MY_URI</code> from the lighting
     *            example.
     * @param vendor
     *            the URL of the partner home page that provides the UI e.g.
     *            <code>"http://www.igd.fraunhofer.de"</code>
     * @param theCaller
     *            instance of {@link ServiceCaller} that will be used to issue
     *            the service request to get the information.
     * @return The description of the service profile.
     */
    protected static String getUIServiceDescription(
	    UserInterfaceService requestedService, String serviceClassURI,
	    String vendor, ServiceCaller theCaller) {
	requestedService.addInstanceLevelRestriction(MergedRestriction
		.getFixedValueRestriction(PROP_CORRELATED_SERVICE_CLASS,
			new Resource(serviceClassURI, true)),
		new String[] { PROP_CORRELATED_SERVICE_CLASS });
	requestedService.addInstanceLevelRestriction(MergedRestriction
		.getFixedValueRestriction(PROP_HAS_VENDOR, new Resource(vendor,
			true)), new String[] { PROP_HAS_VENDOR });
	ServiceRequest req = new ServiceRequest(
		SERVICE_REQUEST_URI_PREFIX_INFO, 5, requestedService, null);
	req.addSimpleOutputBinding(new ProcessOutput(OUTPUT_INSTANCE_INFO),
		new String[] { PROP_DESCRIPTION });
	ServiceResponse resp = theCaller.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {
	    List outputs = resp.getOutputs();
	    if (outputs != null)
		for (Iterator i = outputs.iterator(); i.hasNext();) {
		    ProcessOutput output = (ProcessOutput) i.next();
		    if (output.getURI().equals(OUTPUT_INSTANCE_INFO)) {
			Object o = output.getParameterValue();
			if (o instanceof String)
			    return (String) o;
		    }
		}
	}
	return null;
    }

    /**
     * @param serviceClassURI
     *            the URI of the service class from an underlying ontology, e.g.
     *            the value of <code>Lighting.MY_URI</code> from the lighting
     *            example.
     * @param theCaller
     * @return
     */
    public static final UserInterfaceService[] getUIServiceInfo(
	    String serviceClassURI, ServiceCaller theCaller) {
	return getUIServiceInfo(new UserInterfaceService(), serviceClassURI,
		theCaller);
    }

    /**
     * @param requestedService
     * @param serviceClassURI
     *            the URI of the service class from an underlying ontology, e.g.
     *            the value of <code>Lighting.MY_URI</code> from the lighting
     *            example.
     * @param theCaller
     * @return
     */
    protected static UserInterfaceService[] getUIServiceInfo(
	    UserInterfaceService requestedService, String serviceClassURI,
	    ServiceCaller theCaller) {
	requestedService.addInstanceLevelRestriction(MergedRestriction
		.getFixedValueRestriction(PROP_CORRELATED_SERVICE_CLASS,
			new Resource(serviceClassURI, true)),
		new String[] { PROP_CORRELATED_SERVICE_CLASS });
	ServiceRequest req = new ServiceRequest(
		SERVICE_REQUEST_URI_PREFIX_INFO, 5, requestedService, null);
	req.addSimpleOutputBinding(new ProcessOutput(OUTPUT_INSTANCE_INFO),
		new String[] { MY_URI });
	ServiceResponse resp = theCaller.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {
	    List outputs = resp.getOutputs();
	    if (outputs != null)
		for (Iterator i = outputs.iterator(); i.hasNext();) {
		    ProcessOutput output = (ProcessOutput) i.next();
		    if (output.getURI().equals(OUTPUT_INSTANCE_INFO)) {
			Object o = output.getParameterValue();
			if (o instanceof List) {
			    try {
				return (UserInterfaceService[]) ((List) o)
					.toArray(new UserInterfaceService[((List) o)
						.size()]);
			    } catch (Exception e) {
			    }
			}
		    }
		}
	}
	return null;
    }

    /**
     * Gets the UI service request. This request is sent at the service bus to
     * present the main dialog of the UI service.
     * 
     * @param serviceClassURI
     *            the URI of the service class from an underlying ontology, e.g.
     *            the value of <code>Lighting.MY_URI</code> from the lighting
     *            example.
     * @param vendor
     *            the URL of the partner home page that provides the UI e.g.
     *            <code>"http://www.igd.fraunhofer.de"</code>
     * @param requestingUser
     *            the user that requested the UI service.
     * @return the UI service request.
     */
    public static final ServiceRequest getUIServiceRequest(
	    String serviceClassURI, String vendor, Resource requestingUser) {
	return getUIServiceRequest(new UserInterfaceService(), serviceClassURI,
		vendor, requestingUser);
    }

    /**
     * Gets the UI service request. This request is sent at the service bus to
     * present the main dialog of the UI service.
     * 
     * @param serviceClassURI
     *            the URI of the service class from an underlying ontology, e.g.
     *            the value of <code>Lighting.MY_URI </code> from the lighting
     *            example.
     * @param vendor
     *            the URL of the partner home page that provides the UI e.g.
     *            <code>"http://www.igd.fraunhofer.de"</code>
     * @param requestingUser
     *            the user that requested the UI service.
     * @return the UI service request.
     */
    protected static ServiceRequest getUIServiceRequest(
	    UserInterfaceService requestedService, String serviceClassURI,
	    String vendor, Resource requestingUser) {
	requestedService.addInstanceLevelRestriction(MergedRestriction
		.getFixedValueRestriction(PROP_CORRELATED_SERVICE_CLASS,
			new Resource(serviceClassURI, true)),
		new String[] { PROP_CORRELATED_SERVICE_CLASS });
	requestedService.addInstanceLevelRestriction(MergedRestriction
		.getFixedValueRestriction(PROP_HAS_VENDOR, new Resource(vendor,
			true)), new String[] { PROP_HAS_VENDOR });
	return new ServiceRequest(SERVICE_REQUEST_URI_PREFIX_START, 5,
		requestedService, requestingUser);
    }

    // public static UserInterfaceService[] getVendorUIServices(String vendor,
    // ServiceCaller theCaller) {
    // UserInterfaceService requestedService = new UserInterfaceService();
    // requestedService.instanceLevelRestrictions.put(PROP_HAS_VENDOR,
    // Restriction.getFixedValueRestriction(PROP_HAS_VENDOR, new
    // Resource(vendor, true)));
    // ServiceRequest req = new ServiceRequest(SERVICE_REQUEST_URI_PREFIX_INFO,
    // 5, requestedService, null);
    // req.addSimpleOutputBinding(
    // new ProcessOutput(OUTPUT_INSTANCE_INFO),
    // new PropertyPath(null, true, new String[] {MY_URI}));
    // ServiceResponse resp = theCaller.call(req);
    // if (resp.getCallStatus() == CallStatus.succeeded) {
    // List outputs = resp.getOutputs();
    // if (outputs != null)
    // for (Iterator i=outputs.iterator(); i.hasNext();) {
    // ProcessOutput output = (ProcessOutput) i.next();
    // if (output.getURI().equals(OUTPUT_INSTANCE_INFO)) {
    // Object o = output.getParameterValue();
    // if (o instanceof List) {
    // try {
    // return (UserInterfaceService[])
    // ((List) o).toArray(new UserInterfaceService[((List) o).size()]);
    // } catch (Exception e) {}
    // }
    // }
    // }
    // }
    // return null;
    // }

    /**
     * 
     */
    protected UserInterfaceService() {
	super();
    }

    /**
     * @param uri
     */
    protected UserInterfaceService(String uri) {
	super(uri);
    }

    /**
     * @see ManagedIndividual#getPropSerializationType(String)
     */
    public int getPropSerializationType(String propURI) {
	return PROP_SERIALIZATION_FULL;
    }
}
