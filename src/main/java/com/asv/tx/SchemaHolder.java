package com.asv.tx;

import com.sap.mdm.extension.MetadataManager;
import com.sap.mdm.extension.schema.RepositorySchemaEx;
import com.sap.mdm.session.UserSessionContext;

/**
 * Кеширует объект MDM Schema глобально (для всех потоков)
 * Это сделано для ускорения работы c MDM, т.к. конструирование MDM Schema очень дорогая операция
 * при том что MDM Schema меняется редко.
 * Но нужно учесть что при изменении MDM необходимо перзапустить приложение.
 */
public class SchemaHolder {

    static volatile SchemaHolder INSTANCE;

    private RepositorySchemaEx schema;

    private SchemaHolder(RepositorySchemaEx schema) {
        this.schema = schema;
    }

    public static SchemaHolder getInstance() {
        if (INSTANCE == null) {
            synchronized (SchemaHolder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SchemaHolder(instantiateSchema());
                }
            }
        }
        return INSTANCE;
    }

    public RepositorySchemaEx getSchema() {
        return schema;
    }

    private static RepositorySchemaEx instantiateSchema() {
        return instantiateSchema(UserSessionManager.getUserSessionContext());
    }

    private static RepositorySchemaEx instantiateSchema(UserSessionContext usc) {
        MetadataManager metadataManager = MetadataManager.getInstance();
        RepositorySchemaEx schema = metadataManager.getRepositorySchema(usc);
        return schema;
    }
}
