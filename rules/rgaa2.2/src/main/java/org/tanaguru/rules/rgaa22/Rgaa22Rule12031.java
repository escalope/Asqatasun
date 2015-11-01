/*
 * Asqatasun - Automated webpage assessment
 * Copyright (C) 2008-2015  Asqatasun.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact us by mail: asqatasun AT asqatasun DOT org
 */

package org.tanaguru.rules.rgaa22;

import org.tanaguru.entity.audit.TestSolution;
import org.tanaguru.ruleimplementation.AbstractDetectionPageRuleImplementation;
import org.tanaguru.rules.elementselector.MultipleElementSelector;
import static org.tanaguru.rules.keystore.HtmlElementStore.*;
import static org.tanaguru.rules.keystore.RemarkMessageStore.MANUAL_CHECK_ON_ELEMENTS_MSG;

/**
 * Implementation of the rule 12.3 of the referential RGAA 2.2.
 * <br/>
 * For more details about the implementation, refer to <a href="http://www.tanaguru.org/en/content/rgaa22-rule-12-3">the rule 12.3 design page.</a>
 * @see <a href="http://rgaa.net/Equivalence-de-l-information-mise.html"> 12.3 rule specification </a>
 *
 * @author jkowalczyk
 */
public class Rgaa22Rule12031 extends AbstractDetectionPageRuleImplementation {

    /**
     * Default constructor
     */
    public Rgaa22Rule12031 () {
       super(
                new MultipleElementSelector(APPLET_ELEMENT, OBJECT_ELEMENT, EMBED_ELEMENT),
                // solution when at least one element is found
                TestSolution.NEED_MORE_INFO,
                // solution when no element is found
                TestSolution.NOT_APPLICABLE,
                // detected tag message
                MANUAL_CHECK_ON_ELEMENTS_MSG,
                null
            );
    }

}