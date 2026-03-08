package com.whatsyouretf.userservice.domain.user.service.impl;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import com.whatsyouretf.userservice.domain.etf.repository.EtfPriceRepository;
import com.whatsyouretf.userservice.domain.etf.repository.EtfRepository;
import com.whatsyouretf.userservice.domain.user.dto.*;
import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.entity.UserFavoriteEtf;
import com.whatsyouretf.userservice.domain.user.entity.UserHoldingEtf;
import com.whatsyouretf.userservice.domain.user.repository.UserFavoriteEtfRepository;
import com.whatsyouretf.userservice.domain.user.repository.UserHoldingEtfRepository;
import com.whatsyouretf.userservice.domain.user.repository.UserRepository;
import com.whatsyouretf.userservice.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EtfRepository etfRepository;
    private final EtfPriceRepository etfPriceRepository;
    private final UserFavoriteEtfRepository userFavoriteEtfRepository;
    private final UserHoldingEtfRepository userHoldingEtfRepository;

    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findByIdWithSocialAccounts(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Override
    public UserResponse getMyInfo(Long userId) {
        return getUserById(userId);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 닉네임 중복 체크
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
            }
        }

        user.updateProfile(request.getNickname(), request.getProfileImage());

        return UserResponse.fromWithoutSocialAccounts(user);
    }

    @Override
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Override
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.deactivate();
        log.info("User deactivated: {}", userId);
    }

    // ==================== 관심 ETF ====================

    @Override
    public FavoriteEtfListResponse getFavoriteEtfs(Long userId) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        List<UserFavoriteEtf> favorites = userFavoriteEtfRepository.findAllByUserIdWithEtf(userId);

        // ETF ID 목록 추출
        List<Long> etfIds = favorites.stream()
                .map(f -> f.getEtf().getId())
                .collect(Collectors.toList());

        // 최신 시세 조회
        Map<Long, EtfPrice> latestPrices = etfPriceRepository.findLatestByEtfIds(etfIds).stream()
                .collect(Collectors.toMap(p -> p.getEtf().getId(), p -> p));

        // DTO 변환
        List<FavoriteEtfResponse> responses = favorites.stream()
                .map(f -> FavoriteEtfResponse.from(f, latestPrices.get(f.getEtf().getId())))
                .collect(Collectors.toList());

        return FavoriteEtfListResponse.of(responses);
    }

    @Override
    @Transactional
    public void addFavoriteEtf(Long userId, Long etfId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // ETF 조회
        Etf etf = etfRepository.findById(etfId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ETF_NOT_FOUND));

        // 중복 체크
        if (userFavoriteEtfRepository.existsByUserIdAndEtfId(userId, etfId)) {
            throw new BusinessException(ErrorCode.ALREADY_FAVORITE);
        }

        // 관심 ETF 추가
        UserFavoriteEtf favorite = UserFavoriteEtf.create(user, etf);
        userFavoriteEtfRepository.save(favorite);

        log.info("Added favorite ETF: userId={}, etfId={}", userId, etfId);
    }

    @Override
    @Transactional
    public void removeFavoriteEtf(Long userId, Long etfId) {
        // 관심 ETF 조회
        UserFavoriteEtf favorite = userFavoriteEtfRepository.findByUserIdAndEtfId(userId, etfId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_NOT_FOUND));

        userFavoriteEtfRepository.delete(favorite);

        log.info("Removed favorite ETF: userId={}, etfId={}", userId, etfId);
    }

    @Override
    public boolean isFavoriteEtf(Long userId, Long etfId) {
        return userFavoriteEtfRepository.existsByUserIdAndEtfId(userId, etfId);
    }

    // ==================== 보유 ETF (마이데이터) ====================

    @Override
    public HoldingEtfListResponse getHoldingEtfs(Long userId) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        List<UserHoldingEtf> holdings = userHoldingEtfRepository.findAllByUserIdWithEtf(userId);

        if (holdings.isEmpty()) {
            return HoldingEtfListResponse.of(List.of(), null);
        }

        // ETF ID 목록 추출
        List<Long> etfIds = holdings.stream()
                .map(h -> h.getEtf().getId())
                .collect(Collectors.toList());

        // 최신 시세 조회
        Map<Long, EtfPrice> latestPrices = etfPriceRepository.findLatestByEtfIds(etfIds).stream()
                .collect(Collectors.toMap(p -> p.getEtf().getId(), p -> p));

        // DTO 변환
        List<HoldingEtfResponse> responses = holdings.stream()
                .map(h -> HoldingEtfResponse.from(h, latestPrices.get(h.getEtf().getId())))
                .collect(Collectors.toList());

        // 마지막 동기화 시점
        LocalDateTime lastSyncedAt = holdings.stream()
                .map(UserHoldingEtf::getSyncedAt)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return HoldingEtfListResponse.of(responses, lastSyncedAt);
    }

    @Override
    @Transactional
    public HoldingEtfListResponse syncMyData(Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 기존 보유 ETF 삭제
        userHoldingEtfRepository.deleteAllByUserId(userId);

        // Mock: 랜덤 ETF 보유 정보 생성
        List<Etf> activeEtfs = etfRepository.findByIsActiveTrue();

        if (activeEtfs.isEmpty()) {
            log.warn("No active ETFs found for mock MyData sync");
            return HoldingEtfListResponse.of(List.of(), LocalDateTime.now());
        }

        Random random = new Random();
        int holdingCount = Math.min(random.nextInt(5) + 1, activeEtfs.size()); // 1~5개

        for (int i = 0; i < holdingCount; i++) {
            Etf etf = activeEtfs.get(random.nextInt(activeEtfs.size()));

            // 중복 방지
            if (userHoldingEtfRepository.findByUserIdAndEtfId(userId, etf.getId()).isPresent()) {
                continue;
            }

            int quantity = random.nextInt(100) + 1; // 1~100주
            BigDecimal avgPrice = BigDecimal.valueOf(10000 + random.nextInt(90000)); // 10,000~100,000원

            UserHoldingEtf holding = UserHoldingEtf.builder()
                    .user(user)
                    .etf(etf)
                    .quantity(quantity)
                    .avgPrice(avgPrice)
                    .syncedAt(LocalDateTime.now())
                    .build();

            userHoldingEtfRepository.save(holding);
        }

        log.info("MyData synced (Mock): userId={}", userId);

        return getHoldingEtfs(userId);
    }
}
