// priority: 0

/**
 * 女仆仓管KJS例。
 * 仓管在KJS中暴露了下列可用字段
 * - MaidStorageManagerEvents:事件，用于注册相关内容
 * - MaidStorageManagerUtil 或者 MSMU:工具类，包含一些实用函数
 * - MaidStorageManagerEnum 或者 MSME:枚举，包含一些常量
 * - MaidStorageManagerTypeCasting 或者 MSMTC: 类型转换。可以辅助语言服务器更好的检查类型。
 * - MaidStorageManager 或者 MSM: 包含以上三者。
 *
 * 开发过程中推荐使用probejs生成类型文档并开启checkJS以获得完整的类型检查体验。
 */

/**
 * 配方生成器用于自动生成符合条件的合成指南。
 * addCraftGuideGenerator 仅包含了常用的方法
 * addCraftGuideGeneratorFull 包含了所有方法
 */
//@ts-ignore
const CONFIG_TEST_I = MSMU.GENERATOR_CONFIGS.integerConfig("count", 5, Component.translatable("generate.count"))
MaidStorageManagerEvents.craft(t => t.addCraftGuideGeneratorFull({
    /**
     * 获取当前生成器的ID
     * @returns {ResourceLocation} ID
     */
    type: function () {
        // 偷懒法，对于函数返回值中的ResourceLocation，你可以直接返回string。
        //@ts-ignore
        return "maid_storage_manager:test";
    },
    /**
     * 缓存回调。缓存发生在数据包加载后。你可以在此为配方添加缓存。<b>缓存的配方原料必须和下次添加同ID配方时完全一致，否则可能出现不可预料的错误</b>
     * @param manager 配方管理器
     * @param helper 缓存助手
     */
    onCache: function (arg0, helper) {
        helper.addRecipe(
            ResourceLocation.tryParse("maid_storage_manager:test"),
            [Ingredient.of(Items.STICK)]
        );
    },
    /**
     * 生成配方。生成时请调用 GeneratorGraph#addNode() 添加配方。当配方被认为需要时，将会调用最后一个参数生成合成指南。
     *
     * @param inventory               女仆仓库
     * @param level                   世界
     * @param pos                     位置
     * @param graph                   生成图助手类
     * @param recognizedTypePositions 已经生成过的生成器类型和位置
     */
    generate: function (inventory, level, pos, graphHelper, recorded) {
        const output = MSMU.itemStack(Items.BEDROCK, CONFIG_TEST_I.getValue());
        graphHelper.addRecipeSingleOutput(
            ResourceLocation.tryParse("maid_storage_manager:test"),
            [Ingredient.of(Items.STICK)],
            [CONFIG_TEST_I.getValue()],
            output,
            (items, helper) => helper.makeCraftGuideData(
                [
                    helper.makeCraftGuideStepData(
                        helper.makeTargetNoSide(MSME.STORAGE_ITEM_HANDLER, pos),
                        items,
                        [],
                        MSME.CRAFT_ACTION_COMMON_PLACE_ITEM,
                        false
                    ),
                    helper.makeCraftGuideStepData(
                        helper.makeTargetNoSide(MSME.STORAGE_ITEM_HANDLER, pos),
                        [],
                        [output],
                        MSME.CRAFT_ACTION_COMMON_TAKE_ITEM,
                        false
                    )
                ],
                MSME.CRAFT_TYPE_COMMON
            )
        );
    },
    /**
     * 获取配置项名字。显示在Cloth Config中
     * @return {Internal.Component} 配置项名字
     */
    configName: function () {
        //@ts-ignore
        //偷懒，可以返回String。将显示为translatable
        return "craft_guide_generator_test";
    },
    /**
     * 方块是否合法。用于判断和世界无关的成立条件
     *
     * @param level 世界
     * @param pos   方块位置
     * @return 是否合法
     */
    isBlockValid: function (level, pos) {
        return (level.getBlockState(pos).is(Blocks.BARREL));
    },
    /**
     * 位置是否合法。用于判断和世界相关的成立条件。
     *
     * @param parent      默认判断器
     * @param level       世界
     * @param maid        女仆
     * @param pos         位置
     * @param pathFinding 快速寻路对象
     * @return 是否合法
     */
    positionalAvailable: function (parent, level, maid, pos, pathFinding) {
        return parent();
    },
    /**
     * 允许为多个位置生成合成指南。如果否则只为距离女仆最近的位置生成。
     *
     * @return
     */
    allowMultiPosition: function () {
        return false;
    },
    /**
     * 返回配置项目。
     * @param helper 配置助手
     * @return 配置项目
     */
    configurations: function (helper) {
        return [CONFIG_TEST_I];
    },
    /**
     * 是否允许缓存图时保留合成指南。如果该类型的生成随方块数据而变化（即方块不消失的情况下也可能生成不同的类型），则需要关闭缓存
     *
     * @return 是否允许缓存
     */
    canCacheGraph: function () {
        return true;
    }
}))

/**
 * 合成操作是合成中具体每个步骤干的事。这里注册了一个让女仆跳的步骤
 */
MaidStorageManagerEvents.craft(t => t.addAction(
    ResourceLocation.tryParse("test:jump"),
    //这里可以传入这样的一个lambda来做到全局可重载。
    //@ts-expect-error
    () => global.makeJumpAction(),
    MSM.enums.PATH_FINDING_EXACTLY,
    2,
    true,
    3,
    0)
)

/** @type {Internal.IKJSCraftContextSupplier_} */
global.makeJumpAction = function () {
    let hasJumped = false;
    let startJumpTick = 0;
    return {
        start: function (maid, craftGuideData, craftGuideStepData, layer, helper) {
            // 在开始和tick过程中，你应该返回success,notDone,fail,continue中的其中一个。其中continue表示继续，且会重置超时检测；notDone则不会。
            return helper.continue;
        },
        tick: function (maid, craftGuideData, craftGuideStepData, layer, helper) {
            //helper中提供了一些基本的检测函数
            if (helper.moveIfNotArrive()) return helper.continue;
            if (helper.notStopped()) return helper.continue;
            if (!hasJumped && maid.onGround()) {
                maid.jumpControl.jump();
                hasJumped = true;
                startJumpTick = helper.getTickCount();
                return helper.continue;
            }
            if (hasJumped && maid.onGround() && helper.getTickCount() - startJumpTick > 5) {
                return helper.success;
            }
            return helper.continue;
        },
        stop: function (arg0, arg1, arg2, arg3, a4) {
        }
    }
}

/**
 * 合成类型是当绑定到某类特定的方块时识别的类型。
 */
const TYPET1 = ResourceLocation.tryParse("maid_storage_manager:barrel_bedrock");
MaidStorageManagerEvents.craft(t => t.addCraftTypeFull({

    /**
     * 合成类型ID
     *
     * @return 合成类型ID
     */
    type: function () {
        return TYPET1;
    },

    /**
     * 合成操作ID
     *
     * @return 合成操作ID。注意，该ID必须被注册！否则将在绑定该类型时产生错误。如果计划使用stepTransform，可以使用addActionVirtual进行注册。
     */
    actionType: function () {
        return TYPET1;
    },
    /**
     * 类型代表物品。会被渲染在合成指南右下角。
     *
     * @return 物品
     */
    icon: function () {
        return MSM.utilities.itemStack(Items.BEDROCK, 1);
    },
    /**
     * 创建GUI。
     *
     * @param containerId 容器ID
     * @param level       世界
     * @param player      玩家
     * @param data        合成数据
     * @return GUI。如果为空则不会打开GUI
     */
    createGui: function (containerId, level, player, data) {
        return null;
    },
    /**
     * 检查合成是否可用。
     *
     * @param craftGuideData 合成数据
     */
    available: function (craftGuideData) {
        return true;
    },
    /**
     * 判断目标方块是否是当前类型对应的绑定方块
     *
     * @param level     世界
     * @param pos       方块位置
     * @param direction 方块点击面
     * @return 是否是当前类型对应的绑定方块
     */
    isSpecialType: function (level, pos, dir) {
        return level.getBlockState(pos).is(Blocks.BARREL);
    },
    /**
     * 步骤转换。可以用于通过一些其他步骤来组合成为当前合成的完整步骤。
     *
     * @param steps 原始步骤。一般来说是长度为1的列表
     * @param helper 合成指南辅助类
     * @return 新的步骤
     */
    transformSteps: function (steps, helper) {
        const step0 = steps[0];
        return [
            MSMU.CRAFT_GUIDES.makeCraftGuideStepData(
                step0.storage,
                step0.input.toArray(),
                [],
                MSME.CRAFT_ACTION_COMMON_PLACE_ITEM,
                false
            ),
            MSMU.CRAFT_GUIDES.makeCraftGuideStepData(
                step0.storage,
                [],
                step0.output.toArray(),
                MSME.CRAFT_ACTION_COMMON_TAKE_ITEM,
                false
            )
        ];
    },
    /**
     * 开始合成，获取操作上下文。一般来说该方法不需要改动。
     *
     * @param parent             原始方法
     * @param maid               执行操作的女仆
     * @param craftGuideData     当前进行的合成指南
     * @param craftGuideStepData 当前进行的合成步骤
     * @param layer              当前进行的合成层
     * @return 操作上下文
     */
    start: function (parent, maid, craftGuideData, craftGuideStepData, layer) {
        return parent();
    },
    /**
     * 当使用了当前类型。发生在用户使用合成指南绑定方块并成功识别为当前类型时。可以设置初始值。
     *
     * @param player         玩家
     * @param itemStack      合成指南
     * @param craftGuideData 合成指南数据
     * @param helper         合成指南帮助器
     */
    onTypeUsing: function (player, itemStack, craftGuideData, helper) {
        const step = craftGuideData.getStepByIdx(0);
        step.setInput(0, MSM.utilities.itemStack(Items.STICK, 1));
        step.setOutput(0, MSM.utilities.itemStack(Items.BEDROCK, 1));
    },
}));
//注册上面用到的action
MaidStorageManagerEvents.craft(t => t.addActionVirtual(
    TYPET1,
    1,
    1
))