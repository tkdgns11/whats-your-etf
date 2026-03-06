package com.whatsyouretf.userservice.domain.user.service;

import com.whatsyouretf.userservice.domain.user.dto.UserResponse;
import com.whatsyouretf.userservice.domain.user.dto.UserUpdateRequest;

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
}
