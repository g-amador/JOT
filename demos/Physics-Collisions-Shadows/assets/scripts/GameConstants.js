function GameConstant(varName, varValue) {
    print(varName + " = " + varValue);
}

//Input devices options
new GameConstant("useMouse", true);
new GameConstant("useMouseDebug", false);
new GameConstant("useMouseCursor", false);
new GameConstant("useMouseOffScreenPositionShift", true);
new GameConstant("useKeyBoard", true);
new GameConstant("useKeyBoardDebug", false);

//Geometry options
new GameConstant("showTextures", true);
new GameConstant("showWireframe", false);
new GameConstant("useDisplayLists", false);

//Physics options
new GameConstant("useBroadPhaseCollisionDetection", true);
new GameConstant("useNarrowPhaseCollisionDetection", false);
new GameConstant("showPlanarShadows", false);
new GameConstant("showShadowMaps", false);
new GameConstant("useLights", true);

//Z-Buffer
new GameConstant("showZBuffer", false);

//Scene options
new GameConstant("showGeometries", true);
new GameConstant("useUnreachableSky", false);
new GameConstant("useUnreachableFloor", false);
new GameConstant("Floor", true);
new GameConstant("SkyDome", true);
new GameConstant("SkyBox", false);

//Sound options
new GameConstant("useSoundDebug", false);

//GUI options
new GameConstant("showFPS", true);
new GameConstant("showText", true);
new GameConstant("testMode", false);
new GameConstant("useFullScreen", false);