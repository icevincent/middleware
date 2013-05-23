package org.universAAL.middleware.util;

import java.util.Iterator;

import org.universAAL.middleware.owl.AllValuesFromRestriction;
import org.universAAL.middleware.owl.HasValueRestriction;
import org.universAAL.middleware.owl.Intersection;
import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.owl.TypeURI;

public class ResourceUtil {
    
    public static String toString(TypeExpression e) {
	return toString(e, null);
    }

    public static String toString(TypeExpression e, String prefix) {
	if (prefix == null)
	    prefix = "";
	
	if (e instanceof AllValuesFromRestriction) {
	    AllValuesFromRestriction all = (AllValuesFromRestriction)e;
	    Object o = all.getConstraint();
	    String prefix2 = prefix + "AllValuesFrom (for property " + all.getOnProperty() + ")";
	    if (o instanceof TypeExpression) 
		return prefix2 + "\n" + ResourceUtil.toString((TypeExpression)o, prefix + "  ");
	    else
		return prefix2 + " - unknown: "+o.getClass().getName() + "\n";
	} else if (e instanceof TypeURI) {
	    return prefix + "Type: " + ((TypeURI)e).getURI()+"\n";
	} else 	if (e instanceof HasValueRestriction) {
	    HasValueRestriction has = (HasValueRestriction)e;
	    Object o = has.getConstraint();
	    String prefix2 = prefix + "HasValue (for property " + has.getOnProperty() + ")";
	    if (o instanceof TypeExpression) 
		return prefix2 + "\n" + ResourceUtil.toString((TypeExpression)o, prefix + "  ");
	    else
		// TODO: data values (e.g. int)
		return prefix2 + " - unknown: "+o.getClass().getName() + "\n";
	} else if (e instanceof Intersection) {
	    Intersection in = (Intersection)e;
	    String ret = prefix + "Intersection\n";
	    Iterator it = in.types();
	    while (it.hasNext())  {
		Object el = it.next();
		if (el instanceof TypeExpression) {
		    ret += ResourceUtil.toString((TypeExpression)el, prefix + "  ");
		}
	    }
	    return ret;
	}
	
	return "unknown: " + e.getClass().getName();
    }
}