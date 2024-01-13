detours= [[0]], [[1, -1]], [[1, 0, -1], [-2, 0, 2]], [
    [1, 0, 0, -1],
    [1, 1, -1, -1],
    [2, 0, 0, -2],
], [
    [1, 0, 0, 0, -1],
    [2, 0, 0, 0, -2],
    [1, 1, 0, -1, -1],
    [2, 2, 0, -2, -2],
    [3, 1, 0, -1, -2],
    [3, 1, 0, -1, -1],
], [
    [1, 0, 0, 0, 0, -1],
    [2, 0, 0, 0, 0, -2],
    [1, 1, 0, 0, -1, -1],
    [2, 2, 0, 0, -2, -2],
    [1, 1, 1, -1, -1, -1],
]

for detour in detours:
    for dirs in detour:
        tabs = ''
        for d in dirs:
            first = tabs == ""
            print(f'{tabs}curDir = rotateInt(bestDir, {d});')
            if first:
                print('firstDir = curDir;')
            print(f'{tabs}loc = {"curLoc" if first else "loc"}.add(curDir);')
            print(f'{tabs}if(isMoveable(loc, {"true" if first else "false"}))' + '{')
            tabs += '\t'
        print(f'{tabs}return firstDir;')
        close = len(tabs)
        for i in range(close):
            tabs = tabs[0:-1]
            print(f'{tabs}' + '}')

        tabs = ''
        for d in dirs:
            first = tabs == ""
            print(f'{tabs}curDir = rotateInt(bestDir, {-d});')
            if first:
                print('firstDir = curDir;')
            print(f'{tabs}loc = {"curLoc" if first else "loc"}.add(curDir);')
            print(f'{tabs}if(isMoveable(loc, {"true" if first else "false"}))' + '{')
            tabs += '\t'
        print(f'{tabs}return firstDir;')
        close = len(tabs)
        for i in range(close):
            tabs = tabs[0:-1]
            print(f'{tabs}' + '}')
