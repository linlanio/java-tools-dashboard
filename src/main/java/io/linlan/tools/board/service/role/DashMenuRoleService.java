package io.linlan.tools.board.service.role;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import io.linlan.tools.board.dto.ViewDashMenu;
import io.linlan.tools.board.service.AuthService;
import io.linlan.tools.board.service.DashMenuService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Filename:DashMenuRoleService.java
 * Desc:
 *
 * @author hcday
 * @author <a href="mailto:hcday@qq.com">hcday soo</a>
 * CreateTime:2018/1/3 12:04
 *
 * @version 1.0
 * @since 1.0
 *
 */
@Repository
// @Aspect
public class DashMenuRoleService {

    @Autowired
    private AuthService authService;

    @Autowired
    private DashMenuService dashMenuService;

    @Value("${admin_user_id}")
    private String adminUserId;

    @Around("execution(* io.linlan.tools.board.service.DashMenuService.getMenuList(..))")
    public Object getMenuList(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String userId = authService.getCurrentUser().getUserId();
        if (userId.equals(adminUserId)) {
            return proceedingJoinPoint.proceed();
        } else {
            final List<Long> menuIdList = dashMenuService.getMenuIdByUserRole(userId);
            List<ViewDashMenu> list = (List<ViewDashMenu>) proceedingJoinPoint.proceed();
            return new ArrayList<ViewDashMenu>(Collections2.filter(list, new Predicate<ViewDashMenu>() {
                @Override
                public boolean apply(@Nullable ViewDashMenu viewDashMenu) {
                    return menuIdList.contains(viewDashMenu.getMenuId());
                }
            }));
        }
    }

}
