package kopo.poly.service.impl;

import kopo.poly.dto.MailDTO; // 메일 발송할 때 썼던 MailDTO 클래스를 가져옴.
import kopo.poly.dto.UserInfoDTO; // 회원 정보를 담는 UserInfoDTO 클래스를 가져옴.
import kopo.poly.mapper.IUserInfoMapper; // Mapper 인터페이스를 가져옴. Service가 Mapper를 호출하려면 이 타입이 필요
import kopo.poly.service.IMailService; // 메일 발송을 담당하는 Service 인터페이스
import kopo.poly.service.IUserInfoService; // 이 클래스(UserInfoService)가 구현할 자기 자신의 인터페이스. @Override로 구현하는 메서드들이 정의되어 있는 곳.
import kopo.poly.util.CmmUtil; // null 방지 등에 썼던 공통 유틸 클래스.
import kopo.poly.util.DateUtil;
import kopo.poly.util.EncryptUtil; // 암호화/복호화에 썼던 유틸 클래스.
import lombok.RequiredArgsConstructor; // Lombok 라이브러리 어노테이션. 클래스에 선언된 final 필드들을 파라미터로 받는 생성자를 자동으로 만들어주는 기능.
// 노션에 부가 설명
import lombok.extern.slf4j.Slf4j; // log라는 이름의 로그 객체를 자동으로 생성
import org.springframework.stereotype.Service; // @Service 어노테이션. 이 클래스가 "비즈니스 로직을 처리하는 Service 계층 컴포넌트"임을 Spring에게 알려줘서, Spring이 이 객체를 자동으로 관리

import java.util.Optional; // null 방지에 썼던 자바 표준 클래스.
import java.util.concurrent.ThreadLocalRandom; // 인증번호(6자리 랜덤 숫자) 생성에 썼던 클래스.

@Slf4j // 로그 기능(log 객체)을 자동 부여.
@RequiredArgsConstructor // 클래스 내부에 선언된 final 필드들을 매개변수로 받는 생성자를 자동 생성.
@Service
public class UserInfoService implements IUserInfoService {

    private final IUserInfoMapper userInfoMapper; // 회원관련 SQL 사용하기 위한 Mapper 가져오기

    private final IMailService mailService; // 메일 발송을 위한 MailService 자바 객체 가져오기

    @Override // override는 어떤 기능은 없는 거고, 그냥 인터페이스와의 약속을 지켰다는 걸 컴퓨터가 인식할 수 있도록 하는 표식
              // 기능 구현용이 아니라, 실수로 철자나 파라미터를 틀렸을 때 컴퓨터가 즉시 빨간 줄을 그어서 알려주도록 하는 안전장치(스펠링 체크기)

    // 회원 아이디 중복체크 로직

    public UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception { // <- 이 부분 메서드 시작
        // public UserInfoDTO getUserIdExists(pDTO)..  👉 "지금부터 [아이디 중복체크] 주문 받습니다!" 하고 주문서를 등록하는 단계입니다.
        //{ (메서드 시작)  👉 주문서 등록이 끝나고 주방장이 실제로 칼을 들고 요리를 시작하는 순간입니다.
        // "메서드 시작"이란, 메서드의 이름과 규격을 선언하는 첫 줄이 끝나고 { (여는 중괄호)를 지나 실제로 명령문들이 하나씩 실행되기 직전의 지점을 의미

        log.info("{}.getUserIdExists Start!", this.getClass().getName());
        // this.getClass().getName()은 현재 클래스 이름(UserInfoService)을 자동으로 가져옴
        // Service 단계에 로그를 넣어두지 않으면 어디서 터졌는지 찾을 길이 전혀 없음.
        // 상황: 사용자가 아이디 중복체크 버튼을 눌렀는데 화면에 오류가 뜸. -> 개발자: "어? Controller에서 터진 건가? Service 문제인가? => 이런 로그 기록이 있으면 service 문제인지 바로 알 수 있음.

        // DB에 아이디가 존재하는지 SQL 쿼리 실행
        UserInfoDTO rDTO = userInfoMapper.getUserIdExists(pDTO); // Service가 Mapper를 호출하는 부분
        // userInfoMapper: Mapper 인터페이스(IUserInfoMapper)의 객체. 실제 SQL을 실행
        // rDTO (return DTO): DB 조회 결과를 담은 새 변수. 아이디가 존재하면 그 회원 정보가, 없으면 null이나 빈 값
        // .getUserIdExists(pDTO): Mapper 쪽에 정의된 같은 이름의 메서드를 호출 → 내부적으로 XML에 있는 SQL이 실행됨 (SELECT * FROM USER_INFO WHERE USER_ID = #{userId} 같은 쿼리)

        log.info("{}.getUserIdExists End!", this.getClass().getName());

        return rDTO;
        // Mapper에서 받아온 조회 결과(rDTO)를 이 메서드를 호출한 쪽(Controller)에게 그대로 돌려줌.
        // Controller는 이 결과를 보고 "아이디가 이미 있네요, 다른 아이디 쓰세요" 같은 응답을 사용자에게 보여줌.
    }

    @Override

    // 이메일 중복 체크 + (중복 아니면) 인증번호 발송
    // DB에 입력된 이메일이 존재하는지 체크
    // 이메일이 존재하지 않는다면, 유효한 이메일인지 확인하기 위해 인증번호를 포함하여 입려된 메일에 이메일 발송
    // 이메일에 발송된 인증번호는 controller에 전달

    public UserInfoDTO getEmailExists(UserInfoDTO pDTO) throws Exception {
        // pDTO로 사용자가 입력한 이메일을 받아옴.

        log.info("{}.emailAuth Start!", this.getClass().getName());
        // "이메일 인증 기능이 시작되었습니다!"라는 뜻(Authentication 인증)

        // DB 이메일이 존재하는지 SQL 쿼리 실행
        UserInfoDTO rDTO = Optional.ofNullable(userInfoMapper.getEmailExists(pDTO)).orElseGet(UserInfoDTO::new);
        // userInfoMapper.getEmailExists(pDTO): DB에 이 이메일이 있는지 조회(DB에 가서 "이 이메일 쓰인 적 있어?" 하고 물어보는 실제 조회 구문 / 없으면 null(완전히 비어있음)이라는 위험한 값이 튀어나옴
        // Optional.ofNullable(...): Mapper가 조회 결과가 없으면 null을 반환 -> 그럼, NullPointerException이 터짐... 그래서 Optional.ofNullable(...)로 감싸 보호
        // .orElseGet(UserInfoDTO::new): 조회 결과가 null이면 → orElseGet(UserInfoDTO::new)로 빈 UserInfoDTO 객체를 새로 만들어서 대신 넣어줌

        log.info("rDTO : {}", rDTO); // DB에서 가져온 결과값(rDTO)을 출력

        // 이메일 주소가 중복되지 않는다면.. 메일 발송
        if (CmmUtil.nvl(rDTO.getExistsYn()).equals("N")) {
            // CmmUtil.nvl(...): 값이 null이면 빈 문자열로 바꿔주는 유틸 (null 방지).  <CmmUtil은 개발할 때 자주 쓰는 공통 기능>
            // rDTO.getExistsYn(): DB 조회 결과에서 "이 이메일이 이미 존재하냐(Y/N)" 값을 꺼냄.
            // 값이 "N"(존재 안 함)이면 → 중복 아니므로 인증 절차 진행.

            // 6자리 랜덤 숫자 생성하기
            int authNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
            // ThreadLocalRandom: 수백 명의 사용자가 동시에 인증번호를 요청해도 서버가 멈추거나 느려지지 않고, 각자 자기 전용 도구로 겹치지 않게 초고속으로 랜덤 숫자를 뽑
            // authNumber(인증번호)
            // .nextInt: 뒤에 나올 범위 안에서 정수 숫자 하나를 뽑아줘
            // 100,000 이상부터 1,000,000 미만 사이에서 뽑아

            log.info("authNumber : {}", authNumber); // 생성된 인증번호를 로그로 확인.

            // 인증번호 발송 로직
            // 메일 발송에 필요한 정보(제목, 본문, 받는사람)를 담을 MailDTO 객체 생성.
            MailDTO dto = new MailDTO();

            dto.setTitle("이메일 중복 확인 인증번호 발송 메일"); // 메일 제목 설정.
            dto.setContents("인증번호는 " + authNumber + " 입니다."); // 메일 본문에 방금 만든 인증번호를 넣음.
            dto.setToMail(EncryptUtil.decAES128CBC(CmmUtil.nvl(pDTO.getEmail()))); // 받는 사람 이메일 주소 설정.
            // pDTO.getEmail(): 사용자가 입력한 이메일.
            // 그런데 이게 암호화된 상태로 넘어왔다는 뜻에서,
            // EncryptUtil.decAES128CBC(...): 로 복호화해서 원래 이메일 주소로 되돌림. (메일을 실제로 보내려면 원문 주소가 필요하니까)
            // CmmUtil.nvl(...): 혹시 null이면 빈 문자열로 방지.

            mailService.doSendMail(dto); // 이메일 발송

            dto = null;
            //1. dto 상자에 정보가 들어있음: (이메일, 인증번호 등 민감정보)
            //2. mailService.doSendMail(dto): 메일 발송 완료!
            //3. dto = null;: "볼일 끝났으니 혹시 모를 개인정보 유출 막게 상자 비워!" (안전장치)

            rDTO.setAuthNumber(authNumber); // 인증번호를 결과값에 넣어주기
        }

        log.info("{}.emailAuth End!", this.getClass().getName());

        return rDTO;
        // 이메일이 이미 존재하면: existsYn = "Y"만 담긴 rDTO 반환 (인증번호 없음).
        //이메일이 존재하지 않으면: 메일 발송 후 인증번호까지 채운 rDTO 반환.
    }

    @Override

    // 회원가입 완료되면, 회원가입 메일 발송

    public int insertUserInfo(UserInfoDTO pDTO) throws Exception {

        log.info("{}.insertUserInfo Start!", this.getClass().getName());

        // 회원가입 성공 : 1, 아이디 중복으로인한 가입 취소 : 2, 기타 에러 발생 : 0
        int res; // 결과값을 담을 변수 선언.

        // 회원가입
        int success = userInfoMapper.insertUserInfo(pDTO);

        // db 에 데이터가 등록되었다면(회원가입 성공했다면....
        if (success > 0) {
            res = 1;

            /*
             * ###########################################
             *              메일 발송 로직 추가 시작!!
             * ###########################################
             */

            MailDTO mDTO = new MailDTO();

            // 회원정보화면에서 입력받은 이메일 변수(아직 암호화되어 넘어오기 때문에 복호화 수행함)
            mDTO.setToMail(EncryptUtil.decAES128CBC(CmmUtil.nvl(pDTO.getEmail())));
            // 받는 사람 이메일 설정.
            //pDTO.getEmail()이 암호화된 상태로 넘어오므로, decAES128CBC로 복호화해서 진짜 이메일 주소로 변환. (앞서 getEmailExists에서 봤던 것과 똑같은 패턴)
            //CmmUtil.nvl(...): null 방지.

            mDTO.setTitle("회원가입을 축하드립니다."); //메일 제목 설정.

            //메일 내용에 가입자 이름넣어서 내용 발송
            mDTO.setContents(CmmUtil.nvl(pDTO.getUserName()) + "님의 회원가입을 진심으로 축하드립니다.");
            // 메일 상자(mDTO)에 '메일 본문(내용)'을 적어 넣는 기능(메서드)
            // CmmUtil.nvl(...): 이름이 null이면 빈 문자열로 처리해서 "님의 회원가입을..." 앞이 이상해지는 걸 방지.

            //회원 가입이 성공했기 때문에 메일을 발송함
            mailService.doSendMail(mDTO);

            /*
             * ###########################################
             *              메일 발송 로직 추가 끝!!
             * ###########################################
             */

        } else {
            res = 0;
            // 왜 코드에서는 성공(1)/실패(0)만 나누고, 중복(2)은 구분을 안 해요?:
            // res 반환값 정의: 1 = 가입 성공, 0 = 가입 실패
            // (참고: 아이디 중복 체크는 앞 단계(getUserIdExists)에서 미리 걸러내므로,
            //  여기서는 중복(2) 검사 없이 DB 저장 성공(1) / 실패(0)만 판단함)
        }

        log.info("{}.insertUserInfo End!", this.getClass().getName());

        return res; // 최종 결과 코드(1 성공 / 0 실패)를 Controller에 반환.
    }

    //로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기

    @Override
    public UserInfoDTO getLogin(UserInfoDTO pDTO) throws Exception {
        // pDTO로 로그인 시도 정보(아이디, 비밀번호)를 받음.
        // 리턴 타입이 UserInfoDTO인 이유: 로그인 성공 여부뿐 아니라, 성공 시 회원 정보 전체(이름, 이메일 등)를 화면에 써야 하니까 DTO 통째로 반환.

        log.info("{}.getLogin Start!", this.getClass().getName());

        UserInfoDTO rDTO = Optional.ofNullable(userInfoMapper.getLogin(pDTO)).orElseGet(UserInfoDTO::new);
        // Mapper 호출: 아이디+비밀번호가 일치하는 회원을 DB에서 조회.
        // Optional.ofNullable(...).orElseGet(UserInfoDTO::new): 조회 결과가 없으면(즉 로그인 정보 불일치) null이 올 수 있으니, null 대신 빈 UserInfoDTO 객체로 대체.

        if (!CmmUtil.nvl(rDTO.getUserId()).isEmpty()) {
            // rDTO.getUserId(): 조회 결과에 아이디 값이 들어있는지 확인.
            // CmmUtil.nvl(...): null이면 빈 문자열로 변환.
            // .isEmpty(): 빈 문자열인지 검사.
            // !...isEmpty() = "비어있지 않다" = 아이디 값이 실제로 채워져 있다 = 로그인 성공했다는 뜻.
            // 로그인 성공: 아이디가 "hong123" ➔ .isEmpty()는 False ➔ !를 만나서 True (조건 성립!)
            // 로그인 실패: 아이디가 "" ➔ .isEmpty()는 True ➔ !를 만나서 False (조건 탈락!)

            MailDTO mDTO = new MailDTO();  // 로그인 알림 메일을 보내기 위한 MailDTO 객체 생성.

            mDTO.setToMail(EncryptUtil.decAES128CBC(CmmUtil.nvl(rDTO.getEmail())));
            // 암호화되어 저장된 이메일을 복호화해서 사용.
            // rDTO.getEmail()로 DB에서 암호화된 이메일을 가져왔고, 이걸 복호화 열쇠를 통해 되돌려놓는다.

            mDTO.setTitle("로그인 알림!"); // 메일 제목.

            mDTO.setContents(DateUtil.getDateTime("yyyy.MM.dd hh:mm:ss") + "에 "
                    + CmmUtil.nvl(rDTO.getUserName()) + "님이 로그인하였습니다.");
            // 메일 본문 작성.

            mailService.doSendMail(mDTO); // 완성된 메일을 MailService로 발송
            // 왜 mailservice로 발송해?
            // UserInfoService (회원 관리팀): 로그인이 맞는지, 회원가입이 잘 되었는지, 아이디가 중복인지 같은 회원 정보 관리가 전문이에요.
            // MailService (메일 발송팀): 구글/네이버 메일 서버와 연결하고, 메일이 중간에 튕기지 않게 인터넷으로 쏴주는 메일 발송 전용 기술을 가진 전문 팀이에요.

        }

        log.info("{}.getLogin End!", this.getClass().getName());

        return rDTO;
        // 로그인 성공: 회원 정보가 채워진 rDTO
        // 로그인 실패: 빈 UserInfoDTO (필드들이 다 비어있음)
        // Controller는 rDTO.getUserId()가 비어있는지로 로그인 성공/실패를 판단할 수 있음.
    }

    // 아이디, 비밀번호 찾기에 활용

    @Override
    public UserInfoDTO searchUserIdOrPasswordProc(UserInfoDTO pDTO) throws Exception {

        log.info("{}.searchUserIdOrPasswordProc Start!", this.getClass().getName());

        UserInfoDTO rDTO = userInfoMapper.getUserId(pDTO); // 아이디 찾기든 비밀번호 찾기든 같은 Mapper 메서드(getUserId) 하나로 처리

        log.info("{}.searchUserIdOrPasswordProc End!", this.getClass().getName());

        return rDTO;
    }

    // 비밀번호 재설정 함수 구현

    @Override
    public int newPasswordProc(UserInfoDTO pDTO) throws Exception {

        log.info("{}.newPasswordProc Start!", this.getClass().getName());

        int success = userInfoMapper.updatePassword(pDTO);
        // Mapper 호출: 실제 DB에 새 비밀번호로 UPDATE 실행.
        // success: 영향받은 row 개수 (보통 1이면 성공, 0이면 실패).

        log.info("{}.newPasswordProc End!", this.getClass().getName());

        return success;
    }
}