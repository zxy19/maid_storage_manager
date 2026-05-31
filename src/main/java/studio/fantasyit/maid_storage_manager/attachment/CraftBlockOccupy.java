package studio.fantasyit.maid_storage_manager.attachment;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.util.Conditions;

import java.util.*;
import java.util.function.Predicate;

import static studio.fantasyit.maid_storage_manager.registry.DataAttachmentRegistry.CRAFT_BLOCK_OCCUPY;

public class CraftBlockOccupy implements ValueIOSerializable {
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
    public void serialize(ValueOutput output) {
        ValueOutput.ValueOutputList list = output.childrenList("occupied");
        for (OccupiedRecord key : occupiedPos.keySet()) {
            ValueOutput child = list.addChild();
            child.putString("maidUUID", key.uuid.toString());
            child.putLong("pos", key.pos.asLong());
            child.putInt("index", key.index);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        for (ValueInput child : input.childrenListOrEmpty("occupied")) {
            addOccupy(
                    UUID.fromString(child.getStringOr("maidUUID", "")),
                    child.getIntOr("index", 0),
                    BlockPos.of(child.getLongOr("pos", 0))
            );
        }
    }

    public static CraftBlockOccupy get(Level level) {
        return level.getData(CRAFT_BLOCK_OCCUPY);
    }
}
