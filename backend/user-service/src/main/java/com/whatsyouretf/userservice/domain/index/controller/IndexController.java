package com.whatsyouretf.userservice.domain.index.controller;

import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.common.response.PaginatedResponse;
import com.whatsyouretf.userservice.domain.index.entity.MarketType;
import com.whatsyouretf.userservice.domain.index.repository.IndexSummary;
import com.whatsyouretf.userservice.domain.index.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/index")
@RequiredArgsConstructor
public class IndexController {
        private final IndexService indexService;

        @GetMapping
        public ResponseEntity<ApiResponse<PaginatedResponse<IndexSummary>>> getIndex(
                @RequestParam MarketType marketType,
                Pageable pageable
        ) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(ApiResponse.success(PaginatedResponse.createPaginatedResponse(indexService.getIndexHistory(marketType, pageable))));
        }
}
