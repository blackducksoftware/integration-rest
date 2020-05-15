package com.synopsys.integration.rest.service;

import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.component.IntRestComponent;

import static org.junit.jupiter.api.Assertions.*;

public class IntJsonTransformerTest {
    private static final String FIELD_STRING_VALUE = "test";
    private static final Integer FIELD_INTEGER_VALUE = 1;
    private static final Boolean FIELD_BOOLEAN_VALUE = true;
    private Gson gson = new Gson();

    @Test
    public void getComponentAsTestWithJson() throws IntegrationException {
        SecondSubComponent secondSubComponent = new SecondSubComponent();
        secondSubComponent.stringFields = Arrays.asList(FIELD_STRING_VALUE + 1, FIELD_STRING_VALUE + 2, FIELD_STRING_VALUE + 3);
        secondSubComponent.intFields = Arrays.asList(1, 2, 3);
        secondSubComponent.booleanFields = Arrays.asList(true, false, true);

        FirstSubComponent firstSubComponent = new FirstSubComponent();
        firstSubComponent.secondSubComponentFields = Arrays.asList(secondSubComponent, secondSubComponent, secondSubComponent);
        firstSubComponent.stringField = FIELD_STRING_VALUE;
        firstSubComponent.intField = FIELD_INTEGER_VALUE;
        firstSubComponent.booleanField = FIELD_BOOLEAN_VALUE;

        ExampleComponent exampleComponent = new ExampleComponent();
        exampleComponent.firstSubComponentField = firstSubComponent;
        exampleComponent.stringField = FIELD_STRING_VALUE;
        exampleComponent.intField = FIELD_INTEGER_VALUE;
        exampleComponent.booleanField = FIELD_BOOLEAN_VALUE;

        String exampleComponentJson = gson.toJson(exampleComponent);

        IntLogger intLogger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG);
        IntJsonTransformer intJsonTransformer = new IntJsonTransformer(gson, intLogger);

        final ExampleComponent transformedComponent = intJsonTransformer.getComponentAs(exampleComponentJson, ExampleComponent.class);
        assertNotNull(transformedComponent);
        assertNotNull(transformedComponent.getJson());
        assertNotNull(transformedComponent.getJsonElement());
        assertEquals(FIELD_STRING_VALUE, transformedComponent.stringField);
        assertEquals(FIELD_INTEGER_VALUE, transformedComponent.intField);
        assertEquals(FIELD_BOOLEAN_VALUE, transformedComponent.booleanField);

        FirstSubComponent transformedFirstSubComponent = transformedComponent.firstSubComponentField;
        assertNotNull(transformedFirstSubComponent);
        assertNotNull(transformedFirstSubComponent.getJson());
        assertEquals(FIELD_STRING_VALUE, transformedFirstSubComponent.stringField);
        assertEquals(FIELD_INTEGER_VALUE, transformedFirstSubComponent.intField);
        assertEquals(FIELD_BOOLEAN_VALUE, transformedFirstSubComponent.booleanField);

        final List<SecondSubComponent> transformedSecondSubComponents = transformedFirstSubComponent.secondSubComponentFields;
        assertNotNull(transformedSecondSubComponents);
        for (SecondSubComponent transformedSecondSubComponent : transformedSecondSubComponents) {
            assertNotNull(transformedSecondSubComponent);
            assertNotNull(transformedSecondSubComponent.getJson());
            assertEquals(3, transformedSecondSubComponent.stringFields.size());
            assertEquals(3, transformedSecondSubComponent.intFields.size());
            assertEquals(3, transformedSecondSubComponent.booleanFields.size());
        }
    }

    @Test
    public void testNullJsonObject() {
        IntLogger intLogger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG);
        IntJsonTransformer intJsonTransformer = new IntJsonTransformer(gson, intLogger);

        try {
            intJsonTransformer.getComponentAs((JsonObject)null, IntRestComponent.class);
            fail("Should have thrown IntegrationException");
        } catch (IntegrationException e) {
            // expected
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testInvalidJsonObject() {
        IntLogger intLogger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG);
        IntJsonTransformer intJsonTransformer = new IntJsonTransformer(gson, intLogger);

        try {
            IntRestComponent component = intJsonTransformer.getComponentAs(new JsonObject(), IntRestComponent.class);
            assertNotNull(component);
            assertNotNull(component.getJsonElement());
        } catch (Exception e) {
            fail(e);
        }
    }

    private class ExampleComponent extends IntRestComponent {
        private FirstSubComponent firstSubComponentField;
        private String stringField;
        private Integer intField;
        private Boolean booleanField;

    }

    private class FirstSubComponent extends IntRestComponent {
        private List<SecondSubComponent> secondSubComponentFields;
        private String stringField;
        private Integer intField;
        private Boolean booleanField;

    }

    private class SecondSubComponent extends IntRestComponent {
        private List<String> stringFields;
        private List<Integer> intFields;
        private List<Boolean> booleanFields;

    }

}
