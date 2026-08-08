package com.jarvis.practice.dto;

import com.jarvis.dsa.model.DsaDifficulty;
import com.jarvis.practice.model.PracticeCategoryType;

public record CreatePracticeQuestionRequest(
        PracticeCategoryType categoryType,
        String subCategory,
        String title,
        DsaDifficulty difficulty
) {
}
