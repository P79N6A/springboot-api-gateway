package stephen.webflux.apigateway.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.stereotype.Component;
import stephen.webflux.apigateway.entity.ApiConfig;
import stephen.webflux.apigateway.entity.ApiMeta;
import stephen.webflux.apigateway.entity.User;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class ConfigDao {
    private volatile CuratorFramework client;
    private static final String ZK_ADDRESS = "xxx:9181";
    private static final String ZK_PATH = "/api_gateway/config";

    private volatile HashMap<String, ApiMeta> apiMetaHashMap = new HashMap<>();
    private volatile HashMap<String, User> userHashMap = new HashMap<>();
    private volatile HashMap<String, ApiConfig> apiConfigHashMap = new HashMap<>();

    public ConfigDao() {
        client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new RetryNTimes(10, 5000)
        );
        client.start();
        initConfig();
        watchConfig();
    }

    public User getUserByAccessKey(String accessKey) {
        return userHashMap.get(accessKey);
    }

    public ApiMeta getApiMetaByPath(String path) {
        return apiMetaHashMap.get(path);
    }

    public ApiConfig getApiConfigByUserIdAndApiId(Long userId, Long apiId) {
        return apiConfigHashMap.get(userId + "_" + apiId);
    }

    private void initConfig() {
        try {
            byte[] apiMetaData = client.getData().forPath(ZK_PATH + "/api_meta");
            apiMetaHashMap = parseApiMeta(apiMetaData);

            byte[] userData = client.getData().forPath(ZK_PATH + "/user");
            userHashMap = parseUser(userData);

            byte[] apiConfigData = client.getData().forPath(ZK_PATH + "/api_config");
            apiConfigHashMap = parseApiConfig(apiConfigData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void watchConfig() {
        // 2.Register watcher
        PathChildrenCache watcher = new PathChildrenCache(
                client,
                ZK_PATH,
                true    // if cache data
        );
        watcher.getListenable().addListener((client1, event) -> {
            ChildData data = event.getData();
            if (data == null) {
                System.out.println("No data in event[" + event + "]");
            } else {
                System.out.println("Receive event: "
                        + "type=[" + event.getType() + "]"
                        + ", path=[" + data.getPath() + "]"
                        + ", data=[" + new String(data.getData()) + "]"
                        + ", stat=[" + data.getStat() + "]");

                initConfig();
            }
        });
        try {
            watcher.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<String, ApiMeta> parseApiMeta(byte[] data) {
        HashMap<String, ApiMeta> hashMap = new HashMap<>();
        ArrayList<JSONObject> apiMetas = JSON.parseObject(data, ArrayList.class);
        for (JSONObject apiMeta : apiMetas) {
            hashMap.put(apiMeta.getString("path"), new ApiMeta()
                    .setId(apiMeta.getLong("id"))
                    .setProtocol(apiMeta.getString("protocol"))
                    .setHost(apiMeta.getString("host"))
                    .setPath(apiMeta.getString("path"))
                    .setPort(apiMeta.getInteger("port")));
        }
        return hashMap;
    }

    private HashMap<String, User> parseUser(byte[] data) {
        HashMap<String, User> hashMap = new HashMap<>();
        ArrayList<JSONObject> items = JSON.parseObject(data, ArrayList.class);
        for (JSONObject item : items) {
            hashMap.put(item.getString("accessKey"), new User()
                    .setAccessKey(item.getString("accessKey"))
                    .setId(item.getLong("id"))
                    .setSecretKey(item.getString("secretKey"))
                    .setName(item.getString("name")));
        }
        return hashMap;
    }

    private HashMap<String, ApiConfig> parseApiConfig(byte[] data) {
        HashMap<String, ApiConfig> hashMap = new HashMap<>();
        ArrayList<JSONObject> items = JSON.parseObject(data, ArrayList.class);
        for (JSONObject item : items) {
            String key = +item.getJSONObject("user").getLong("id")
                    + "_"
                    + item.getJSONObject("apiMeta").getLong("id");
            JSONObject apiMetaJsonObject = item.getJSONObject("apiMeta");
            ApiMeta apiMeta = new ApiMeta()
                    .setId(apiMetaJsonObject.getLong("id"))
                    .setProtocol(apiMetaJsonObject.getString("protocol"))
                    .setHost(apiMetaJsonObject.getString("host"))
                    .setPath(apiMetaJsonObject.getString("path"))
                    .setPort(apiMetaJsonObject.getInteger("port"));

            JSONObject userJsonObject = item.getJSONObject("user");
            User user = new User()
                    .setAccessKey(userJsonObject.getString("accessKey"))
                    .setId(userJsonObject.getLong("id"))
                    .setSecretKey(userJsonObject.getString("secretKey"))
                    .setName(userJsonObject.getString("name"));

            hashMap.put(key, new ApiConfig()
                    .setQps(item.getInteger("qps"))
                    .setQuota(item.getLong("quota"))
                    .setApiMeta(apiMeta)
                    .setUser(user));
        }
        return hashMap;
    }

    public void changeConfig() {
        try {
            ArrayList<ApiMeta> apiMetas = new ArrayList<>();
            ApiMeta apiMeta = new ApiMeta()
                    .setId(112)
                    .setProtocol("http")
                    .setHost("yq01-holmes-stjweb02.yq01.baidu.com")
                    .setPath("/id/next")
                    .setPort(8001);
            apiMetas.add(apiMeta);
            client.setData().forPath(ZK_PATH + "/api_meta", JSON.toJSONBytes(apiMetas));

            ArrayList<User> users = new ArrayList<>();
            User user = new User()
                    .setId(1)
                    .setName("test")
                    .setAccessKey("access_key")
                    .setSecretKey("secret_key");
            users.add(user);
            client.setData().forPath(ZK_PATH + "/user", JSON.toJSONBytes(users));

            ArrayList<ApiConfig> apiConfigs = new ArrayList<>();
            ApiConfig apiConfig = new ApiConfig().setUser(user).setQps(3000).setQuota(20000).setApiMeta(apiMeta);
            apiConfigs.add(apiConfig);
            client.setData().forPath(ZK_PATH + "/api_config", JSON.toJSONBytes(apiConfigs));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
