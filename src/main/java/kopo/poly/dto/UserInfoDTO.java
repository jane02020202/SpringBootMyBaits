package kopo.poly.dto;

// 기본 개념
// 구현 순서 = dto -> mapper -> service -> controller
//dto는 mapper, service, controller 간 데이터 운반 담당
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class UserInfoDTO {

    private String userId;
    private String userName;
    private String password;
    private String email;
    private String addr1;
    private String addr2;

    private String regId;
    private String regDt;
    private String chgId;
    private String chgDt;

    // DB 테이블에 존재하지 않는 가상의 컬럼
    private String existsYn; // 아이디/이메일 중복 여부를 화면에 알려주는 임시값

    private int authNumber; // 이메일 인증번호를 잠깐 비교하는 임시값
}