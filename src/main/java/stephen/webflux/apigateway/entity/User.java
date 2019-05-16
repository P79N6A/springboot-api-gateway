package stephen.webflux.apigateway.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class User {
    private long id;

    private String name;

    private String accessKey;

    private String secretKey;
}
