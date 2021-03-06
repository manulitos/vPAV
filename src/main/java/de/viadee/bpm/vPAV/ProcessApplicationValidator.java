/**
 * Copyright � 2017, viadee Unternehmensberatung GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the viadee Unternehmensberatung GmbH.
 * 4. Neither the name of the viadee Unternehmensberatung GmbH nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <viadee Unternehmensberatung GmbH> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.viadee.bpm.vPAV;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.context.ApplicationContext;

import de.viadee.bpm.vPAV.beans.BeanMappingGenerator;
import de.viadee.bpm.vPAV.processing.model.data.CheckerIssue;
import de.viadee.bpm.vPAV.processing.model.data.CriticalityEnum;

public class ProcessApplicationValidator extends AbstractRunner {

    /**
     * find issues with given ApplicationContext (Spring)
     * 
     * @param ctx
     *            spring context
     * @return all issues
     */
    public static Collection<CheckerIssue> findModelInconsistencies(ApplicationContext ctx) {

        RuntimeConfig.getInstance().setBeanMapping(BeanMappingGenerator.generateBeanMappingFile(ctx));
        RuntimeConfig.getInstance().setClassLoader(ProcessApplicationValidator.class.getClassLoader());
        run_vPAV();

        return AbstractRunner.getfilteredIssues();
    }

    /**
     * find issues with given ApplicationContext (Spring)
     * 
     * @param ctx
     *            spring context
     * @return issues with status error
     */
    public static Collection<CheckerIssue> findModelErrors(ApplicationContext ctx) {

        RuntimeConfig.getInstance().setBeanMapping(BeanMappingGenerator.generateBeanMappingFile(ctx));
        RuntimeConfig.getInstance().setClassLoader(ProcessApplicationValidator.class.getClassLoader());
        run_vPAV();

        return filterErrors(AbstractRunner.getfilteredIssues(), CriticalityEnum.ERROR);
    }

    /**
     * find model errors without spring context
     * 
     * @return all issues
     */
    public static Collection<CheckerIssue> findModelInconsistencies() {

        RuntimeConfig.getInstance().setClassLoader(ProcessApplicationValidator.class.getClassLoader());
        run_vPAV();

        return AbstractRunner.getfilteredIssues();
    }

    /**
     * find model errors without spring context
     * 
     * @return issues with status error
     */
    public static Collection<CheckerIssue> findModelErrors() {

        RuntimeConfig.getInstance().setClassLoader(ProcessApplicationValidator.class.getClassLoader());
        run_vPAV();

        return filterErrors(AbstractRunner.getfilteredIssues(), CriticalityEnum.ERROR);
    }

    /**
     * filter an issue collection by status
     * 
     * @param filteredIssues
     * @param status
     * @return issues with status
     */
    private static Collection<CheckerIssue> filterErrors(Collection<CheckerIssue> filteredIssues,
            CriticalityEnum status) {
        Collection<CheckerIssue> filteredErrors = new ArrayList<CheckerIssue>();

        for (CheckerIssue issue : filteredIssues) {
            if (issue.getClassification().equals(status)) {
                filteredErrors.add(issue);
            }
        }
        return filteredErrors;
    }
}
