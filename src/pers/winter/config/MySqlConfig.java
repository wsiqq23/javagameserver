package pers.winter.config;

@AnnConfig(filePath = "config/mysql.properties")
public class MySqlConfig {
    private short dbCount;
    public short getDbCount(){
        return dbCount;
    }
}
