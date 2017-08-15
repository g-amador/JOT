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

//Input HOG2Map files options
new GameConstant("HOG2Maps", false);
new GameConstant("HOG2MapsWithScells", false);

//Geometry options
new GameConstant("showTextures", true);
new GameConstant("showWireframe", false);
new GameConstant("useDisplayLists", false);

//Geometry generators options
new GameConstant("useTerrainGenerators", true);
new GameConstant("useTerrainGeneratorsWithMiddleMinimumHeight", true);
new GameConstant("useMazeGenerators", false);
new GameConstant("useMazeGeneratorsDebug", true);

//Physics options
new GameConstant("useBroadPhaseCollisionDetection", true);
new GameConstant("useNarrowPhaseCollisionDetection", false);
new GameConstant("useLights", true);

//Scene options
new GameConstant("showGeometries", true);
new GameConstant("useUnreachableSky", false);
new GameConstant("useUnreachableFloor", false);
new GameConstant("Floor", true);
new GameConstant("SkyDome", false);
new GameConstant("SkyBox", false);

//Sound options
new GameConstant("useSoundDebug", false);

//GUI options options
new GameConstant("showFPS", true);
new GameConstant("showText", true);
new GameConstant("testMode", true);
new GameConstant("useFullScreen", false);