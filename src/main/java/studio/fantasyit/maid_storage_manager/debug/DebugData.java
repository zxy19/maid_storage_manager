package studio.fantasyit.maid_storage_manager.debug;

import net.minecraft.nbt.CompoundTag;
import studio.fantasyit.maid_storage_manager.network.Network;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DebugData {
    private static DebugData instance = null;
    public static DebugData getInstance(){
        if(instance == null){
            instance = new DebugData();
        }
        return instance;
    }
    ConcurrentMap<String, CompoundTag> data;
    protected DebugData(){
        data = new ConcurrentHashMap<>();
    }
    public void setData(String type, CompoundTag data){
        this.data.put(type, data);
    }
    public Optional<CompoundTag> getData(String type){
        if(data.containsKey(type))
            return Optional.of(data.get(type));
        return Optional.empty();
    }
    public void setDataAndSync(String type, CompoundTag data){
        setData(type, data);
        Network.sendDebugDataPacket(type, data);
    }
}
