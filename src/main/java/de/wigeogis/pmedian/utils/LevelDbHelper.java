package de.wigeogis.pmedian.utils;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

import de.wigeogis.pmedian.config.LevelDBConfig;
import org.iq80.leveldb.DB;

/**
 * Created by sn on 17.07.2018.
 */
public class LevelDbHelper {

    private static DB db;

    private static LevelDbHelper ourInstance = new LevelDbHelper();

    public static LevelDbHelper getInstance() {
        return ourInstance;
    }

    private LevelDbHelper() {
        db = LevelDBConfig.getDatabaseInstance();
    }

    public  void insertTo(String key, String value){
        db.put(bytes(key), bytes(value));
    }

    public String get(String key){
        return asString(db.get(bytes(key)));
    }

}
