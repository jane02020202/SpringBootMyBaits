package kopo.poly.mapper;
// SQL 실제로 실행
// mapper는 스스로 작동 안하고, service가 호출해야 작동

import kopo.poly.dto.UserInfoDTO;
import org.apache.ibatis.annotations.Mapper; // @Mapper라는 이름표(어노테이션)를 쓰기 위해 가져옴 (ibatis는 MyBatis의 옛날 이름 흔적)

@Mapper // MyBatis에서 해당 인터페이스가 SQL Mapper임을 인삭하게 함 / XML mapper와 자동 연결
public interface IUserInfoMapper {

    int insertUserInfo(UserInfoDTO pDTO) throws Exception;
    // int - 리턴타입 ( 끝나면 숫자 하나 돌려줌) / insertUserInfo - 함수이름 (회원정보 등록해라) /  UserInfoDTO pDTO -  파라미터(함수 실행에 필요한 필수 입력값)
    // 파라미터란? "___를 회원가입 시켜줘" 이 빈칸에 누구를 넣을지 정해줘야만 함수가 작동하는 데 그 빈칸이 파라미터
    //파라미터 개념 부가 설명 - userInfoDTO pDTO = new UserInfoDTO(); / pDTO.setUserId("hong123"); / pDTO.setPassword("1234"); / mapper.insertUserInfo(pDTO); <- 빈칸에 pDTO 넣어서 호출!
    // throws Exception - "나는 에러 처리 안할게, 나를 부른 쪽에서(service) 문제 처리해"라고 책임을 전가
    int updatePassword(UserInfoDTO pDTO) throws Exception; // 비밀번호 새 값으로 바꾸기

    UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;
    // UserInfoDTO - 리턴타입(끝나면 회원정보 하나 돌려줄게) / getUserIdExists - 함수 이름 (이 아이디가 이미 있는 확인)
    // 사용자가 회원가입할 때 아이디를 입력하면, 그 아이디가 DB(USER_INFO)에 이미 존재하는지 확인
    UserInfoDTO getLogin(UserInfoDTO pDTO) throws Exception; // 아이디+비밀번호 조합이 맞는지 확인
    UserInfoDTO getEmailExists(UserInfoDTO pDTO) throws Exception; // 사용자가 회원가입할 떄 이메일을 입력, 그 이메일이 DB에 이미 존재하는지 확인
    UserInfoDTO getUserId(UserInfoDTO pDTO) throws Exception; // 특정 아이디(USER_ID)로 DB를 조회, 그 회원의 전체 정보(이름, 이메일, 주소 등)을 가져옴
}