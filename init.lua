
rge.assets.registerInputGen("dir", "Data")

rge.assets.registerInputGen("dir", "ldraw")
rge.assets.registerInputGen("dir", "ldraw/models")
rge.assets.registerInputGen("dir", "ldraw/parts")
rge.assets.registerInputGen("dir", "ldraw/p")

rge.window.setSize(1920, 1080)
rge.window.center()
rge.window.show()

rootNode = rge.newDrawNode()
rootNode.setModel(rge.getModel("ldr", "car.ldr"))

lights = rge.newLightGroup()
camera = rge.newCamera()
camera.setFrustrum(60.0, rge.window.getWidth()/rge.window.getHeight(), 0.1, 1000.0)

rge.clearColor(100, 100, 100)

dirVec = rge.newVector3()
dirVec.y(-1)
dirVec.z(1)
directional = rge.newDirectionalLight()
directional.direction(dirVec)
directional.intensity(0.5)
directional.color(255, 255, 255)

ambient = rge.newAmbientLight()
ambient.intensity(0.1)
lights.add(ambient)
lights.add(directional)

