package pers.winter.config;

@AnnConfig(filePath = "config/mongodb.properties")
public class MongoDBConfig {
    private String url;
    private String databaseName;
    private int connectTimeout;
    private int maxPoolSize;
    private int minPoolSize;
    private int maxWaitTime;
    private int maxConnectionIdleTime;

    public String getUrl() {
        return url;
    }
    public String getDatabaseName(){
        return databaseName;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public int getMaxWaitTime() {
        return maxWaitTime;
    }

    public int getMaxConnectionIdleTime() {
        return maxConnectionIdleTime;
    }
}
