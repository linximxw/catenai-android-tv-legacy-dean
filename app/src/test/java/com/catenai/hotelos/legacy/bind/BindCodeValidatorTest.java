package com.catenai.hotelos.legacy.bind;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BindCodeValidatorTest {

    @Test
    public void isValid_acceptsSixDigits() {
        assertTrue(BindCodeValidator.isValid("123456"));
    }

    @Test
    public void isValid_rejectsBlankAndShortInput() {
        assertFalse(BindCodeValidator.isValid(""));
        assertFalse(BindCodeValidator.isValid("12345"));
    }

    @Test
    public void isValid_rejectsNonDigitInput() {
        assertFalse(BindCodeValidator.isValid("12AB56"));
        assertFalse(BindCodeValidator.isValid("123 56"));
    }
}
