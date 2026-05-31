# GeckoLib & MoLang Migration

## GeckoLib3 Structure Overview

新版本对GeckoLib3进行了从"单控制器手动动画"到"动画控制器驱动 + RenderState渲染管线"的全面架构升级。

### 核心架构变化

| 旧版本 (OLD) | 新版本 (NEW) | 说明 |
|---|---|---|
| `AnimationController<T>` 单体类 | `IAnimationController<T>` 接口 + `BedrockAnimationController`, `CodedAnimationController`, `HybridAnimationController` | 拆分为接口驱动，支持基岩版动画控制器JSON驱动 |
| `AnimationControllerContext` | `ControllerContext` + `MolangContext` | Molang上下文统一为分层上下文 |
| `AnimationPoint` / `TransitionPoint` 顶层类 | `point/AnimationPoint`, `point/KeyFramePoint`, `point/BeginningTransitionPoint`, `point/EndingTransitionPoint` | 过渡点拆分为首/尾两个类 |
| `IGeoEntity` + `IGeoEntityRenderer` 接口组合 | `GeckoRenderData` + `RenderContext` + `RenderContextManager` | 实体渲染接口完全移除，改为数据驱动 |
| `IGeoRenderer<T>` 简单泛型 | `IGeoRenderer<TState extends EntityRenderState, TData extends GeckoRenderData>` | 泛型参数增加RenderState和RenderData |
| `render()` 方法 | `preSubmit()` + `submit()` 方法 | 渲染分为预提交和提交两个阶段 |
| `ILocationBone` / `IBone` 接口 | `IBoneView` 接口 | 骨骼访问器简化 |
| `ILocationModel` 接口 | `GeoModelState` + `GeoModelStateExtractor` + `GeoLocatorType` | 定位器系统完全重做，从字符串匹配变为类型化查找 |
| `ILoopType` 接口 + 内部枚举 | `LoopType` 枚举 | 简化为直接枚举 |
| `IGeoBuilder` 接口 | `GeoBuilder` 静态方法 | 移除接口抽象层 |
| `AnimationFile` record | `AnimationFile` class | record → class |
| 无 | `AnimationControllerFile` class | 新增基岩版动画控制器文件 |
| 无 | `EntityStateTracker` | 新增实体状态追踪器 |

### 渲染管线变化

OLD: `render(AnimatedGeoModel, T, partialTick, RenderType, PoseStack, MultiBufferSource, VertexConsumer, ...)` 直接渲染
  → `renderEarly()` → `renderLate()` → `renderRecursively(bone)` → `renderCubesOfBone()`

NEW: `preSubmit(state, data, ctx, poseStack, submitNodeCollector)` → `submit(state, data, ctx, poseStack, submitNodeCollector, renderType)`
  → `data.modelState.visitRenderBones(pose, visitor)` 批量访问
  → `renderCubesOfBone(GeoBone, PoseStack.Pose, VertexConsumer, TState, GeckoRenderData)`

**关键差异**:
- OLD使用 `AnimatedGeoBone` 树递归渲染；NEW使用 `GeoModelState` 扁平化骨骼数组 + 批量CPU端变换
- OLD使用 `RenderUtils.prepMatrixForBone()` 辅助；NEW直接在 `GeoModelStateExtractor` 中预计算所有变换矩阵
- NEW的 `submitNodeCollector.submitCustomGeometry()` 是NeoForge新渲染管线（1.21.1+）

---

## GeckoLib File Changes

### `core/` 子包变化

#### 新增文件（完整列表）

| 文件路径 (相对 geckolib3/) | 说明 |
|---|---|
| `core/EntityStateTracker.java` | 泛型实体状态追踪器，管理实体tick状态、位置增量、渲染tick增量 |
| `core/builder/controller/AnimationControllerData.java` | 基岩版动画控制器数据 |
| `core/builder/controller/AnimationControllerState.java` | 动画控制器状态定义 |
| `core/builder/LoopType.java` | 替换 `ILoopType` 接口，简化为三值枚举：LOOP, PLAY_ONCE, HOLD_ON_LAST_FRAME |
| `core/controller/IAnimationController.java` | 动画控制器统一接口 |
| `core/controller/BedrockAnimationController.java` | 基岩版动画控制器（支持JSON驱动、状态机、层级控制器） |
| `core/controller/CodedAnimationController.java` | 代码驱动动画控制器（兼容旧版动画谓词模式） |
| `core/controller/HybridAnimationController.java` | 混合控制器（可能同时支持代码和JSON） |
| `core/controller/AnimationPlayer.java` | 动画播放器，管理单个动画的生命周期 |
| `core/controller/IBoneAnimationQueue.java` | 骨骼动画队列接口 |
| `core/controller/BlendBoneAnimationQueue.java` | 混合骨骼动画队列（基岩版控制器用） |
| `core/controller/AnimationPlayerHolder.java` | AnimationPlayer持有者 |
| `core/controller/transition/IBlendTransition.java` | 混合过渡接口 |
| `core/controller/transition/LinearBlendTransition.java` | 线性混合过渡 |
| `core/controller/transition/SegmentedBlendTransition.java` | 分段混合过渡 |
| `core/event/AnimationEvent.java` | 动画事件（从 `predicate/` 移出，增加 RenderContext / EntityRenderState） |
| `core/event/GeckoAsyncTask.java` | 异步渲染数据任务 |
| `core/event/GeckoSyncTask.java` | 同步渲染数据任务 |
| `core/event/GeckoUpdateTask.java` | 渲染数据更新任务基类 |
| `core/eventframe/InstructionKeyFrameExecutor.java` | 指令关键帧执行器（从 `event/` 移入） |
| `core/eventframe/SoundKeyframeExecutor.java` | 声音关键帧执行器（新增） |
| `core/keyframe/AnimationVec3.java` | Vector3f风格的动画向量（替换单个浮点值） |
| `core/keyframe/point/AnimationPoint.java` | 动画点（从顶层移入） |
| `core/keyframe/point/KeyFramePoint.java` | 关键帧点（从顶层移入） |
| `core/keyframe/point/BeginningTransitionPoint.java` | 起始过渡点 |
| `core/keyframe/point/EndingTransitionPoint.java` | 结束过渡点 |
| `core/keyframe/event/PointType.java` | 点类型枚举 |
| `core/molang/binding/ScopedObject.java` | 作用域对象接口 |
| `core/molang/binding/TransientObject.java` | 瞬时对象接口 |
| `core/molang/context/ControllerContext.java` | 动画控制器Molang上下文 |
| `core/molang/context/DebugSource.java` | 调试源接口 |
| `core/molang/context/MolangContext.java` | Molang统一上下文（整合动画、控制器、声音管理） |
| `core/molang/storage/IContextVariableStorage.java` | 上下文变量存储接口 |
| `core/molang/storage/MolangMemory.java` | Molang内存管理（作用域变量+栈内存） |
| `core/molang/storage/StackMemory.java` | 栈内存（函数调用帧） |
| `core/molang/value/FloatValue.java` | 替换 DoubleValue（double→float） |
| `core/processor/IBoneView.java` | 骨骼视图接口 |
| `core/processor/DebugInfo.java` | 调试信息 |
| `core/snapshot/DirtyTracker.java` | 脏标记追踪器（OLD有，NEW保留） |

#### 移除文件

| 文件路径 | 说明 |
|---|---|
| `core/controller/AnimationController.java` | 旧版单控制器，被拆分 |
| `core/controller/AnimationControllerContext.java` | 旧版控制器上下文 |
| `core/event/InstructionKeyFrameExecutor.java` | 移至 `eventframe/` |
| `core/event/KeyframeEvent.java` | 关键帧事件基类，移除 |
| `core/event/ParticleKeyFrameEvent.java` | 粒子关键帧事件，移除 |
| `core/event/SoundKeyframeEvent.java` | 声音关键帧事件，移除 |
| `core/event/predicate/AnimationEvent.java` | 移至 `event/AnimationEvent.java` |
| `core/keyframe/AnimationPoint.java` | 移至 `point/` |
| `core/keyframe/AnimationPointQueue.java` | 移除（队列机制改为骨骼队列内嵌） |
| `core/keyframe/KeyFramePoint.java` | 移至 `point/` |
| `core/keyframe/TransitionPoint.java` | 拆分为 Beginning/Ending |
| `core/builder/ILoopType.java` | 接口→枚举 LoopType |
| `core/molang/value/DoubleValue.java` | 改为 FloatValue |
| `core/molang/storage/IForeignVariableStorage.java` | 改名为 IContextVariableStorage |
| `core/molang/storage/VariableStorage.java` | 替换为 MolangMemory |
| `core/processor/IBone.java` | 替换为 IBoneView |
| `core/processor/ILocationBone.java` | 合并到 IBoneView |
| `core/processor/PointData.java` | 移除 |
| `core/util/Memoizer.java` | 移除 |
| `util/RenderUtils.java` | 移除（功能被GeoModelStateExtractor替代） |

### `geo/` 子包变化

#### 新增文件

| 文件路径 | 说明 |
|---|---|
| `geo/GeckoRenderData.java` | 渲染数据容器（含modelState, texture, color, overlayUV, transform, ctx） |
| `geo/RenderContext.java` | record: level, irisShadow, inventory, offScreen, immutable |
| `geo/RenderContextManager.java` | 全局渲染上下文管理器（inventory/level/offscreen状态） |
| `geo/animated/GeoModelState.java` | 模型状态（扁平化骨骼变换矩阵数组+渲染骨骼索引） |
| `geo/animated/GeoModelStateExtractor.java` | 从AnimatedGeoModel提取骨骼变换到GeoModelState |
| `geo/render/built/GeoLocatorType.java` | 定位器类型（LEFT_HAND, RIGHT_HAND, BACKPACK, HEAD等） |

#### 移除文件

| 文件路径 | 说明 |
|---|---|
| `geo/IGeoEntity.java` | 实体接口，被GeckoRenderData替代 |
| `geo/IGeoEntityRenderer.java` | 渲染器接口，合并到IGeoRenderer |
| `geo/animated/ILocationModel.java` | 定位模型接口，用GeoLocatorType替代 |
| `geo/render/IGeoBuilder.java` | Builder接口，移除 |
| `geo/render/built/GeoCube.java` | Cube类，合并到GeoMesh中 |
| `geo/render/built/GeoQuad.java` | Quad类，移除 |
| `geo/render/built/GeoVertex.java` | Vertex类，移除 |

### `sound/` 包（全新）

| 文件路径 | 说明 |
|---|---|
| `sound/data/SoundData.java` | 声音数据 |
| `sound/data/SoundFormat.java` | 声音格式 |
| `sound/instance/CustomSoundInstance.java` | 自定义声音实例 |
| `sound/instance/IStoppableSound.java` | 可停止声音接口 |
| `sound/instance/MinecraftSoundInstance.java` | Minecraft声音实例封装 |
| `sound/instance/SoundInstanceManager.java` | 声音实例管理器 |
| `sound/stream/AudioStreamProvider.java` | 音频流提供者 |
| `sound/stream/CustomAudioStream.java` | 自定义音频流 |
| `sound/stream/LoopingAudioStream.java` | 循环音频流 |
| `sound/stream/VorbisAudioStream.java` | Vorbis音频流 |

### `resource/` 包变化

| 文件 | 状态 | 说明 |
|---|---|---|
| `ConditionManager.java` | **新增** | 条件管理器（swing, use, hold, tac, armor, vehicle, passenger, chair） |
| `GeckoAsset.java` | **新增** | record: sounds, userFunctions, eventHandlers |
| `GeckoContainer.java` | **新增** | record: model, animation, controllerFactory, animControllers, conditionManager, texture, asset, type |
| `GeckoLibCache.java` | **保留** | 保留但有改动 |

### `file/` 包变化

| 文件 | 状态 | 说明 |
|---|---|---|
| `AnimationFile.java` | **改动** | record→class, animations是Object2ReferenceMap公开字段 |
| `AnimationControllerFile.java` | **新增** | 动画控制器文件 |

### `util/` 包变化

| 文件 | 状态 | 说明 |
|---|---|---|
| `OrderedSegmentSearcher.java` | **新增** | 有序分段搜索器（优化关键帧查找） |
| `RenderUtils.java` | **移除** | 骨骼矩阵辅助方法已内化到GeoModelStateExtractor |

---

## GeckoLib API Changes

### IGeoRenderer 接口变化

```java
// OLD
public interface IGeoRenderer<T> {
    MultiBufferSource getCurrentRTB();
    void setCurrentRTB(MultiBufferSource bufferSource);
    ResourceLocation getTextureLocation(T animatable);
    default void render(AnimatedGeoModel model, T animatable, float partialTick, RenderType type,
                        PoseStack poseStack, @Nullable MultiBufferSource bufferSource,
                        @Nullable VertexConsumer buffer, int packedLight, int packedOverlay,
                        float red, float green, float blue, float alpha)
    default void renderRecursively(AnimatedGeoBone bone, ...)
    default void renderCubesOfBone(AnimatedGeoBone bone, PoseStack poseStack, VertexConsumer buffer, ...)
    default void renderEarly(T animatable, PoseStack poseStack, float partialTick, ...)
    default void renderLate(T animatable, PoseStack poseStack, float partialTick, ...)
    default RenderType getRenderType(T animatable, float partialTick, PoseStack poseStack, ...)
    default Color getRenderColor(T animatable, float partialTick, ...)
    default float getWidthScale(T animatable)
    default float getHeightScale(T entity)
}

// NEW
public interface IGeoRenderer<TState extends EntityRenderState, TData extends GeckoRenderData> {
    // 移除: getCurrentRTB, setCurrentRTB, getTextureLocation
    // 移除: render, renderRecursively, renderChildBones
    // 移除: renderEarly, renderLate
    // 移除: getRenderColor, getWidthScale, getHeightScale
    
    default void preSubmit(TState state, TData data, RenderContext ctx,
                          PoseStack poseStack, SubmitNodeCollector submitNodeCollector)
    default void submit(TState state, TData data, RenderContext ctx,
                       PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType type)
    default void renderCubesOfBone(GeoBone bone, PoseStack.Pose poseState,
                                   VertexConsumer buffer, TState state, GeckoRenderData data)
    @Nullable default RenderType getRenderType(GeckoRenderData data, boolean visible, boolean glowing)
}
```

**关键迁移要点**:
1. 泛型从 `T` (任意实体) → `TState extends EntityRenderState, TData extends GeckoRenderData`
2. `render()` 方法拆分为 `preSubmit()` + `submit()`，适配NeoForge 26.1的SubmitNodeCollector渲染管线
3. 颜色从 `float red, green, blue, alpha` 四个参数改为 `data.color` (int packed)
4. `packedLight` 从参数传入改为从 `state.lightCoords` 获取
5. `packedOverlay` 从参数传入改为从 `data.overlayUV` 获取
6. 骨骼变换从运行时 `RenderUtils.prepMatrixForBone()` 改为预计算的 `data.modelState` 中获取
7. `renderCubesOfBone` 参数 `PoseStack` → `PoseStack.Pose`（预计算的姿态）
8. `getRenderType` 现在只需要 RenderData + 可见/发光标志

### GeoReplacedEntityRenderer 变化

```java
// OLD
public abstract class GeoReplacedEntityRenderer<T extends LivingEntity, E extends AnimatableEntity<T>>
        extends LivingEntityRenderer<T, HumanoidModel<T>> implements IGeoRenderer<T>
// 内部持有: currentAnimatable, widthScale, heightScale, dispatchedMat, renderEarlyMat, rtb

// NEW
public abstract class GeoReplacedEntityRenderer<TEntity extends LivingEntity, TState extends LivingEntityRenderState, TData extends GeckoRenderData>
        extends LivingEntityRenderer<TEntity, TState, EntityModel<TState>> implements IGeoRenderer<TState, TData>
// 内部持有: layerRenderers (List<GeoLayerRenderer>), currentModelRenderCycle
```

**关键变化**:
1. `render()` 重写改为 `submit()` 重写（适配新渲染管线）
2. 不再持有 `currentAnimatable`、`widthScale`/`heightScale` 等渲染状态
3. 这些状态被移入 `GeckoRenderData` 和 `RenderContext`
4. 使用 `getGeckoRenderData(state)` 抽象方法获取渲染数据

### AnimationController → IAnimationController

OLD的 `AnimationController<T>` 是一个完整的单体类，包含：
- 动画队列管理
- 过渡处理（单一过渡曲线）
- 骨骼动画队列
- 关键帧处理
- 声音/粒子监听器

NEW拆分为接口和多个实现：
- `IAnimationController<T>` — 统一接口（getName, getStateName, updateModel, process, visitBoneAnimationQueues）
- `BedrockAnimationController<T>` — 支持JSON动画控制器、状态机、层级
- `CodedAnimationController<T>` — 代码驱动的动画控制器（兼容旧谓词模式）
- `AnimationPlayer` — 独立动画播放器

**CodedAnimationController** 是OLD `AnimationController` 最接近的替换，但：
- `updateModel()` 和 `process()` 分离
- 不需要 `ExpressionEvaluator<AnimationContext>` 作为process参数，改为 `ExpressionEvaluator<MolangContext>`
- 移除了 `shouldResetTick`, `needsAnimationReload`, `justStopped` 等内部状态到AnimationPlayer

### ILoopType → LoopType

```java
// OLD: 接口 + 内部枚举
public interface ILoopType {
    boolean isRepeatingAfterEnd();
    enum EDefaultLoopTypes implements ILoopType { LOOP, PLAY_ONCE, HOLD_ON_LAST_FRAME; }
}
// NEW: 直接枚举
public enum LoopType { LOOP, PLAY_ONCE, HOLD_ON_LAST_FRAME; }
```

### AnimationFile 变化

```java
// OLD: record
public record AnimationFile(Map<String, Animation> animations)

// NEW: class
public class AnimationFile {
    private final Object2ReferenceMap<String, Animation> animations = new Object2ReferenceOpenHashMap<>();
    public Object2ReferenceMap<String, Animation> animations() { return this.animations; }
}
```

### GeoBone 变化

```java
// OLD
public class GeoBone {
    GeoBone parent;  // mutable
    List<GeoBone> children;  // hierarchical tree
    String name;
    Vector3f pivot, rotation;
    GeoMesh cubes;
    Boolean mirror, inflate, dontRender, reset;
    BoneSnapshot initialSnapshot;
    boolean glow;
}

// NEW
public class GeoBone {
    // 移除: parent, children (树结构)
    // 移除: mirror, inflate, dontRender, reset, initialSnapshot
    String name;
    int pooledName;  // 新增: 字符串池化ID
    Vector3f pivot, initialRotation;  // rotation→initialRotation
    GeoMesh cubes;
    int traverseOrder;  // 新增: 遍历顺序（扁平化索引）
    int depth;  // 新增: 深度
    int subTreeSize;  // 新增: 子树大小
    @Nullable GeoLocatorType locatorType;  // 新增: 定位器类型
    boolean glow;
}
```

### AnimatedGeoBone 变化

```java
// OLD: implements IBone
// 包含: children (List<AnimatedGeoBone>), isHidden, areCubesHidden, hideChildBonesToo
// 方法: getInitialSnapshot(), getName(), setRotationX/Y/Z(), setPositionX/Y/Z(),
//       setScaleX/Y/Z(), isHidden(), setHidden(), cubesAreHidden(), childBonesAreHiddenToo()

// NEW: implements IBoneView
// 移除: children, isHidden, hideChildBonesToo
// 新增: tracking, globalPivot
// 方法: getPooledName(), getPivot(), getRotation(), getScale(), getPosition(),
//       areCubesHidden(), areChildrenHidden(), setTracking(), getGlobalPivot()
```

### AnimatedGeoModel 变化

```java
// OLD: implements ILocationModel
// 持有: geoModel, topLevelBones, bones (Map<String, AnimatedGeoBone>)
// 定位器通过字符串解析: getLocatorHierarchy("LeftHandLocator") → List<AnimatedGeoBone>

// NEW: 不实现任何定位接口
// 持有: geoModel, flatBoneList (ReferenceArrayList), boneMap (Int2ReferenceOpenHashMap)
//      locatorMap (ReferenceArrayList<ReferenceArrayList<AnimatedGeoBone>>)
// 定位器通过类型查找: locatorGroup(GeoLocatorType.LEFT_HAND) → ReferenceArrayList<AnimatedGeoBone>
```

---

## MoLang Changes

### 数值类型变更: Double → Float

全局从 `double` 切换到 `float`:

```java
// OLD
public final class DoubleExpression implements Expression {
    private final double value;
    public double value() { return value; }
    public <R> R visit(ExpressionVisitor<R> visitor) { return visitor.visitDouble(this); }
}

// NEW
public final class FloatExpression implements Expression {
    private final float value;
    public float value() { return value; }
    public <R> R visit(ExpressionVisitor<R> visitor) { return visitor.visitFloat(this); }
}
```

### ExpressionVisitor 变化

新增/改动的访问方法：

| 方法 | OLD | NEW |
|---|---|---|
| 数值访问 | `visitDouble(DoubleExpression)` | `visitFloat(FloatExpression)` |
| 数组访问 | — | `visitArray(ArrayAccessExpression)` *新增* |
| 作用域函数 | `buildExecutionScopeFunction(ExecutionScopeExpression)` | **已移除** |

### IValue 变化 (geckolib3.core.molang.value)

```java
// OLD
public interface IValue {
    default double evalAsDouble(ExpressionEvaluator<?> evaluator)
    default boolean evalAsBoolean(ExpressionEvaluator<?> evaluator)
    Object evalUnsafe(ExpressionEvaluator<?> evaluator) throws Exception;
}

// NEW
public interface IValue {
    default float evalAsFloat(ExpressionEvaluator<?> evaluator)   // double→float
    default int evalAsInt(ExpressionEvaluator<?> evaluator)       // 新增
    default boolean evalAsBoolean(ExpressionEvaluator<?> evaluator)
    default Object eval(ExpressionEvaluator<?> evaluator)         // 新增（带try-catch）
    Object evalUnsafe(ExpressionEvaluator<?> evaluator)
}
```

### MolangParser (geckolib3.core.molang) 变化

OLD的 `MolangParser` 位于 `core/molang/`，NEW保留但内部有变化：
- 支持 `array[index]` 语法（新增 `ArrayAccessExpression`）
- 解析器错误处理改进

### Runtime存储变化

```java
// OLD
IForeignVariableStorage { Object getPublic(int name); }
IScopedVariableStorage { ... }
ITempVariableStorage { ... }
VariableStorage implements IScopedVariableStorage, ITempVariableStorage

// NEW
IContextVariableStorage { ... }                                      // 取代 IForeignVariableStorage
MolangMemory implements IScopedVariableStorage                       // 取代 VariableStorage
    + StackMemory getStackMemory()                                   // 新增栈内存
StackMemory implements ITempVariableStorage                          // 新增独立栈内存
```

### MolangContext (新增)

`geckolib3.core.molang.context.MolangContext` 是新的统一Molang上下文，整合了：
- `AnimationContext` — 动画相关查询变量
- `ControllerContext` — 动画控制器相关变量
- `SoundInstanceManager` — 声音管理
- `RandomSource` — 随机源
- `MolangMemory` — 内存/变量
- `DebugSource` — 调试信息

### Molang目录结构（独立包 `molang/`）

独立Molang包（`com.github.tartaricacid.touhoulittlemaid.molang`）的结构变化较小：

| 变化 | 文件 | 说明 |
|---|---|---|
| **新增** | `parser/ast/ArrayAccessExpression.java` | 数组访问AST节点 |
| **替换** | `parser/ast/FloatExpression.java` | 替换 `DoubleExpression.java` |
| **改动** | `parser/ast/ExpressionVisitor.java` | `visitDouble`→`visitFloat`, 新增`visitArray`, 移除`buildExecutionScopeFunction` |
| **保留** | `lexer/` 全部 | 词法分析器无变化 |
| **保留** | `runtime/` 全部 | 运行时绑定无变化 |

---

## Render Pipeline Changes

### 整体渲染流程对比

```
OLD:
render(model, animatable, partialTick, type, poseStack, bufferSource, buffer, packedLight, packedOverlay, r, g, b, a)
├── setCurrentRTB(bufferSource)
├── renderEarly(animatable, poseStack, partialTick, bufferSource, buffer, ...)
│   └── poseStack.scale(widthScale, heightScale, widthScale)  [if INITIAL cycle]
├── buffer = bufferSource.getBuffer(type)
├── renderLate(animatable, poseStack, partialTick, bufferSource, buffer, ...)
└── for each topLevelBone:
    └── renderRecursively(bone, poseStack, buffer, ...)
        ├── poseStack.pushPose()
        ├── RenderUtils.prepMatrixForBone(poseStack, bone)  // 运行时计算变换
        ├── renderCubesOfBone(bone, poseStack, buffer, ...)  // 逐面手动emit顶点
        ├── renderChildBones(bone, poseStack, buffer, ...)
        └── poseStack.popPose()

NEW:
preSubmit(state, data, ctx, poseStack, submitNodeCollector)
├── if INITIAL cycle: poseStack.mulPose(data.transform)  // scale等由外部处理
└── [no other action]

submit(state, data, ctx, poseStack, submitNodeCollector, renderType)
├── submitNodeCollector.submitCustomGeometry(poseStack, type, (pose, vertexConsumer) -> {
│   ├── if outlined: handle outline buffer
│   └── data.modelState.visitRenderBones(pose, (bone, poseState) -> {
│       └── renderCubesOfBone(bone, poseState, vertexConsumer, state, data)
│   })
│   └── data.close()  // 归还数据
└── })

GeoModelStateExtractor.extract(model, state)  // 在submit前执行，预计算所有骨骼变换矩阵
├── for each bone in flatBoneList:
│   ├── manage poseStack depth
│   ├── extractBone(poseStack, bone, state)
│   └── if cubes: state.renderBoneIndices.add(boneIndex)
```

### 关键差异总结

1. **PoseStack使用**: OLD在渲染时操作PoseStack；NEW在`GeoModelStateExtractor`中预计算变换矩阵存入`GeoModelState.data[]`数组
2. **骨骼遍历**: OLD递归遍历树结构；NEW通过`flatBoneList` + `renderBoneIndices` 扁平化数组迭代
3. **顶点emit**: OLD在`IGeoRenderer.renderCubesOfBone`中手动调用`buffer.addVertex()` 6个面各4个顶点；NEW同样手动emit但颜色改为int packed
4. **宽高缩放**: OLD在`renderEarly`中`poseStack.scale()`；NEW在`GeckoRenderData.transform`中预计算
5. **定位器**: OLD通过`ILocationModel`接口字符串匹配查找骨骼；NEW通过`GeoLocatorType` + `visitLocatorGroup()` 类型化查找
6. **Sodium/Embeddium兼容**: OLD在`IGeoRenderer.renderRecursively`中有显式兼容调用；NEW中Sodium/Embeddium兼容已移除（需确认是否正确）

### 性能优化方向

1. **扁平化骨骼**: GeoModel使用flatBoneList + traverseOrder替代递归树，减少函数调用开销
2. **变换预计算**: GeoModelStateExtractor在CPU端批量计算所有骨骼变换，而不是GPU渲染时逐个计算
3. **位运算可见性**: 使用`(scaleX==0?0:1)+(scaleY==0?0:1)+(scaleZ==0?0:1)<2` 位运算快速判断
4. **字符串池化**: GeoBone使用pooledName (int) 替代 name (String) 进行查找
5. **OrderedSegmentSearcher**: 优化关键帧片段查找，利用有序性从上次位置继续搜索
```

