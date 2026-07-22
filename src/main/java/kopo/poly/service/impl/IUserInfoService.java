package kopo.poly.service;

import kopo.poly.dto.UserInfoDTO;

public interface IUserInfoService {

    UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception; // 아이디 중복 체크

    UserInfoDTO getEmailExists(UserInfoDTO pDTO) throws Exception; // 이메일 주소 중복 체크 및 인증 값

    UserInfoDTO getLogin(UserInfoDTO pDTO) throws Exception;

    UserInfoDTO searchUserIdOrPasswordProc(UserInfoDTO pDTO) throws Exception;

    // 내부적으로 Mapper의 getUserId, getEmailExists 등을 상황에 따라 호출해서 "아이디 찾기 또는 비밀번호 찾기"라는 하나의 비즈니스 절차를 처리
    // 아이디 찾기 -> 사용자가 회원가입할 때 이메일을 입력하면, 그 이메일이 DB에 이미 존재하는지 확인

    int insertUserInfo(UserInfoDTO pDTO) throws Exception; // 회원가입하기

    int newPasswordProc(UserInfoDTO pDTO) throws Exception; // Mapper의 updatePassword를 호출하면서, 그 전후로 유효성 검사·암호화(적당한 비밀번호인지 검사)
}