package com.bkrc.bkrcv3.recommendation.application.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRecommendationRequest(@NotBlank @Size(max = 100) String recommendation) {}
