package com.whatsyouretf.userservice.domain.ai.dto;

import com.whatsyouretf.userservice.domain.ai.entity.RatingType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 리뷰 평가 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingRequest {

    /** 평가 유형: HELPFUL / NOT_HELPFUL */
    @NotNull(message = "평가 유형은 필수입니다.")
    private RatingType rating;

    /** 추가 코멘트 (선택, 최대 500자) */
    @Size(max = 500, message = "코멘트는 최대 500자입니다.")
    private String comment;
}
