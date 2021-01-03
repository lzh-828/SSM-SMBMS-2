package com.smbms.mapper;

import com.smbms.pojo.SmbmsRole;
import com.smbms.pojo.SmbmsUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface UserMapper {
    Integer getUserCount(@Param("queryname") String queryname,
                         @Param("queryUserRole") Integer queryUserRole);

    List<SmbmsUser> findUserPage(@Param("startIndex") int startIndex,
                                 @Param("pageSize") Integer pageSize,
                                 @Param("queryname") String queryname,
                                 @Param("queryUserRole") Integer queryUserRole);

    List<SmbmsRole> findRoleList();

    int addUserSave(SmbmsUser user);

    SmbmsUser findUserByUserCode(String userCode);

    SmbmsUser findUserById(Integer id);

    int deleteUserById(Integer uid);

    /*int modifyPasswordById(@Param("newpassword") String newpassword, @Param("id") long id);*/
}
