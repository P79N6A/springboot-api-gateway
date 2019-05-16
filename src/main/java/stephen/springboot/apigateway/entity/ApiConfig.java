package stephen.springboot.apigateway.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ApiConfig {
    private ApiMeta apiMeta;

    private User user;

    private int qps = 30;

    private long quota = 100;
}
