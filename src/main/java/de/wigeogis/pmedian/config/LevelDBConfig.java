package de.wigeogis.pmedian.config;


import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import java.io.File;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;



/**
 * Created by sn on 13.07.2018.
 */
@Configuration
public class LevelDBConfig {

    @Value("${leveldb.root}")
    private String dbPath;

    private static DB db;


    @Bean
    public Boolean initLevelDB() throws Exception{
        Options options = new Options();
        options.createIfMissing(true);
        db = factory.open(new File(dbPath + "dmatrix"), options);
        return true;
    }


    public static DB getDatabaseInstance(){
        return db;
    }
}
