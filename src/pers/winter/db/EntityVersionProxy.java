package pers.winter.db;

public class EntityVersionProxy {
    public static void setEntityVersion(AbstractBaseEntity entity, int version){
        entity.setEntityVersion(version);
    }
}
