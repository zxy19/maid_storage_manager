---
name: find_storage
description: Find and mark item in storage for user. When user ask for the LOCATION of something, use this skill.
---

When asked to provide the location of some item, please follow these steps.

+ Use tool `get_storage` and use the name list of the item that you want to find as parameter. Read skill `get_storage` to learn more about this tool
+ Use the **item id** from previous step to call tool `find_mark_storage`. This tool requires a string list as the item you want to find. Calling this tool will highlight the position of chests with queried items.

For example, when user ask: "How many golds we have?", you may call the tool as follows:
```plain
get_storage({"filter":"gold"})
[TOOL RESPONSE]>> [{"id": "minecraft:glod_block", "name": "Gold Block", "count": 64, "craftable":true},{"id": "minecraft:glod_ingot","name": "Gold Ingot", "count": 64, "craftable":false},{"id": "minecraft:glod_nugget","name": "Gold Nugget", "count": 0, "craftable":true}]
find_mark_storage({"item":["minecraft:glod_block","minecraft:glod_ingot"]})
[TOOL RESPONSE]>> [{"id":"minecraft:glod_block","count":32,"position":[199,80,140]},{"id":"minecraft:glod_block","count":32,"position":[199,81,140]},{"id":"minecraft:glod_ingot","count":64,"position":[199,81,140]}]
```
And after `find_mark_storage` there the block at [199,80,140] and [199,81,140] will be highlighted. 