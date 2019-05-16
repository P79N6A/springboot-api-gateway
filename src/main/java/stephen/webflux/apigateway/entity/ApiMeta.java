package stephen.webflux.apigateway.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ApiMeta {
    private long id;

    private String name;

    private String protocol;

    private String host;

    private int port;

    private String path;

    private int timeout = 5;

    private boolean needCache = false;

    private int cacheExpirationTime = 30;

    private boolean needAuthentication = true;
}
