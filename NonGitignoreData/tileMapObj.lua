
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
	
	local map = {}
	
	-- CLASS FUNCTIONS
	
	map.clampX = function(self, x)
		if x < 1 then
			return 1
		elseif x > #self.surf then
			return #self.surf
		end
		return x
	end
	
	map.clampY = function(self, y)
		if y < 1 then
			return 1
		elseif y > #self.surf[1] then
			return #self.surf[1]
		end
		return y
	end
	
	map.breakRock = function(self, x, y)
		
		local function isIn(val, array)
			for i, v in ipairs(array) do
				if v == val then return true end
			end
			return false
		end
		
		local id = self.tmap.tile(x-1, y-1)
		if isIn(id, cliffconnects) then
			self.tmap.tile(x-1, y-1, 20)
		else
			return
		end
		
		for ox = 0,1 do
			for oy = 0,1 do
				local ax = x + ox
				local ay = y + oy
				local hoff = ((ay-1)*(2*#self.surf+1)) + (ax-1)
				self.tmap.pointHeight(hoff, self.high[ax][ay]/divFactor)
			end
		end
		
		for ox = -1, 1 do
			for oy = -1, 1 do
				local ax = x + ox
				local ay = y + oy
				if ax >= 1 and ax <= #self.surf and ay >= 1 and ay <= #self.surf[1] then
					updateMidpoint(self.tmap, ax, ay)
				end
			end
		end
		
	end
	
	map.updateTile = function(self, x, y)
		
	end
	
	-- CLASS FUNCTIONS END
	
	if string.sub(configNodePath, -1, -1) ~= "/" then
		configNodePath = configNodePath.."/"
	end
	
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


