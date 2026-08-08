package com.jarvis.spacedrepetition.service;

import com.jarvis.spacedrepetition.dto.SpacedRepetitionResult;
import com.jarvis.spacedrepetition.model.ReviewQuality;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpacedRepetitionCalculatorTest {

    private SpacedRepetitionCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new SpacedRepetitionCalculator();
    }

    @Test
    @DisplayName("Initial review with GOOD quality should set interval to 1 day and increase ease factor by 0.1")
    void testInitialReviewGood() {
        SpacedRepetitionResult result = calculator.calculateNextRevision(2.5, 1, 0, ReviewQuality.GOOD);

        assertEquals(2.6, result.easeFactor());
        assertEquals(1, result.intervalDays());
        assertEquals(1, result.repetitionCount());
        assertNotNull(result.lastStudiedAt());
        assertNotNull(result.nextRevisionAt());
    }

    @Test
    @DisplayName("Second review with GOOD quality should set interval to 3 days")
    void testSecondReviewGood() {
        SpacedRepetitionResult result = calculator.calculateNextRevision(2.6, 1, 1, ReviewQuality.GOOD);

        assertEquals(2.7, result.easeFactor());
        assertEquals(3, result.intervalDays());
        assertEquals(2, result.repetitionCount());
    }

    @Test
    @DisplayName("Subsequent review with GOOD quality should multiply interval by ease factor")
    void testSubsequentReviewGood() {
        // 3 days * 2.7 = 8.1 -> rounded to 8 days
        SpacedRepetitionResult result = calculator.calculateNextRevision(2.7, 3, 2, ReviewQuality.GOOD);

        assertEquals(2.8, result.easeFactor());
        assertEquals(8, result.intervalDays());
        assertEquals(3, result.repetitionCount());
    }

    @Test
    @DisplayName("Review with STRUGGLED quality should reset interval to 1, reset repetition count, and reduce ease factor")
    void testReviewStruggled() {
        SpacedRepetitionResult result = calculator.calculateNextRevision(2.8, 8, 3, ReviewQuality.STRUGGLED);

        assertEquals(2.6, result.easeFactor());
        assertEquals(1, result.intervalDays());
        assertEquals(0, result.repetitionCount());
    }

    @Test
    @DisplayName("Ease factor should not drop below minimum floor of 1.3")
    void testEaseFactorFloor() {
        SpacedRepetitionResult result = calculator.calculateNextRevision(1.4, 1, 0, ReviewQuality.STRUGGLED);

        assertEquals(1.3, result.easeFactor());
        assertEquals(1, result.intervalDays());
        assertEquals(0, result.repetitionCount());
    }
}
