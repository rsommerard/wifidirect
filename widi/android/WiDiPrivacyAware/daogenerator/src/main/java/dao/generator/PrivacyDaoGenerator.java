package dao.generator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class PrivacyDaoGenerator {
    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "fr.rsommerard.privacyaware.dao");
        schema.enableKeepSectionsByDefault();

        createDataTable(schema);
        createDevicesTable(schema);

        new DaoGenerator().generateAll(schema, "app/src/main/java");
    }

    private static void createDevicesTable(Schema schema) {
        Entity devices = schema.addEntity("Device");

        devices.addIdProperty();
        devices.addStringProperty("name").notNull();
        devices.addStringProperty("address").notNull().unique().index();
        devices.addStringProperty("timestamp").notNull();
    }

    private static void createDataTable(Schema schema) {
        Entity data = schema.addEntity("Data");

        data.implementsSerializable();

        data.addIdProperty();
        data.addStringProperty("content").notNull();
    }
}
