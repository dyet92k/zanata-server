/**
 * 
 */
package org.zanata.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zanata.webtrans.shared.model.DocValidationResultInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;


/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public interface ValidationService
{
   /**
    * Return all ValidationActions with enabled=true on those which are defined
    * to the project
    * 
    * @param projectSlug
    * @return
    */
   Collection<ValidationAction> getValidationAction(String projectSlug);
   

   /**
    * Return all ValidationActions on those which are customized to the version
    * 
    * @param projectSlug
    * @param versionSlug
    * @return
    */
   Collection<ValidationAction> getValidationAction(String projectSlug, String versionSlug);

   /**
    * Return all ValidationActions with enabled=true on those which are
    * customized to the version
    * 
    * @param HProjectIteration
    * @return
    */
   Collection<ValidationAction> getValidationObject(HProjectIteration version);

   /**
    * Run validation check on HTextFlow and HTextFlowTarget with specific locale
    * from list of HDocuments against validations rules
    * 
    * Returns if documents has validation errors
    * 
    * @param hDocs
    * @param validations
    * @param localeId
    */
   Map<DocumentId, Boolean> runValidations(List<HDocument> hDocs, List<ValidationId> validationIds, Long localeId);

   /**
    * Run validation check on HTextFlow and HTextFlowTarget with specific locale
    * from list of HDocuments against validations rules and return full report
    * 
    * @param hDocs
    * @param validations
    * @param localeId
    */
   Map<DocumentId, List<DocValidationResultInfo>> runValidationsFullReport(List<HDocument> hDocs, List<ValidationId> validationIds, Long localeId);

   /**
    * Filter list of text flow with those only contains validation error
    * 
    * @param textFlows
    * @param id
    */
   List<HTextFlow> filterHasErrorTexFlow(List<HTextFlow> textFlows, List<ValidationId> validationIds, Long localeId);
}
