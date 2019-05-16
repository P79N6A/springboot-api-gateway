package stephen.webflux.apigateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import stephen.webflux.apigateway.dao.ConfigDao;
import stephen.webflux.apigateway.entity.ApiConfig;
import stephen.webflux.apigateway.entity.ApiMeta;
import stephen.webflux.apigateway.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证服务
 * <p>
 * 参数校验、用户解析
 */
@Slf4j
@Service
public class AuthenticationService {
    @Autowired
    private ConfigDao configDao;

    public ApiConfig authenticate(HttpServletRequest request) {
        String accessKey = request.getParameter("access_key");
        User user = configDao.getUserByAccessKey(accessKey);
        String path = request.getRequestURI();
        path = path.substring(4);

        ApiMeta apiMeta = configDao.getApiMetaByPath(path);

        ApiConfig apiConfig = configDao.getApiConfigByUserIdAndApiId(user.getId(), apiMeta.getId());
        apiConfig.setUser(user).setApiMeta(apiMeta);

        return apiConfig;
    }
}
