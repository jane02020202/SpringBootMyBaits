package kopo.poly.service.impl;

import kopo.poly.dto.OcrDTO;
import kopo.poly.service.IOcrService;
import kopo.poly.util.CmmUtil;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j // 로그 기능(log 객체)을 자동 부여
@Service
public class OcrService implements IOcrService {

    // application.properties 에 설정해둔 Tesseract 학습모델(언어 데이터) 경로를 주입받음
    // 예: orc.model.data=C:/model/tessdata
    @Value("${orc.model.data}")
    private String ocrModel;

    /**
     * 이미지 파일로부터 문자 읽어 오기
     *
     * @param pDTO 이미지 파일 정보
     * @return pDTO 이미지로부터 읽은 문자열
     */
    @Override
    public OcrDTO getreadforImageText(OcrDTO pDTO) throws Exception { // <- 메서드 시작

        log.info("{}.getReadforImageText Start!", this.getClass().getName());

        // Controller에서 업로드해서 저장해둔 이미지 파일 경로 + 파일명으로 File 객체 생성
        File imageFile = new File(
                CmmUtil.nvl(pDTO.getFilePath()) + "//" + CmmUtil.nvl(pDTO.getFileName()));

        // OCR 기술 사용을 위한 Tesseract 플랫폼 객체 생성
        ITesseract instance = new Tesseract();

        // OCR 분석에 필요한 기준 데이터(나라별 언어 학습 데이터가 있는 폴더) 설정
        // 저장 경로는 물리경로 사용(전체 경로)
        instance.setDatapath(ocrModel);

        // 인식할 언어 설정 (기본 값은 영어)
        instance.setLanguage("kor"); // 한국어 설정
        // instance.setLanguage("eng"); // 영어 설정

        // 이미지 파일로부터 텍스트 읽기(글씨 인식하기)
        String result = instance.doOCR(imageFile);

        // 읽은 글자를 DTO에 저장하기
        pDTO.setTextFromImage(result);

        log.info("result : {}", result);

        log.info("{}.getReadforImageText End!", this.getClass().getName());

        return pDTO;
    }
}