package com.whatsyouretf.userservice.domain.user.service.impl;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.user.dto.*;
import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.repository.UserFavoriteEtfRepository;
import com.whatsyouretf.userservice.domain.user.repository.UserHoldingEtfRepository;
import com.whatsyouretf.userservice.domain.user.repository.UserRepository;
import com.whatsyouretf.userservice.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
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
    // TODO: 팀원이 etf repository 구현 후 활성화

    @Override
    public FavoriteEtfListResponse getFavoriteEtfs(Long userId) {
        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    @Override
    @Transactional
    public void addFavoriteEtf(Long userId, Long etfId) {
        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    @Override
    @Transactional
    public void removeFavoriteEtf(Long userId, Long etfId) {
        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    @Override
    public boolean isFavoriteEtf(Long userId, Long etfId) {
        return false;
    }

    // ==================== 보유 ETF (마이데이터) ====================
    // TODO: 팀원이 etf repository 구현 후 활성화

    @Override
    public HoldingEtfListResponse getHoldingEtfs(Long userId) {
        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    @Override
    @Transactional
    public HoldingEtfListResponse syncMyData(Long userId) {
        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }
}
