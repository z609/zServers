package me.z609.servers;

@FunctionalInterface
public interface Callback<Returns, Param> {

    Returns callback(Param param);

}
