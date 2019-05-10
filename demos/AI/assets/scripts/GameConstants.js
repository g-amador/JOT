function GameConstant(varName, varValue) {
    print(varName + " = " + varValue);
}

//Input devices options
new GameConstant("useMouse", true);
new GameConstant("useMouseDebug", false);
new GameConstant("useMouseCursor", false);
new GameConstant("useMouseOffScreenPositionShift", false);
new GameConstant("useKeyBoard", true);
new GameConstant("useKeyBoardDebug", false);

//Input HOG2Map files options
new GameConstant("HOG2Maps", true);
new GameConstant("HOG2MapsDebug", false);
new GameConstant("HOG2MapsWithScells", true);

//Geometry options
new GameConstant("showTextures", true);
new GameConstant("showWireframe", false);
new GameConstant("useDisplayLists", false);

//Geometry generators options
new GameConstant("useDiagonalEdges", true);
new GameConstant("useMazeGenerators", false);
new GameConstant("useMazeGeneratorsDebug", false);
new GameConstant("useUniformRegularGridCosts", false);

//Physics options
new GameConstant("useBroadPhaseCollisionDetection", false);
new GameConstant("useLights", true);

//A.I. options
new GameConstant("useSteeringBehaviors", true);
new GameConstant("useSteeringBehaviorsDebug", false);
new GameConstant("useSteeringBehaviorsGroups", false);
new GameConstant("useSteeringBehaviorsPathFollow", false);
new GameConstant("useSteeringBehaviorsLoopPathFollow", false);
new GameConstant("usePathFinders", true);
new GameConstant("usePathFindersDebug", true);
new GameConstant("usePathFindersReconstructPathDebug", false);
new GameConstant("usePathFindersTimeout", false);
new GameConstant("usePathFindersResetVisited", false);
new GameConstant("usePathFindersLowLevelGraph", true);
new GameConstant("usePathFindersAdaptivityTest", false);
new GameConstant("usePathFindersRemoveNeighborsAdaptivityTest", false);

//Scene options
new GameConstant("showGeometries", false);
new GameConstant("useUnreachableSky", false);
new GameConstant("useUnreachableFloor", false);
new GameConstant("Floor", true);
new GameConstant("SkyDome", false);
new GameConstant("SkyBox", true);

//Pathfinders render options
new GameConstant("showPath", true);
new GameConstant("showGraph", false);
new GameConstant("showVisited", true);
new GameConstant("showGraphDoorNodes", true);

//Sound options
new GameConstant("useSoundDebug", false);

//GUI options
new GameConstant("showFPS", true);
new GameConstant("showText", true);
new GameConstant("testMode", false);
new GameConstant("useFullScreen", false);

//Extra options
new GameConstant("useObstaclesConfig1", false);
new GameConstant("useObstaclesConfig2", false);
new GameConstant("useObstaclesConfig3", false);

//Obstacle terrain with 31x31 cells
new GameConstant("_21x21_withObstacles", false);

//Open terrain with 31x31 cells
new GameConstant("_31x31", false);

//Open terrain with 52x52 cells
new GameConstant("_52x52", false);

//Maze with 50x250 cells
new GameConstant("Maze_50x50", false);

//Maze with 100x100 cells
new GameConstant("Maze_100x100", false);

//Maze with 250x250 cells
new GameConstant("Maze_250x250", false);

//A.I. Programming Game A.I. by example book maps.
new GameConstant("AIbook1", false);
new GameConstant("AIbook2", false);

//Dragon Age Origins maps
new GameConstant("useArena2", true);
new GameConstant("useBrc202d", true);
new GameConstant("useBrc203d", false);
new GameConstant("useBrc204d", false);
new GameConstant("useDen011d", false);
new GameConstant("useDen500d", false);
new GameConstant("useDen501d", false);
new GameConstant("useDen602d", false);
new GameConstant("useHrt201n", false);
new GameConstant("useLak304d", true);

//Warcraft III maps
new GameConstant("useBattleground", false);
new GameConstant("useBlastedlands", true);
new GameConstant("useDivideandconquer", false);
new GameConstant("useDragonfire", false);
new GameConstant("useFrostsabre", false);
new GameConstant("useGardenofwar", false);
new GameConstant("useHarvestmoon", false);
new GameConstant("useIsleofdread", true);
new GameConstant("useThecrucible", false);
new GameConstant("useTranquilpaths", true);
