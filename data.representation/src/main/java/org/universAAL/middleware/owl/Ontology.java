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
import java.util.HashMap;
import java.util.Iterator;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.container.utils.StringUtils;

public abstract class Ontology {
    
    public static final String TYPE_OWL_ONTOLOGY = ManagedIndividual.OWL_NAMESPACE + "Ontology";
    
    public static final String PROP_OWL_IMPORT = ManagedIndividual.OWL_NAMESPACE + "imports";
    
    
    // array of String: URIs of Ontologies
    private volatile ArrayList imports = new ArrayList();
    
    // classURI -> OntClassInfo
    private volatile HashMap ontClassInfoMap = new HashMap();
    
    // classURI -> OntClassInfo
    private volatile HashMap extendedOntClassInfoMap = new HashMap();
    
    private Resource info;
    
    private String ontClassInfoURIPermissionCheck = null;
    private Object ontClassInfoURIPermissionCheckSync = new Object();
    
    
    public Ontology(String ontURI) {
	if (!isValidOntologyURI(ontURI))
	    throw new IllegalArgumentException("Not a valid Ontology URI:"+ontURI);
	
	info = new Resource(ontURI);
	info.addType(TYPE_OWL_ONTOLOGY, true);
    }
    
    private boolean isValidOntologyURI(String ontURI) {
	if (ontURI == null)
	    return false;
	if (!StringUtils.startsWithURIScheme(ontURI))
	    return false;
	if (ontURI.length() == ontURI.lastIndexOf('#'))
	    return false;
	return true;
    }
    
    protected boolean addImport(String ontURI) {
	if (!isValidOntologyURI(ontURI))
	    return false;
	synchronized (imports) {
	    if (imports.contains(ontURI))
		return true;
	    ArrayList temp = new ArrayList(imports.size() + 1);
	    temp.addAll(imports);
	    temp.add(ontURI);
	    imports = temp;
	    info.setProperty(PROP_OWL_IMPORT, ontURI);
	}
	return true;
    }

    public Resource getInfo() {
	return info;
    }
    
    public abstract void create();
    
    public final boolean checkPermission(String uri) {
	if (uri == null)
	    return false;
	if (uri.equals(ontClassInfoURIPermissionCheck))
	    return true;
	return false;
    }
    
    
    public boolean hasOntClass(String classURI) {
	if (ontClassInfoMap.containsKey(classURI))
	    return true;
	return extendedOntClassInfoMap.containsKey(classURI);
    }
    
    public final OntClassInfo[] getOntClassInfo() {
	synchronized (ontClassInfoMap) {
	    //return (OntClassInfo[]) ontClassInfoMap.keySet().toArray();
	    return (OntClassInfo[]) ontClassInfoMap.values().toArray(new OntClassInfo[1]);
	}
    }

    protected OntClassInfoSetup createNewAbstractOntClassInfo(String classURI) {
	return createNewOntClassInfo(classURI, null, -1);
    }

    protected OntClassInfoSetup createNewOntClassInfo(String classURI,
	    ResourceFactory fac) {
	return createNewOntClassInfo(classURI, fac, -1);
    }

    protected OntClassInfoSetup createNewOntClassInfo(String classURI,
	    ResourceFactory fac, int factoryIndex) {
	OntClassInfoSetup setup = newOntClassInfo(classURI, fac, factoryIndex);
	OntClassInfo info = setup.getInfo();

	HashMap temp = new HashMap();
	synchronized (ontClassInfoMap) {
	    temp.putAll(ontClassInfoMap);
	    temp.put(classURI, info);
	    ontClassInfoMap = temp;
	}
	return setup;
    }

    protected OntClassInfoSetup extendExistingOntClassInfo(String classURI) {
	OntClassInfoSetup setup = newOntClassInfo(classURI, null, 0);
	OntClassInfo info = setup.getInfo();

	HashMap temp = new HashMap();
	synchronized (extendedOntClassInfoMap) {
	    temp.putAll(extendedOntClassInfoMap);
	    temp.put(classURI, info);
	    extendedOntClassInfoMap = temp;
	}
	return setup;
    }
    
    private final OntClassInfoSetup newOntClassInfo(String classURI, ResourceFactory fac,
	    int factoryIndex) {
	OntClassInfoSetup setup = null;
	synchronized (ontClassInfoURIPermissionCheckSync) {
	    ontClassInfoURIPermissionCheck = classURI;
	    setup = OntClassInfo.create(classURI, this, fac, factoryIndex);
	    ontClassInfoURIPermissionCheck = null;
	}
	return setup;
    }
    
    public Resource[] getResourceList() {
	HashMap map = ontClassInfoMap;
	Resource[] lst = new Resource[1+map.size()];
	lst[0] = info;
	Iterator it = map.values().iterator();
	int i = 1;
	while (it.hasNext())
	    lst[i++] = (Resource) it.next();
	return lst;
    }
}