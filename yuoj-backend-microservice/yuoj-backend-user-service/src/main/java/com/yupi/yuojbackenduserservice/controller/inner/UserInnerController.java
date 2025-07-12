package com.yupi.yuojbackenduserservice.controller.inner;
import com.yupi.yuojbackendmodel.model.entity.User;
import com.yupi.yuojbackendmodel.model.vo.UserVO;
import com.yupi.yuojbackendserviceclient.service.UserFeignClint;
import com.yupi.yuojbackenduserservice.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
/**
 * 该服务仅内部调用
 */
@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeignClint {

    @Resource
    private UserService userService;
    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    /// 只有需要远程调用的才需要打注解
    @PostMapping("/get/userVo")
    public UserVO getUserVO(@RequestBody User user){
        return userService.getUserVO(user);
    }

    /**
     * 通过id获取用户信息
     */
    @GetMapping("/get/id")
    public User getById(@RequestParam("userId") long userId){
        return userService.getById(userId);
    }

    /**
     * 批量查询用户信息
     */
    @GetMapping("/get/ids")
    public List<User> listByIds(@RequestParam("idList") Collection<Long> idList){
        return userService.listByIds(idList);
    }
}
