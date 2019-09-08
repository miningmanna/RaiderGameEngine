
rge.assets.registerInputGen("dir", "Data")

rge.assets.registerInputGen("dir", "ldraw")
rge.assets.registerInputGen("dir", "ldraw/models")
rge.assets.registerInputGen("dir", "ldraw/parts")
rge.assets.registerInputGen("dir", "ldraw/p")

rge.window.setSize(1920, 1080)
rge.window.center()
rge.window.show()

rootNode = rge.createDrawNode()
rootNode.setModel(rge.getModel("ldr", "car.ldr"))

lights = rge.createLightGroup()
camera = rge.createCamera()
camera.setFrustrum(60.0, rge.window.getWidth()/rge.window.getHeight(), 0.1, 1000.0)

rge.setClearColor(100, 100, 100)

ambient = rge.createAmbientLight();
ambient.setIntensity(0.2)
lights.addLight(ambient)

