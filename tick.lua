
speed = 2.0
mouseSens = 0.005

if rge.input.isDown("ESC") then
	rge.window.setShouldClose(true)
end

if rge.input.isDown("W") then
	camera.moveZ(speed)
end
if rge.input.isDown("A") then
	camera.moveX(-speed)
end
if rge.input.isDown("S") then
	camera.moveZ(-speed)
end
if rge.input.isDown("D") then
	camera.moveX(speed)
end
if rge.input.isDown("SPACE") then
	camera.moveY(speed)
end
if rge.input.isDown("LEFT_CTRL") then
	camera.moveY(-speed)
end

camera.rotateHorizontal	(mouseSens * rge.input.getMouseDX())
camera.rotateVertical	(mouseSens * rge.input.getMouseDY())

camera.update()

rge.useCamera(camera)
rge.useLightGroup(lights)

rge.queueRender(rootNode)
