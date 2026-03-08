package com.whatsyouretf.userservice.domain.ai.controller;

import com.whatsyouretf.userservice.common.auth.CustomUserDetails;
import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.ai.dto.*;
import com.whatsyouretf.userservice.domain.ai.service.AiFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * AI 피드백 API 컨트롤러
 * <p>
 * 포트폴리오 AI 리뷰 관련 API를 제공합니다.
 */
@Tag(name = "AI Feedback", description = "포트폴리오 AI 피드백 API")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiFeedbackController {

    private final AiFeedbackService aiFeedbackService;

    /**
     * 포트폴리오 AI 리뷰 요청
     */
    @Operation(summary = "포트폴리오 AI 리뷰 요청",
            description = "사용자가 구성한 포트폴리오에 대해 Bull/Bear 양면 분석을 요청합니다.")
    @PostMapping("/portfolio/review")
    public ResponseEntity<ApiResponse<PortfolioReviewResponse>> requestReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PortfolioReviewRequest request
    ) {
        PortfolioReviewResponse response = aiFeedbackService.requestReview(
                userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 리뷰 결과 조회
     */
    @Operation(summary = "리뷰 결과 조회", description = "요청한 AI 리뷰 결과를 조회합니다.")
    @GetMapping("/portfolio/review/{reviewId}")
    public ResponseEntity<ApiResponse<PortfolioReviewResponse>> getReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId
    ) {
        PortfolioReviewResponse response = aiFeedbackService.getReview(
                userDetails.getUserId(), reviewId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 리뷰 히스토리 조회
     */
    @Operation(summary = "리뷰 히스토리 조회", description = "내 AI 리뷰 히스토리를 조회합니다.")
    @GetMapping("/portfolio/reviews")
    public ResponseEntity<ApiResponse<ReviewHistoryResponse>> getReviewHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    ) {
        ReviewHistoryResponse response = aiFeedbackService.getReviewHistory(
                userDetails.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 리뷰 평가 (도움됨/안됨)
     */
    @Operation(summary = "리뷰 평가", description = "AI 리뷰에 대한 사용자 평가를 등록합니다.")
    @PostMapping("/portfolio/review/{reviewId}/rating")
    public ResponseEntity<ApiResponse<Void>> rateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @Valid @RequestBody RatingRequest request
    ) {
        aiFeedbackService.rateReview(userDetails.getUserId(), reviewId, request);
        return ResponseEntity.ok(ApiResponse.success("평가가 등록되었습니다. 감사합니다!"));
    }
}
