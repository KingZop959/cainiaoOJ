package com.yupi.yuojbackendserviceclient.service;

import com.yupi.yuojbackendcommon.common.ErrorCode;
import com.yupi.yuojbackendcommon.exception.BusinessException;
import com.yupi.yuojbackendmodel.model.entity.User;
import com.yupi.yuojbackendmodel.model.enums.UserRoleEnum;
import com.yupi.yuojbackendmodel.model.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static com.yupi.yuojbackendcommon.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
//这个是开启OpenFeign的注解，name参数意味着在nacos中叫什么名字
@FeignClient(name = "yuoj-backend-user-service",path = "/api/user/inner")
public interface UserFeignClint{

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     *
     * 在这个服务模块中 对于这种参数非常复杂，害怕序列化导致错误或者信息丢失的接口，可以不用去userService的实现类发请求调用
     * 可以直接把它的实现类中对于这个方法的实现复制过来
     * 作为默认实现
     *
     * 还有一些很简单的服务，不用查数据库的比如下面的isAdmin，只需要看一下role属性，也不用远程调用了
     */
    default User getLoginUser(HttpServletRequest request){
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        /// 本来是还有这一段的，因为我们单体式的时候没有开redis，没有用分布式存储
        /// 用户登陆状态信息就是存的userId，所以每次都得去数据库查完整的用户信息
        /// 这里我们改为微服务后是用的分布式存储 session已经改为了存到redis中所以不需要查库了，redis中存的是完整的用户信息
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        /*long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }*/
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    default boolean isAdmin(User user){
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }


    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    /// 只有需要远程调用的才需要打注解
    @PostMapping("/get/userVo")
    UserVO getUserVO(@RequestBody User user);

    /**
     * 通过id获取用户信息
     */
    @GetMapping("/get/id")
    User getById(@RequestParam("userId") long userId);

    /**
     * 批量查询用户信息
     */
    @GetMapping("/get/ids")
    List<User> listByIds(@RequestParam("idList") Collection<Long> idList);
}
