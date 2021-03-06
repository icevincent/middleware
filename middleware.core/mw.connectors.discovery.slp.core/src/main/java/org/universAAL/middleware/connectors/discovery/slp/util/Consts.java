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
package org.universAAL.middleware.connectors.discovery.slp.util;

/**
 * Widely-used constants for the SLP Discovery Connector
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public final class Consts {
    public static String AALSPaceServiceTypeName = "aalSpaceServiceType";
    public static String SDPProtocols = "sdpprtocol";
    public static String BROWSE_SLP_NETWORK = "browseSLPNetwork";
    public static String SLP_INIT_DELAY_SCAN = "slpInitDelayScan";
    public static String SLP_PERIOD_SCAN = "slpPeriodScan";

    public static String SEARCH_ALL = "("
	    + org.universAAL.middleware.interfaces.aalspace.Consts.AALSPaceName
	    + "=*)";

}
