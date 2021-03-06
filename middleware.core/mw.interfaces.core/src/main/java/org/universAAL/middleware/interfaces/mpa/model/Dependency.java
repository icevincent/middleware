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

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * 
 * Dependency of feature.
 * 
 * 
 * <p>
 * Java class for dependency complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="dependency">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://karaf.apache.org/xmlns/features/v1.0.0>featureName">
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" default="0.0.0" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dependency", namespace = "http://karaf.apache.org/xmlns/features/v1.0.0", propOrder = { "value" })
public class Dependency implements Serializable {

    private final static long serialVersionUID = 12343L;
    @XmlValue
    protected String value;
    @XmlAttribute(name = "version")
    protected String version;

    /**
     * 
     * Feature name should be non empty string.
     * 
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getValue() {
	return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setValue(String value) {
	this.value = value;
    }

    public boolean isSetValue() {
	return (this.value != null);
    }

    /**
     * Gets the value of the version property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getVersion() {
	if (version == null) {
	    return "0.0.0";
	} else {
	    return version;
	}
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setVersion(String value) {
	this.version = value;
    }

    public boolean isSetVersion() {
	return (this.version != null);
    }

}
