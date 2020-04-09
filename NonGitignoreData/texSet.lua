
local texSetMap = {
}

function initTexSets(cfg)
	local texSets = cfg.getNode("Lego*/Textures")
	
	for k, v in pairs(texSets) do
		
		local function log10(x) return math.log(x)/math.log(10) end
		
		texSetMap[string.upper(k)] =
		{
			width = tonumber(v.surftextwidth),
			height = tonumber(v.surftextheight),
			formatString =	v.texturebasename..
							"%0"..math.ceil(log10(v.surftextwidth)).."d"..
							"%0"..math.ceil(log10(v.surftextheight)).."d.bmp"
		}
		
	end
end

function getTextureSet(texSetName)
	local name = string.upper(string.sub(texSetName, 11, -1))
	
	local texSet = texSetMap[name]
	print("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
	print(name)
	print(texSetName)
	for k, v in pairs(texSetMap) do
		print(v.formatString)
	end
	
	print(texSet.width)
	print(texSet.height)
	
	local rawTileMapTexture = rge.newRawTileMapTexture(texSet.width, texSet.height)
	print(rawTileMapTexture)
	for x = 0,texSet.width-1,1 do
		for y = 0,texSet.height-1,1 do
			rawTileMapTexture.texture(x, y, string.format(texSet.formatString, x, y))
		end
	end
	
	return rawTileMapTexture
end