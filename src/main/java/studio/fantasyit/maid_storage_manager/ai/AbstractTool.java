package studio.fantasyit.maid_storage_manager.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.agent.tool.ITool;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.LLMCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.request.ChatCompletion;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;

public abstract class AbstractTool<T> implements ITool<T> {
    @Override
    public LLMCallback onCall(String toolCallId, T result, LLMCallback callback) {
        return callback.addToolResult(call(result,callback.getMaid(),callback),toolCallId);
    }

    abstract public String call(T data, EntityMaid maid, LLMCallback callback);

    @Override
    public boolean trigger(EntityMaid maid, ChatCompletion chatCompletion) {
        return maid.getTask().getUid().equals(StorageManageTask.TASK_ID);
    }
}