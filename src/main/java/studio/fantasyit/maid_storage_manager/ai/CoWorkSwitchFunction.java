package studio.fantasyit.maid_storage_manager.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.service.function.IFunctionCall;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.response.ToolResponse;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.StringParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.request.ChatCompletion;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;

public class CoWorkSwitchFunction implements IFunctionCall<CoWorkSwitchFunction.EnableData> {

    @Override
    public String getId() {
        return "switch_cowork";
    }

    @Override
    public String getDescription(EntityMaid entityMaid) {
        //return "你可以使用这个工具来切换协同工作模式。协同工作模式下，你会主动跟着玩家，并能够实时看到玩家的打开的箱子等容器的内容。你需要传入一个布尔值enable，即\"true\"或\"false\"，来表示是否要开启协同工作模式";
        return "You can use this tool to switch to Coworker Mode. In this mode ,you would follow the player and you can see the contents of the player's opened containers in real time. You need to pass a boolean value enable, which is \"true\" or \"false\" to indicate whether you want to enable coworker mode";
    }

    @Override
    public Parameter addParameters(ObjectParameter objectParameter, EntityMaid entityMaid) {
        objectParameter.addProperties("enable", StringParameter.create().setPattern("true|false"));
        return objectParameter;
    }

    @Override
    public boolean addToChatCompletion(EntityMaid maid, ChatCompletion chatCompletion) {
        return maid.getTask().getUid().equals(StorageManageTask.TASK_ID);
    }

    @Override
    public Codec<EnableData> codec() {
        return EnableData.CODEC;
    }

    @Override
    public ToolResponse onToolCall(EnableData en, EntityMaid entityMaid) {
        StorageManagerConfigData.Data data = entityMaid.getOrCreateData(StorageManagerConfigData.KEY, StorageManagerConfigData.Data.getDefault());
        data.coWorkMode(en.enable.equals("true"));
        entityMaid.setAndSyncData(StorageManagerConfigData.KEY, data);
        return new ToolResponse(en.enable.equals("true") ? "协同工作模式已开启" : "协同工作模式已关闭");
    }

    public record EnableData(String enable) {
        public static Codec<EnableData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("enable").forGetter(EnableData::enable)
                ).apply(instance, EnableData::new));
    }
}