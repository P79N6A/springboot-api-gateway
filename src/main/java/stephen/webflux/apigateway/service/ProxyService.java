package stephen.webflux.apigateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import stephen.webflux.apigateway.common.response.ResponseResult;
import stephen.webflux.apigateway.dao.ConfigDao;
import stephen.webflux.apigateway.entity.ApiConfig;
import stephen.webflux.apigateway.entity.ApiMeta;

import javax.servlet.http.HttpServletRequest;

/**
 * 请求转发服务
 */
@Slf4j
@Service
public class ProxyService {
    @Autowired
    private ConfigDao configDao;

    @Autowired
    private RestTemplate restTemplate;

    public ResponseResult proxy(HttpServletRequest request, ApiConfig apiConfig) {
        ApiMeta apiMeta = apiConfig.getApiMeta();
        String url = apiMeta.getProtocol() + "://" + apiMeta.getHost() +
                ":" + apiMeta.getPort() + apiMeta.getPath();


        return restTemplate.getForObject(url, ResponseResult.class);
    }

    public void changeConfig(){
        configDao.changeConfig();
    }
}
