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
package org.universAAL.middleware.context.data;

import org.universAAL.middleware.context.ContextPublisher;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 14, 2012
 * 
 */
public interface IProvisionsData {

    void addProvision(ContextPublisher contextPublisher);

    boolean exist(ContextPublisher contextPublisher);
}
