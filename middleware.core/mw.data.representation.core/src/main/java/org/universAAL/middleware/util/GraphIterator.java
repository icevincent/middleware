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
package org.universAAL.middleware.util;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.universAAL.middleware.rdf.Resource;

/**
 * Iterate over all Resources of an RDF graph.
 * 
 * @author Carsten Stockloew
 */
public class GraphIterator implements Iterator {

    /**
     * The set of resources that have been visited before. During iteration,
     * this will be used to avoid endless loop in case of cycles.
     */
    protected Set visitedResources = new HashSet();

    /**
     * The current information at a certain depth.
     */
    protected StackElement se;

    /**
     * The stack contains information for previous depth. When going one level
     * deeper into the graph, the information about the current state, e.g. the
     * enumerator of properties, is stored in the stack to be restored when all
     * child nodes have been processed.
     */
    protected Stack stack = new Stack();

    /**
     * The next element as being returned by {@link #next()}. It can be
     * calculated by {@link #next()} and by {@link #hasNext()}.
     */
    protected GraphIteratorElement nextElement = null;

    /**
     * Element to be stored in the stack.
     */
    protected class StackElement {
	/** The parent node. */
	private Resource nodeParent;

	/** The child node. Can be a {@link Resource}, a Literal, or a list. */
	private Object nodeChild = null;

	/** The depth, i.e. the distance to the root node. */
	private int depth;

	/**
	 * Enumerator for properties to iterate over all property URIs of a
	 * Resource.
	 */
	private Enumeration enumProp;

	/** The current property URI. */
	private String propURI;

	/**
	 * If the child node is a list, this variable holds the iterator over
	 * elements of the list.
	 */
	private Iterator lstIterator = null;

	/**
	 * If the child node is a list, this variable holds the index of the
	 * currently selected element of the list.
	 */
	private int lstIndex = 0;

	/**
	 * If the child node is a list, this variable holds the currently
	 * selected element of the list.
	 */
	private Object lstElement = null;
    }

    /**
     * A specialized iterator to iterate only over instances of {@link Resource}
     * .
     */
    protected static class GraphIteratorResources extends GraphIterator {
	boolean first = true;

	GraphIteratorResources(Resource root) {
	    super(root);
	}

	protected void createNext() {
	    if (first)
		return;

	    while (true) {
		super.createNext();
		if (nextElement == null)
		    return;
		if (nextElement.getObject() instanceof Resource) {
		    if (!visitedResources.contains(nextElement.getObject()))
			return;
		}
		// force createNext
		nextElement = null;
	    }
	}

	public boolean hasNext() {
	    if (first)
		return true;
	    return super.hasNext();
	}

	public Object next() {
	    if (first) {
		first = false;
		visitedResources.add(se.nodeParent);
		return se.nodeParent;
	    } else {
		GraphIteratorElement el = (GraphIteratorElement) super.next();
		if (el == null)
		    return null;
		visitedResources.add(el.getObject());
		return el.getObject();
	    }
	}
    }

    protected GraphIterator(Resource root) {
	stepDeeper(root);
    }

    /**
     * Create a new Iterator that iterates over all triples. The return value of
     * {@link #next()} is of type {@link GraphIteratorElement}.
     * 
     * @param root
     *            the {@link Resource} that is the root of the graph.
     * @return an {@link Iterator} to iterate over elements of the graph.
     */
    public static Iterator getIterator(Resource root) {
	if (root == null)
	    throw new NullPointerException(
		    "The argument of a GraphIterator can not be null.");
	return new GraphIterator(root);
    }

    /**
     * Create a new Iterator that iterates over all Resources. The return value
     * of {@link #next()} is of type {@link Resource}.
     * 
     * @param root
     *            the {@link Resource} that is the root of the graph.
     * @return an {@link Iterator} to iterate over elements of the graph.
     */
    public static Iterator getResourceIterator(Resource root) {
	if (root == null)
	    throw new NullPointerException(
		    "The argument of a GraphIterator can not be null.");
	return new GraphIteratorResources(root);
    }

    // this is as a separate method so that it can be overwritten, e.g. to
    // provide a sorted list of properties
    protected Enumeration getPropertyEnumeration(Resource r) {
	return r.getPropertyURIs();
    }

    protected void stepDeeper(Resource root) {
	visitedResources.add(root.getURI());
	StackElement newSe = new StackElement();
	newSe.nodeParent = root;
	newSe.depth = 0;
	newSe.enumProp = getPropertyEnumeration(newSe.nodeParent);
	if (se != null) {
	    // this is not the first level
	    newSe.depth = se.depth + 1;
	    stack.push(se);
	}
	se = newSe;
    }

    protected boolean stepHigher() {
	if (stack.isEmpty())
	    return false;
	se = (StackElement) stack.pop();
	return true;
    }

    protected void createResult() {
	if (se.nodeChild instanceof List)
	    nextElement = new GraphIteratorElement(se.nodeParent, se.propURI,
		    se.lstElement, se.depth, true, se.lstIndex,
		    (List) se.nodeChild);
	else
	    nextElement = new GraphIteratorElement(se.nodeParent, se.propURI,
		    se.nodeChild, se.depth, false, 0, null);
    }

    protected void createNext() {
	if (nextElement != null)
	    return;

	// if the current element is a resource -> follow path
	Resource r = null;
	if (se.nodeChild instanceof Resource) {
	    r = (Resource) se.nodeChild;
	} else if (se.lstElement instanceof Resource) {
	    r = (Resource) se.lstElement;
	}
	if (r != null && !visitedResources.contains(r.getURI()))
	    stepDeeper(r);

	while (true) {
	    // increase iterator
	    if (se.nodeChild instanceof List) {
		if (se.lstIterator.hasNext()) {
		    // next property list element
		    se.lstElement = se.lstIterator.next();
		    se.lstIndex++;
		} else {
		    // list is done -> next property
		    se.lstElement = null;
		    se.lstIndex = 0;
		    se.lstIterator = null;
		    se.nodeChild = null;
		    continue;
		}
	    } else {
		if (se.enumProp.hasMoreElements()) {
		    se.propURI = (String) se.enumProp.nextElement();
		    se.nodeChild = se.nodeParent.getProperty(se.propURI);
		    if (se.nodeChild instanceof List) {
			se.lstIndex = 0;
			se.lstIterator = ((List) se.nodeChild).iterator();
		    }
		} else {
		    // resource is done -> stepHigher
		    if (stepHigher())
			continue;
		    // this case should only happen if next() is called if
		    // hasNext() would return false
		    return;
		}
	    }

	    // we are now at the next element, go on to the next valid element
	    if (se.nodeChild instanceof Resource) {
		createResult();
		return;
	    } else if (se.nodeChild instanceof List) {
		if (se.lstElement != null) {
		    createResult();
		    return;
		}
	    } else if (se.nodeChild != null) {
		createResult();
		return;
	    }
	}
    }

    /** @see java.util.Iterator#hasNext() */
    public boolean hasNext() {
	createNext();
	return nextElement != null;
    }

    /**
     * @see java.util.Iterator#next()
     * @return the next element of the iterator. The type of the return value
     *         depends on the type of the iterator.
     */
    public Object next() {
	createNext();
	Object retVal = nextElement;
	nextElement = null;
	return retVal;
    }

    /** @see java.util.Iterator#remove() */
    public void remove() {
	throw new UnsupportedOperationException(
		"Removing resources is not allowed in a GraphIterator");
    }
}
