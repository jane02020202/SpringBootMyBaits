package kopo.poly.service.impl;

import kopo.poly.dto.MailDTO;
import kopo.poly.service.IMailService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailService implements IMailService {

    private final JavaMailSender mailSender;

    @Override
    public void doSendMail(MailDTO pDTO) throws Exception {

        log.info("{}.doSendMail Start!", this.getClass().getName());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ppna2002@naver.com");
        message.setTo(CmmUtil.nvl(pDTO.getToMail()));
        message.setSubject(CmmUtil.nvl(pDTO.getTitle()));
        message.setText(CmmUtil.nvl(pDTO.getContents()));

        mailSender.send(message);

        log.info("{}.doSendMail End!", this.getClass().getName());
    }
}