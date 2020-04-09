
require "tileMapPrepare"
require "texSet"

local mapTypes =
{
	{
		name = "surf",
		configStrs = { "TerrainMap" }
	},
	{
		name = "high",
		configStrs = { "SurfaceMap" }
	},
	{
		name = "dugg",
		configStrs = { "PredugMap" }
	},
	{
		name = "emrg",
		configStrs = { "EmergeMap" }
	},
	{
		name = "cror",
		configStrs = { "CryOreMap" }
	},
	{
		name = "path",
		configStrs = { "PathMap" }
	},
	{
		name = "tuto",
		configStrs = { "BlockPointersMap" }
	},
}

local function returnMapStr(cfg, cfgp, list)
	local res = nil
	for i, v in ipairs(list) do
		local val = cfg.getValue(cfgp..v)
		if val ~= nil then return val end
	end
end

function getMap(config, configNodePath)
	
	if string.sub(configNodePath, -1, -1) ~= "/" then
		configNodePath = configNodePath.."/"
	end
	
	local map = {}
	
	for i, v in ipairs(mapTypes) do
		-- Get all maps entered in config
		local path = returnMapStr(config, configNodePath, v.configStrs)
		if path ~= nil then
			map[v.name] = rge.get(path)
		end
	end
	
	local tmap = loadFromMaps(map.surf, map.high, map.path)
	local tex = getTextureSet(config.getValue(configNodePath.."TextureSet"))
	populateRawTex(tex)
	tmap.texture(tex)
	
	map.tmap = rge.newTileMap(tmap)
	
	return map
end

function breakRock(self, x, y)
	
	
	
end