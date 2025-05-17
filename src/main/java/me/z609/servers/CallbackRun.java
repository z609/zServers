package me.z609.servers;

@FunctionalInterface
public interface CallbackRun<Param> {

    void callback(Param param);

}
