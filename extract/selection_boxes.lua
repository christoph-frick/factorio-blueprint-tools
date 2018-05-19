#!/usr/local/bin/lua

JSON = (loadfile "JSON.lua")()

package.path = package.path .. ";" .. "/var/factorio-data/core/lualib/?.lua"
package.path = package.path .. ";" .. "/var/factorio-data/base/?.lua"

-- most likely defined in Factorio itself; those are neede to load 0.16
defines = {}
defines.direction = {}
defines.direction.north	= 0
defines.direction.northeast = 1
defines.direction.east	= 2
defines.direction.southeast = 3
defines.direction.south	= 4
defines.direction.southwest = 5
defines.direction.west = 6
defines.direction.northwest = 7
defines.difficulty_settings = {}
defines.difficulty_settings.recipe_difficulty = {}
defines.difficulty_settings.recipe_difficulty.normal = 0
defines.difficulty_settings.technology_difficulty = {}
defines.difficulty_settings.technology_difficulty.normal = 0

require('dataloader')
require('data')

-- find all entities with a selection_box
local result = {}
for ok,ov in pairs(data.raw) do
	for ik,iv in pairs(ov) do
		if (iv.selection_box) then
			result[ik] = iv.selection_box
		end
	end
end

print(JSON:encode_pretty(result))
