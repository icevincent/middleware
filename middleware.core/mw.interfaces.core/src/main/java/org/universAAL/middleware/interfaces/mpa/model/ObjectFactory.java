/*
        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

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
package org.universAAL.middleware.interfaces.mpa.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the
 * org.universAAL.middleware.connectors.deploy.model package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Features_QNAME = new QName(
	    "http://karaf.apache.org/xmlns/features/v1.0.0", "features");
    private final static QName _OntologyTypeLocationUrl_QNAME = new QName(
	    "http://universaal.org/aal-mpa/v1.0.0", "url");
    private final static QName _OntologyTypeLocationRuntimeId_QNAME = new QName(
	    "http://universaal.org/aal-mpa/v1.0.0", "runtimeId");
    private final static QName _OntologyTypeLocationPath_QNAME = new QName(
	    "http://universaal.org/aal-mpa/v1.0.0", "path");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package:
     * org.universAAL.middleware.connectors.deploy.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AalMpa }
     * 
     */
    public AalMpa createAalMpa() {
	return new AalMpa();
    }

    /**
     * Create an instance of {@link DeploymentUnit }
     * 
     */
    public DeploymentUnit createDeploymentUnit() {
	return new DeploymentUnit();
    }

    /**
     * Create an instance of {@link OntologyType }
     * 
     */
    public OntologyType createOntologyType() {
	return new OntologyType();
    }

    /**
     * Create an instance of {@link DeploymentUnit.ContainerUnit }
     * 
     */
    public DeploymentUnit.ContainerUnit createDeploymentUnitContainerUnit() {
	return new DeploymentUnit.ContainerUnit();
    }

    /**
     * Create an instance of {@link AalMpa.ApplicationManagement }
     * 
     */
    public AalMpa.ApplicationManagement createAalMpaApplicationManagement() {
	return new AalMpa.ApplicationManagement();
    }

    /**
     * Create an instance of {@link AalMpa.ApplicationProfile }
     * 
     */
    public AalMpa.ApplicationProfile createAalMpaApplicationProfile() {
	return new AalMpa.ApplicationProfile();
    }

    /**
     * Create an instance of {@link AalMpa.ApplicationProfile.Runtime }
     * 
     */
    public AalMpa.ApplicationProfile.Runtime createAalMpaApplicationProfileRuntime() {
	return new AalMpa.ApplicationProfile.Runtime();
    }

    /**
     * Create an instance of {@link AalMpa.ApplicationProfile.AalSpace }
     * 
     */
    public AalMpa.ApplicationProfile.AalSpace createAalMpaApplicationProfileAalSpace() {
	return new AalMpa.ApplicationProfile.AalSpace();
    }

    /**
     * Create an instance of {@link AalMpa.App }
     * 
     */
    public AalMpa.App createAalMpaApp() {
	return new AalMpa.App();
    }

    /**
     * Create an instance of {@link AalMpa.ApplicationProvider }
     * 
     */
    public AalMpa.ApplicationProvider createAalMpaApplicationProvider() {
	return new AalMpa.ApplicationProvider();
    }

    /**
     * Create an instance of {@link AalMpa.ApplicationPart }
     * 
     */
    public AalMpa.ApplicationPart createAalMpaApplicationPart() {
	return new AalMpa.ApplicationPart();
    }

    /**
     * Create an instance of {@link ExecutionUnit }
     * 
     */
    public ExecutionUnit createExecutionUnit() {
	return new ExecutionUnit();
    }

    /**
     * Create an instance of
     * {@link org.universAAL.middleware.connectors.deploy.model.Broker }
     * 
     */
    public org.universAAL.middleware.interfaces.mpa.model.Broker createBroker() {
	return new org.universAAL.middleware.interfaces.mpa.model.Broker();
    }

    /**
     * Create an instance of {@link ArtifactType }
     * 
     */
    public ArtifactType createArtifactType() {
	return new ArtifactType();
    }

    /**
     * Create an instance of {@link Part }
     * 
     */
    public Part createPart() {
	return new Part();
    }

    /**
     * Create an instance of {@link ProfileType }
     * 
     */
    public ProfileType createProfileType() {
	return new ProfileType();
    }

    /**
     * Create an instance of {@link VersionType }
     * 
     */
    public VersionType createVersionType() {
	return new VersionType();
    }

    /**
     * Create an instance of {@link ToBeDefined }
     * 
     */
    public ToBeDefined createToBeDefined() {
	return new ToBeDefined();
    }

    /**
     * Create an instance of {@link FeaturesRoot }
     * 
     */
    public FeaturesRoot createFeaturesRoot() {
	return new FeaturesRoot();
    }

    /**
     * Create an instance of {@link Dependency }
     * 
     */
    public Dependency createDependency() {
	return new Dependency();
    }

    /**
     * Create an instance of {@link ConfigFile }
     * 
     */
    public ConfigFile createConfigFile() {
	return new ConfigFile();
    }

    /**
     * Create an instance of {@link Config }
     * 
     */
    public Config createConfig() {
	return new Config();
    }

    /**
     * Create an instance of {@link Bundle }
     * 
     */
    public Bundle createBundle() {
	return new Bundle();
    }

    /**
     * Create an instance of {@link Feature }
     * 
     */
    public Feature createFeature() {
	return new Feature();
    }

    /**
     * Create an instance of {@link OntologyType.Location }
     * 
     */
    public OntologyType.Location createOntologyTypeLocation() {
	return new OntologyType.Location();
    }

    /**
     * Create an instance of {@link DeploymentUnit.ContainerUnit.Karaf }
     * 
     */
    public DeploymentUnit.ContainerUnit.Karaf createDeploymentUnitContainerUnitKaraf() {
	return new DeploymentUnit.ContainerUnit.Karaf();
    }

    /**
     * Create an instance of
     * {@link AalMpa.ApplicationManagement.RemoteManagement }
     * 
     */
    public AalMpa.ApplicationManagement.RemoteManagement createAalMpaApplicationManagementRemoteManagement() {
	return new AalMpa.ApplicationManagement.RemoteManagement();
    }

    /**
     * Create an instance of {@link AalMpa.ApplicationProfile.Runtime.Broker }
     * 
     */
    public AalMpa.ApplicationProfile.Runtime.Broker createAalMpaApplicationProfileRuntimeBroker() {
	return new AalMpa.ApplicationProfile.Runtime.Broker();
    }

    /**
     * Create an instance of {@link AalMpa.ApplicationProfile.Runtime.Managers }
     * 
     */
    public AalMpa.ApplicationProfile.Runtime.Managers createAalMpaApplicationProfileRuntimeManagers() {
	return new AalMpa.ApplicationProfile.Runtime.Managers();
    }

    /**
     * Create an instance of
     * {@link AalMpa.ApplicationProfile.AalSpace.AlternativeProfiles }
     * 
     */
    public AalMpa.ApplicationProfile.AalSpace.AlternativeProfiles createAalMpaApplicationProfileAalSpaceAlternativeProfiles() {
	return new AalMpa.ApplicationProfile.AalSpace.AlternativeProfiles();
    }

    /**
     * Create an instance of
     * {@link AalMpa.ApplicationProfile.AalSpace.RequirredOntologies }
     * 
     */
    public AalMpa.ApplicationProfile.AalSpace.RequirredOntologies createAalMpaApplicationProfileAalSpaceRequirredOntologies() {
	return new AalMpa.ApplicationProfile.AalSpace.RequirredOntologies();
    }

    /**
     * Create an instance of {@link AalMpa.App.License }
     * 
     */
    public AalMpa.App.License createAalMpaAppLicense() {
	return new AalMpa.App.License();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FeaturesRoot }
     * {@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://karaf.apache.org/xmlns/features/v1.0.0", name = "features")
    public JAXBElement<FeaturesRoot> createFeatures(FeaturesRoot value) {
	return new JAXBElement<FeaturesRoot>(_Features_QNAME,
		FeaturesRoot.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://universaal.org/aal-mpa/v1.0.0", name = "url", scope = OntologyType.Location.class)
    public JAXBElement<Object> createOntologyTypeLocationUrl(Object value) {
	return new JAXBElement<Object>(_OntologyTypeLocationUrl_QNAME,
		Object.class, OntologyType.Location.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://universaal.org/aal-mpa/v1.0.0", name = "runtimeId", scope = OntologyType.Location.class)
    public JAXBElement<String> createOntologyTypeLocationRuntimeId(String value) {
	return new JAXBElement<String>(_OntologyTypeLocationRuntimeId_QNAME,
		String.class, OntologyType.Location.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://universaal.org/aal-mpa/v1.0.0", name = "path", scope = OntologyType.Location.class)
    public JAXBElement<String> createOntologyTypeLocationPath(String value) {
	return new JAXBElement<String>(_OntologyTypeLocationPath_QNAME,
		String.class, OntologyType.Location.class, value);
    }

}
