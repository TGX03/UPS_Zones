package de.tgx03.ups;

/**
 * A class representing a country and its corresponding zone IDs
 */
class Country {

    public final String name;
    public final short expressID;
    public final short standardID;
    public final boolean standard;
    public final boolean expedited;

    /**
     * @param name The name of this country
     * @param express Its ID for express shipping
     * @param standard Its ID for standard or expedited shipping
     * @param standardAvailable Whether standard shipping is available
     * @param expedited Whether standard is actually expedited
     */
    public Country (String name, short express, short standard, boolean standardAvailable, boolean expedited) {
        this.name = name;
        this.expressID = express;
        this.standardID = standard;
        this.standard = standardAvailable;
        this.expedited = expedited;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
