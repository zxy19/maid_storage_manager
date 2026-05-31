# Vanilla API Migration — Screen & Menu System (NeoForge 21.1 → 26.1)

> 对比 MC 1.21.1 (NeoForge 21.1) → MC 26.1 (NeoForge 26.1)，文档所有 Screen/Container 子类的变化。

---

## 1. Screen (GUI) System — 渲染架构重写

这是**最大的 API 变化**。整个渲染模型从立即模式改为提取模式，方法全部重命名。

### 1.1 核心渲染方法对照表

| 旧 API (MC 1.21.1) | 新 API (MC 26.1) | 说明 |
|---|---|---|
| `render(GuiGraphics, int mouseX, int mouseY, float partialTicks)` | `extractRenderState(GuiGraphicsExtractor, int mouseX, int mouseY, float partialTicks)` | 主渲染入口 |
| `renderBg(GuiGraphics, float partialTicks, int x, int y)` | `extractBackground(GuiGraphicsExtractor, int mouseX, int mouseY, float partialTick)` (AbstractContainerScreen) | 背景渲染，参数顺序改变 |
| `renderLabels(GuiGraphics, int x, int y)` | `extractLabels(GuiGraphicsExtractor, int x, int y)` | 标签渲染 |
| `renderTooltip(GuiGraphics, int x, int y)` | `extractTooltip(GuiGraphicsExtractor, int x, int y)` | Tooltip 渲染 |
| `renderTransparentBackground(GuiGraphics)` | `extractTransparentBackground(GuiGraphicsExtractor)` | 透明背景 |
| `renderContents(GuiGraphics, int, int, float)` | `extractContents(GuiGraphicsExtractor, int, int, float)` | 内容渲染（部分子类使用） |

> **注意**：新版本中 `extractBackground` 与 `extractContents` 可能在不同继承层次使用。需根据实际父类确定覆盖哪个方法。本 mod 的 `AbstractMaidContainerGui` 使用 `extractBackground`。

### 1.2 Screen 构造函数变化

| 旧 API | 新 API | 文件 |
|---|---|---|
| 在子类构造函数中设置 `this.imageWidth` / `this.imageHeight` 字段 | 通过 `super(container, inv, titleIn, imageWidth, imageHeight)` 传入 | `AbstractMaidContainerGui`, 所有 BackpackScreen |

```java
// OLD
public EmptyBackpackContainerScreen(EmptyBackpackContainer container, Inventory inv, Component titleIn) {
    super(container, inv, titleIn);
    this.imageHeight = 256;
    this.imageWidth = 256;
}

// NEW
public EmptyBackpackContainerScreen(EmptyBackpackContainer container, Inventory inv, Component titleIn) {
    super(container, inv, titleIn, 256, 256);
}
```

### 1.3 keyPressed 签名变化

| 旧 API | 新 API |
|---|---|
| `keyPressed(int keyCode, int scanCode, int modifiers)` | `keyPressed(KeyEvent event)` |
| `InputConstants.getKey(keyCode, scanCode)` | `InputConstants.getKey(event)` |

### 1.4 Button onClick 签名变化

| 旧 API | 新 API |
|---|---|
| `onClick(double mouseX, double mouseY)` | `onClick(MouseButtonEvent event, boolean doubleClick)` |

---

## 2. GuiGraphics → GuiGraphicsExtractor — 渲染基类替换

### 2.1 核心类型替换

| 旧类型 | 新类型 |
|---|---|
| `net.minecraft.client.gui.GuiGraphics` | `net.minecraft.client.gui.GuiGraphicsExtractor` |

### 2.2 渲染方法对照表

| 旧 GuiGraphics 方法 | 新 GuiGraphicsExtractor 方法 | 说明 |
|---|---|---|
| `graphics.blit(texture, x, y, u, v, w, h)` | `GuiTools.guiBlit(graphics, texture, x, y, u, v, w, h)` | blit 不再是实例方法 |
| `graphics.drawString(font, text, x, y, color)` | `graphics.text(font, text, x, y, color)` | 或 直接调用 |
| `graphics.drawString(font, text, x, y, color, shadow)` | `graphics.text(font, text, x, y, color, shadow)` | shadow 参数保留 |
| `graphics.drawCenteredString(font, text, x, y, color)` | `graphics.centeredText(font, text, x, y, color)` | 居中文本 |
| `graphics.renderItem(stack, x, y)` | `graphics.item(stack, x, y)` | 物品渲染 |
| `graphics.renderComponentTooltip(font, list, x, y)` | `graphics.setTooltipForNextFrame(font, list, Optional.empty(), x, y)` | Tooltip 设置 |
| `graphics.renderTooltip(font, component, x, y)` | `graphics.setTooltipForNextFrame(font, List.of(component), Optional.empty(), x, y)` | 单项 tooltip |
| `graphics.fill(x1, y1, x2, y2, color)` | `graphics.fill(x1, y1, x2, y2, color)` | 不变 |

### 2.3 Pose 栈方法变化

| 旧 API | 新 API |
|---|---|
| `graphics.pose().pushPose()` | `graphics.pose().pushMatrix()` |
| `graphics.pose().popPose()` | `graphics.pose().popMatrix()` |
| `graphics.pose().translate(x, y, z)` | `graphics.pose().translate(x, y)` — z 参数移除 |
| `graphics.pose().scale(x, y, z)` | `graphics.pose().scale(x, y)` — z 参数移除 |

### 2.4 颜色常量变化

| 旧 | 新 | 说明 |
|---|---|---|
| `0x333333` (无 alpha) | `0xFF333333` | 颜色值需要显式 alpha 通道 |
| `0xffffff` | `0xFFFFFFFF` or `0xffffff` | 白色需视情况添加 alpha |

---

## 3. RenderSystem 使用变化

| 旧 API | MC 26.1 变化 |
|---|---|
| `RenderSystem.setShader(GameRenderer::getPositionTexShader)` | **移除** — 不再需要手动设置 shader |
| `RenderSystem.setShaderTexture(0, texture)` | **移除** — GuiGraphicsExtractor 内部处理 |
| `RenderSystem.enableBlend()` / `disableBlend()` | 视情况保留或移除 |
| `RenderSystem.enableDepthTest()` / `disableDepthTest()` | 部分注释掉 |
| `RenderSystem.depthMask(false)` | 部分注释掉 |

> 新项目中有多处 RenderSystem 调用被注释掉（如 CuriosContainerScreen、ScrollRenderEvent），grep 可看到 `// RenderSystem.` 注释残迹。

---

## 4. AbstractContainerMenu (Container) System

### 4.1 stillValid() — Entity interaction check 变化

| 旧 API | 新 API | 文件 |
|---|---|---|
| `playerIn.canInteractWithEntity(entity, distance)` | `playerIn.isWithinEntityInteractionRange(entity, distance)` | `AbstractMaidContainer` |

### 4.2 clicked() 方法签名变化

| 旧 API | 新 API |
|---|---|
| `clicked(int slotId, int button, ClickType clickTypeIn, Player player)` | `clicked(int slotId, int button, ContainerInput containerInput, Player player)` |
| `ClickType.SWAP` | `ContainerInput.SWAP` |

### 4.3 Inventory 方法变化

| 旧 API | 新 API | 文件 |
|---|---|---|
| `player.getInventory().selected` | `player.getInventory().getSelectedSlot()` | `PicnicBasketContainer` |

### 4.4 食物属性检测变化

| 旧 API | 新 API | 文件 |
|---|---|---|
| `stack.getFoodProperties(null) != null` | `stack.has(DataComponents.FOOD)` | `PicnicBasketContainer` |

### 4.5 容器打开位置变化

| 旧位置 | 新位置 | 文件 |
|---|---|---|
| `EntityMaid` 中 `serverPlayer.openMenu(...)` | `MaidMiscManager` 中 `serverPlayer.openMenu(...)` | 女仆 GUI 打开代码移入 Manager 类 |

---

## 5. MenuType Registration — 保持不变

`IMenuTypeExtension.create()` 模式在两个版本中**完全一致**，无需修改：

```java
public static final MenuType<SmallBackpackContainer> TYPE = IMenuTypeExtension.create(
    (windowId, inv, data) -> new SmallBackpackContainer(windowId, inv, data.readInt()));
```

`DeferredRegister<MenuType<?>>` + `Registries.MENU` 也保持不变。

---

## 6. Slot System — SlotItemHandler → ResourceHandlerSlot

这是第二个**重大 API 变化**。

### 6.1 物品存储接口替换

| 旧接口/类 | 新接口/类 |
|---|---|
| `net.neoforged.neoforge.items.IItemHandler` | `net.neoforged.neoforge.transfer.ResourceHandler<ItemResource>` |
| `net.neoforged.neoforge.items.ItemStackHandler` | `net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler` |
| `net.neoforged.neoforge.items.SlotItemHandler` | `net.neoforged.neoforge.transfer.item.ResourceHandlerSlot` |

### 6.2 SlotItemHandler vs ResourceHandlerSlot 构造函数对比

```java
// OLD: SlotItemHandler
new SlotItemHandler(handler, index, x, y)                                    // 简单
new SlotItemHandler(maid.getMaidInv(), index, xPosition, yPosition)          // 示例

// NEW: ResourceHandlerSlot
new ResourceHandlerSlot(resourceHandler, slotModifier, index, x, y)          // 需要 IndexModifier
new ResourceHandlerSlot(maid.getMaidInv(), maidInv::set, index, xPosition, yPosition) // 示例
```

**关键变化**：`ResourceHandlerSlot` 需要额外的 `IndexModifier<ItemResource>` 参数，通常通过方法引用 `handler::set` 或 `ItemsUtil.createIndexModifier()` 创建。

### 6.3 BackpackSlot/BaubleSlot 模式变化

#### 旧模式（SlotItemHandler 子类）：
```java
public static class BackpackSlot extends SlotItemHandler implements ITriggerSlotChange {
    public BackpackSlot(EntityMaid maid, int index, int xPosition, int yPosition) {
        super(maid.getMaidInv(), index, xPosition, yPosition);
        this.maid = maid;
    }
}
// 使用:
addSlot(new BackpackSlot(maid, 6 + i, 143 + 18 * i, 59));
```

#### 新模式（ResourceHandlerSlot 子类）：
```java
public static class BackpackSlot extends ResourceHandlerSlot implements ITriggerSlotChange {
    private BackpackSlot(EntityMaid maid, IndexModifier<ItemResource> slotModifier, int index, int xPosition, int yPosition) {
        super(maid.getMaidInv(), slotModifier, index, xPosition, yPosition);
        this.maid = maid;
    }
    public static BackpackSlot create(EntityMaid maid, int index, int xPosition, int yPosition) {
        ItemStacksResourceHandler maidInv = maid.getMaidInv();
        return new BackpackSlot(maid, maidInv::set, index, xPosition, yPosition);
    }
}
// 使用:
addSlot(BackpackSlot.create(maid, 6 + i, 143 + 18 * i, 59));
```

### 6.4 getNoItemIcon() 签名变化

| 旧 API | 新 API | 说明 |
|---|---|---|
| `@OnlyIn(Dist.CLIENT) public Pair<ResourceLocation, ResourceLocation> getNoItemIcon()` | `public Identifier getNoItemIcon()` | 返回类型从 Pair 变为单个 Identifier |
| `return Pair.of(BLOCK_ATLAS, EMPTY_MAINHAND_SLOT)` | `return EMPTY_MAINHAND_SLOT` | 不再需要 atlas 参数 |
| 需要 `@OnlyIn(Dist.CLIENT)` 注解 | **移除** `@OnlyIn` 注解 | |

### 6.5 getSlots() → size()

| 旧 API | 新 API | 文件 |
|---|---|---|
| `maid.getMaidBauble().getSlots()` | `maid.getMaidBauble().size()` | `BaubleContainer` |

### 6.6 setLastArmorItem/setLastHandItem → setItemSlot

| 旧 API | 新 API | 文件 |
|---|---|---|
| `maid.setLastArmorItem(equipmentSlot, stack)` | `maid.setItemSlot(equipmentSlot, stack)` | `MaidMainContainer.quickMoveStack()`, `BaubleContainer.quickMoveStack()` |
| `maid.setLastHandItem(equipmentSlot, stack)` | `maid.setItemSlot(equipmentSlot, stack)` | 同上 |

> 注意：旧 `MaidMainContainer` 中 `quickMoveStack` 底部有 `setLastArmorItem`/`setLastHandItem` 逻辑，新版本已**整段移除**。该逻辑仅在新 `BaubleContainer.quickMoveStack()` 中保留。

---

## 7. Menu Opening (openMenu)

### 7.1 openMenu 签名

两个版本一致：
```java
serverPlayer.openMenu(guiProvider, buffer -> buffer.writeInt(id));
serverPlayer.openMenu(item, data -> ItemStack.STREAM_CODEC.encode(data, stack));
```

### 7.2 @EventBusSubscriber 变化

| 旧 API | 新 API | 文件 |
|---|---|---|
| `@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)` | `@EventBusSubscriber(value = Dist.CLIENT)` | `InitContainerGui` |

`bus` 参数已被移除。

---

## 8. Tooltip System

### 8.1 GuiGraphics 上的 renderTooltip

| 旧 API | 新 API |
|---|---|
| `graphics.renderTooltip(font, component, mouseX, mouseY)` | `graphics.setTooltipForNextFrame(font, List.of(component), Optional.empty(), mouseX, mouseY)` |
| `graphics.renderComponentTooltip(font, list, mouseX, mouseY)` | `graphics.setTooltipForNextFrame(font, list, Optional.empty(), mouseX, mouseY)` |

### 8.2 ITooltipButton 接口

| 旧 API | 新 API |
|---|---|
| `void renderTooltip(GuiGraphics graphics, Minecraft mc, int mouseX, int mouseY)` | `void renderTooltip(GuiGraphicsExtractor graphics, Minecraft mc, int mouseX, int mouseY)` |

参数中的 `Graphics` → `GraphicsExtractor`。

---

## 9. Network Sending (Client Side)

| 旧 API | 新 API |
|---|---|
| `PacketDistributor.sendToServer(packet)` | `ClientPacketDistributor.sendToServer(packet)` |
| `import net.neoforged.neoforge.network.PacketDistributor` | `import net.neoforged.neoforge.client.network.ClientPacketDistributor` |

---

## 10. Animation / Entity Render in GUI

| 旧 API | 新 API |
|---|---|
| `InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, ...)` | `InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, ...)` |

---

## 11. Identifier (ResourceLocation) 重命名

| 旧 API | 新 API |
|---|---|
| `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` |
| `ResourceLocation.fromNamespaceAndPath(modid, path)` | `Identifier.fromNamespaceAndPath(modid, path)` |
| `ResourceLocation.parse("item/empty_slot_sword")` | `Identifier.parse("container/slot/sword")` |

> 注意：空槽位纹理路径也变了：`item/empty_slot_sword` → `container/slot/sword`，`slot/empty_back_show_slot` → `container/slot/back_show`。

---

## 12. isClientSide 方法化

| 旧 API | 新 API | 说明 |
|---|---|---|
| `maid.level.isClientSide` (字段) | `maid.level.isClientSide()` (方法) | Level.isClientSide 从字段变成方法 |

---

## 13. 各种小 API 变化

| 旧 API | 新 API | 文件 |
|---|---|---|
| `SharedConstants.getCurrentVersion().getName()` | `SharedConstants.getCurrentVersion().name()` | `AbstractMaidContainerGui` |
| `maid.getGameRecordManager()` | `maid.getGameManager()` | `AbstractMaidContainerGui` |
| `CustomPackLoader.MAID_MODELS` (包简写) | `client.resource.loader.CustomPackLoader.MAID_MODELS` | 包路径变化 |
| `CacheIconManager.openMaidModelGui(maid)` | `getMinecraft().setScreen(new MaidModelGui(maid))` | 直接设置 screen |
| `@Nullable` from `javax.annotation` | 部分移除（保留时使用 javax） | |
| `@Nonnull` from `javax.annotation` | `@NonNull` from `org.jspecify.annotations` | 检查具体文件 |

---

## 14. 移除的容器/屏幕类（仅旧项目有）

以下类在新项目中已被移除：

| 类 | 说明 |
|---|---|
| `CraftingTableBackpackContainer` + Screen | 合成台背包 |
| `EnderChestBackpackContainer` + Screen | 末影箱背包 |
| `FurnaceBackpackContainer` + Screen | 熔炉背包 |
| `TankBackpackContainer` + Screen | 流体储罐背包 |
| `CacheScreen.java` | 缓存界面 |
| `CacheIconManager.java` | 缓存图标管理 |
| `MaidHandsInvWrapper.java` | 女仆双手物品包装 |
| `BoardStateTooltip.java` | 棋盘状态 Tooltip |
| `YsmMaidInfo.java` | YSM 信息 |
| YSM skin 按钮相关代码 | `ysmSkin` 变量和按钮逻辑完全移除 |

对应的 `InitContainer` 中也少了 4 个 `MenuType` 注册，`InitContainerGui` 中少了 4 个 Screen 注册。

---

## 15. Complete Import Migration Table

### 15.1 Screen / GUI 相关导入

| 旧导入 | 新导入 |
|---|---|
| `net.minecraft.client.gui.GuiGraphics` | `net.minecraft.client.gui.GuiGraphicsExtractor` |
| `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` |
| `com.mojang.blaze3d.systems.RenderSystem` | 大部分不再需要 |
| `net.minecraft.client.renderer.GameRenderer` | 不再需要 |
| `net.neoforged.api.distmarker.OnlyIn` | 不再需要（Slot.getNoItemIcon 不再需要） |
| `net.neoforged.neoforge.network.PacketDistributor` | `net.neoforged.neoforge.client.network.ClientPacketDistributor` |
| `com.mojang.blaze3d.platform.InputConstants` (用于 keyPressed) | 需要 `net.minecraft.client.input.KeyEvent` |

### 15.2 Container 相关导入

| 旧导入 | 新导入 |
|---|---|
| `net.neoforged.neoforge.items.IItemHandler` | `net.neoforged.neoforge.transfer.ResourceHandler` + `net.neoforged.neoforge.transfer.item.ItemResource` |
| `net.neoforged.neoforge.items.ItemStackHandler` | `net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler` |
| `net.neoforged.neoforge.items.SlotItemHandler` | `net.neoforged.neoforge.transfer.item.ResourceHandlerSlot` |
| `net.minecraft.world.inventory.ClickType` | `net.minecraft.world.inventory.ContainerInput` |
| `net.neoforged.neoforge.transfer.IndexModifier` | 新增依赖 |
| `net.minecraft.core.component.DataComponents` | 新增依赖（用于 `stack.has(DataComponents.FOOD)`） |

### 15.3 Slot 相关导入

| 旧导入 | 新导入 |
|---|---|
| `com.mojang.datafixers.util.Pair` (用于 getNoItemIcon) | 不再需要 |
| `net.neoforged.api.distmarker.OnlyIn` (用于 getNoItemIcon) | 不再需要 |
| `net.neoforged.neoforge.items.SlotItemHandler` | `net.neoforged.neoforge.transfer.item.ResourceHandlerSlot` |

---

## 16. Changes Found in TouhouLittleMaid Code

### 16.1 Screen Classes Affected

| 类 | 主要变化 |
|---|---|
| `AbstractMaidContainerGui` | `render()` → `extractRenderState()`, `renderBg()` → `extractBackground()`, `renderLabels()` → `extractLabels()`, `renderTooltip()` → `extractTooltip()`, 构造函数签名, GuiGraphics → GuiGraphicsExtractor, PacketDistributor → ClientPacketDistributor, ResourceLocation → Identifier, YSM skin 移除 |
| `EmptyBackpackContainerScreen` | `renderBg()` → `extractBackground()`, RenderSystem 调用移除, GuiTools 使用 |
| `SmallBackpackContainerScreen` | 同上 |
| `MiddleBackpackContainerScreen` | 同上 |
| `BigBackpackContainerScreen` | 同上 |
| `BaubleContainerScreen` | 同上 |
| `WirelessIOContainerGui` | `render()` → `extractRenderState()`, `renderBg()` → `extractContents()`, `renderLabels()` → `extractLabels()`, tooltip 方式变化 |
| `MaidBeaconGui` | `render()` → `extractRenderState()`, `keyPressed()` 签名变化, GuiTools 使用 |
| `ModelSwitcherGui` | 同上模式 |
| `ModelDownloadGui` | 同上模式 |
| `AIChatScreen` | 同上模式 |
| `MaidSoundPackGui` | 同上模式 |
| `MaidModelGui` / `AbstractModelGui` | 同上模式 |
| All widget classes (buttons) | `GuiGraphics` → `GuiGraphicsExtractor` |
| `ITooltipButton` | 接口签名 `GuiGraphics` → `GuiGraphicsExtractor` |

### 16.2 Container Classes Affected

| 类 | 主要变化 |
|---|---|
| `AbstractMaidContainer` | `stillValid()`: `canInteractWithEntity` → `isWithinEntityInteractionRange` |
| `MaidMainContainer` | `SlotItemHandler` → `ResourceHandlerSlot`, `getNoItemIcon()` 签名变化, 移除 `@OnlyIn`, `setLastArmorItem`/`setLastHandItem` → `setItemSlot`, `isClientSide` → `isClientSide()` |
| `SmallBackpackContainer` | `BackpackSlot` 工厂方法模式 |
| `MiddleBackpackContainer` | 同上 |
| `BigBackpackContainer` | 同上 |
| `EmptyBackpackContainer` | 同上 |
| `BaubleContainer` | `BaubleSlot` 工厂方法模式, `getSlots()` → `size()`, `isClientSide` → `isClientSide()` |
| `PicnicBasketContainer` | `ItemStackHandler` → `ItemStacksResourceHandler`, `SlotItemHandler` → `ResourceHandlerSlot`, `getFoodProperties` → `has(DataComponents.FOOD)`, `clicked()` 签名变化, `selected` → `getSelectedSlot()` |
| `WirelessIOContainer` | `ItemStackHandler` → `ItemStacksResourceHandler` (推断) |
| `CraftingTableBackpackContainer` | **已移除** |
| `EnderChestBackpackContainer` | **已移除** |
| `FurnaceBackpackContainer` | **已移除** |
| `TankBackpackContainer` | **已移除** |

---

**总结**：MC 26.1 的 Screen/Menu API 经历了**渲染架构完全重写**（立即模式 → 提取模式）、**物品存储接口替换**（IItemHandler → ResourceHandler）、以及大量**方法签名现代化**。迁移时需要逐类修改渲染方法、Slot 子类和网络发送方式。
