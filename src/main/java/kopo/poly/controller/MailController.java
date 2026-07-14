package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import kopo.poly.dto.MailDTO;
import kopo.poly.dto.MsgDTO;
import kopo.poly.service.IMailService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping(value = "/mail")
@RequiredArgsConstructor
@Controller
public class MailController {

    private final IMailService mailService;

    /**
     * 메일 발송하기 폼
     */
    @GetMapping(value = "mailForm")
    public String mailForm() {

        log.info("{}.mailForm Start!", this.getClass().getName());

        return "mail/mailForm";
    }

    @ResponseBody
    @PostMapping(value = "sendMail")
    public MsgDTO sendMail(HttpServletRequest request) {

        log.info("{}.sendMail Start!", this.getClass().getName());

        String toMail = CmmUtil.nvl(request.getParameter("toMail"));
        String title = CmmUtil.nvl(request.getParameter("title"));
        String contents = CmmUtil.nvl(request.getParameter("contents"));

        log.info("toMail : {} / title : {} / contents : {}", toMail, title, contents);

        MailDTO pDTO = new MailDTO();
        pDTO.setToMail(toMail);
        pDTO.setTitle(title);
        pDTO.setContents(contents);

        String msg;

        try {
            mailService.doSendMail(pDTO);
            msg = "메일 발송하였습니다.";

        } catch (Exception e) {
            msg = "메일 발송에 실패하였습니다.";
            log.info("[ERROR] sendMail : {}", e);
        }

        MsgDTO dto = new MsgDTO();
        dto.setMsg(msg);

        log.info("{}.sendMail End!", this.getClass().getName());

        return dto;
    }
}