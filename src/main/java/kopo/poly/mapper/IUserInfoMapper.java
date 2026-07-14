package kopo.poly.mapper;

import kopo.poly.dto.UserInfoDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserInfoMapper {

    int insertUserInfo(UserInfoDTO pDTO) throws Exception;
    int updatePassword(UserInfoDTO pDTO) throws Exception;

    UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;
    UserInfoDTO getLogin(UserInfoDTO pDTO) throws Exception;
    UserInfoDTO getEmailExists(UserInfoDTO pDTO) throws Exception;
    UserInfoDTO getUserId(UserInfoDTO pDTO) throws Exception;
}