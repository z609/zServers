package me.z609.servers;

public interface Callback<Returns, Param> {

    Returns callback(Param param);

}
