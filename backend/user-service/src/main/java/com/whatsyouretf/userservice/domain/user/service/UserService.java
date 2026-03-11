package com.whatsyouretf.userservice.domain.user.service;

import com.whatsyouretf.userservice.domain.user.dto.*;

public interface UserService {

    /**
     * 사용자 조회 (ID)
     */
    UserResponse getUserById(Long userId);

    /**
     * 내 정보 조회
     */
    UserResponse getMyInfo(Long userId);

    /**
     * 프로필 수정
     */
    UserResponse updateProfile(Long userId, UserUpdateRequest request);

    /**
     * 닉네임 중복 체크
     */
    boolean checkNicknameDuplicate(String nickname);

    /**
     * 회원 탈퇴
     */
    void deactivateUser(Long userId);

    // ==================== 관심 ETF ====================

    /**
     * 관심 ETF 목록 조회
     */
    FavoriteEtfListResponse getFavoriteEtfs(Long userId);

    /**
     * 관심 ETF 추가
     */
    void addFavoriteEtf(Long userId, Long etfId);

    /**
     * 관심 ETF 삭제
     */
    void removeFavoriteEtf(Long userId, Long etfId);

    /**
     * 관심 ETF 여부 확인
     */
    boolean isFavoriteEtf(Long userId, Long etfId);

    // ==================== 보유 ETF (마이데이터) ====================

    /**
     * 보유 ETF 목록 조회
     */
    HoldingEtfListResponse getHoldingEtfs(Long userId);

    /**
     * 마이데이터 동기화 (Mock)
     */
    HoldingEtfListResponse syncMyData(Long userId);
}
