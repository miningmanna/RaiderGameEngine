
require "connectTex"
groundConnectMap = makeGroundConnections()
cliffConnectMap = makeCliffConnections()

local divFactor = 7
local wallHeight = 1

local ppconnects = { 30, 31 }
local cliffconnects = { 1, 2, 3, 4, 8, 10, 11 }

local types =
{
	{
		-- Solid rock
		surf = 1,
		path = 0,
		id = 1,
		texmap = cliffConnectMap,
		connects = cliffconnects,
		tex = {
			ERROR		= { x = 0, y = 7 },
			ROOF		= { x = 7, y = 0 },
			FLAT		= { x = 0, y = 5 },
			OCORNER		= { x = 5, y = 5 },
			ICORNER		= { x = 3, y = 5 },
			DIAGONAL	= { x = 7, y = 7 }
		}
	},
	{
		-- Dirt rock
		surf = 2,
		path = 0,
		id = 2,
		texmap = cliffConnectMap,
		connects = cliffconnects,
		tex = {
			ERROR		= { x = 0, y = 7 },
			ROOF		= { x = 7, y = 0 },
			FLAT		= { x = 0, y = 2 },
			OCORNER		= { x = 5, y = 2 },
			ICORNER		= { x = 3, y = 2 },
			DIAGONAL	= { x = 7, y = 7 }
		}
	},
	{
		-- Loose rock
		surf = 3,
		path = 0,
		id = 3,
		texmap = cliffConnectMap,
		connects = cliffconnects,
		tex = {
			ERROR		= { x = 0, y = 7 },
			ROOF		= { x = 7, y = 0 },
			FLAT		= { x = 0, y = 3 },
			OCORNER		= { x = 5, y = 3 },
			ICORNER		= { x = 3, y = 3 },
			DIAGONAL	= { x = 7, y = 7 }
		}
	},
	{
		-- Hard rock
		surf = 4,
		path = 0,
		id = 4,
		texmap = cliffConnectMap,
		connects = cliffconnects,
		tex = {
			ERROR		= { x = 0, y = 7 },
			ROOF		= { x = 7, y = 0 },
			FLAT		= { x = 0, y = 4 },
			OCORNER		= { x = 5, y = 4 },
			ICORNER		= { x = 3, y = 4 },
			DIAGONAL	= { x = 7, y = 7 }
		}
	},
	{
		-- Ore seam
		surf = 8,
		path = 0,
		id = 8,
		texmap = cliffConnectMap,
		connects = cliffconnects,
		tex = {
			ERROR		= { x = 0, y = 7 },
			ROOF		= { x = 7, y = 0 },
			FLAT		= { x = 4, y = 0 },
			OCORNER		= { x = 0, y = 7 },
			ICORNER		= { x = 0, y = 7 },
			DIAGONAL	= { x = 7, y = 7 }
		}
	},
	{
		-- Crystal seam
		surf = 10,
		path = 0,
		id = 10,
		texmap = cliffConnectMap,
		connects = cliffconnects,
		tex = {
			ERROR		= { x = 0, y = 7 },
			ROOF		= { x = 7, y = 0 },
			FLAT		= { x = 2, y = 0 },
			OCORNER		= { x = 0, y = 7 },
			ICORNER		= { x = 0, y = 7 },
			DIAGONAL	= { x = 7, y = 7 }
		}
	},
	{
		-- recharge seam
		surf = 11,
		path = 0,
		id = 11,
		texmap = cliffConnectMap,
		connects = cliffconnects,
		tex = {
			ERROR		= { x = 0, y = 7 },
			ROOF		= { x = 7, y = 0 },
			FLAT		= { x = 6, y = 7 },
			OCORNER		= { x = 0, y = 7 },
			ICORNER		= { x = 0, y = 7 },
			DIAGONAL	= { x = 7, y = 7 }
		}
	},
	{
		-- Dirt floor
		surf = 5,
		path = 0,
		id = 5,
		texmap = groundConnectMap,
		tex = {
			NCONNECT = { x = 0, y = 0 },
		}
	},
	{
		-- Full rubble floor
		surf = 5,
		path = 1,
		id = 20,
		texmap = groundConnectMap,
		tex = {
			NCONNECT = { x = 1, y = 0 },
		}
	},
	{
		-- Unpowered Power path
		surf = 5,
		path = 2,
		id = 30,
		connects = ppconnects,
		texmap = groundConnectMap,
		tex = {
			NCONNECT = { x = 6, y = 5 },
			XCONNECT = { x = 6, y = 0 },
			TCONNECT = { x = 6, y = 4 },
			LCONNECT = { x = 6, y = 3 },
			ICONNECT = { x = 6, y = 2 },
			ECONNECT = { x = 6, y = 5 },
		}
	},
	{
		-- Water
		surf = 9,
		path = 0,
		id = 9,
		texmap = groundConnectMap,
		tex = {
			NCONNECT = { x = 4, y = 5 },
		}
	},
	{
		-- Lava
		surf = 6,
		path = 0,
		id = 6,
		texmap = groundConnectMap,
		tex = {
			NCONNECT = { x = 4, y = 6 },
		}
	},
}

globalTileTypes = {}
local state = rge.newVector3()
for i, v in ipairs(types) do
	local t = rge.newTileType()
	globalTileTypes[i] = t
	t.id(v.id)
	t.connects(v.connects)
	if v.connects ~= nil then
		for i = 0,255 do
			local connect = v.texmap[i]
			state.x(v.tex[connect.name].x)
			state.y(v.tex[connect.name].y)
			state.z(connect.rot)
			t.state(i, state)
		end
	else
		local i = 0
		local connect = v.texmap[i]
		state.x(v.tex[connect.name].x)
		state.y(v.tex[connect.name].y)
		state.z(connect.rot)
		t.state(i, state)
	end
end

local function determineType(surf, path)
	
	for i, v in ipairs(types) do
		if v.surf == surf and v.path == path then
			return v.id, i
		end
	end
	return 0
end

local function neighbourFieldAndTypeIndex(tmap, x, y)
	
	local id = tmap.tile(x, y)
	local idi = nil
	local connects = nil
	for i, v in ipairs(types) do
		if v.id == id then
			connects = v.connects
			idi = i
			break
		end
	end
	if idi == nil then
		return
	end
	if connects ~= cliffconnects then
		return 0, idi
	end
	
	local field = 0
	local off = 0
	for ox = -1, 1 do
		for oy = -1, 1 do
			if ox == 0 and oy == 0 then goto continue end
			
			local b = 0
			local ax = x + ox
			local ay = y + oy
			if ax < 0 then ax = 0 end
			if ax >= tmap.width() then ax = tmap.width()-1 end
			if ay < 0 then ay = 0 end
			if ay >= tmap.height() then ay = tmap.height()-1 end
			
			local nid = tmap.tile(ax, ay)
			
			for i, v in ipairs(connects) do
				if v == nid then
					b = 1
					break
				end
			end
			
			field = bit32.bor(field, bit32.lshift(b, off))
			
			off = off+1
			::continue::
		end
	end
	
	return field, idi
end

function loadFromMaps(surf, high, path)
	
	local tmap = rge.newRawTileMap(#surf, #surf[1])
	
	for x = 1, #surf do
		for y = 1, #surf[x] do
			local p = 0
			if path ~= nil then p = path[x][y] end
			local t = determineType(surf[x][y], p)
			tmap.tile(x-1, y-1, t)
			
			if tid ~= nil then
				local t = types[tid]
				local gt = globalTileTypes[tid]
			end
			
			local hoff = ((y-1)*(2*#surf+1)) + (x-1)
			tmap.pointHeight(hoff, wallHeight + high[x][y]/divFactor)
			::continue::
		end
	end
	
	-- set edges
	for x = 1, #surf do
		local y = #surf[x]+1
		local hoff = ((y-1)*(2*#surf+1)) + (x-1)
		tmap.pointHeight(hoff, wallHeight + high[x][y-1]/divFactor)
	end
	for y = 1, #surf[1] do
		local x = #surf+1
		local hoff = ((y-1)*(2*#surf+1)) + (x-1)
		tmap.pointHeight(hoff, wallHeight + high[x-1][y]/divFactor)
	end
	tmap.pointHeight((#surf+1)*(#surf[1]+1) + (#surf)*(#surf[1]) - 1, 1.5 + high[#surf][#surf[1]]/divFactor)
	
	-- set points touching ground to ground height
	for x = 1, #surf do
		for y = 1, #surf[x] do
			local id = tmap.tile(x-1, y-1)
			local tid = nil
			for i, v in ipairs(types) do
				if v.id == id then
					tid = i
					break
				end
			end
			if tid == nil then
				goto continue
			end
			if types[tid].texmap == groundConnectMap then
				for ox = 0,1 do
					for oy = 0,1 do
						local ax = x + ox
						local ay = y + oy
						local hoff = ((ay-1)*(2*#surf+1)) + (ax-1)
						tmap.pointHeight(hoff, high[x][y]/divFactor)
					end
				end
			end
			::continue::
		end
	end
	
	-- update midpoint
	for x = 1, #surf do
		for y = 1, #surf[x] do
			
			local field, tid = neighbourFieldAndTypeIndex(tmap, x-1, y-1)
			
			local rot = globalTileTypes[tid].state(field).z()/90
			
			local hoffc1 = 0
			local hoffc2 = 0
			if types[tid].texmap[field].name == "DIAGONAL" then -- Diagonal special case
				rot = rot + 1
			end
			
			if rot % 2 == 0 then
				hoffc1 = ((y-1)*(2*#surf+1)) + (x-1)
				hoffc2 = (y*(2*#surf+1)) + x
			else
				hoffc1 = ((y-1)*(2*#surf+1)) + x
				hoffc2 = (y*(2*#surf+1)) + (x-1)
			end
			
			local hoffm = ((y-1)*(2*#surf+1)) + #surf + 1 + (x-1)
			
			local h1 = tmap.pointHeight(hoffc1)
			local h2 = tmap.pointHeight(hoffc2)
			local mid = (h2+h1) / 2
			tmap.pointHeight(hoffm, mid)
		end
	end
	
	return tmap
end

function populateRawTex(rawtex)
	for i, v in ipairs(globalTileTypes) do
		rawtex.addType(v)
	end
end