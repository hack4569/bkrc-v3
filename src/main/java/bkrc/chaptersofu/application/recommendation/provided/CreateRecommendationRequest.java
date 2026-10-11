package bkrc.chaptersofu.application.recommendation.provided;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRecommendationRequest(
        @NotNull Integer itemId,
        @NotBlank String cover,
        @NotBlank String title,
        @NotBlank String link,
        @NotBlank @Size(max = 100) String recommendation
) {}
