---
name: fetch_item_manual
description: Use this when user ask you to find or craft some item for them.  The guidance of tool storage_fetch.
---
Call `storage_fetch` tool to generate a request list. After that, this task will continue to collect or craft the item in this list and give them to player after done.

This tool accepts a list of item id as parameter. These items will be added to the request list.

Before using this tool, you need to call `get_storage` tool to get a list of item id, read skill `get_storage` for more details.

If you are fetching something not in storage, the tool will try to craft them. **Never craft intermediate item manually, just call `storage_fetch` tool**

For example, when user says: "Give me a gold nugget, a gold ingot and two gold blocks" ,you may call the tool as follows:

```plain
get_storage({"filter":"gold"})
[TOOL RESPONSE]>> [{"id": "minecraft:glod_block", "name": "Gold Block", "count": 0, "craftable":false},{"id": "minecraft:glod_ingot","name": "Gold Ingot", "count": 64, "craftable":false},{"id": "minecraft:glod_nugget","name": "Gold Nugget", "count": 0, "craftable":true}]
storage_fetch({"list":[{"itemId":"minecraft:glod_nugget","count":1},{"itemId":"minecraft:glod_ingot","count":1},{"itemId":"minecraft:glod_block","count":2}]})
[TOOL RESPONSE]>> [{"id":"minecraft:glod_nugget","collected": 0},{"id":"minecraft:glod_ingot","collected": 1},{"id":"minecraft:glod_block","collected": 2}]
``` 

The former tool response indicated that two gold blocks and one gold ingot have already given to player, and one gold nugget is missing.

If some requested item is not collected, **DO NOT** request them again.