package me.z609.servers.server.world;

import net.minecraft.server.v1_12_R1.ScoreboardObjective;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Coordinates {

    public static final double EPSILON = 0.0001;

    public final double x;
    public final double y;
    public final double z;

    public final float yaw;
    public final float pitch;

    public Coordinates(Location location){
        this(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Coordinates(int x, int y, int z) {
        this(x, y, z, 0, 0);
    }

    public Coordinates(int x, int y, int z, float yaw, float pitch) {
        this((double)x, (double)y, (double)z);
    }

    public Coordinates(double x, double y, double z){
        this(x,y,z,0,0);
    }

    public Coordinates(double x, double y, double z, float yaw, float pitch){
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location asLocation(World world){
        return new Location(world, x, y, z, yaw, pitch);
    }

    public String serialize(){
        return serialize(this);
    }

    public Vector asVector() {
        return new Vector(x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Coordinates || obj instanceof Location)) return false;

        Coordinates other = (obj instanceof Location)
                ? new Coordinates((Location) obj)
                : (Coordinates) obj;

        return Math.abs(x - other.x) < EPSILON &&
                Math.abs(y - other.y) < EPSILON &&
                Math.abs(z - other.z) < EPSILON;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(x) ^ Double.hashCode(y) ^ Double.hashCode(z);
    }

    @Override
    public String toString() {
        return "[Coordinates:" + hashCode() +  ":" + x + "," + y + "," + z + "," + yaw + "," + pitch + "]";
    }

    public static String serialize(Location location){
        return serialize(new Coordinates(location));
    }

    public static String serialize(Coordinates coordinates){
        return coordinates.x +"," + coordinates.y + "," + coordinates.z + "," + coordinates.yaw + "," + coordinates.pitch;
    }

    public static Coordinates parse(String s){
        if(s == null) {
            return null;
        }

        if(!s.contains(",")) {
            throw new IllegalArgumentException("Invalid string " + s + " (not a coordinate)");
        }

        String[] args = s.split(",");
        if(args.length < 3) {
            throw new IllegalArgumentException("Invalid string " + s + " (not a coordinate)");
        }

        double x = 0, y = 0, z = 0;
        float yaw = 0, pitch = 0;
        try {
            x = Double.parseDouble(args[0]);
            y = Double.parseDouble(args[1]);
            z = Double.parseDouble(args[2]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid string " + s + " (not a coordinate)", ex);
        }

        if(args.length > 3){
            try {
                yaw = Float.parseFloat(args[3]);
            } catch (NumberFormatException ignored) {
            }
        }

        if(args.length > 4){
            try {
                pitch = Float.parseFloat(args[4]);
            } catch (NumberFormatException ignored) {
            }
        }

        return new Coordinates(x, y, z, yaw, pitch);
    }

    public static String serializeList(List<Coordinates> list) {
        return list.stream()
                .map(obj -> Coordinates.serialize(obj))
                .collect(Collectors.joining(";"));
    }

    public static List<Coordinates> parseList(String s){
        String[] args = s.split(";");
        List<Coordinates> list = new ArrayList<>();
        for(int i = 0; i < args.length; i++){
            try {
                list.add(parse(args[i]));
            } catch (Exception ex) {
                Logger.getLogger("Minecraft").warning(ex.getMessage());
            }
        }
        return list;
    }
}
