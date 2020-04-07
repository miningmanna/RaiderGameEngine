
rge.assetSource("dir", "Data/CAPTAIN")
rge.assetSource("dir", "Data")
rge.assetSource("dir", "NonGitignoreData")
rge.assetSource("wad", "Data/test2.wad")

require "connectTex"
groundConnectMap = makeGroundConnections()

rge.assetSource("dir", "ldraw")
rge.assetSource("dir", "ldraw/models")
rge.assetSource("dir", "ldraw/parts")
rge.assetSource("dir", "ldraw/p")

rge.window.setSize(1920, 1080)
rge.window.center()
rge.window.show()

rootNode = rge.newDrawNode()
--rootNode.model(rge.get("Buildings/Docks/dock2.lwo"))

lights = rge.newLightGroup()
camera = rge.newCamera()
camera.setFrustrum(60.0, rge.window.getWidth()/rge.window.getHeight(), 0.1, 1000.0)
--aspect = rge.window.getWidth()/rge.window.getHeight()
--camera.setOrtho(-10*aspect, 10*aspect, -10, 10, 0.01, 10000)

rge.clearColor(100, 100, 100)

dirVec = rge.newVector3()
dirVec.y(-1)
dirVec.z(1)
directional = rge.newDirectionalLight()
directional.direction(dirVec)
directional.intensity(0.5)
directional.color(255, 255, 255)

ambient = rge.newAmbientLight()
ambient.intensity(0.5)
lights.add(ambient)
lights.add(directional)

conf = rge.get("Lego.cfg")
if(conf == nil) then
	print("FOR FUCKS SAKE!")
else
	print("Getting: Lego*/Main/RenameReplace")
	print(conf.getValue("Lego*/Main/RenameReplace"))
end

--[[moveSet = {}

move = rge.newMove()
move.loop(false)
move.length(8)
times = move.times()
keys = move.keys()

times[1] = 0
times[2] = 4
times[3] = 8


keys[1] = rge.newMatrix4().identity()
keys[2] = rge.newMatrix4().identity()
keys[2].translate(rge.newVector3(0, 0, -60))
keys[3] = rge.newMatrix4().identity()

move.applyFrames()
moveSet.move = move

move = rge.newMove()
move.length(8)
move.loop(true)
times = move.times()
keys = move.keys()

times[1] = 0
times[2] = 4
times[3] = 8


keys[1] = rge.newMatrix4().identity()
keys[2] = rge.newMatrix4().identity()
keys[2].translate(rge.newVector3(-60, 0, 0))
keys[3] = rge.newMatrix4().identity()

move.applyFrames()
moveSet.b = {}
moveSet.b.move = move

bNode = rge.newDrawNode()
bNode.model(rge.get("Buildings/Barracks/LPbarracks.lwo"))
bNode.name("b")
rootNode.addSubNode(bNode)
rootNode.setMoves(moveSet)

]]--

lws = rge.get("NEW_Captain_Point_E.lws")

local function genNodeTree(root, nodeNames, models)
	
	for k,v in pairs(nodeNames) do
		if k ~= "name" then
			local node = rge.newDrawNode()
			node.name(k)
			print("LUA NODE: "..k)
			if models[k].model ~= nil then
				node.model(rge.get(models[k].model))
			end
			root.addSubNode(node)
			genNodeTree(node, v, models[k])
		end
	end
end

local function setLoop(moveSet, val)
	
	if moveSet.move ~= nil then
		moveSet.move.loop(val)
	end
	
	for k,v in pairs(moveSet) do
		if k ~= "move" then
			setLoop(v, val)
		end
	end
	
end

genNodeTree(rootNode, lws.nodes, lws.models)

setLoop(lws.moveSet, true)
rootNode.setMoves(lws.moveSet)

mouseSens = 0.005

function update(dt)
	
	if rge.input.isDown("ESC") then
		rge.window.setShouldClose(true)
	end
	
	
	if rge.input.isDown("LEFT_SHIFT") then
		speed = 10
	else
		speed = 2
	end
	
	if rge.input.isDown("W") then
		camera.moveZ(speed*dt)
	end
	if rge.input.isDown("A") then
		camera.moveX(-speed*dt)
	end
	if rge.input.isDown("S") then
		camera.moveZ(-speed*dt)
	end
	if rge.input.isDown("D") then
		camera.moveX(speed*dt)
	end
	if rge.input.isDown("SPACE") then
		camera.moveY(speed*dt)
	end
	if rge.input.isDown("LEFT_CTRL") then
		camera.moveY(-speed*dt)
	end
	
	camera.rotateHorizontal	(mouseSens * rge.input.getMouseDX())
	camera.rotateVertical	(mouseSens * rge.input.getMouseDY())
	
	camera.update()
	
	rge.use(camera)
	rge.use(lights)
	
	rootNode.advance(dt)
	rge.render(rootNode)
	rge.render(tileMapNode)

end

rge.registerEvent("update", update);

surfmapPath = conf.getValue("Lego*/Levels/Tutorial04/TerrainMap")
surfmapPath = string.sub(surfmapPath, 23, -1)
surfmap = rge.get(surfmapPath)
--surfmap = rge.get("Level09/Surf_09.map")

pathmapPath = conf.getValue("Lego*/Levels/Tutorial04/PathMap")
pathmapPath = string.sub(pathmapPath, 23, -1)
pathmap = rge.get(pathmapPath)
--pathmap = rge.get("Level09/Path_09.map")



tileTypes = {
	{
		id = 1,
		x = 0,
		y = 5,
		connects = { 1 }
	},
	{
		id = 4,
		x = 0,
		y = 2
	},
	{
		id = 5,
		x = 0,
		y = 0
	},
	{
		id = 6,
		x = 4,
		y = 6
	},
	{
		id = 9,
		x = 4,
		y = 5
	},
}

rawTileMap = rge.newRawTileMap(#surfmap, #surfmap[1])

for x = 1, #surfmap do
	for y = 1, #surfmap[x] do
		if pathmap[x][y] == 2 then
			rawTileMap.tile(x-1, y-1, 99)
		else
			rawTileMap.tile(x-1, y-1, surfmap[x][y])
		end
	end
end

rawTileMapTexture = rge.newRawTileMapTexture(8, 8)
for x = 0,7,1 do
	for y = 0,7,1 do
		rawTileMapTexture.texture(x, y, string.format('IceSplit/ICE%d%d.BMP', x, y))
	end
end

for i, v in ipairs(tileTypes) do
	t = rge.newTileType()
	t.id(v.id)
	local stateV = rge.newVector3()
	stateV.x(v.x)
	stateV.y(v.y)
	for i = 0,255,1 do
		t.state(i, stateV)
	end
	if(v.connects ~= nil) then
		print("AYOAYO")
		t.connects(v.connects)
		stateV.x(7)
		stateV.y(0)
		t.state(255, stateV)
	end
	rawTileMapTexture.addType(t)
end

ppt = rge.newTileType()
ppt.id(99)
ppt.connects({ 99 })
pptTexMap =
{
	XCONNECT = { x = 6, y = 0 },
	TCONNECT = { x = 6, y = 4 },
	ICONNECT = { x = 6, y = 2 },
	LCONNECT = { x = 6, y = 3 },
	ECONNECT = { x = 6, y = 5 }
}
local pptstateV = rge.newVector3()
for i = 0,255 do
	local connect = groundConnectMap[i]
	pptstateV.x(pptTexMap[connect.name].x)
	pptstateV.y(pptTexMap[connect.name].y)
	pptstateV.z(connect.rot)
	ppt.state(i, pptstateV)
end
rawTileMapTexture.addType(ppt)

rawTileMap.texture(rawTileMapTexture)

tileMap = rge.newTileMap(rawTileMap)

tileMapNode = rge.newDrawNode()
tileMapNode.model(tileMap)

for y = 1, #surfmap[1] do
	for x = 1,#surfmap  do
		io.write(string.format("%d ", surfmap[x][y]))
	end
	io.write("\n")
end




