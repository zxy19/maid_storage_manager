# Vanilla API Migration — Client, Rendering, Network, Text (1.20.1 → 1.21.1)

> **重要说明**：本项目的网络栈在两个版本中已预先现代化为 `CustomPacketPayload + StreamCodec` 模式。
> OLD 和 NEW 的 `NetworkHandler.java` 几乎完全相同。此文档记录基于两个项目实际代码的比较。

---

## 1. Network API

### 1.1 两个版本已统一使用现代 API

本项目在 1.20.1 (Forge) 版本中已经使用了 NeoForge 1.21+ 的现代网络 API，因此网络栈在本次迁移中**几乎无变化**。以下列出 API 对比作为参考：

```java
// === 注册：两个版本相同 ===
public static void registerPacket(final RegisterPayloadHandlersEvent event) {
    final PayloadRegistrar registrar = event.registrar("1.0.0");

    // playToServer: 客户端→服务端
    registrar.playToServer(MaidModelPackage.TYPE, MaidModelPackage.STREAM_CODEC, MaidModelPackage::handle);
    // playToClient: 服务端→客户端
    registrar.playToClient(SyncDataPackage.TYPE, SyncDataPackage.STREAM_CODEC, SyncDataPackage::handle);
}
```

| 概念 | 旧 Forge API (SimpleChannel) | 新 NeoForge API (本项目两版本共用) |
|------|---------------------------|----------------------------------|
| 注册事件 | `RegisterPayloadHandlersEvent` | 同 |
| 注册器 | `PayloadRegistrar` | 同 |
| 包类型 | 实现 `CustomPacketPayload` | 同 |
| 编解码器 | `StreamCodec<ByteBuf, T>` (静态字段 `STREAM_CODEC`) | 同 |
| 发送单玩家 | `PacketDistributor.sendToPlayer(player, payload)` | 同 |
| 发送附近实体 | `PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, toSend)` | 同 |
| 发送范围内 | `PacketDistributor.sendToPlayersNear(serverLevel, null, x, y, z, distance, toSend)` | 同 |

### 1.2 数据包格式模板

```java
// NEW — 标准 CustomPacketPayload 模式
public record SyncDataPackage(float power, int maidNum) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncDataPackage> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "sync_data"));
    public static final StreamCodec<ByteBuf, SyncDataPackage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,     SyncDataPackage::power,
            ByteBufCodecs.VAR_INT,   SyncDataPackage::maidNum,
            SyncDataPackage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDataPackage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> /* 处理 */);
        }
    }
}
```

### 1.3 发送辅助方法

```java
// 两个项目中的 NetworkHandler 静态辅助方法完全相同
public static void sendToClientPlayer(CustomPacketPayload payload, ServerPlayer player) {
    PacketDistributor.sendToPlayer(player, payload);
}

public static void sendToNearby(Entity entity, CustomPacketPayload toSend) {
    if (entity.level instanceof ServerLevel) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, toSend);
    }
}

public static void sendToNearby(Entity entity, CustomPacketPayload toSend, int distance) {
    if (entity.level instanceof ServerLevel serverLevel) {
        BlockPos pos = entity.blockPosition();
        PacketDistributor.sendToPlayersNear(serverLevel, null, pos.getX(), pos.getY(), pos.getZ(), distance, toSend);
    }
}
```

### 1.4 `RegistryFriendlyByteBuf` vs `FriendlyByteBuf`

| 项目 | 使用 |
|------|------|
| OLD | `import net.minecraft.network.FriendlyByteBuf` — 用于非包类（序列化工具等） |
| NEW | `import net.minecraft.network.RegistryFriendlyByteBuf` — 包编解码用 |
| 原因 | 1.21.1 的 `StreamCodec` 使用 `RegistryFriendlyByteBuf` 来支持注册表项编解码（如 `ItemStack`） |
| 注意 | `StreamCodec<ByteBuf, T>` 泛型声明仍为 `ByteBuf`，但运行时传入 `RegistryFriendlyByteBuf` |

### 1.5 OLD 独有的包

OLD 包含 2 个 YSM 模组集成包，NEW 已移除：
- `YsmMaidModelPackage` (ToServer)
- `SyncYsmMaidDataPackage` (ToClient)

### 1.6 NEW 新增：客户端代理类

NEW 新增 `network/client/` 子包，包含 20 个客户端代理类，将客户端处理逻辑从消息类中分离。例如：

```java
// OLD: 在消息类中直接处理（含 @OnlyIn(Dist.CLIENT)）
@OnlyIn(Dist.CLIENT)
private static void handleData(SyncDataPackage message) {
    Minecraft mc = Minecraft.getInstance();
    mc.player.setData(InitDataAttachment.POWER_NUM, new PowerAttachment(message.power));
}

// NEW: 委托给 Proxy 类
public static void handle(SyncDataPackage message, IPayloadContext context) {
    if (context.flow().isClientbound()) {
        context.enqueueWork(() -> SyncDataPackageProxy.handle(message));
    }
}
```

---

## 2. Rendering API

### 2.1 EntityRenderState 系统（重大变更）

1.21.1 引入了 `EntityRenderState` 模式，实体渲染数据被提取到轻量级状态对象中：

```java
// OLD: EntityRenderer 单泛型
public class EntityMaidRenderer extends EntityRenderer<EntityMaid> { ... }

// NEW: EntityRenderer 双泛型（实体 + 渲染状态）
public class EntityMaidRenderer extends MobRenderer<EntityMaid, EntityMaidRenderState, EntityMaidModel> {
    @Override
    public EntityMaidRenderState createRenderState() {
        return new EntityMaidRenderState();
    }
}
```

| 渲染器基类 | OLD 泛型 | NEW 泛型 |
|-----------|---------|----------|
| `EntityRenderer<T>` | `<T extends Entity>` | `<T extends Entity, S extends EntityRenderState>` |
| `LivingEntityRenderer` | `<T extends LivingEntity, M extends EntityModel<T>>` | `<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<S>>` |
| `MobRenderer` | `<T extends Mob, M extends EntityModel<T>>` | `<T extends Mob, S extends MobRenderState, M extends EntityModel<S>>` |
| `HumanoidMobRenderer` | `<T extends Mob, M extends HumanoidModel<T>>` | `<T extends Mob, S extends HumanoidMobRenderState, M extends HumanoidModel<S>>` |
| `BlockEntityRenderer<T>` | `<T extends BlockEntity>` | `<T extends BlockEntity, S extends BlockEntityRenderState>` |

**项目新增的渲染状态类：**

| 渲染状态类 | 父类 |
|-----------|------|
| `EntityMaidRenderState` | `LivingEntityRenderState` |
| `EntityChairRenderState` | `LivingEntityRenderState` |
| `EntityFairyRenderState` | `LivingEntityRenderState` |
| `EntityBroomRenderState` | `LivingEntityRenderState` |
| `EntityTombstoneRenderState` | `EntityRenderState` |
| `EntityBoxRenderState` | `EntityRenderState` |
| `EntityPowerPointRenderState` | `EntityRenderState` |
| `EntityDanmakuRenderState` | `EntityRenderState` |
| `MaidFishingHookRenderState` | `EntityRenderState` |
| `AltarRenderState` | `BlockEntityRenderState` |
| `CChessRenderState` | `BlockEntityRenderState` |

### 2.2 EntityRenderers / BlockEntityRenderers 注册

```java
// OLD: 使用 TileEntityXxx.TYPE 静态字段
BlockEntityRenderers.register(TileEntityAltar.TYPE, TileEntityAltarRenderer::new);
BlockEntityRenderers.register(TileEntityStatue.TYPE, TileEntityStatueRenderer::new);

// NEW: 使用 InitBlocks.XXX_TE.get() 从 DeferredRegister 获取
BlockEntityRenderers.register(InitBlocks.ALTAR_TE.get(), TileEntityAltarRenderer::new);
BlockEntityRenderers.register(InitBlocks.STATUE_TE.get(), TileEntityStatueRenderer::new);
```

### 2.3 PoseStack 使用

PoseStack 类名未变，导入路径未变 (`com.mojang.blaze3d.vertex.PoseStack`)，但使用方式有变化：

| 变更 | OLD | NEW |
|------|-----|-----|
| 骨骼变换 | `RenderUtils.prepMatrixForBone(poseStack, bone)` 运行时计算 | `GeoModelStateExtractor` 预计算到 `PoseStack.Pose` 数组 |
| `renderCubesOfBone` | `(bone, poseStack, buffer, ...)` → PoseStack 整体 | `(bone, poseState, buffer, ...)` → PoseStack.Pose（单帧态） |
| 渲染提交 | 裸 `buffer.addVertex()` | `submitNodeCollector.submitCustomGeometry(poseStack, type, callback)` |

### 2.4 RenderType

`RenderType` 基本模式不变，但 GeckoLib 渲染管线中 `getRenderType` 签名简化：

```java
// OLD: getRenderType(T animatable, float partialTick, PoseStack poseStack, MultiBufferSource, VertexConsumer, int packedLight, ...)
// NEW: getRenderType(GeckoRenderData data, boolean visible, boolean glowing)
@Nullable
default RenderType getRenderType(GeckoRenderData data, boolean visible, boolean glowing)
```

### 2.5 VertexConsumer

使用方式不变，但在 GeckoLib 渲染中颜色参数从分离分量变为 packed int：

```java
// OLD: renderCubesOfBone(..., float red, float green, float blue, float alpha)
// NEW: data.color 为 int packed ARGB 颜色，由 data 提供
```

### 2.6 SubmitNodeCollector（新增）

1.21.1 新增 `SubmitNodeCollector`，用于收集所有渲染节点后批量提交：

```java
// 在 render() 中
@Override
public void render(S entity, S state, PoseStack poseStack,
                   MultiBufferSource bufferSource, int packedLight) {
    SubmitNodeCollector collector = SubmitNodeCollector.get();
    this.geckoRenderer.preSubmit(state, data, ctx, poseStack, collector);
    this.geckoRenderer.submit(state, data, ctx, poseStack, collector, renderType);
}
```

### 2.7 MultiBufferSource

`MultiBufferSource` 使用方式基本不变，但不再作为渲染器的内部字段缓存（OLD 中有 `setCurrentRTB` / `getCurrentRTB`，NEW 中移除）。

### 2.8 RenderSystem

`RenderSystem` 静态方法调用减少，大部分功能被 `SubmitNodeCollector` 管线替代。仍保留的用法包括：
- `RenderSystem.enableBlend()`
- `RenderSystem.defaultBlendFunc()`
- `RenderSystem.setShaderTexture()`

### 2.9 HumanoidModel / EntityModel

| OLD | NEW |
|-----|-----|
| `EntityModel<T extends LivingEntity>` | `EntityModel<T extends EntityRenderState>` |
| `HumanoidModel<T extends LivingEntity>` | `HumanoidModel<T extends HumanoidRenderState>` |

---

## 3. GUI System

### 3.1 Screen 类

Screen 类构造函数和基本生命周期未变：

```java
// 两者相同
public class MyScreen extends Screen {
    public MyScreen(Component title) {
        super(title);
    }
}
```

使用 `@EventBusSubscriber` 绑定至 `RegisterMenuScreensEvent` 的模式也相同：

```java
@EventBusSubscriber(value = Dist.CLIENT)
public final class InitContainerGui {
    @SubscribeEvent
    public static void clientSetup(RegisterMenuScreensEvent event) {
        event.register(InitContainer.MAID_CONFIG_CONTAINER.get(), MaidConfigContainerGui::new);
    }
}
```

### 3.2 AbstractContainerScreen

`AbstractContainerScreen` 泛型不变，仍为 `<T extends AbstractContainerMenu>`。容器屏幕的实现模式完全相同。

### 3.3 MenuType 注册

`MenuType` 注册 API 不变，通过 `DeferredRegister` 注册。项目中使用 `IForgeMenuType.create()` 或直接构造函数创建。

### 3.4 Tooltip System

`appendHoverText` 方法签名有变化——新增 `TooltipContext` 和 `TooltipDisplay` 参数：

```java
// OLD (1.20.1)
public void appendHoverText(ItemStack stack, @Nullable Level level,
                            List<Component> tooltip, TooltipFlag flag)

// NEW (1.21.1)
public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext context,
                            TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
```

**关键变化：**
- `Level` → `Item.TooltipContext`（更窄的接口，不直接暴露整个 Level）
- `List<Component>` → `Consumer<Component>` + `TooltipDisplay`（延迟构建提示文本，性能优化）

项目在各物品类中已经适配签名：

```java
@Override  // NEW 签名
public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext worldIn,
                            TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {
    tooltip.accept(Component.translatable("...").withStyle(ChatFormatting.GRAY));
}
```

### 3.5 已移除的 GUI 相关文件

| 文件 | 原因 |
|------|------|
| `gui/entity/cache/` (整包子包) | 缓存图标系统重构，缓存现在由 `CustomPackTextureLoader` 管理 |
| `init/InitSpecialItemRender.java` | 不再需要 |
| 4 个特殊背包 GUI (CraftingTable、EnderChest、Furnace、Tank) | 对应背包系统移除 |

---

## 4. Text Component System

### 4.1 Component 接口

`Component` 接口未变，仍是 `net.minecraft.network.chat.Component`。核心子类型变化：

| 子类型 | 状态 |
|--------|------|
| `MutableComponent` | **仍存在**，作为 `Component` 的可变子接口 |
| `TextComponent` | 仍存在 |
| `TranslatableComponent` | 仍存在 |
| `CommonComponents` | 新增 `SPACE` 等常量 |

**注意**：`MutableComponent` 在 1.21.1 中**并未被移除**。项目中仍广泛使用：
```java
MutableComponent title = Component.translatable("...").withStyle(...);
```

### 4.2 Style 变化

`Style` 类的方法签名基本不变，但实际 pattern 从 Builder/Direct 调用变为 `withXxx` 链式：

```java
// 两个版本均可使用的写法
MutableComponent msg = Component.translatable("key")
    .withStyle(style -> style
        .withColor(ChatFormatting.GOLD)
        .withBold(true)
        .withUnderlined(true)
        .withClickEvent(clickEvent)
        .withHoverEvent(hoverEvent));
```

### 4.3 ClickEvent — 构造函数重写

**这是 1.21.1 最大的文本系统变化。** 旧的枚举驱动构造函数被替换为子类 + 静态工厂。

```java
// OLD (1.20.1)
ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://example.com");
ClickEvent runCmd = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/say hello");

// NEW (1.21.1)
ClickEvent clickEvent = new ClickEvent.OpenUrl(URI.create("https://example.com"));
ClickEvent runCmd = new ClickEvent.RunCommand("/say hello");
```

| OLD | NEW |
|-----|-----|
| `new ClickEvent(Action.OPEN_URL, urlString)` | `new ClickEvent.OpenUrl(URI)` |
| `new ClickEvent(Action.RUN_COMMAND, command)` | `new ClickEvent.RunCommand(String)` |
| `new ClickEvent(Action.SUGGEST_COMMAND, command)` | `new ClickEvent.SuggestCommand(String)` |
| `new ClickEvent(Action.CHANGE_PAGE, page)` | `new ClickEvent.ChangePage(int)` |
| `new ClickEvent(Action.COPY_TO_CLIPBOARD, text)` | `new ClickEvent.CopyToClipboard(String)` |

### 4.4 HoverEvent — 构造函数重写

与 ClickEvent 类似，HoverEvent 也从枚举驱动变为子类 + 静态工厂：

```java
// OLD (1.20.1)
HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, component);

// NEW (1.21.1)
HoverEvent hoverEvent = new HoverEvent.ShowText(component);
```

| OLD | NEW |
|-----|-----|
| `new HoverEvent(Action.SHOW_TEXT, Component)` | `new HoverEvent.ShowText(Component)` |
| `new HoverEvent(Action.SHOW_ITEM, HoverEvent.ItemStackInfo)` | `new HoverEvent.ShowItem(HoverEvent.ItemStackInfo)` |
| `new HoverEvent(Action.SHOW_ENTITY, HoverEvent.EntityTooltipInfo)` | `new HoverEvent.ShowEntity(HoverEvent.EntityTooltipInfo)` |

### 4.5 ChatFormatting

`ChatFormatting` 未变，包名仍为 `net.minecraft.ChatFormatting`。`withColor(ChatFormatting.GOLD)` 等用法完全相同。

### 4.6 CommonComponents

1.21.1 新增 `net.minecraft.network.chat.CommonComponents`，提供常用 Component 常量：

```java
CommonComponents.SPACE          // 空格
CommonComponents.NEW_LINE       // 换行
CommonComponents.EMPTY          // 空
CommonComponents.joinLines(...)  // 用换行符连接多个 Component
```

---

## 5. Client Events

### 5.1 EventBusSubscriber 注解变化

```java
// OLD: 需要显式指定 bus = Bus.MOD
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = MOD_ID)

// NEW: bus 默认为 MOD，可省略
@EventBusSubscriber(value = Dist.CLIENT, modid = MOD_ID)
// 或
@EventBusSubscriber(value = Dist.CLIENT)
```

### 5.2 事件处理分工变化

| 事件 | OLD (在 ClientSetupEvent 中) | NEW (独立类) |
|------|---------------------------|-------------|
| `RegisterKeyMappingsEvent` | `onRegisterKeyMappings()` | `KeyMappingRegister.onRegisterKeyMappings()` |
| `AddPackFindersEvent` | `onAddPackFinders()` | **移除**（LegacyPackRepositorySource 删除） |
| `RegisterClientReloadListenersEvent` | `onRegisterClientReloadListeners()` | `ClientReloadListenerRegistry` |
| `RegisterGuiLayersEvent` | `onRegisterGuiLayers()` | 仍在 `ClientSetupEvent` 中 |

### 5.3 新增 / 移除的客户端事件类

| 文件 | 状态 |
|------|------|
| `ReloadResourceEvent.java` | **移除** → 替换为 `CustomPackReloadListener` |
| `SpecialMaidRenderEvent.java` | **移除** |
| `ClientRecipeEvent.java` | **新增** |
| `RegisterSpecialModelEvent.java` | **新增** |
| `UseNameTagEvent.java` | **新增** |

### 5.4 ClientTickEvent

用法不变，仍为 `net.neoforged.neoforge.client.event.ClientTickEvent`。

---

## 6. Input System

### 6.1 KeyMapping 变化

#### 分类（Category）

```java
// OLD: 字符串分类
new KeyMapping("desc", KeyConflictContext.IN_GAME, KeyModifier.NONE,
    InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X,
    "key.category.touhou_little_maid");

// NEW: KeyMapping.Category 对象（在 CategoryRegister 中定义并注册）
public static final KeyMapping.Category MAID_CATEGORY = new KeyMapping.Category(
    Identifier.fromNamespaceAndPath(MOD_ID, "main")
);
event.registerCategory(MAID_CATEGORY);

new KeyMapping("desc", KeyConflictContext.IN_GAME, KeyModifier.NONE,
    InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X,
    MAID_CATEGORY);
```

#### matches 方法

```java
// OLD
STT_CHAT_KEY.matches(event.getKey(), event.getScanCode())
    && STT_CHAT_KEY.getKeyModifier().equals(KeyModifier.getActiveModifier());

// NEW (getKeyEvent 被标记为 @removal，但当前仍可用)
STT_CHAT_KEY.matches(event.getKeyEvent());  // @SuppressWarnings("removal")
```

### 6.2 Mouse Input

`mouseHandler.isMouseGrabbed()` 等 API 不变，仍在 `net.minecraft.client.Minecraft` 中。

### 6.3 InputEvent

`InputEvent.Key` 类不变，通过 `event.getAction()`, `event.getKey()`, `event.getScanCode()` 访问。

---

## 7. Particle System

项目新增了 `client/particle/ParticleSpawner.java`，提供 MoLang 集成的粒子生成系统。原版 `ParticleEngine` 使用方式不变，但新系统中通过 MoLang 表达式调用。

---

## 8. Client Class Structure Changes

### 8.1 TouhouLittleMaidClient

两个版本的 `TouhouLittleMaidClient.java` 基本相同，差异在于：
- NEW 使用 `IConfigScreenFactory` 替代旧的配置屏幕扩展点
- OLD 没有 `KeyMapping.Category` 注册（在 `KeyMappingRegister` 中）

### 8.2 ClientSetupEvent 精简

OLD 的 `ClientSetupEvent` 包含 4 个事件处理方法和 5 个额外 import。NEW 精简为 2 个方法（`onClientSetup` + `onRegisterGuiLayers`），其余事件提取至独立类。

---

## 9. Complete Import Migration Table

| 旧导入 (OLD - 1.20.1) | 新导入 (NEW - 1.21.1) | 说明 |
|------------------------|------------------------|------|
| `net.minecraft.network.FriendlyByteBuf` | `net.minecraft.network.RegistryFriendlyByteBuf` | 网络编解码用（行内编解码仍用 `io.netty.buffer.ByteBuf`） |
| `net.minecraft.network.protocol.common.custom.CustomPacketPayload` | 同 | 两个版本均已使用 |
| `net.minecraft.network.codec.StreamCodec` | 同 | 两个版本均已使用 |
| `com.mojang.blaze3d.vertex.PoseStack` | 同 | 不变 |
| `com.mojang.blaze3d.vertex.VertexConsumer` | 同 | 不变 |
| `net.minecraft.client.renderer.MultiBufferSource` | 同 | 不变 |
| `net.minecraft.client.renderer.RenderType` | 同 | 不变 |
| `net.minecraft.client.gui.screens.Screen` | 同 (或 `net.minecraft.client.gui.screens.Screen`) | 不变 |
| `net.minecraft.client.gui.screens.inventory.AbstractContainerScreen` | 同 | 不变 |
| `net.minecraft.world.inventory.MenuType` | 同 | 不变 |
| `net.minecraft.network.chat.Component` | 同 | 不变 |
| `net.minecraft.network.chat.MutableComponent` | 同 | 仍存在 |
| `net.minecraft.network.chat.Style` | 同 | 不变 |
| `net.minecraft.network.chat.ClickEvent` | 同 | 但构造函数完全变化 |
| `net.minecraft.network.chat.HoverEvent` | 同 | 但构造函数完全变化 |
| `net.minecraft.ChatFormatting` | 同 | 不变 |
| `net.minecraft.client.Minecraft` | 同 | 不变 |
| `net.minecraft.client.renderer.entity.EntityRenderers` | 同 | 不变 |
| `net.minecraft.client.renderer.blockentity.BlockEntityRenderers` | 同 | 不变 |
| `net.minecraft.client.KeyMapping` | 同 | 不变 |
| `net.minecraft.world.item.TooltipFlag` | 同 | 不变 |
| `com.mojang.blaze3d.platform.InputConstants` | 同 | 不变 |
| `net.neoforged.fml.common.EventBusSubscriber` | 同 | 但 bus 参数可省略（默认 MOD） |
| `net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent` | 同 | 不变 |
| `net.neoforged.neoforge.client.event.RegisterGuiLayersEvent` | 同 | 不变 |
| `net.neoforged.neoforge.client.event.RegisterMenuScreensEvent` | 同 | 不变 |
| `net.neoforged.neoforge.client.event.EntityRenderersEvent` | 同 | 不变 |
| `net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent` | 同 | 不变 |
| `net.neoforged.neoforge.client.event.InputEvent` | 同 | 不变 |
| `net.neoforged.neoforge.client.settings.KeyConflictContext` | 同 | 不变 |
| `net.neoforged.neoforge.client.settings.KeyModifier` | 同 | 不变 |
| `net.neoforged.neoforge.client.event.RenderLivingEvent` | 同 | 注意：移至 `net.neoforged.neoforge.client.event` 包 |
| `net.neoforged.neoforge.client.event.ClientTickEvent` | 同 | 不变 |
| `net.neoforged.neoforge.event.AddPackFindersEvent` | **移除** | 项目不再订阅此事件 |
| `net.minecraft.server.packs.PackType` | 保留（资源包代码仍在） | 但 `AddPackFindersEvent` 处理代码已移除 |
| `net.neoforged.neoforge.network.PacketDistributor` | 同 | 不变 |
| `net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent` | 同 | 不变 |
| `net.neoforged.neoforge.network.handling.IPayloadContext` | 同 | 不变 |
| `net.minecraft.client.renderer.entity.HumanoidMobRenderer` | 同 (或 `net.minecraft.client.renderer.entity.HumanoidMobRenderer`) | 但泛型从 `<T extends Mob>` 变为 `<T extends Mob, S extends HumanoidMobRenderState, M extends HumanoidModel<S>>` |
| `net.minecraft.client.renderer.entity.MobRenderer` | 同 | 泛型增加渲染状态参数 |
| `net.minecraft.client.renderer.entity.state.EntityRenderState` | **新增** | 1.21.1 新引入 |
| `net.minecraft.client.renderer.entity.state.LivingEntityRenderState` | **新增** | 1.21.1 新引入 |
| `net.minecraft.client.model.EntityModel` | 同 | 但泛型从 `<T extends LivingEntity>` 变为 `<T extends EntityRenderState>` |
| `net.minecraft.client.model.HumanoidModel` | 同 | 泛型从 `<T extends LivingEntity>` 变为 `<T extends HumanoidRenderState>` |
| `net.minecraft.client.renderer.SubmitNodeCollector` | **新增** | 1.21.1 渲染管线新增 |
| `net.minecraft.client.renderer.state.level.CameraRenderState` | **新增** | 1.21.1 新增 |

---

## 10. Deleting Guide

### 从 1.20.1 迁移到 1.21.1 的检查清单

| 检查项 | 操作 |
|--------|------|
| `MutableComponent` | 保留使用，未移除 |
| `ClickEvent` 构造 | 从 `new ClickEvent(Action.XXX, String)` 改为 `new ClickEvent.XxxXxx(...)` |
| `HoverEvent` 构造 | 从 `new HoverEvent(Action.XXX, ...)` 改为 `new HoverEvent.XxxXxx(...)` |
| `appendHoverText` 签名 | 添加 `@Nullable Item.TooltipContext`, `TooltipDisplay` 参数；`List<Component>` 改为 `Consumer<Component>` |
| `EntityRenderer<T>` 泛型 | 改为 `EntityRenderer<T, S extends EntityRenderState>` |
| `EntityModel<T extends LivingEntity>` | 改为 `EntityModel<T extends EntityRenderState>` |
| `BlockEntityRenderer<T>` | 改为 `BlockEntityRenderer<T, S extends BlockEntityRenderState>` |
| `@EventBusSubscriber(bus = ...)` | 省略 `bus` 参数（默认 MOD） |
| `KeyMapping` 分类 | 从字符串改为 `KeyMapping.Category` 对象，需调用 `event.registerCategory()` |
| `matches()` 方法 | 旧双参数已被 `@removal` 标记，使用 `keyMapping.matches(event.getKeyEvent())` |
| `render()` 方法签名 | 注意 `EntityRenderer.render()` 是否添加了 `RenderState` 参数 |
| `FriendlyByteBuf` import | 改为 `RegistryFriendlyByteBuf`（但 `StreamCodec<ByteBuf, T>` 泛型声明不变） |
| `packedLight` 获取 | 从参数传入改为从 `state.lightCoords` 获取（新渲染管线） |
| `packedOverlay` 获取 | 从参数传入改为从 `data.overlayUV` 获取 |
| PoseStack 在渲染中的使用 | 改为通过 `SubmitNodeCollector.submitCustomGeometry()` 提交 |
