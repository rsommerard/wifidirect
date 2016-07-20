package fr.inria.rsommerard.widitestingproject.dao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table "DEVICE".
 */
public class Device {

    private Long id;
    /** Not-null value. */
    private String name;
    /** Not-null value. */
    private String address;
    /** Not-null value. */
    private String timestamp;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public Device() {
    }

    public Device(Long id) {
        this.id = id;
    }

    public Device(Long id, String name, String address, String timestamp) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getName() {
        return name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setName(String name) {
        this.name = name;
    }

    /** Not-null value. */
    public String getAddress() {
        return address;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setAddress(String address) {
        this.address = address;
    }

    /** Not-null value. */
    public String getTimestamp() {
        return timestamp;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // KEEP METHODS - put your custom methods here
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("[");

        str.append("id: ").append(id).append(", ");
        str.append("name: ").append(name).append(", ");
        str.append("address: ").append(address).append(", ");
        str.append("timestamp: ").append(timestamp);
        str.append("]");

        return str.toString();
    }
    // KEEP METHODS END

}
