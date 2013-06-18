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
package org.universAAL.middleware.connectors.exception;

/**
 * Communication Connector Exception
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:filippo.palumbo@isti.cnr.it">Filippo Palumbo</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class CommunicationConnectorException extends RuntimeException {

    private String description;
    private int errorCode;

    public CommunicationConnectorException(int i, String description) {
        super(description);
        this.description = description;
        this.errorCode = i;
    }

    public CommunicationConnectorException(
            CommunicationConnectorErrorCode code, String description) {
        this(code.ordinal(), description);
    }

    public CommunicationConnectorException(CommunicationConnectorErrorCode code) {
        this(code.ordinal(), code.toString());
    }

    public CommunicationConnectorException(
            CommunicationConnectorErrorCode code, Throwable t) {
        super("Internal exception", t);
        this.errorCode = code.ordinal();
        this.description = code.toString();
    }

    public CommunicationConnectorException(
            CommunicationConnectorErrorCode code, String msg, Throwable t) {
        super(msg, t);
        this.errorCode = code.ordinal();
        this.description = msg;
    }

    public String getDescription() {
        return description;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
