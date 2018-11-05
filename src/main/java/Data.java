import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"timestamp", "longitude", "latitude", "mmsi", "heading", "rate_of_turn", "speed", "status"})
public class Data {
    public String timestamp;
    public double longitude;
    public double latitude;
    public long mmsi;
    public String heading;
    public double rate_of_turn;
    public double speed;
    public int status;

    public boolean valid() {
        return longitude > 3.8 && longitude < 4.75 &&
                latitude > 51.7 && latitude < 52.0;
    }

    @Override
    public String toString() {
        return "Data{" +
                "timestamp='" + timestamp + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", mmsi=" + mmsi +
                ", heading='" + heading + '\'' +
                ", rate_of_turn=" + rate_of_turn +
                ", speed=" + speed +
                ", status=" + status +
                '}';
    }
}
