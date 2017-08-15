function GameConstant(varName, varValue) {
    print(varName + " = " + varValue);
}

//Input devices options
new GameConstant("useMouse", true);
new GameConstant("useMouseCursor", false);
new GameConstant("useKeyBoard", true);

//Geometry options
new GameConstant("showTextures", false);
new GameConstant("showWireframe", false);

//Physics options
new GameConstant("useBroadPhaseCollisionDetection", true);
new GameConstant("useNarrowPhaseCollisionDetection", false);

//Ripple options        
new GameConstant("useRain", false);
new GameConstant("useRipple", true);

//Lagrangian simulators options
new GameConstant("useParticleSystems3D", true);

//Z-Buffer
new GameConstant("showZBuffer", false);

//Shadows options        
new GameConstant("showPlanarShadows", false);
new GameConstant("showShadowMaps", false);

//Lights options
new GameConstant("useLights", true);

//Scene options
new GameConstant("showGeometries", true);
new GameConstant("Floor", false);
new GameConstant("SkyDome", true);
new GameConstant("SkyBox", false);
new GameConstant("addSkyBoxCeil", true);
new GameConstant("addSkyBoxWalls", true);