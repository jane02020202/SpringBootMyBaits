package kopo.poly.util;

// import javax.crypto.*; 로도 표현 가능 / AES 암호화 관련 (javax.crypto)
import javax.crypto.BadPaddingException; // 복호화할 때 패딩이 이상하면 발생하는 에러도구(보통 key/IV가 틀렸을 때 생김)
import javax.crypto.Cipher; // 실제로 잠그고 여는 자물쇠 도구 자체. AES 암호화/복호화의 핵심 클래스
import javax.crypto.IllegalBlockSizeException; // 암호화할 데이터 블록 크기가 이상할 때 발생하는 에러 도구.
import javax.crypto.NoSuchPaddingException; //  "PKCS5Padding" 같은 패딩 방식을 못 찾을 때 발생하는 에러 도구.

import javax.crypto.spec.IvParameterSpec; //  IV(보조 양념 값)를 만드는 도구. 앞서 배운, 매번 암호 결과를 다르게 섞어주는 값
import javax.crypto.spec.SecretKeySpec; // 열쇠(key)를 진짜 암호화용 형태로 만드는 도구. 문자열 key를 Cipher가 알아먹을 수 있게 변환

import java.nio.charset.StandardCharsets; // 글자 ↔ 바이트 변환할 때 쓰는 규칙(UTF-8). 이메일 문자열을 바이트로 바꿀 때 씀.

// import java.security.*;로도 표현 가능 / 해시(SHA-256) 관련 (java.security)
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest; //  SHA-256 해시를 실제로 계산하는 도구. 비밀번호를 64자리 이상한 문자로 바꾸는 핵심 클래스
import java.security.NoSuchAlgorithmException; //  "SHA-256" 알고리즘 자체를 못 찾을 때 발생하는 에러 도구.
// java.security는 "SHA-256 전용 창고"가 아니라 "보안 관련 공통 창고"
//InvalidKeyException, InvalidAlgorithmParameterException → "key 문제", "설정값 문제"는 AES든 다른 암호화 방식이든 다 같이 쓸 수 있는
// 공통 에러라서 여기 있음 (AES 전용 창고인 javax.crypto가 아니라)

import java.util.Base64; //  Base64 번역기. 도형 덩어리(바이트) ↔ 저장 가능한 글자로 바꿔주는 그 도구.

public class EncryptUtil {

    private static final String addMessage = "PolyDataAnalysis";
    // 비밀번호를 해시할 떄 붙이는 Salt 값 (Salt 값이란? 각자 다른 사용자가 같은 1234를 입력했더라도 다른 값으로 구분하기 위해 붙이는 값)

    //개념 설명

    //private - 자바 클래스는 여러 개가 모여 하나의 프로그램을 만듦. 각 클래스 안에 변수/메서드를 다른 클래스에서도 쓸 수 있게 할지, 못 쓰게 할지 정하는 접근 제한자
    // => key라는 변수는 EncryptUtil 클래스 안에서만 쓸 수 있음
    // 왜 막을까? key나 salt같은 민감한 값은 외부에서 함부로 건드리거나 훔쳐볼 수 없게 감쳐야 함.

    // static — 클래스 변수/메서드
    //자바에서 보통 클래스는 이렇게 써요: javaUserInfoDTO dto = new UserInfoDTO(); / dto.setUserId("test01"); => 객체를 새로 만들어야(new) 사용 가능
    //이렇게 new로 객체(인스턴스)를 하나 만들어야 그 안의 기능을 쓸 수 있음.
    // But, static이 붙은 변수나 메서드는 객체를 만들지 않고도 클래스 이름으로 바로 접근 => EncryptUtil.encHashSHA256("1234");
    //왜 EncryptUtil은 static을 쓸까? => 암호화 기능은 "누가 만들었는지"에 따라 결과가 달라지는 게 아니라, 항상 똑같은 방식으로 동작하는 순수한 도구

    //  final - final이 붙으면, 이 변수는 한번 값이 정해지면 이후에 절대 바꿀 수 없음. => key = "다른값"; // 컴파일 에러! final 변수는 재할당 불가능
    // 왜 필요할까? key, Salt, IV 같은 값은 암호화할 때랑 복호화할 때 반드시 똑같아야 정상 동작

    private static final byte[] ivBytes = new byte[16];
    // AES 암호화할 때 쓰는 보조 열쇠 / AES로 잠글 때, 열쇠(KEY) 하나만 쓰는 게 아니라 IV라는 보조 재료도 같이 씀(잠글 때(암호화) 쓴 IV랑, 열 때(복호화) 쓴 IV가 완전히 똑같아야 함)
    private static final String key = "PolyTechnic12345";
    // AES 암호화에 쓰는 진짜 열쇠

    public static String encHashSHA256(String str) { // 문자열(str)을 하나 받아서 [1234], SHA-256 해시로 변환 후, 문자열로 돌려주는 함수
        String result; // 최종 결과값을 담을 그릇 미리 준비(밑에 보면 try...catch...return 코드가 있는데, try 블록에서 성공하면 result값에 해시값을 넣고, 에러가 나면 빈 문자열 넣)
        String plainText = addMessage + str; // salt값을 입력값(str) 앞에 붙이는 부분

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256"); // MessageDigest.getInstance("SHA-256"): "SHA-256 방식으로 계산해주는 계산기"를 하나 가져옴
            digest.update(plainText.getBytes()); // digest.update(plainText.getBytes()): plainText("PolyDataAnalysis1234")를 바이트(컴퓨터가 읽는 형태)로 바꿔서 계산기에 넣음
            byte[] hash = digest.digest(); // digest.digest(): 계산기가 실제로 SHA-256 계산을 수행해서 결과를 냄 → 결과는 byte[] hash(이진 데이터 형태)

            // 이진 데이터를 사람이 읽을 수 있는 64글자 문자로 바꾸는 단계
            //SHA-256 계산이 끝나면 hash라는 결과물이 나오는데, 이건 컴퓨터만 알아보는 이진 데이터 덩어리 -> 32개의 조각(바이트)으로 이루어져 있는데, 사람은 이걸 그대로 보면 못 읽
            //이 32개의 조각을 하나씩 사람이 읽을 수 있는 문자(16진수)로 번역해서, 이어붙이는 작업

            StringBuilder sb = new StringBuilder(); // 번역한 글자들을 이어붙일 빈 종이를 하나 준비
            for (byte b : hash) { // hash라는 상자 안에 32개의 조각(바이트)이 들어있는데, 하나씩 꺼내면서 반복 작업
                sb.append(String.format("%02x", b)); // 방금 꺼낸 조각 하나를, 16진수 문자 2개짜리로 번역해서 준비해둔 종이에 이어붙
            }

            result = sb.toString(); // 완성된 64글자 문자열을 최종 결과에 저장

        } catch (NoSuchAlgorithmException e) { // 혹시라도 SHA-256이라는 알고리즘을 못 찾는 상황이 생기면, 에러 대신 빈 문자열을 반환하도록 안정장치
            result = "";
        }

        return result; // 최종적으로 완성된 64글자 해시값을 함수 밖으로 돌려줌
    }

    public static String encAES128CBC(String str)

            // throws가 뭐야? 이 메서드를 실행하다가 이런 문제들이 생길 수 있다는 경고문
            throws NoSuchAlgorithmException, NoSuchPaddingException, // AES 알고리즘을 찾을 수 없을 때 (자물쇠 방식 자체가 없음) / PKCS5Padding 방식을 찾을 수 없을 때 (빈 공간 채우는 방식이 없음)
            InvalidKeyException, InvalidAlgorithmParameterException, // key 값이 유효하지 않을 때 (열쇠 길이가 틀렸거나 잘못됨) / IV 값이 유효하지 않을 때 (보조 양념 값이 잘못됨)
            IllegalBlockSizeException, BadPaddingException { // 암호화할 데이터 크기가 맞지 않을 때 (상자 크기가 안 맞음) / 복호화 시 패딩이 잘못됐을 때 (포장이 이상하게 되어 있음, 주로 복호화 때 발생)
            //패딩이란? 빈 공간 채우기(AES는 데이터를 16바이트씩 딱 맞는 블록으로 나눠서 암호화. 그런데 이메일 길이가 16의 배수로 딱 떨어지는 경우 거의x

        byte[] textBytes = str.getBytes(StandardCharsets.UTF_8); // 이메일을 컴퓨터 글자(숫자 덩어리(바이트)로 바꿔줌

        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        // key와 IV 준비

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        // 지금부터 AES 방식으로, 잠그는 모도르 설정

        byte[] encrypted = cipher.doFinal(textBytes);
        // doFinal이 실제로 자물쇠를 잠그는 동작 -> 이상한 도형 덩어리 나옴

        return Base64.getEncoder().encodeToString(encrypted);
        // 그 이상한 도형 덩어리를 암호화 할 때 Base64로 번역해서 저장시킴
    }

    public static String decAES128CBC(String str)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {

        byte[] encryptedBytes = Base64.getDecoder().decode(str); // DB에 저장했던 BASE64 글자를 다시 원래의 이상한 도형 덩어리(바이트)로 되돌림

        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        // 잠글 때랑 똑같은 열쇠, 똑같은 보조값을 사용 / 다른 열쇠로 절대 안열림

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        // 여는 모드로 변환 ENCRYPT_MODE <-> DECRYPT_MODE

        byte[] decrypted = cipher.doFinal(encryptedBytes);
        // doFinal이 실제로 자무ㄹ쇠를 여는 동작 -> 원래의 도형 덩어리(바이트)나옴

        return new String(decrypted, StandardCharsets.UTF_8);
        // 그 덩어리를 사람이 읽는 글자로 변환 -> 원본 이메일 반환
    }
}