package com.smbms.controller;

import com.alibaba.fastjson.JSON;
import com.smbms.pojo.SmbmsRole;
import com.smbms.pojo.SmbmsUser;
import com.smbms.service.UserService;
import com.smbms.utils.EmptyUtils;
import com.smbms.utils.PageBean;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jsp")
public class UserController {

    @Resource
    private UserService userService;

    /*删除用户*/
    @ResponseBody
    @RequestMapping(value = "/deluser",method = RequestMethod.GET)
    public String deleteUser(@RequestParam Integer uid){
        //根据id查询用户是否存在
        SmbmsUser userById = userService.findUserById(uid);
        //进行删除
        Map map = new HashMap();
        if (userById == null){
            map.put("delResult","notexist");
            return JSON.toJSONString(map);
        }
        boolean result = userService.deleteUserById(uid);
        if (result){
            map.put("delResult","true");
        }else {
            map.put("delResult","false");
        }
        return JSON.toJSONString(map);



    }

    /*密码修改*//*
    @RequestMapping(value = "/changePassword",method = RequestMethod.POST)
    public String changePassword(HttpServletRequest request,@RequestParam String newpassword){
        SmbmsUser userSession = (SmbmsUser) request.getSession().getAttribute("userSession");
        //根据id修改密码
        boolean result = userService.modifyPasswordById(newpassword,userSession.getId());
        if (result){
            return "frame";
        }else {
            request.setAttribute("message","修改密码失败,请重试");
            return "pwdmodify";
        }
    }

    *//*验证登录密码是否正确*//*
    @ResponseBody
    @RequestMapping(value = "/pwdmodify",method = RequestMethod.GET)
    public String pwdModify(HttpServletRequest request,@RequestParam String oldpassword){
        //1.获取当前session
        SmbmsUser userSession = (SmbmsUser) request.getSession().getAttribute("userSession");
        Map<String,String> map=new HashMap();
        if (userSession == null) {
            map.put("result","sessionerror");
        }else if (userSession.getUserPassword().equals(oldpassword)){
            map.put("result","true");
        }else if (oldpassword == null || oldpassword ==""){
            map.put("result","error");
        }else {
            map.put("result","false");
        }
        return JSON.toJSONString(map);
    }*/

    /*Ajax 根据id查询用户所有信息*/
    @ResponseBody
    @RequestMapping(value = "/userinfo/{id}",method = RequestMethod.GET)
    public String findUserById(@PathVariable Integer id){
        SmbmsUser smbmsUser=userService.findUserById(id);
        return JSON.toJSONString(smbmsUser);
    }

   /*ajax请求查询用户编码是否存在*/
    @ResponseBody
    @RequestMapping(value = "/ucexist",method = RequestMethod.GET)
    public String ucExist(@RequestParam String userCode){

        SmbmsUser smbmsUser=userService.findUserByUserCode(userCode);
        Map map=new HashMap();
        if (EmptyUtils.isNotEmpty(smbmsUser)){
            map.put("userCode","exist");
            return JSON.toJSONString(map);
        }else {
            map.put("userCode","noexist");
            return JSON.toJSONString(map);
        }
    }
    /*ajax查询用户角色列表  ajax请求必加注解@ResponseBody*/
    @ResponseBody
    @RequestMapping(value = "/getrolelist",method = RequestMethod.GET)
    public String getRoleList(){
        //直接调用查询用户角色列表的方法
        List<SmbmsRole> roleList = userService.findRoleList();
        return JSON.toJSONString(roleList);
    }

    @RequestMapping(value = "useraddsave.html",method = RequestMethod.POST)
    public String addUserSave(HttpServletRequest request,
                              SmbmsUser user,
                              @RequestParam(value = "a_idPicPath",required = false) MultipartFile acctch){
        String targetFileName = null;
        //1.判空
        if (!acctch.isEmpty()){
            //2.1定义上传文件目标路径 自适应拼接
            targetFileName = request.getServletContext().getRealPath("static"+ File.separator+"fileUpLoad");
            //2.2获取源文件名
            String oddFilename = acctch.getOriginalFilename();
            //2.3获取文件大小
            int fileSize = 500000;//50k
            //2.4获取后缀名
            String extension = FilenameUtils.getExtension(oddFilename);
            //jpg png jpeg
            if (acctch.getSize() > fileSize){
                request.setAttribute("uploadFileError","上传文件大小超过限制!");
                return "useradd";
            }else if (extension.equalsIgnoreCase("jpg") ||
                      extension.equalsIgnoreCase("jpeg") ||
                      extension.equalsIgnoreCase("png")){
                //3.定义上传的文件名 时间戳 + 随机数 + _PerSon.后缀
                String fileName = System.currentTimeMillis()+""+ RandomUtils.nextInt(1000000)+"_Person."+extension;
                //4.创建文件对象 如果文件不存在 创建文件
                File file=new File(targetFileName,fileName);
                if (!file.exists()){
                    file.mkdirs();//创建文件
                }
                //5.核心 正式的进行文件上传
                try {
                    acctch.transferTo(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    request.setAttribute("uploadFileError","系统错误,请重试!");
                    return "useradd";
                }
                //6.记录上传的文件路径
                targetFileName = targetFileName+File.separator+fileName;
            }else {
                request.setAttribute("uploadFileError","上传文件类型不正确!支持.png .jpg .jpeg");
                return "useradd";
            }
        }
        //定义上传路径
        user.setIdPicPath(targetFileName);
        //创建者
        SmbmsUser userSession = (SmbmsUser) request.getSession().getAttribute("userSession");
        user.setCreatedBy(userSession.getId());
        //创建时间
        user.setCreationDate(new Timestamp(new Date().getTime()));
        //进行保存数据
        if (userService.addUserSave(user)){
            return "redirect:/jsp/query";
        }
        return "useradd";
    }

    @RequestMapping(value = "/query")
    public String getUserList(HttpServletRequest request,
                              @RequestParam(required = false) Integer pageIndex,
                              @RequestParam(required = false) String queryname,
                              @RequestParam(required = false) Integer queryUserRole){
        // 分页查询用户列表
        // 1.定义起始页
        if (pageIndex == null){
            pageIndex = 1;
        }
        // 2.定义每页显示多少行
        Integer pageSize = 5;

        PageBean<SmbmsUser> pageBean =  userService.findUserPage(pageIndex,pageSize,queryname,queryUserRole);

        //  3.查询角色列表
        List<SmbmsRole> roleList = userService.findRoleList();
        // 将数据回显到页面上  增加用户的体验
        request.setAttribute("queryUserName",queryname);
        request.setAttribute("queryUserRole",queryUserRole);
        /* 将查询到的数据传入给前端*/
        request.setAttribute("roleList",roleList);
        request.setAttribute("userList",pageBean.getResult());
        request.setAttribute("totalPageCount",pageBean.getTotalPage());
        request.setAttribute("totalCount",pageBean.getTotalCount());
        request.setAttribute("currentPageNo",pageBean.getCurrentPageNo());

        return "userlist";
    }
}
