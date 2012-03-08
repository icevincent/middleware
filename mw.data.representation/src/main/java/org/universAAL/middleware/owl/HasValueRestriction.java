/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut f�r Graphische Datenverarbeitung
	
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.Variable;

/**
 * Implementation of OWL HasValue Restriction: it contains all individuals that
 * are connected by the specified property to the given individual.
 * 
 * @author Carsten Stockloew
 */
public class HasValueRestriction extends PropertyRestriction {

    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "HasValueRestriction";

    public static final String PROP_OWL_HAS_VALUE = OWL_NAMESPACE + "hasValue";;

    private boolean hasVarRefAsValue = false;

    static {
	register(HasValueRestriction.class, null, PROP_OWL_HAS_VALUE, null);
    }

    /** Standard constructor for exclusive use by serializers. */
    HasValueRestriction() {
    }

    public HasValueRestriction(String propURI, Object o) {
	if (propURI == null || o == null)
	    throw new NullPointerException();
	setOnProperty(propURI);
	if (o instanceof String && isQualifiedName((String) o))
	    o = new Resource((String) o);
	super.setProperty(PROP_OWL_HAS_VALUE, o);
    }

    public String getClassURI() {
	return MY_URI;
    }

    public Object getConstraint() {
	return getProperty(PROP_OWL_HAS_VALUE);
    }

    /** @see org.universAAL.middleware.owl.ClassExpression#copy() */
    public ClassExpression copy() {
	return copyTo(new HasValueRestriction());
    }

    /** Helper function for {@link #checkValue(Object, Hashtable)}. */
    private List resolveVariables(List l, Hashtable context) {
	List result = new ArrayList(l.size());
	for (int i = 0; i < l.size(); i++)
	    result.add(Variable.resolveVarRef(l.get(i), context));
	return result;
    }

    /** Helper function for {@link #checkValue(Object, Hashtable)}. */
    private int checkValueLists(List first, List second, Hashtable context) {
	if (first.size() != second.size())
	    return -1;
	Hashtable aux = new Hashtable(second.size());
	for (int i = 0; i < first.size(); i++) {
	    Object o = first.get(i);
	    if (o instanceof Variable) {
		if (((Variable) o).getMinCardinality() > 1)
		    return -1;
		boolean found = false;
		for (Iterator j = second.iterator(); !found && j.hasNext();) {
		    Object oo = j.next();
		    if (ManagedIndividual.checkMembership(((Variable) o)
			    .getParameterType(), oo)) {
			aux.put(((Variable) o).getURI(), oo);
			j.remove();
			found = true;
		    }
		}
		if (!found)
		    return -1;
	    } else if (!second.remove(o)) {
		boolean found = false;
		for (Iterator j = second.iterator(); !found && j.hasNext();) {
		    Object oo = j.next();
		    if (oo instanceof Variable) {
			if (((Variable) oo).getMinCardinality() > 1)
			    return -1;
			if (ManagedIndividual.checkMembership(((Variable) oo)
				.getParameterType(), o)) {
			    aux.put(((Variable) oo).getURI(), o);
			    j.remove();
			    found = true;
			}
		    }
		}
		if (!found)
		    return -1;
	    }
	}
	if (!second.isEmpty())
	    return -1;
	if (!aux.isEmpty())
	    if (context == null)
		return -1;
	    else {
		for (Iterator i = aux.keySet().iterator(); i.hasNext();) {
		    Object key = i.next();
		    context.put(key, aux.get(key));
		}
		return 1;
	    }
	return 0;
    }

    /** Helper function for {@link #hasMember(Object, Hashtable)}. */
    // -1 -> incompatible; 0 -> equal; 1 -> compatible
    private int checkValue(Object value, Hashtable context) {
	if (value == null)
	    return 1;

	Object myValue = props.get(PROP_OWL_HAS_VALUE);
	if (myValue == null)
	    // no value restriction => all values are compatible
	    return 1;

	if (myValue instanceof List)
	    myValue = resolveVariables((List) myValue, context);
	else {
	    List aux = new ArrayList(1);
	    aux.add(Variable.resolveVarRef(myValue, context));
	    myValue = aux;
	}

	if (value == null) {
	    if (((List) myValue).size() == 1)
		myValue = ((List) myValue).get(0);
	    else
		return -1;

	    // an optional parameter without any existing and / or default value
	    // means that null value is accepted; then we remark that under the
	    // condition that this parameter remains null, the null value is
	    // acceptable;
	    // for this purpose rdf:nil is used. An existing remark means that
	    // the above was asserted previously
	    if (RDF_EMPTY_LIST.equals(myValue))
		return 0;
	    if (myValue instanceof Variable
		    && ((Variable) myValue).getMinCardinality() == 0
		    && ((Variable) myValue).getDefaultValue() == null) {
		context.put(((Variable) myValue).getURI(), RDF_EMPTY_LIST);
		return 0;
	    }
	    return -1;
	}

	if (value instanceof List)
	    value = resolveVariables((List) value, context);
	else {
	    List aux = new ArrayList(1);
	    aux.add(Variable.resolveVarRef(value, context));
	    value = aux;
	}

	return checkValueLists((List) myValue, (List) value, context);
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#hasMember(Object,
     *      Hashtable)
     */
    public boolean hasMember(Object member, Hashtable context) {
	if (!(member instanceof Resource))
	    return member == null;

	Object value = ((Resource) member).getProperty(getOnProperty());
	if (checkValue(value, context) == -1)
	    return false;
	return true;
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#isDisjointWith(ClassExpression,
     *      Hashtable)
     */
    public boolean isDisjointWith(ClassExpression other, Hashtable context) {
	if (!(other instanceof PropertyRestriction))
	    return other.isDisjointWith(this, context);

	PropertyRestriction r = (PropertyRestriction) other;
	Object o = getOnProperty();
	if (o == null || !o.equals(r.getOnProperty()))
	    return false;

	o = r.getProperty(PROP_OWL_HAS_VALUE);
	Hashtable cloned = (context == null) ? null : (Hashtable) context
		.clone();
	switch (checkValue(o, cloned)) {
	case -1: // incompatible
	    return true;
	case 0: // equal
	    if (cloned == null || cloned.size() == context.size())
		// unconditional equality
		return false;
	    else
		// TODO: because the equality was conditional, there is still a
		// chance to
		// return true by adopting complement conditions into context
		return false;
	}

	return true;
    }

    /** @see org.universAAL.middleware.owl.ClassExpression#isWellFormed() */
    public boolean isWellFormed() {
	return getOnProperty() != null && (hasProperty(PROP_OWL_HAS_VALUE));
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#matches(ClassExpression,
     *      Hashtable)
     */
    public boolean matches(ClassExpression subset, Hashtable context) {
	Object noRes = matchesNonRestriction(subset, context);
	if (noRes instanceof Boolean)
	    return ((Boolean) noRes).booleanValue();

	PropertyRestriction other = (PropertyRestriction) noRes;

	Hashtable cloned = (context == null) ? null : (Hashtable) context
		.clone();

	Object o = other.getProperty(PROP_OWL_HAS_VALUE);
	switch (checkValue(o, cloned)) {
	case -1:
	    return false;
	case 0:
	case 1:
	    synchronize(context, cloned);
	    return true;
	}

	return false;
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public void setProperty(String propURI, Object o) {
	if (o == null || propURI == null || props.containsKey(propURI))
	    return;

	// handle this restriction
	if (PROP_OWL_HAS_VALUE.equals(propURI)) {
	    ClassExpression hasVal = (ClassExpression) getProperty(PROP_OWL_HAS_VALUE);
	    if (hasVal != null)
		return;
	    super.setProperty(PROP_OWL_HAS_VALUE, o);
	    hasVarRefAsValue = Variable.isVarRef(o);
	    return;
	}

	// do not handle other restrictions
	if (propMap.containsKey(propURI))
	    return;

	// for everything else: call super
	super.setProperty(propURI, o);
    }
}