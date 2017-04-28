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

//Sound options
new GameConstant("useSoundDebug", false);

//Geometry options
new GameConstant("showTextures", true);
new GameConstant("showWireframe", false);
new GameConstant("useDisplayLists", false);

//GUI options
new GameConstant("useFullScreen", false);