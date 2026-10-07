package com.example.notes;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Used only by the failure-detection scenario of the experiment.
 * Passes normally; fails when a workflow is dispatched with inject_failure=true,
 * which sets the environment variable INJECT_FAILURE=true.
 */
@Tag("unit")
class InjectedFailureTest {

    @Test
    void injectedFailureSwitchIsOff() {
        assertNotEquals("true", System.getenv("INJECT_FAILURE"),
                "Failure deliberately injected for the experiment");
    }
}
