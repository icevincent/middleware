/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.owl;

import org.universAAL.middleware.rdf.TypeMapper;

public final class LongRestriction extends BoundedValueRestriction {

    public static final String DATATYPE_URI = TypeMapper
	    .getDatatypeURI(Long.class);

    public LongRestriction() {
	super(DATATYPE_URI);
    }

    public LongRestriction(long min, boolean minInclusive, long max,
	    boolean maxInclusive) {
	this(new Long(min), minInclusive, Long.valueOf(max), maxInclusive);
    }

    public LongRestriction(Long min, boolean minInclusive, Long max,
	    boolean maxInclusive) {
	super(TypeMapper.getDatatypeURI(Long.class), min, minInclusive, max,
		maxInclusive);
    }

    protected Comparable getNext(Comparable c) {
	return Long.valueOf(((Long) c).longValue() + 1);
    }

    protected Comparable getPrevious(Comparable c) {
	return Long.valueOf(((Long) c).longValue() - 1);
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#copy() */
    public TypeExpression copy() {
	return copyTo(new LongRestriction());
    }
}
