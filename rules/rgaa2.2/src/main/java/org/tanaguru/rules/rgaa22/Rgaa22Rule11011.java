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
import org.tanaguru.ruleimplementation.AbstractMarkerPageRuleImplementation;
import org.tanaguru.rules.elementchecker.element.ChildElementPresenceChecker;
import org.tanaguru.rules.elementselector.SimpleElementSelector;
import org.tanaguru.rules.keystore.HtmlElementStore;
import static org.tanaguru.rules.keystore.HtmlElementStore.TABLE_ELEMENT;
import static org.tanaguru.rules.keystore.MarkerStore.DATA_TABLE_MARKER;
import static org.tanaguru.rules.keystore.MarkerStore.PRESENTATION_TABLE_MARKER;
import static org.tanaguru.rules.keystore.RemarkMessageStore.CHECK_TABLE_IS_DATA_TABLE_MSG;
import static org.tanaguru.rules.keystore.RemarkMessageStore.CHECK_TABLE_IS_PRESENTATION_TABLE_MSG;
import static org.tanaguru.rules.keystore.RemarkMessageStore.DATA_TABLE_WITHOUT_HEADER_MSG;
import static org.tanaguru.rules.keystore.RemarkMessageStore.HEADER_DETECTED_CHECK_ALL_HEADERS_ARE_WELL_FORMED_MSG;

/**
 * Implementation of the rule 11.1 of the referential RGAA 2.2.
 * <br/>
 * For more details about the implementation, refer to <a href="http://www.tanaguru.org/en/content/rgaa22-rule-11-1">the rule 11.1 design page.</a>
 * @see <a href="http://rgaa.net/Presence-des-balises-th-pour.html"> 11.1 rule specification </a>
 *
 * @author jkowalczyk
 */
public class Rgaa22Rule11011 extends AbstractMarkerPageRuleImplementation {

    /**
     * Default constructor
     */
    public Rgaa22Rule11011 () {
        super(
                new SimpleElementSelector(TABLE_ELEMENT),

                // the data tables are part of the scope
                DATA_TABLE_MARKER,

                // the presentation tables are not part of the scope
                PRESENTATION_TABLE_MARKER,

                // checker for elements identified by marker
                new ChildElementPresenceChecker(
                    HtmlElementStore.TH_ELEMENT,
                    // nmi when element is found
                    TestSolution.NEED_MORE_INFO, 
                    // na when element is not found
                    TestSolution.FAILED, 
                    // message associated with each found element
                    HEADER_DETECTED_CHECK_ALL_HEADERS_ARE_WELL_FORMED_MSG,
                    // message associated with each not found element
                    DATA_TABLE_WITHOUT_HEADER_MSG),

                // checker for elements not identified by marker
                new ChildElementPresenceChecker(
                    HtmlElementStore.TH_ELEMENT,
                    // nmi when element is found
                    TestSolution.NEED_MORE_INFO, 
                    // na when element is not found
                    TestSolution.NEED_MORE_INFO, 
                    // message associated with each found element
                    CHECK_TABLE_IS_DATA_TABLE_MSG,
                    // message associated with each not found element
                    CHECK_TABLE_IS_PRESENTATION_TABLE_MSG)
            );
    }
    
}