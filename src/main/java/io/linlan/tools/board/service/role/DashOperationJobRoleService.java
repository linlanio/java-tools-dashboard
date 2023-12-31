package io.linlan.tools.board.service.role;

import com.alibaba.fastjson.JSONObject;
import io.linlan.tools.board.service.AuthService;
import io.linlan.tools.board.service.job.DashOperationJobService;
import io.linlan.commons.core.Rcode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import io.linlan.commons.script.json.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * 
 * Filename:DashOperationJobRoleService.java
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
//@Aspect
public class DashOperationJobRoleService {
    @Autowired
    private DashOperationJobService dashboardJobService;

    @Autowired
    private AuthService authService;

    @Around("execution(* io.linlan.tools.board.service.job.DashOperationJobService.update(..))")
    public Object update(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String json = (String) proceedingJoinPoint.getArgs()[1];
        JSONObject jo = JsonUtils.parseJO(json);
        String userId = authService.getCurrentUser().getUserId();
        if (dashboardJobService.checkJobRole(userId, jo.getLong("id"), RolePermission.PATTERN_EDIT) > 0) {
            Object value = proceedingJoinPoint.proceed();
            return value;
        } else {
            return Rcode.error("No Permission");
        }
    }

    @Around("execution(* io.linlan.tools.board.service.job.DashOperationJobService.delete(..))")
    public Object delete(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Long id = (Long) proceedingJoinPoint.getArgs()[1];
        String userId = authService.getCurrentUser().getUserId();
        if (dashboardJobService.checkJobRole(userId, id, RolePermission.PATTERN_DELETE) > 0) {
            Object value = proceedingJoinPoint.proceed();
            return value;
        } else {
            return Rcode.error("No Permission");
        }
    }
}
