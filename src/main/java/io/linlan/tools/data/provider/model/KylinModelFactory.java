package io.linlan.tools.data.provider.model;

import com.alibaba.fastjson.JSONObject;
import io.linlan.tools.data.provider.KylinDataProvider;
import io.linlan.commons.script.json.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 */
public class KylinModelFactory {

    public static KylinBaseModel getKylinModel(Map<String, String> dataSource, Map<String, String> query) throws Exception {
        KylinBaseModel model = null;
        String modelName = query.get(KylinDataProvider.DATA_MODEL);
        String serverIp = dataSource.get(KylinDataProvider.SERVERIP);
        String username = dataSource.get(KylinDataProvider.USERNAME);
        String password = dataSource.get(KylinDataProvider.PASSWORD);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(username, password));
        ResponseEntity<String> restful = restTemplate.getForEntity(
                "http://" + serverIp + "/kylin/api/model/{modelName}", String.class, modelName);
        JSONObject modelJson = JsonUtils.parseJO(restful.getBody());

        String kylinVersion = modelJson.getString("version");
        String[] kvs = kylinVersion.split("\\.");
        String bigVersion = kvs[0];
        if ("1".equals(bigVersion)) {
            model = new KylinModel1x(modelJson, dataSource, query, kvs);
        } else if ("2".equals(bigVersion)) {
            model = new KylinModel2x(modelJson, dataSource, query, kvs);
        }
        return model;
    }
}
