package pers.winter.db;

public class Constants {
    public enum DBType{
        MONGO,MYSQL
    }
    public enum CacheType{
        REDIS,MEMORY
    }
    /**
     * Enumeration of the entity's actions.
     * delete, insert and update
     */
    public enum Action{
        DELETE(0),
        INSERT(1),
        UPDATE(2);
        private final int number;
        Action(int number){
            this.number = number;
        }
        public int toNumber(){
            return number;
        }
    }
}
