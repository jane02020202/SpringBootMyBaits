package kopo.poly.service;

import kopo.poly.dto.MailDTO;

public interface IMailService {

    void doSendMail(MailDTO pDTO) throws Exception;
}
