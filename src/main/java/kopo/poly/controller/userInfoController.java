package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.MsgDTO;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping(value = "/user")
public class userInfoController {

    private final IUserInfoService userInfoService;

    /**
     * 회원가입 화면으로 이동
     */
    @GetMapping(value = "userRegForm")
    public String userRegForm() {
        log.info("{}.user/userRegForm", this.getClass().getName());

        return "user/userRegForm";
    }

    /**
     * 회원 가입 전 아이디 중복체크하기(Ajax를 통해 입력한 아이디 정보 받음)
     */
    @ResponseBody
    @PostMapping(value = "getUserIdExists")
    public UserInfoDTO getUserExists(HttpServletRequest request) throws Exception {

        log.info("{}.getUserIdExists Start!", this.getClass().getName());

        String userId = CmmUtil.nvl(request.getParameter("userId")); // 회원아이디

        log.info("userId : {}", userId);

        UserInfoDTO pDTO = new UserInfoDTO();
        pDTO.setUserId(userId);

        // 회원아이디를 통해 중복된 아이디인지 조회
        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserIdExists(pDTO)).orElseGet(UserInfoDTO::new);

        log.info("{}.getUserIdExists End!", this.getClass().getName());

        return rDTO;
    }

    /**
     * 회원 가입 전 이메일 중복체크하기(Ajax를 통해 입력한 아이디 정보 받음)
     * 유효한 이메일인지 확인하기 위해 인증번호 포함하여 입력된 메일에 인증번호 발송
     */
    @ResponseBody
    @PostMapping(value = "getEmailExists")
    public UserInfoDTO getEmailExists(HttpServletRequest request) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());

        String email = CmmUtil.nvl(request.getParameter("email")); // 회원이메일

        log.info("email : {}", email);

        UserInfoDTO pDTO = new UserInfoDTO();
        pDTO.setEmail(EncryptUtil.encAES128CBC(email));

        // 입력된 이메일이 중복된 이메일인지 조회
        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getEmailExists(pDTO)).orElseGet(UserInfoDTO::new);

        log.info("{}.getEmailExists End!", this.getClass().getName());

        return rDTO;
    }

    /**
     * 회원가입 로직 처리
     */
    @ResponseBody
    @PostMapping(value = "insertUserInfo")
    public MsgDTO insertUserInfo(HttpServletRequest request) {

        log.info("{}.insertUserInfo start!", this.getClass().getName());

        int res = 0; // 회원가입 결과
        String msg = ""; // 회원가입 결과에 대한 메시지를 전달할 변수
        MsgDTO dto; // 결과 메시지 구조

        // 웹(회원정보 입력화면)에서 받는 정보를 저장할 변수
        UserInfoDTO pDTO;

        try {
            /*
             * ###############################################################
             *      웹(회원정보 입력화면)에서 받는 정보를 String 변수에 저장 시작!!
             *
             *      무조건 웹으로 받은 정보는 DTO에 저장하기 위해 임시로 String 변수에 저장함
             * ###############################################################
             */
            String userId = CmmUtil.nvl(request.getParameter("userId")); // 아이디
            String userName = CmmUtil.nvl(request.getParameter("userName")); // 이름
            String password = CmmUtil.nvl(request.getParameter("password")); // 비밀번호
            String email = CmmUtil.nvl(request.getParameter("email")); // 이메일
            String addr1 = CmmUtil.nvl(request.getParameter("addr1")); // 주소
            String addr2 = CmmUtil.nvl(request.getParameter("addr2")); // 상세주소
            /*
             * ###############################################################
             *      웹(회원정보 입력화면)에서 받는 정보를 String 변수에 저장 끝!!
             *
             *      무조건 웹으로 받은 정보는 DTO에 저장하기 위해 임시로 String 변수에 저장함
             * ###############################################################
             */

            /*
             * ###############################################################
             *      반드시, 값을 받았으면, 꼭 로그를 찍어서 값이 제대로 들어오는지 파악해야함
             *              반드시 작성할 것
             * ###############################################################
             */
            log.info("userId : " + userId);
            log.info("userName : " + userName);
            log.info("password : " + password);
            log.info("email : " + email);
            log.info("addr1 : " + addr1);
            log.info("addr2 : " + addr2);

            /*
             * ###############################################################
             *      웹(회원정보 입력화면)에서 받는 정보를 DTO에 저장하기 시작!!
             *
             *      무조건 웹으로 받은 정보는 DTO에 저장해야 한다고 이해하길 권함
             * ###############################################################
             */

            // 웹(회원정보 입력화면)에서 받는 정보를 저장할 변수를 메모리에 올리기
            pDTO = new UserInfoDTO();

            pDTO.setUserId(userId);
            pDTO.setUserName(userName);

            // 비밀번호는 절대로 복호화되지 않도록 해시 알고리즘으로 암호화함
            pDTO.setPassword(EncryptUtil.encHashSHA256(password));

            // 민감 정보인 이메일은 AES128-CBC로 암호화
            pDTO.setEmail(EncryptUtil.encAES128CBC(email));
            pDTO.setAddr1(addr1);
            pDTO.setAddr2(addr2);
            /*
             * ###############################################################
             *      웹(회원정보 입력화면)에서 받는 정보를 DTO에 저장하기 끝!!
             *
             *      무조건 웹으로 받은 정보는 DTO에 저장해야 한다고 이해하길 권함
             * ###############################################################
             */

            /*
             * 회원가입
             * 회원가입이 완료되면, Service로부터 1의 결과 값을 받음
             */
            res = userInfoService.insertUserInfo(pDTO);

            log.info("회원가입 결과(res) : " + res);

            // Sevice로부터 1의 결과 값 받으면, 회원가입 완료 메시지 전달
            if (res == 1) {
                msg = "회원가입되었습니다.";

                // 추후 회원가입 입력화면에서 ajax를 활용해서 아이디 중복, 이메일 중복을 체크하길 바람

            } else if (res == 2) {
                msg = "이미 가입된 아이디입니다.";

            } else {
                msg = "오류로 인해 회원가입이 실패하였습니다.";
            }

        } catch (Exception e) {
            // 저장이 실패되면 사용자에게 보여줄 메시지
            msg = "실패하였습니다. : " + e;
            log.info(e.toString());

        } finally {
            // 결과 메시지 전달하기
            dto = new MsgDTO();
            dto.setResult(res);
            dto.setMsg(msg);

            log.info("{}.insertUserInfo End!", this.getClass().getName());
        }

        return dto;
    }

    @GetMapping(value = "login")
    public String login() {

        log.info("{}.login Start!", this.getClass().getName());

        log.info("{}.login End!", this.getClass().getName());

        return "user/login";
    }

    @ResponseBody
    @PostMapping(value = "loginProc")
    public MsgDTO loginProc(HttpServletRequest request, HttpSession session) {

        log.info("{}.loginProc Start!", this.getClass().getName());

        int res = 0;
        String msg = "";
        MsgDTO dto;

        UserInfoDTO pDTO;

        try {

            String userId = CmmUtil.nvl(request.getParameter("userId"));
            String password = CmmUtil.nvl(request.getParameter("password"));

            log.info("userId : {} / password : {}", userId, password);

            pDTO = new UserInfoDTO();

            pDTO.setUserId(userId);

            pDTO.setPassword(EncryptUtil.encHashSHA256(password));

            UserInfoDTO rDTO = userInfoService.getLogin(pDTO);

            if (!CmmUtil.nvl(rDTO.getUserId()).isEmpty()) {

                res = 1;

                msg = "로그인이 성공했습니다.";

                session.setAttribute("SS_USER_ID", userId);
                session.setAttribute("SS_USER_NAME", CmmUtil.nvl(rDTO.getUserName()));

            } else {
                msg = "아이디와 비밀번호가 올바르지 않습니다.";
            }

        } catch (Exception e) {

            msg = "시스템 문제로 로그인이 실패했습니다.";
            res = 2;
            log.info(e.toString());

        } finally {

            dto = new MsgDTO();
            dto.setResult(res);
            dto.setMsg(msg);

            log.info("{}.loginProc End!", this.getClass().getName());
        }

        return dto;
    }

    @GetMapping(value = "loginResult")
    public String loginSuccess() {

        log.info("{}.user/loginResult Start!", this.getClass().getName());

        log.info("{}.user/loginResult End!", this.getClass().getName());

        return "user/loginResult";
    }

    /**
     * 아이디 찾기 화면
     */

    @GetMapping(value = "searchUserId")
    public String searchUserId() {

        log.info("{}.user/searchUserId Start!", this.getClass().getName());

        log.info("{}.user/searchUserId End!", this.getClass().getName());

        return "user/searchUserId";
    }

    /**
     * 아이디 찾기 로직 수행
     */

    @PostMapping(value = "searchUserIdProc")
    public String searchUserIdProc(HttpServletRequest request, ModelMap model) throws Exception {

        log.info("{}.searchUserIdProc Start!", this.getClass().getName());

        String userName = CmmUtil.nvl(request.getParameter("userName"));
        String email = CmmUtil.nvl(request.getParameter("email"));

        log.info("userName : {} /email : {}", userName, email);

        UserInfoDTO pDTO = new UserInfoDTO();
        pDTO.setUserName(userName);
        pDTO.setEmail(EncryptUtil.encAES128CBC(email));

        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.searchUserIdOrPasswordProc(pDTO))
                .orElseGet(UserInfoDTO::new);

        model.addAttribute("rDTO", rDTO);

        log.info("{}.searchUserIdProc End!", this.getClass().getName());

        return "user/searchUserIdResult";
    }

    /**
     * 비밀번호 찾기 화면
     */
    @GetMapping(value = "searchPassword")
    public String searchPassword(HttpSession session) {

        log.info("{}.searchPassword Start!", this.getClass().getName());

        // 강제 URL 입력 등 오는 경우가 있어 세션 삭제
        // 비밀번호 재생성하는 화면은 보안을 위해 생성한 NEW_PASSWORD 세션 삭제
        session.setAttribute("NEW_PASSWORD", "");
        session.removeAttribute("NEW_PASSWORD");

        log.info("{}.searchPassword End!", this.getClass().getName());

        return "user/searchPassword";
    }

    /**
     * 비밀번호 찾기 로직 수행
     * <p>
     * 아이디, 이름, 이메일 일치하면, 비밀번호 재발급 화면 이동
     */
    @PostMapping(value = "searchPasswordProc")
    public String searchPasswordProc(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {

        log.info("{}.searchPasswordProc Start!", this.getClass().getName());

        /*
         * ##########################################################################
         *          웹(회원정보 입력화면)에서 받는 정보를 String 변수에 저장!!
         *
         *      무조건 웹으로 받은 정보는 DTO에 저장하기 위해 임시로 String 변수에 저장함
         * ##########################################################################
         */
        String userId = CmmUtil.nvl(request.getParameter("userId"));       // 아이디
        String userName = CmmUtil.nvl(request.getParameter("userName"));   // 이름
        String email = CmmUtil.nvl(request.getParameter("email"));         // 이메일

        /*
         * ##########################################################################
         *  반드시, 값을 받았으면, 꼭 로그를 찍어서 값이 제대로 들어오는지 파악해야함
         *                      반드시 작성할 것
         * ##########################################################################
         */
        log.info("userId : {} / userName : {} / email : {} ", userId, userName, email);

        /*
         * ##########################################################################
         *          웹(회원정보 입력화면)에서 받는 정보를 DTO에 저장하기!!
         *
         *      무조건 웹으로 받은 정보는 DTO에 저장해야 한다고 이해하길 권함
         * ##########################################################################
         */
        UserInfoDTO pDTO = new UserInfoDTO();
        pDTO.setUserId(userId);
        pDTO.setUserName(userName);
        pDTO.setEmail(EncryptUtil.encAES128CBC(email));

        // 비밀번호 찾기 가능한지 확인하기
        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.searchUserIdOrPasswordProc(pDTO))
                .orElseGet(UserInfoDTO::new);

        model.addAttribute("rDTO", rDTO);

        // 비밀번호 재생성하는 화면은 보안을 위해 반드시 NEW_PASSWORD 세션이 존재해야 접속 가능하도록 구현
        // userId 값을 넣은 이유는 비밀번호 재설정하는 newPasswordProc 함수에서 사용하기 위함
        session.setAttribute("NEW_PASSWORD", userId);

        log.info("{}.searchPasswordProc End!", this.getClass().getName());

        return "user/newPassword";
    }

    /**
     * 비밀번호 재설정
     * <p>
     * 아이디, 이름, 이메일 일치하면, 비밀번호 재발급 화면 이동
     */
    @PostMapping(value = "newPasswordProc")
    public String newPasswordProc(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {

        log.info("{}.user/newPasswordProc Start!", this.getClass().getName());

        String msg; // 웹에 보여줄 메시지

        // 정상적인 접근인지 체크
        String newPassword = CmmUtil.nvl((String) session.getAttribute("NEW_PASSWORD"));

        if (!newPassword.isEmpty()) {

            /*
             * ##########################################################################
             *          웹(회원정보 입력화면)에서 받는 정보를 String 변수에 저장!!
             *
             *      무조건 웹으로 받은 정보는 DTO에 저장하기 위해 임시로 String 변수에 저장함
             * ##########################################################################
             */
            String password = CmmUtil.nvl(request.getParameter("password")); // 신규 비밀번호

            /*
             * ##########################################################################
             *  반드시, 값을 받았으면, 꼭 로그를 찍어서 값이 제대로 들어오는지 파악해야함
             *                      반드시 작성할 것
             * ##########################################################################
             */
            log.info("password : {}", password);

            /*
             * ##########################################################################
             *          웹(회원정보 입력화면)에서 받는 정보를 DTO에 저장하기!!
             *
             *      무조건 웹으로 받은 정보는 DTO에 저장해야 한다고 이해하길 권함
             * ##########################################################################
             */
            UserInfoDTO pDTO = new UserInfoDTO();
            pDTO.setUserId(newPassword);

            // 비밀번호는 절대로 복호화되지 않도록 해시 알고리즘으로 암호화함
            pDTO.setPassword(EncryptUtil.encHashSHA256(password));

            // 비밀번호 재설정
            userInfoService.newPasswordProc(pDTO);

            // 비밀번호 재생성하는 화면은 보안을 위해 생성한 NEW_PASSWORD 세션 삭제
            session.setAttribute("NEW_PASSWORD", "");
            session.removeAttribute("NEW_PASSWORD");

            msg = "비밀번호가 재설정되었습니다.";

        } else { // 비정상 접근
            msg = "비정상 접근입니다.";
        }

        model.addAttribute("msg", msg);

        log.info("{}.user/newPasswordProc End!", this.getClass().getName());

        return "user/newPasswordResult";
    }

}