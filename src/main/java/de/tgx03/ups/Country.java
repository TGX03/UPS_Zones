package de.tgx03.ups;

class Country {

    public final String name;
    public final short expressID;
    public final short standardID;
    public final boolean standard;
    public final boolean expedited;

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
