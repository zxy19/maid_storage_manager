package studio.fantasyit.maid_storage_manager.attachment;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.util.Conditions;

import java.util.*;
import java.util.function.Predicate;

import static studio.fantasyit.maid_storage_manager.registry.DataAttachmentRegistry.CRAFT_BLOCK_OCCUPY;

public class CraftBlockOccupy implements INBTSerializable<CompoundTag> {
    public record OccupiedRecord(UUID uuid, BlockPos pos, int index) {
        @Override
        public int hashCode() {
            return uuid.hashCode() * 3 + pos.hashCode() * 7 + index * 13;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OccupiedRecord(UUID uuid1, BlockPos pos1, int index1)) {
                return uuid1.equals(this.uuid) &&
                        pos1.equals(this.pos) &&
                        index1 == this.index;
            }
            return false;
        }
    }

    public Map<OccupiedRecord, MutableInt> occupiedPos;

    public CraftBlockOccupy() {
        this.occupiedPos = new HashMap<>();
    }

    public void addOccupy(EntityMaid maid, int index, BlockPos pos) {
        addOccupy(maid.getUUID(), index, pos);
    }

    public void addOccupy(UUID maidUUID, int index, BlockPos pos) {
        OccupiedRecord occupiedRecord = new OccupiedRecord(maidUUID, pos, index);
        if (this.occupiedPos.containsKey(occupiedRecord)) {
            this.occupiedPos.get(occupiedRecord).setValue(12000);
        } else {
            this.occupiedPos.put(occupiedRecord, new MutableInt(12000));
        }
    }

    public void removeOccupyFor(EntityMaid maid, int index) {
        removeIf(record -> record.uuid.equals(maid.getUUID()) && record.index == index);
    }

    public void removeAllOccupies() {
        this.occupiedPos.clear();
    }

    public void removeAllOccupiesFor(EntityMaid maid) {
        removeIf(occupiedRecord -> occupiedRecord.uuid.equals(maid.getUUID()));
    }

    public void tick(ServerLevel level) {
        removeIf(or ->
                occupiedPos.get(or).decrementAndGet() < 0 || !(level.getEntity(or.uuid) instanceof EntityMaid maid && maid.isAlive() && Conditions.takingRequestList(maid))
        );
    }

    protected void removeIf(Predicate<OccupiedRecord> predicate) {
        Set<OccupiedRecord> ks = new HashSet<>(occupiedPos.keySet());
        for (OccupiedRecord k : ks) {
            if (occupiedPos.containsKey(k) && predicate.test(k))
                occupiedPos.remove(k);
        }
    }

    public boolean isOccupiedByNonCurrent(EntityMaid maid, BlockPos pos, int index) {
        return this.occupiedPos
                .keySet()
                .stream().anyMatch(or ->
                        (or.index != index || !or.uuid.equals(maid.getUUID()))
                                &&
                                or.pos.equals(pos)
                );
    }


    public boolean isOccupiedByAny(BlockPos pos) {
        return this.occupiedPos
                .keySet()
                .stream().anyMatch(or ->
                        or.pos.equals(pos)
                );
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider var1) {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        List<OccupiedRecord> keys = new ArrayList<>(occupiedPos.keySet());
        for (int i = 0; i < keys.size(); i++) {
            CompoundTag tmp = new CompoundTag();
            tmp.putLong("pos", keys.get(i).pos.asLong());
            tmp.putInt("index", keys.get(i).index);
            tmp.putUUID("maidUUID", keys.get(i).uuid);
            listTag.add(tmp);
        }
        tag.put("occupied", listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider var1, CompoundTag nbt) {
        ListTag listTag = nbt.getList("occupied", ListTag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tmp = listTag.getCompound(i);
            addOccupy(tmp.getUUID("maidUUID"), tmp.getInt("index"), BlockPos.of(tmp.getLong("pos")));
        }
    }

    public static CraftBlockOccupy get(Level level) {
        return level.getData(CRAFT_BLOCK_OCCUPY);
    }
}
