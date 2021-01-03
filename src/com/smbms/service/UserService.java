package com.smbms.service;

import com.smbms.pojo.SmbmsRole;
import com.smbms.pojo.SmbmsUser;
import com.smbms.utils.PageBean;

import java.util.List;

public interface UserService {
    PageBean<SmbmsUser> findUserPage(Integer pageIndex, Integer pageSize,String queryname,Integer queryUserRole);

    List<SmbmsRole> findRoleList();

    boolean addUserSave(SmbmsUser user);

    SmbmsUser findUserByUserCode(String userCode);

    SmbmsUser findUserById(Integer id);

    boolean deleteUserById(Integer uid);

    /*boolean modifyPasswordById(String newpassword, long id);*/
}
