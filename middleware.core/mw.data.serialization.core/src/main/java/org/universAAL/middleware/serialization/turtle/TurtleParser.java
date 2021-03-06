/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research 
	
	Copyright Aduna (http://www.aduna-software.com/) 2001-2007
	
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
package org.universAAL.middleware.serialization.turtle;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.container.utils.StringUtils;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.PropertyRestriction;
import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.TypeExpressionFactory;
import org.universAAL.middleware.rdf.ClosedCollection;
import org.universAAL.middleware.rdf.LangString;
import org.universAAL.middleware.rdf.OpenCollection;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;

/**
 * Serialization and Deserialization of RDF graphs. This class implements the
 * interface
 * {@link org.universAAL.middleware.serialization.MessageContentSerializer} and
 * can be called to translate RDF graphs into <i>Terse RDF Triple Language
 * (Turtle)</i> and vice versa. While this class handles the deserialization,
 * the actual serialization is realized by {@link TurtleWriter}.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 */
public class TurtleParser {

    // debug, to be commented out (together with the code it controls)
    public static boolean dbg = false;

    /** URI for an empty RDF List. */
    private static final Resource NIL = new Resource(Resource.RDF_EMPTY_LIST);

    /**
     * Information of references of a resource. For a certain resource, this
     * class holds detailed information about all places that have the resource
     * as RDF object.
     */
    private class RefData {
	/** The resource that references the resource in question. */
	Resource src;

	/** The property of the resource 'src'. */
	String prop;

	/** If the property given by 'prop' is a list, this is the list. */
	List l;

	/**
	 * If the property given by 'prop' is a list, this is the index in the
	 * list.
	 */
	int i;
    }

    /**
     * A List of references. Elements of the list 'refs' are instances of
     * {@link RefData}.
     */
    private class ParseData {
	// String label = null;
	List refs = new ArrayList(3);
    }

    /** The character stream to read from. */
    private PushbackReader reader;

    private Resource subject;

    private Resource firstResource = null;

    private String predicate;

    private Object object;

    private boolean eofAfterImplicitBlankNodeAsSubject = false;

    private Hashtable namespaceTable = new Hashtable();

    /**
     * The set of resources in the serialized String. This table is used in the
     * first step of deserialization to store all resources as instances of
     * {@link Resource}. In a second step, these resources are specialized as
     * objects of subclasses of {@link Resource}.
     */
    private Hashtable resources = new Hashtable();

    /**
     * The set of specialized resources.
     */
    // private Hashtable specialized = new Hashtable();

    /**
     * The set of references of resources during the first step of
     * deserialization. The table stores for a resource URI (key of the table)
     * the {@link ParseData} (value of the table) which holds a list of all
     * references to this resource. This is needed during specialization to
     * update all references to the specialized object.
     */
    private Hashtable parseTable = new Hashtable();

    /**
     * The set of blank nodes. Blank nodes are serialized with a prefix "_:" (in
     * our case, this is followed by "BN" and a number). Those blank nodes are
     * transformed in a {@link Resource} with an anonymous URI. If there are
     * multiple references to this node, this table will store a mapping from
     * the blank node identifier to the URI of the URI.
     */
    private Hashtable blankNodes = new Hashtable();

    // debug: TODO: remove
    private List lstResources = new LinkedList();

    private static final String stringifiedPosInf = Double
	    .toString(Double.POSITIVE_INFINITY);
    private static final String stringifiedNegInf = Double
	    .toString(Double.NEGATIVE_INFINITY);

    public TurtleParser() {
    }

    private void addRef(Resource referred, Resource referredBy, String prop,
	    List l, int i) {
	// we do not need to keep book on references to resources representing a
	// type - they are handled directly in reportStatement()
	if (referred.serializesAsXMLLiteral()
		|| Resource.PROP_RDF_TYPE.equals(prop))
	    return;

	RefData rd = new RefData();
	rd.src = referredBy;
	rd.prop = prop;
	rd.l = l;
	rd.i = i;
	getData(referred).refs.add(rd);
    }

    private boolean containsResource(List l) {
	for (int i = 0; i < l.size(); i++)
	    if (l.get(i) instanceof Resource
		    && !((Resource) l.get(i)).serializesAsXMLLiteral())
		return true;
	return false;
    }

    public Object deserialize(String serialized, String resourceURI) {
	if (serialized == null)
	    return null;

	return deserialize(serialized, false, resourceURI);
    }

    private Object deserialize(String serialized, boolean wasXMLLiteral,
	    String resourceURI) {
	try {
	    parse(new StringReader(serialized), "");
	    Resource result = finalizeAndGetRoot(resourceURI);
	    if (result != null) {
		if (dbg) {
		    System.out
			    .println(" ---------------------\n  deserialization result:\n"
				    + result.toStringRecursive());
		}
		if (wasXMLLiteral)
		    result = result.copy(true);
		else if (Resource.TYPE_RDF_LIST.equals(result.getType())) {
		    // Object first =
		    // result.getProperty(Resource.PROP_RDF_FIRST);
		    // if (first == null)
		    // return null;
		    // Object rest = result.getProperty(Resource.PROP_RDF_REST);
		    // if (rest != null
		    // && rest.toString().equals(Resource.RDF_EMPTY_LIST))
		    // rest = new ArrayList(1);
		    // else if (!(rest instanceof List))
		    // return null;
		    // ((List) rest).add(0, first);
		    // return rest;
		    return result.asList();
		}
	    }
	    return result;
	} catch (Exception e) {
	    LogUtils.logError(TurtleUtil.moduleContext, TurtleParser.class,
		    "deserialize", new Object[] {
			    "Turtle-Serializer: Failed to parse\n    ",
			    serialized, "\n    returning null!" }, e);
	    return null;
	}
    }

    private String stringifyRefData(Object key, RefData rd) {
	String buf = "\n        ";
	if (rd != null) {
	    buf += rd.src;
	    buf += "\n          prop: " + rd.prop;
	    buf += "\n          key:  " + key;
	}
	return buf;
    }

    private void logOpenItems(String text, Hashtable openItems) {
	Object[] msgParts = new Object[openItems.size() + 1];
	msgParts[0] = text;
	int ind = 1;
	Enumeration enumKey = openItems.keys();
	while (enumKey.hasMoreElements()) {
	    Object key = enumKey.nextElement();
	    RefData rd = (RefData) openItems.get(key);
	    msgParts[ind++] = stringifyRefData(key, rd);
	}

	LogUtils.logDebug(TurtleUtil.moduleContext, TurtleParser.class,
		"logOpenItems", msgParts, null);
	// if (dbg) {
	// String s = "";
	// for (int i=0; i<msgParts.length; i++)
	// s += msgParts[i];
	// System.out.println("logOpenItems: " + s);
	// }
    }

    /**
     * Second step of deserialization. This method takes the list of resources
     * (and references data) from the first step and specializes all resources.
     * 
     * @param resourceURI
     *            Can be used to select a root node
     * @return the root node.
     */
    private Resource finalizeAndGetRoot(String resourceURI) {
	Resource aux;
	Resource specialized;
	Resource result = null;
	Hashtable openItems = new Hashtable();
	Hashtable specializedResources = new Hashtable();

	// some debug output
	if (dbg)
	    if (resources.size() != 0) {
		String buf = "";
		buf += "Resources:\n";
		for (Object o : lstResources) {
		    buf += "   " + o.toString() + "\n";
		}
		buf += "Parse Tables\n";
		for (Iterator i = resources.values().iterator(); i.hasNext();) {
		    aux = (Resource) i.next();
		    ParseData pd = (ParseData) parseTable.get(aux.getURI());
		    buf += "    Resource: " + aux.getURI()
			    + (pd == null ? " pd=null\n" : "\n");
		    if (pd != null && pd.refs != null) {
			for (Iterator j = pd.refs.iterator(); j.hasNext();) {
			    RefData rd = (RefData) j.next();
			    buf += "          src:  " + rd.src + "\n";
			    buf += "          prop: " + rd.prop;
			    if (rd.l != null)
				buf += "   (List, index " + rd.i + ")";
			    buf += "\n";
			}
		    }
		    buf += "              - "
			    + aux.toStringRecursive().replace("\n",
				    "\n              - ");
		    buf += "\n";
		}
		LogUtils.logDebug(TurtleUtil.moduleContext, TurtleParser.class,
			"finalizeAndGetRoot", new Object[] { buf }, null);

		// additional output: are there any entries in the parseTable
		// that we do not have in the table resource?
		// Those elements would not be considered..
		for (Object o : parseTable.keySet()) {
		    if (!resources.containsKey(o))
			System.out
				.println(" -- the element is not in resources: "
					+ o);
		}
		System.out.println(" -- Sizes: resource (" + resources.size()
			+ "), lstResources (" + lstResources.size() + ")");
		System.out.println();
	    }

	// for (Iterator i = resources.values().iterator(); i.hasNext();) {
	// aux = (Resource) i.next();
	// i.remove();

	// Iterate in reverse.
	ListIterator li = lstResources.listIterator(lstResources.size());
	while (li.hasPrevious()) {
	    Object prev = li.previous();
	    aux = (Resource) resources.get(prev);
	    if (aux == null) {
		System.out.println(" -- continue for " + prev);
		continue;
	    }

	    if (dbg) {
		if (aux.getTypes().length == 2
			&& aux.getTypes()[1].equals(MergedRestriction.MY_URI))
		    System.out.println("found mergedrestriction");
		System.out.println("-- finalizeAndGetRoot: processing\n"
			+ aux.toStringRecursive());
	    }
	    ParseData pd = (ParseData) parseTable.remove(aux.getURI());
	    specialized = (aux.numberOfProperties() == 0) ? aux : specialize(
		    aux, specializedResources, openItems);
	    if (/* resourceURI != null && */specialized != null) {
		// this.specialized.put(aux.getURI(), specialized);
		if (dbg)
		    System.out.println("-- finalizeAndGetRoot: specialized\n"
			    + specialized.toStringRecursive() + "\n\n");
	    } else {
		if (dbg)
		    System.out
			    .println("-- finalizeAndGetRoot: specialized null\n\n");
	    }
	    if (firstResource == aux)
		firstResource = specialized;
	    if (aux.numberOfProperties() > 0
		    && (pd == null || pd.refs.isEmpty())) {
		if (result == null)
		    result = specialized;
		else {
		    if (resourceURI == null)
			throw new RuntimeException("Root resource not unique: "
				+ result.getURI() + "  " + aux.getURI());
		}
	    }

	    // update all the places, that reference this resource, to the
	    // specialized one
	    if (pd != null && pd.refs != null) {
		for (int j = 0; j < pd.refs.size(); j++) {
		    RefData rd = (RefData) pd.refs.get(j);
		    aux = (Resource) specializedResources.get(rd.src.getURI());
		    if (aux == null) {
			aux = rd.src;
		    }
		    if (rd.l == null) {
			// the specialized resources is the property value
			// directly, not an element in a list
			if (!testSetProperty(aux, rd.prop, specialized)) {
			    // TODO: check comment
			    // we only store this as an open item if there is a
			    // chance that we can specialize it later. There is
			    // no chance if there is no type info available
			    // if (specialized.getTypes().length == 0) {
			    if (dbg)
				System.out.println("-- openItems.put 1: "
					+ specialized + " - " + rd);
			    openItems.put(specialized, rd);
			    // }
			}
		    } else {
			// the specialized resource is an element in a
			// list, we leave this list as an open item until all
			// elements of the list are specialized and then we
			// try again to set the property of the source with
			// this list
			rd.l.set(rd.i, specialized);
			if (dbg)
			    System.out.println("-- openItems.put 3: " + rd.prop
				    + rd.src.getURI() + " - " + rd);
			openItems.put(rd.prop + rd.src.getURI(), rd);
		    }
		}
	    }
	}

	int size = Integer.MAX_VALUE;
	// try to resolve the open items until there is no change anymore in the
	// set of open items
	while (!openItems.isEmpty() && size > openItems.size()) {
	    if (dbg)
		logOpenItems("Open Items during step 2:", openItems);
	    size = openItems.size();
	    Hashtable newOpenItems = new Hashtable();
	    for (Iterator i = openItems.keySet().iterator(); i.hasNext();) {
		Object o = i.next();
		RefData rd = (RefData) openItems.get(o);
		if (rd == null) {
		    // maybe it is a bug in the JVM because this shouldn't be
		    // possible to occur, but it happens!!
		    LogUtils.logDebug(
			    TurtleUtil.moduleContext,
			    TurtleParser.class,
			    "finalizeAndGetRoot",
			    new Object[] {
				    "RefData is null, please investigate: ", o },
			    null);
		    i.remove();
		    continue;
		}
		boolean srcSpecialized = true;
		aux = (Resource) specializedResources.get(rd.src.getURI());
		if (aux == null) {
		    aux = rd.src;
		    srcSpecialized = false;
		}
		if (o instanceof String) {
		    // the property value is a list of elements, so we try to
		    // find for each element of the list a specialized resource
		    o = rd.l;
		    for (int j = 0; j < rd.l.size(); j++)
			if (rd.l.get(j) instanceof Resource) {
			    specialized = (Resource) specializedResources
				    .get(((Resource) rd.l.get(j)).getURI());
			    if (specialized != null)
				rd.l.set(j, specialized);
			}
		} else if (o instanceof Resource) {
		    specialized = (Resource) specializedResources
			    .get(((Resource) o).getURI());
		    if (specialized != null)
			o = specialized;
		}

		if (testSetProperty(aux, rd.prop, o)) {
		    i.remove();
		    if (!srcSpecialized
			    && (TypeExpression.OWL_CLASS.equals(aux.getType()) || PropertyRestriction.MY_URI
				    .equals(aux.getType())))
			specialize(aux, specializedResources, newOpenItems);
		}
	    }
	    // move everything from NewOpenItems to openItems
	    openItems.putAll(newOpenItems);
	}

	if (!openItems.isEmpty()) {
	    logOpenItems(
		    "There are relationships not resolved (please note, that this "
			    + "does not necessarily imply that something is wrong):",
		    openItems);
	    if (dbg) {
		String s = "-- Specialized items:\n";
		int i = 1;
		for (Object o : specializedResources.keySet()) {
		    s += "   - specialized item " + i + "\n";
		    s += "     key: " + o + " (" + o.getClass() + ")\n";
		    Object val = specializedResources.get(o);
		    s += "     val: " + val + " (" + val.getClass() + ")\n";
		    i++;
		}
		System.out.println(s);
	    }

	    openItems.clear();
	}

	if (resourceURI == null) {
	    if (result == null) {
		result = firstResource;
	    } else {
		// it can happen that the result was specialized after result
		// was set, so we check that here
		Resource r = (Resource) specializedResources.get(result
			.getURI());
		if (r != null)
		    result = r;
	    }
	} else {
	    Resource r = (Resource) specializedResources.get(resourceURI);
	    if (r == null) {
		r = (Resource) resources.get(resourceURI);
	    }
	    result = r;
	}

	resources.clear();
	parseTable.clear();
	return result;
    }

    /**
     * Tries to set a property and checks whether setting has worked. It returns
     * true if either the property value was directly set or if the property
     * value was set as a List with only the one given value.
     * 
     * @param r
     *            The subject for which to set the property value.
     * @param propURI
     *            The property for which to set the value.
     * @param value
     *            The value to set for the given property.
     * @return true, if setting the property was successful.
     */
    private boolean testSetProperty(Resource r, String propURI, Object value) {
	// if (r == null || propURI == null || value == null)
	// return false; // do we need to check this?
	if (dbg) {
	    System.out.println(" -- trying to set property:");
	    System.out.println("      res:  " + r.getURI());
	    System.out.println("      prop: " + propURI);
	    if (value instanceof Resource)
		System.out
			.println("      val:  " + ((Resource) value).getURI());
	    else if (value instanceof List)
		for (Object o : (List) value) {
		    if (o instanceof Resource)
			System.out.println("      val:  "
				+ ((Resource) o).getURI());
		    else
			System.out.println("      val:  "
				+ o.getClass().getName());
		}
	}

	try {
	    // boolean retVal =
	    r.setProperty(propURI, value);
	    // if (dbg)
	    // System.out.println("     -> setting prop: " + retVal);
	    // return retVal;

	    Object realValue = r.getProperty(propURI);
	    if (value.equals(realValue)) {
		if (dbg)
		    System.out.println("     -> setting prop successful 1");
		return true;
	    }
	    if (realValue == null) {
		if (dbg)
		    System.out.println("     -> setting prop not successful 2");
		return false;
	    }
	    if (realValue instanceof List) {
		List realList = (List) realValue;
		if (realList.size() == 1) {
		    if (value.equals(realList.get(0))) {
			if (dbg)
			    System.out
				    .println("     -> setting prop successful 2");
			return true;
		    }
		} else if (value instanceof List) {
		    // we test each element of the list
		    List valList = (List) value;
		    if (valList.size() == valList.size()) {
			for (Object o : valList) {
			    if (!realList.contains(o))
				return false;
			}
			return true;
		    }
		}
	    }
	    if (dbg)
		System.out.println("     -> setting prop not successful 2");
	    return false;
	} catch (Exception e) {
	    if (dbg) {
		System.out
			.println("--Problem in testSetProperty (the property could not be set): ");
		System.out.println("  r:    " + r);
		System.out.println("  prop: " + propURI);
		System.out.println("  val:  " + value);
	    }
	    return false;
	}
    }

    private ParseData getData(Resource r) {
	ParseData d = (ParseData) parseTable.get(r.getURI());
	if (d == null) {
	    d = new ParseData();
	    parseTable.put(r.getURI(), d);
	}
	return d;
    }

    private Resource getResource(String uri) {
	if (dbg)
	    System.out.println(" -- getResource: " + uri);
	Resource r;
	if (uri == null) {
	    r = new Resource();
	    resources.put(r.getURI(), r);
	    lstResources.add(r.getURI());
	} else {
	    r = (Resource) resources.get(uri);
	    if (r == null) {
		if (uri.startsWith("_:")) {
		    // bNode ID
		    r = (Resource) blankNodes.get(uri);
		    if (r == null) {
			r = new Resource();
			blankNodes.put(uri, r);
			lstResources.add(r.getURI());// + "\t" + uri);
		    }
		    // getData(r).label = uri;
		} else {
		    r = new Resource(uri);
		    lstResources.add(r.getURI());
		}
		resources.put(r.getURI(), r);
	    }
	}
	return r;
    }

    private void parse(Reader reader, String baseURI) {
	if (reader == null) {
	    throw new IllegalArgumentException("Reader must not be 'null'");
	}
	if (baseURI == null) {
	    throw new IllegalArgumentException("base URI must not be 'null'");
	}

	// Allow at most 2 characters to be pushed back:
	this.reader = new PushbackReader(reader, 2);

	int c = skipWSC();
	while (c != -1) {
	    parseStatement();
	    if (eofAfterImplicitBlankNodeAsSubject)
		break;
	    c = skipWSC();
	}
    }

    private List parseCollection() {
	verifyCharacter(read(), "(");

	int c = skipWSC();

	List l = new ClosedCollection();
	if (c == ')')
	    // Empty list
	    read();
	else {
	    parseObject();
	    l.add(object);
	    if (object instanceof Resource)
		addRef((Resource) object, subject, predicate, l, 0);

	    int i = 1;
	    while (skipWSC() != ')') {
		parseObject();
		l.add(object);
		if (object instanceof Resource)
		    addRef((Resource) object, subject, predicate, l, i);
		i++;
	    }

	    // Skip ')'
	    read();
	}

	return l;
    }

    private void parseDirective() {
	// Verify that the first characters form the string "prefix"
	verifyCharacter(read(), "@");

	StringBuffer sb = new StringBuffer(8);

	int c = read();
	while (c != -1 && !TurtleUtil.isWhitespace(c)) {
	    sb.append((char) c);
	    c = read();
	}

	String directive = sb.toString();
	if (directive.equals("prefix")) {
	    parsePrefixID();
	} else if (directive.equals("base")) {
	    throw new RuntimeException("Base not supported!");
	} else if (directive.length() == 0) {
	    throw new RuntimeException(
		    "Directive name is missing, expected @prefix or @base");
	} else {
	    throw new RuntimeException("Unknown directive \"@" + directive
		    + "\"");
	}
    }

    private Resource parseImplicitBlank() {
	verifyCharacter(read(), "[");

	Resource bNode = getResource(null);

	int c = read();
	if (c != ']') {
	    unread(c);

	    // Remember current subject and predicate
	    Resource oldSubject = subject;
	    String oldPredicate = predicate;

	    // generated bNode becomes subject
	    subject = bNode;

	    // Enter recursion with nested predicate-object list
	    skipWSC();

	    parsePredicateObjectList();

	    skipWSC();

	    // Read closing bracket
	    verifyCharacter(read(), "]");

	    // Restore previous subject and predicate
	    subject = oldSubject;
	    predicate = oldPredicate;
	}

	return bNode;
    }

    private String parseLongString() {
	StringBuffer sb = new StringBuffer(1024);

	int doubleQuoteCount = 0;
	int c;

	while (doubleQuoteCount < 3) {
	    c = read();

	    if (c == -1) {
		throw new RuntimeException("Unexpected end of file!");
	    } else if (c == '"') {
		doubleQuoteCount++;
	    } else {
		doubleQuoteCount = 0;
	    }

	    sb.append((char) c);

	    if (c == '\\') {
		// This escapes the next character, which might be a '"'
		c = read();
		if (c == -1) {
		    throw new RuntimeException("Unexpected end of file!");
		}
		sb.append((char) c);
	    }
	}

	return sb.substring(0, sb.length() - 3);
    }

    private Resource parseNodeID() {
	// Node ID should start with "_:"
	verifyCharacter(read(), "_");
	verifyCharacter(read(), ":");

	// Read the node ID
	int c = read();
	if (c == -1) {
	    throw new RuntimeException("Unexpected end of file!");
	} else if (!TurtleUtil.isNameStartChar(c)) {
	    throw new RuntimeException("Expected a letter, found '" + (char) c
		    + "'");
	}

	StringBuffer name = new StringBuffer(32).append("_:");
	name.append((char) c);

	// Read all following letter and numbers, they are part of the name
	c = read();
	while (TurtleUtil.isNameChar(c)) {
	    name.append((char) c);
	    c = read();
	}

	unread(c);

	return getResource(name.toString());
    }

    /**
     * Parse a number in abbreviated form.
     * 
     * @see http://www.w3.org/TR/turtle/ , section 2.5.2
     * @return an instance of either {@link BigInteger}, {@link Double}, or
     *         {@link BigDecimal}.
     */
    private Object parseNumber() {
	StringBuffer value = new StringBuffer(8);
	String datatype = TypeMapper.getDatatypeURI(BigInteger.class);

	int c = read();

	// read optional sign character
	if (c == '+') {
	    value.append((char) c);
	    c = read();
	} else if (c == '-') {
	    value.append((char) c);
	    c = read();
	    if (c == 'I') {
		int c2 = read();
		if (c2 == 'N') {
		    int c3 = read();
		    if (c3 == 'F') {
			// special value '-INF'
			return TypeMapper.getJavaInstance(stringifiedNegInf,
				TypeMapper.getDatatypeURI(Double.class));
		    } else {
			unread(c3);
			unread(c2);
		    }
		} else {
		    unread(c2);
		}
	    }
	}

	while (StringUtils.isDigit((char) c)) {
	    value.append((char) c);
	    c = read();
	}

	if (c == '.' || c == 'e' || c == 'E') {
	    // We're parsing a decimal or a double
	    datatype = TypeMapper.getDatatypeURI(BigDecimal.class);

	    // read optional fractional digits
	    if (c == '.') {
		value.append((char) c);

		c = read();
		while (StringUtils.isDigit((char) c)) {
		    value.append((char) c);
		    c = read();
		}

		if (value.length() == 1) {
		    // We've only parsed a '.'
		    throw new RuntimeException("Object for statement missing");
		}
	    } else {
		if (value.length() == 0) {
		    // We've only parsed an 'e' or 'E'
		    throw new RuntimeException("Object for statement missing");
		}
	    }

	    // read optional exponent
	    if (c == 'e' || c == 'E') {
		datatype = TypeMapper.getDatatypeURI(Double.class);
		value.append((char) c);

		c = read();
		if (c == '+' || c == '-') {
		    value.append((char) c);
		    c = read();
		}

		if (!StringUtils.isDigit((char) c)) {
		    throw new RuntimeException("Exponent value missing");
		}

		value.append((char) c);

		c = read();
		while (StringUtils.isDigit((char) c)) {
		    value.append((char) c);
		    c = read();
		}
	    }
	}

	// Unread last character, it isn't part of the number
	unread(c);

	return TypeMapper.getJavaInstance(value.toString(), datatype);
    }

    private void parseObject() {
	int c = peek();

	if (c == '(') {
	    object = parseCollection();
	} else if (c == '[') {
	    object = parseImplicitBlank();
	} else {
	    object = parseValue(true);
	    // if (object instanceof Resource
	    // && ((Resource) object).serializesAsXMLLiteral()) {
	    // lstResources.add((Resource)object);
	    // resources.put(((Resource) object).getURI(), object);
	    // }
	}
    }

    private void parseObjectList() {
	List l = new OpenCollection();
	parseObject();

	int i = 0;
	while (skipWSC() == ',') {
	    l.add(object);
	    if (object instanceof Resource)
		addRef((Resource) object, subject, predicate, l, i);
	    i++;
	    read();
	    skipWSC();
	    parseObject();
	}

	if (l.isEmpty()) {
	    if (object instanceof Resource)
		addRef((Resource) object, subject, predicate, null, -1);
	} else {
	    l.add(object);
	    if (object instanceof Resource)
		addRef((Resource) object, subject, predicate, l, i);
	    object = l;
	}

	reportStatement(subject, predicate, object);
    }

    private String parsePredicate() {
	// Check if the short-cut 'a' is used
	int c1 = read();

	if (c1 == 'a') {
	    int c2 = read();

	    if (TurtleUtil.isWhitespace(c2)) {
		// Short-cut is used, return the rdf:type URI
		return Resource.PROP_RDF_TYPE;
	    }

	    // Short-cut is not used, unread all characters
	    unread(c2);
	}
	unread(c1);

	// Predicate is a normal resource
	Object predicate = parseValue(false);
	if (predicate instanceof Resource) {
	    return predicate.toString();
	} else if (predicate != null || !eofAfterImplicitBlankNodeAsSubject)
	    throw new RuntimeException("Illegal predicate value: " + predicate);
	else
	    return null;
    }

    private void parsePredicateObjectList() {
	predicate = parsePredicate();
	if (eofAfterImplicitBlankNodeAsSubject)
	    return;

	skipWSC();

	parseObjectList();

	while (skipWSC() == ';') {
	    read();

	    int c = skipWSC();

	    if (c == '.' || // end of triple
		    c == ']') // end of predicateObjectList inside blank node
	    {
		break;
	    }

	    predicate = parsePredicate();

	    skipWSC();

	    parseObjectList();
	}
    }

    private void parsePrefixID() {
	skipWSC();

	// Read prefix ID (e.g. "rdf:" or ":")
	StringBuffer prefixID = new StringBuffer(8);

	while (true) {
	    int c = read();

	    if (c == ':') {
		unread(c);
		break;
	    } else if (TurtleUtil.isWhitespace(c)) {
		break;
	    } else if (c == -1) {
		throw new RuntimeException("Unexpected end of file!");
	    }

	    prefixID.append((char) c);
	}

	skipWSC();

	verifyCharacter(read(), ":");

	skipWSC();

	// Read the namespace URI
	String namespace = parseURI();

	namespaceTable.put(prefixID.toString(), namespace.toString());
    }

    private Object parseQNameOrBoolean(boolean parseAsResource) {
	// First character should be a ':' or a letter
	int c = read();
	if (c == -1) {
	    throw new RuntimeException("Unexpected end of file!");
	}
	if (c != ':' && !TurtleUtil.isPrefixStartChar(c)) {
	    throw new RuntimeException("Expected a ':' or a letter, found '"
		    + (char) c + "'");
	}

	String namespace = null;

	if (c == ':') {
	    // qname using default namespace
	    namespace = (String) namespaceTable.get("");
	    if (namespace == null) {
		throw new RuntimeException(
			"Default namespace used but not defined");
	    }
	} else {
	    // c is the first letter of the prefix
	    StringBuffer prefix = new StringBuffer(8);
	    prefix.append((char) c);

	    c = read();
	    while (TurtleUtil.isPrefixChar(c)) {
		prefix.append((char) c);
		c = read();
	    }

	    if (c != ':') {
		// prefix may actually be a boolean value
		String value = prefix.toString();

		if (value.equals("true") || value.equals("false")) {
		    return TypeMapper.getJavaInstance(value,
			    TypeMapper.getDatatypeURI(Boolean.class));
		} else if (value.equals("NaN")) {
		    return TypeMapper.getJavaInstance(value,
			    TypeMapper.getDatatypeURI(Double.class));
		} else if (value.equals("INF")) {
		    return TypeMapper.getJavaInstance(stringifiedPosInf,
			    TypeMapper.getDatatypeURI(Double.class));
		}
	    }

	    verifyCharacter(c, ":");

	    namespace = (String) namespaceTable.get(prefix.toString());
	    if (namespace == null) {
		throw new RuntimeException("Namespace prefix '"
			+ prefix.toString() + "' used but not defined");
	    }
	}

	// c == ':', read optional local name
	StringBuffer localName = new StringBuffer(16);
	c = read();
	if (TurtleUtil.isNameStartChar(c)) {
	    localName.append((char) c);

	    c = read();
	    while (TurtleUtil.isNameChar(c)) {
		localName.append((char) c);
		c = read();
	    }
	}

	// Unread last character
	unread(c);

	// Note: namespace has already been resolved
	if (parseAsResource)
	    return getResource(namespace + localName.toString());
	else
	    return new Resource(namespace + localName.toString());
    }

    private Object parseQuotedLiteral() {
	String label = parseQuotedString();

	// Check for presence of a language tag or datatype
	int c = peek();

	if (c == '@') {
	    read();

	    // Read language
	    StringBuffer lang = new StringBuffer(8);

	    c = read();
	    if (c == -1) {
		throw new RuntimeException("Unexpected end of file!");
	    }
	    if (!TurtleUtil.isLanguageStartChar(c)) {
		throw new RuntimeException("Expected a letter, found '"
			+ (char) c + "'");
	    }

	    lang.append((char) c);

	    c = read();
	    while (TurtleUtil.isLanguageChar(c)) {
		lang.append((char) c);
		c = read();
	    }

	    unread(c);

	    return new LangString(label, lang.toString());
	    // return TypeMapper.getJavaInstance(label, null);
	} else if (c == '^') {
	    read();

	    // next character should be another '^'
	    verifyCharacter(read(), "^");

	    // Read datatype
	    Object datatype = parseValue(true);
	    if (datatype instanceof Resource)
		datatype = datatype.toString();
	    else if (!(datatype instanceof String))
		throw new RuntimeException("Illegal datatype value: "
			+ datatype);

	    if (datatype.equals(TurtleUtil.xmlLiteral)) {
		Object o = new TurtleParser().deserialize(label, true, null);
		if (o instanceof Resource) {
		    Resource r = (Resource) o;
		    r.literal();
		}
		return o;
	    }

	    return TypeMapper.getJavaInstance(label, (String) datatype);
	} else {
	    return TypeMapper.getJavaInstance(label, null);
	}
    }

    private String parseQuotedString() {
	String result = null;

	// First character should be '"'
	verifyCharacter(read(), "\"");

	// Check for long-string, which starts and ends with three double quotes
	int c2 = read();
	int c3 = read();

	if (c2 == '"' && c3 == '"') {
	    // Long string
	    result = parseLongString();
	} else {
	    // Normal string
	    unread(c3);
	    unread(c2);

	    result = parseString();
	}

	return TurtleUtil.decodeString(result);
    }

    private void parseStatement() {
	int c = peek();

	if (c == '@') {
	    parseDirective();
	    skipWSC();
	    verifyCharacter(read(), ".");
	} else {
	    parseTriples();
	    if (!eofAfterImplicitBlankNodeAsSubject) {
		skipWSC();
		verifyCharacter(read(), ".");
	    }
	}
    }

    private String parseString() {
	StringBuffer sb = new StringBuffer(32);

	while (true) {
	    int c = read();

	    if (c == '"') {
		break;
	    } else if (c == -1) {
		throw new RuntimeException("Unexpected end of file!");
	    }

	    sb.append((char) c);

	    if (c == '\\') {
		// This escapes the next character, which might be a '"'
		c = read();
		if (c == -1) {
		    throw new RuntimeException("Unexpected end of file!");
		}
		sb.append((char) c);
	    }
	}

	return sb.toString();
    }

    private void parseSubject() {
	int c = peek();

	if (c == '(') {
	    List l = parseCollection();
	    if (l == null || l.isEmpty())
		subject = NIL;
	    else {
		subject = getResource(null);
		subject.addType(Resource.TYPE_RDF_LIST, true);
		subject.setProperty(Resource.PROP_RDF_FIRST, l.remove(0));
		subject.setProperty(Resource.PROP_RDF_REST, l);
	    }
	} else if (c == '[') {
	    subject = parseImplicitBlank();
	} else {
	    Object value = parseValue(true);

	    if (value instanceof Resource) {
		subject = (Resource) value;
	    } else {
		throw new RuntimeException("Illegal subject value: " + value);
	    }
	}
    }

    private void parseTriples() {
	parseSubject();
	if (firstResource == null)
	    firstResource = subject;

	skipWSC();
	parsePredicateObjectList();

	subject = null;
	predicate = null;
	object = null;
    }

    private String parseURI() {
	StringBuffer uriBuf = new StringBuffer(100);

	// First character should be '<'
	int c = read();
	verifyCharacter(c, "<");

	// Read up to the next '>' character
	while (true) {
	    c = read();

	    if (c == '>') {
		break;
	    } else if (c == -1) {
		throw new RuntimeException("Unexpected end of file!");
	    }

	    uriBuf.append((char) c);

	    if (c == '\\') {
		// This escapes the next character, which might be a '>'
		c = read();
		if (c == -1) {
		    throw new RuntimeException("Unexpected end of file!");
		}
		uriBuf.append((char) c);
	    }
	}

	try {
	    return TurtleUtil.decodeString(uriBuf.toString());
	} catch (Exception e) {
	    throw new RuntimeException(e.getMessage());
	}
    }

    /**
     * 
     * @param parseAsResource
     *            if the value is a Resource, this parameter determines whether
     *            the value should be treated as a Resource for further
     *            specialization. It is used to differentiate between rdf
     *            predicate and rdf subject/object. In universAAL predicates are
     *            used only as String; although they are in reality also
     *            resources, they cannot be specialized and don't need to be
     *            investigated further. If parseAsResource is true, the value is
     *            treated as a Resource for further processing (i.e. rdf
     *            subject/object). If it is false, the value is a rdf predicate
     *            which cannot be specialized.
     * @return
     */
    private Object parseValue(boolean parseAsResource) {
	int c = peek();

	if (c == '<') {
	    // uriref, e.g. <foo://bar>
	    if (parseAsResource)
		return getResource(parseURI());
	    else
		return new Resource(parseURI());
	} else if (c == ':' || TurtleUtil.isPrefixStartChar(c)) {
	    // qname or boolean
	    return parseQNameOrBoolean(parseAsResource);
	} else if (c == '_') {
	    // node ID, e.g. _:n1
	    return parseNodeID();
	} else if (c == '"') {
	    // quoted literal, e.g. "foo" or """foo"""
	    return parseQuotedLiteral();
	} else if (StringUtils.isDigit((char) c) || c == '.' || c == '+'
		|| c == '-') {
	    // integer or double, e.g. 123 or 1.2e3
	    return parseNumber();
	} else if (c == -1) {
	    // postpone an error if the subject is an implicit blank node used
	    // as subject
	    if (subject != null && subject.isAnon()
		    && resources.get(subject.getURI()) != null) {
		eofAfterImplicitBlankNodeAsSubject = true;
		return null;
	    }
	    throw new RuntimeException("Unexpected end of file!");
	} else {
	    throw new RuntimeException("Expected an RDF value here, found '"
		    + (char) c + "'");
	}
    }

    /**
     * Returns the next character of input stream without changing the position
     * in the stream.
     */
    private int peek() {
	int result = read();
	unread(result);
	return result;
    }

    /** Read a single character from the input stream. */
    private int read() {
	try {
	    return reader.read();
	} catch (Exception e) {
	    throw new RuntimeException(e.getMessage());
	}
    }

    private void reportStatement(Resource subj, String pred, Object obj) {
	if (obj == null || (obj instanceof List && ((List) obj).isEmpty()))
	    obj = NIL;
	else if (!Resource.PROP_RDF_TYPE.equals(pred)
		&& ((obj instanceof Resource && !((Resource) obj)
			.serializesAsXMLLiteral()) || (obj instanceof List && containsResource((List) obj))))
	    // postpone for later -> see finalizeAndGetRoot
	    return;

	subj.setProperty(pred, obj);
    }

    /** Skip the rest of the line. */
    private void skipLine() {
	int c = read();
	while (c != -1 && c != 0xD && c != 0xA) {
	    c = read();
	}

	// c is equal to -1, \r or \n.
	// In case c is equal to \r, we should also read a following \n.
	if (c == 0xD) {
	    c = read();

	    if (c != 0xA) {
		unread(c);
	    }
	}
    }

    /** Skip white space characters. */
    private int skipWSC() {
	int c = read();
	while (TurtleUtil.isWhitespace(c) || c == '#') {
	    if (c == '#') {
		skipLine();
	    }

	    c = read();
	}

	unread(c);

	return c;
    }

    private Resource specialize(Resource r, Hashtable specialized,
	    Hashtable openItems) {
	// check if it has already been handled
	Resource substitution = (Resource) specialized.get(r.getURI());
	if (substitution != null)
	    return substitution;

	String[] types = r.getTypes();
	if (types == null || types.length == 0) {
	    // no type info -> this resource cannot be specialized
	    // (we still store it in 'specialized' table to avoid handling it
	    // again)
	    specialized.put(r.getURI(), r);
	    return r;
	} else {
	    String uri = r.getURI();
	    String type = OntologyManagement.getInstance()
		    .getMostSpecializedClass(types);
	    if (type == null) {
		substitution = TypeExpressionFactory.specialize(r);
		if (substitution == null)
		    // postpone the specialization until all props are set
		    return r;
	    } else {
		substitution = OntologyManagement.getInstance().getResource(
			type, uri);
	    }
	    if (substitution == null) {
		// the resource cannot be specialized
		LogUtils.logDebug(TurtleUtil.moduleContext, TurtleParser.class,
			"specialize", new Object[] {
				"Resource not specialized: type = ", type },
			null);
		specialized.put(r.getURI(), r);
		return r;
	    }
	}

	specialized.put(r.getURI(), substitution);

	// now transfer all properties from the original resource to the
	// specialized resource
	for (Enumeration e = r.getPropertyURIs(); e.hasMoreElements();) {
	    String prop = (String) e.nextElement();
	    Object val = r.getProperty(prop);
	    // String s = "";
	    // try {
	    // s = substitution.toStringRecursive();
	    // if (r == substitution) {
	    // if (dbg) System.out.println(" ----- equal");
	    // }
	    // // the following setProperty can fail, e.g. if the value is not
	    // // yet specialized or is not yet well formed (maybe some
	    // // properties of the value are still missing)
	    // substitution.setProperty(prop, val);
	    // } catch (Exception ex) {
	    // if (dbg) System.out.println("--Problem: ");
	    // if (dbg) System.out.println("  prop: "+prop);
	    // if (dbg) System.out.println("  val:  "+val);
	    // if (dbg) System.out.println("  r:    "+r.getURI());
	    // if (dbg) System.out.println("  r:    "+r.getClass().getName());
	    // if (dbg) System.out.println("  sub:  "+substitution.getURI());
	    // if (dbg)
	    // System.out.println("  sub:  "+substitution.getClass().getName());
	    // // if (dbg) System.out.println("-- before --");
	    // // if (dbg) System.out.println(s);
	    // // if (dbg) System.out.println("-- after --");
	    // // if (dbg) System.out.println(substitution.toStringRecursive());
	    // ex.printStackTrace();
	    // }
	    // if (!val.equals(substitution.getProperty(prop))) {
	    if (!testSetProperty(substitution, prop, val)) {
		// Something went wrong and the property could not be set.
		// We will store the property as an open item to retry later.
		// Type info is done separately, we don't need to do this later.
		if (!substitution.isBlockingAddingTypes()
			|| !Resource.PROP_RDF_TYPE.equals(prop)) {
		    RefData rd = new RefData();
		    rd.src = substitution;
		    rd.prop = prop;
		    if (val instanceof Resource) {
			if (dbg)
			    System.out.println("-- openItems.put 2: " + val);
			openItems.put(val, rd);
		    } else if (val instanceof List) {
			rd.l = (List) val;
			openItems.put(rd.prop + rd.src.getURI(), rd);
		    } else {
			LogUtils.logWarn(TurtleUtil.moduleContext,
				TurtleParser.class, "specialize", new Object[] {
					"Property '", prop,
					"' could not be set for a resource!" },
				null);
		    }
		}
	    }
	}

	return substitution;
    }

    /** Pushes a previously read character back to the input stream. */
    private void unread(int c) {
	if (c != -1) {
	    try {
		reader.unread(c);
	    } catch (IOException e) {
		throw new RuntimeException(e.getMessage());
	    }
	}
    }

    /**
     * Tests, if the given character is contained in the set of expected
     * characters.
     */
    private void verifyCharacter(int c, String expected) {
	if (c == -1) {
	    throw new RuntimeException("Unexpected end of file!");
	} else if (expected.indexOf((char) c) < 0) {
	    StringBuffer msg = new StringBuffer(32);
	    msg.append("Expected ");
	    for (int i = 0; i < expected.length(); i++) {
		if (i > 0) {
		    msg.append(" or ");
		}
		msg.append('\'');
		msg.append(expected.charAt(i));
		msg.append('\'');
	    }
	    msg.append(", found '");
	    msg.append((char) c);
	    msg.append("'");

	    throw new RuntimeException(msg.toString());
	}
    }
}
