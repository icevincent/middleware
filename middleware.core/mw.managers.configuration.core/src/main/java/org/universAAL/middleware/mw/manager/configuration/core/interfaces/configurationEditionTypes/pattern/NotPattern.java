/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
 * Copyright 2014 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationEditionTypes.pattern;

import org.universAAL.middleware.owl.Complement;
import org.universAAL.middleware.owl.TypeExpression;

/**
 * Negate any given pattern.
 * @author amedrano
 *
 */
public class NotPattern implements EntityPattern {

    private EntityPattern pat;

    /**
     * Match entities that NOT match the given pattern
     * @param onPattern the pattern to complement.
     */
    public NotPattern(EntityPattern onPattern) {
	pat = onPattern;
    }

    /** {@ inheritDoc}	 */
    public TypeExpression getRestriction() {
	return new Complement(pat.getRestriction());
    }

}