package studio.fantasyit.maid_storage_manager.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.LLMCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.StringParameter;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;

public class CoWorkSwitchFunction extends AbstractTool<CoWorkSwitchFunction.EnableData> {

    @Override
    public String id() {
        return "switch_cowork";
    }

    @Override
    public String summary(EntityMaid entityMaid) {
        return "You can use this tool to switch to Coworker Mode. In this mode ,you would follow the player and you can see the contents of the player's opened containers in real time. You need to pass a boolean value enable, which is \"true\" or \"false\" to indicate whether you want to enable coworker mode";
    }

    @Override
    public Parameter parameters(ObjectParameter objectParameter, EntityMaid entityMaid) {
        objectParameter.addProperties("enable", StringParameter.create().setPattern("true|false"));
        return objectParameter;
    }

    @Override
    public Codec<EnableData> codec() {
        return EnableData.CODEC;
    }

    @Override
    public String invocationSummary(EnableData enableData) {
        return "Co-work mode ->"+ (enableData.enable.equals("true") ? "enabled" : "disabled");
    }

    @Override
    public String call(EnableData en, EntityMaid maid, LLMCallback callback) {
        StorageManagerConfigData.Data data = maid.getOrCreateData(StorageManagerConfigData.KEY, StorageManagerConfigData.Data.getDefault());
        data.coWorkMode(en.enable.equals("true"));
        maid.setAndSyncData(StorageManagerConfigData.KEY, data);
        return (en.enable.equals("true") ? "Cowork mode enabled." : "Cowork mode disabled.");
    }

    public record EnableData(String enable) {
        public static Codec<EnableData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("enable").forGetter(EnableData::enable)
                ).apply(instance, EnableData::new));
    }
}