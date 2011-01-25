/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import com.google.inject.ImplementedBy;
import java.util.List;
import java.util.Map;
import org.pjherring.mongogwt.shared.IsStorable;
import org.pjherring.mongogwt.shared.exception.ValidationException;

/**
 *
 * @author pjherring
 */
@ImplementedBy(ValidateImpl.class)
public interface Validate {
    void validate(IsStorable isStorable) throws ValidationException;
    void validate(IsStorable isStorable, boolean doThrowExceptions);
    Map<String, List<ValidationException>> getValidationErrorMap();
}
