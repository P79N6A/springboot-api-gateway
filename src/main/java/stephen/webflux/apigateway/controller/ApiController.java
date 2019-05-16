package stephen.webflux.apigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stephen.webflux.apigateway.common.response.ResponseResult;
import stephen.webflux.apigateway.common.response.ResponseResultGenerator;
import stephen.webflux.apigateway.entity.ApiConfig;
import stephen.webflux.apigateway.service.AuthenticationService;
import stephen.webflux.apigateway.service.AuthorizationService;
import stephen.webflux.apigateway.service.ProxyService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private ProxyService proxyService;

    @RequestMapping("/change")
    public ResponseResult change(){
        proxyService.changeConfig();
        return ResponseResultGenerator.genSuccessResult();
    }

    @RequestMapping("/**")
    public ResponseResult index(HttpServletRequest request) {
        ApiConfig apiConfig = authenticationService.authenticate(request);
        if (authorizationService.authorize(apiConfig)) {
            return proxyService.proxy(request, apiConfig);
        } else {
            return ResponseResultGenerator.genFailResult("quota or qps is not enough");
        }
    }
}
