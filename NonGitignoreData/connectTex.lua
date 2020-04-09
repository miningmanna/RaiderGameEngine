
function print2DTable(t)
	for x = 1, #t do
		for y = 1, #t[x] do
			io.write(t[x][y])
		end
		io.write("\n")
	end
end

ringTable =
{
	{x = 1, y = 1},
	{x = 1, y = 2},
	{x = 1, y = 3},
	{x = 2, y = 3},
	{x = 3, y = 3},
	{x = 3, y = 2},
	{x = 3, y = 1},
	{x = 2, y = 1}
}

function rotateTable(t, r)
	local o = 8 - r*2
	local res =
	{
		{-1, -1, -1},
		{-1, -1, -1},
		{-1, -1, -1}
	}
	res[2][2] = t[2][2]
	
	for i = 0, 7 do
		op = ringTable[1 + i]
		np = ringTable[1 + ((o+i)%8)]
		res[np.x][np.y] = t[op.x][op.y]
	end
	
	return res
end

function makePerms(mask)
	
	local baseMask = 0
	
	local bitOffseti = 1
	local bitOffset = {}
	local offset = 0
	for y = 1,3 do
		for x = 1,3 do
			if x == 2 and y == 2 then
				goto skip
			end
			
			if mask[x][y] == 1 then
				bitOffset[bitOffseti] = offset
				bitOffseti = bitOffseti +1
			end
			
			if mask[x][y] == 2 then
				baseMask = bit32.bor(baseMask , bit32.lshift( 1, offset ))
			end
			
			offset = offset + 1
			::skip::
		end
	end
	local lim = math.pow(2, #bitOffset)
	local res = {}
	for i = 1,lim do
		local perm = i-1
		local permMask = 0
		for i = 1,#bitOffset do
			permMask = bit32.bor(permMask, bit32.lshift(bit32.band(bit32.rshift(perm, (i-1)), 1), bitOffset[i]))
		end
		res[i] = bit32.bor(baseMask, permMask)
	end
	return res
end

cliffPatterns =
{
	{
		name = "ERROR",
		mask = {
			{1, 1, 1},
			{1, 0, 1},
			{1, 1, 1}
		}
	},
	{
		name = "ROOF",
		mask = {
			{2, 2, 2},
			{2, 0, 2},
			{2, 2, 2}
		}
	},
	{
		name = "FLAT",
		mask = {
			{2, 2, 2},
			{2, 0, 2},
			{1, 3, 1}
		}
	},
	{
		name = "OCORNER",
		mask = {
			{2, 2, 1},
			{2, 0, 3},
			{1, 3, 1}
		}
	},
	{
		name = "ICORNER",
		mask = {
			{2, 2, 2},
			{2, 0, 2},
			{2, 2, 3}
		}
	},
	{
		name = "DIAGONAL",
		mask = {
			{2, 2, 3},
			{2, 0, 2},
			{3, 2, 2}
		}
	}
}

groundPatterns =
{
	{
		name = "ECONNECT",
		mask = {
			{1, 3, 1},
			{3, 0, 3},
			{1, 2, 1}
		}
	},
	{
		name = "TCONNECT",
		mask = {
			{1, 2, 1},
			{3, 0, 2},
			{1, 2, 1}
		}
	},
	{
		name = "LCONNECT",
		mask = {
			{1, 3, 1},
			{3, 0, 2},
			{1, 2, 1}
		}
	},
	{
		name = "ICONNECT",
		mask = {
			{1, 2, 1},
			{3, 0, 3},
			{1, 2, 1}
		}
	},
	{
		name = "XCONNECT",
		mask = {
			{1, 2, 1},
			{2, 0, 2},
			{1, 2, 1}
		}
	},
	{
		name = "NCONNECT",
		mask = {
			{1, 3, 1},
			{3, 0, 3},
			{1, 3, 1}
		}
	}
}

function makeConnections(patterns)
	local connects = {}
	for i = 1, 256 do
		connects[i] = {}
	end
	
	for i = 1,#patterns do
		local pattern = patterns[i]
		for _rot = 3,0,-1 do
			local perms = makePerms(rotateTable(pattern.mask, _rot))
			for j = 1,#perms do
				connects[perms[j]] = { name = pattern.name, rot = _rot*90 }
			end
		end
	end
	
	return connects
end

function makeGroundConnections()
	return makeConnections(groundPatterns)
end



function makeCliffConnections()
	return makeConnections(cliffPatterns)
end
