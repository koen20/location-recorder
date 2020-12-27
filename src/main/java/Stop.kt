public class Stop {
    private String name;
    private double lat;
    private double lon;
    private int radius;
    private boolean userAdded;


    public Stop(String name, double lat, double lon, int radius, boolean userAdded) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.userAdded = userAdded;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isUserAdded() {
        return userAdded;
    }
}
