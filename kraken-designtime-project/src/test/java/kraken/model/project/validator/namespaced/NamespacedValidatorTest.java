package kraken.model.project.validator.namespaced;

import java.util.List;

import kraken.model.Rule;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class NamespacedValidatorTest {

    @Test
    public void shouldReturnErrorValidationMessageIfNameContainsNamespaceSeparator() {
        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setName("namespaced:name");

        List<ValidationMessage> validationMessages = NamespacedValidator.validate(rule);

        assertThat(validationMessages.size(), equalTo(1));
        assertThat(validationMessages.get(0).getSeverity(), is(Severity.ERROR));
    }

}