# Maid Storage Manager
![cover](cover.png)  
Adds a new job to the Touhou Maid mod. Now maids can pick up items from the ground and store them in nearby chests, or find specific items for you based on your request lists.

---

## Development Stage Notice
This mod is in early testing phase. If you encounter issues like maids freezing during work, please enable the debug option in the configuration file, screenshot the stuck position in-game, and provide feedback with trigger conditions.

---

## Usage
### Filter
Place an item frame on a chest and insert a **Filter**/**No Access** into it.  
If a maid detects items in the chest that do not match the filter, she will attempt to remove them and store them in other chests.

### Deposit
Throw items directly to the maid. During idle time, she will store all items from her inventory into nearby chests following these priorities:
1. Chests with active whitelist filters will be prioritized.
2. Chests that previously stored the same item type.
3. Any accessible chest without No Access.

### Request
You can ask maids to retrieve specific items:
1. Craft a **Request List** item.
2. Right-click to open the GUI.
3. Fill in required items and quantities (use mouse wheel to adjust numbers).
4. *(Optional)* Sneak + right-click a target chest to bind a storage location.
5. Give the configured list to the maid.

The maid will search nearby for listed items. Upon completion (regardless of success), she will place found items and the list itself into the target chest. Requested target chests ignore No Access restrictions.

### Storage List
During idle time, maids automatically scan nearby chest contents. Give them a **Storage List** item to record memorized item quantities, which will return a **Recorded Storage List**.

### Storage Define

Storage Define specifies which containers a maid can access and from which faces. Right-click blocks to select/deselect faces; sneak + right-click to select blocks (no face data, e.g., furnaces can directly access all slots).
Right-click air to switch definition modes:

+ Add: Append new storage accesses to the maid's existing list
+ Remove: Delete specified storage from maid's accessible locations
+ Replace: Completely overwrite maid's access points (maid will ONLY use these)
+ Replace Specific: When accessing specified locations, only allow interaction through selected faces


---

## Compatibility Notes
- Maids can retrieve items from AE2 terminals (*auto-crafting not yet supported*). Configurable.
- Chest opening/closing animations are synchronized in multiplayer if the container supports state visibility.
- Uses item insertion method for multiblock container recognition. Watch for mod conflicts.

---

## Asset License
GUI textures under `assets/texture/gui` are modified from Touhou Maid assets and follow CC-BY-NC-SA 4.0. Other assets and code licenses are specified in the license file.  