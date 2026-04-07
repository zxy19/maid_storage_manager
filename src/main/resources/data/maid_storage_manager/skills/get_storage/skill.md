---
name: get_storage
description: Get the item in maid storage memory. Use this skill when you need to query items maid has viewed or when you want to search item by name.
---
Call `get_storage` tool to query the item storage.
This tool accepts an optional string as filter pattern. If not provided, all items will be returned. 

For example, when user ask: "Where are my gold and iron?",you may call the tool as follows:
```plain
get_storage({"filter":"gold"})
[TOOL RESPONSE]>> [{"id": "minecraft:glod_block", "name": "Gold Block", "count": 64, "craftable":true},{"id": "minecraft:glod_ingot","name": "Gold Ingot", "count": 64, "craftable":false},{"id": "minecraft:glod_nugget","name": "Gold Nugget", "count": 0, "craftable":true}]
get_storage({"filter":"iron"})
[TOOL RESPONSE]>> [{"id": "minecraft:iron_axe", "name": "Iron Axe", "count": 2, "craftable":false}]
``` 

Notice that **never tell user about ids**, use item name instead.