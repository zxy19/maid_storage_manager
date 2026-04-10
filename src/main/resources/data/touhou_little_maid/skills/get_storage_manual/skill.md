---
name: get_storage_manual
description: Get the item in maid storage memory. Use this skill when you need to query items maid has viewed or when you want to search item by name. The guidance of tool get_storage.
---
Call `get_storage` tool to query the item storage.
This tool accepts an optional string as filter pattern. If not provided, all items will be returned.
If you want to search in all items(not only in storage), use parameter `all` to set true. This would be useful to search all item in the game.

## Basic

For example, when user ask: "Where are my gold and iron?",you may call the tool as follows:
```plain
get_storage({"filter":"gold"})
[TOOL RESPONSE]>> [{"id": "minecraft:glod_block", "name": "Gold Block", "count": 64, "craftable":true},{"id": "minecraft:glod_ingot","name": "Gold Ingot", "count": 64, "craftable":false},{"id": "minecraft:glod_nugget","name": "Gold Nugget", "count": 0, "craftable":true}]
get_storage({"filter":"iron","all": true})
[TOOL RESPONSE]>> [{"id": "minecraft:iron_axe", "name": "Iron Axe"},{"id": "minecraft:iron_ingot", "name": "Iron Ingot"},....]
``` 
**Warning**, if the result of your filter is greater than 150, you may get an error message.

You should always prefer use user's input language to perform the query.

## Tooltip
If you want to read more detail about the item, use parameter `showTooltip`.
```plain
get_storage({"filter":"diamond"})
[TOOL RESPONSE]>> [{"id": "minecraft:diamond_sword", "name": "Diamond Sword", "count": 1, "craftable":false, "tooltip": "On mainhand：\\n 5.5 Damage\\n 1 Speed\\nDurability：912"}]
``` 
If you want also search pattern in tooltip, use parameter `queryTooptip`. 

## Craftable

Craftable indicates that there is a craft guide that provides this item. Sometimes some item without craft guide or even not in memory may be craftable, you may call `simulate_crafting` to check it.

**Never calculate the crafting path your self, always use `simulate_crafting` instead. Never check ingredients before calling tool.**

```plain
simulate_crafting({"itemId": "minecraft:diamond_sword","cound":1})
[TOOL RESPONSE]>> {"success": true, "steps":2, "consumes": [{"id": "minecraft:diamond", "count": 2}, {"id": "minecraft:oak_log", "count": 2}]}
simulate_crafting({"itemId": "minecraft:torch","cound":1})
[TOOL RESPONSE]>> {"success": false, "missing": [{"id": "minecraft:coal", "count": 1}], "fails":[]}
```

If no fails or missing info are provided, it means there's no craft guide related to this item.

If tool has returned `success:true`, that means you can use `fetch_item` for this item. Read `fetch_item_manual` for more detail.

Simulate crafting DO NOT generate actual item. USE `fetch_item` to get item.

## Extra
**Never tell user about ids**, use item name instead.
If something not found, try to use `all` parameter.