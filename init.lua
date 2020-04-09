
rge.assetSource("dir", "Data/CAPTAIN")
rge.assetSource("dir", "Data")
rge.assetSource("dir", "NonGitignoreData")
rge.assetSource("wad", "Data/test2.wad")

require "tileMapObj"

rge.assetSource("dir", "ldraw")
rge.assetSource("dir", "ldraw/models")
rge.assetSource("dir", "ldraw/parts")
rge.assetSource("dir", "ldraw/p")

rge.window.setSize(1920, 1080)
rge.window.center()
rge.window.show()

modelShader = rge.assets.getShader("lwo")
tilemapShader = rge.assets.getShader("tilemap")

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

initTexSets(conf)

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
				local m = rge.get(models[k].model)
				if m ~= nil then
					m.shader(modelShader)
					node.model(m)
				end
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


levelpath = "Lego*/Levels/Level22/"
mapObj = getMap(conf, levelpath)
mapObj.tmap.shader(tilemapShader)

tileMapNode = rge.newDrawNode()
tileMapNode.model(mapObj.tmap)

changerate = 20.0
changedt = 1/changerate

st = 0
x = -1
y = 0

function update(dt)
	
	st = st + dt
	
	if st > changedt then
		st = math.fmod(st, changedt)
		x = x+1
		if x >= #mapObj.surf then
			x = 0
			y = y + 1
			if y >= #mapObj.surf[1] then
				y = 0
			end
		end
		
		mapObj.tmap.tile(x, y, 30)
		
	end
	
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

--levelpath = "Lego*/Levels/Tutorial04/"

surfmapPath = conf.getValue(levelpath.."TerrainMap")
--surfmapPath = string.sub(surfmapPath, 23, -1)
surfmap = rge.get(surfmapPath)
--surfmap = rge.get("Level09/Surf_09.map")

--pathmapPath = conf.getValue(levelpath.."PathMap")
--pathmapPath = string.sub(pathmapPath, 23, -1)
--pathmap = rge.get(pathmapPath)
--pathmap = rge.get("Level09/Path_09.map")

--heightmapPath = conf.getValue(levelpath.."SurfaceMap")
--heightmap = rge.get(heightmapPath)



texSet = conf.getValue(levelpath.."TextureSet")
print("Texset:")
print(texSet)

--rawTileMap = loadFromMaps(surfmap, heightmap, pathmap)

for y = 1, #surfmap[1] do
	for x = 1,#surfmap  do
		io.write(string.format("%d ", surfmap[x][y]))
	end
	io.write("\n")
end




