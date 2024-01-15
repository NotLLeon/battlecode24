from tabulate import tabulate
from collections import defaultdict


tableHeaders = ['Winner', 'OpposingBot', 'Maps', 'Reason For Winning', 'Round', 'TeamA']
def transformGameDataAsRows(gamesResList):
    retList = []
    for currDict in gamesResList:
        currRow = [currDict['winningBot'], currDict['opposingBotName'], currDict['map'],
                   currDict['winningReason'], currDict['round'], currDict['teamA']]
        retList.append(currRow)
    return retList

def printTabulate(data, mapsList):
    print(tabulate(data, headers=mapsList, tablefmt="grid"))

# use this for the grid instead of the table
# Maps [opposing_bot][map] -> (ResultOnSideA, ResultOnSideB)
def transformGameDataAsNestedMap(gamesResList):
    retDict = defaultdict(lambda: defaultdict(list))

    for currDict in gamesResList:
        retDict[currDict['opposingBotName']][currDict['map']].append(currDict)
    return retDict


# Funcs below are for generating the code of the game output
def winLossStr(gameDict):
    return 'W' if gameDict['currBotLabel'] == gameDict['winner'] else 'L'

def winnerReasonStr(gameDict):

    if gameDict['winningReason'] == 'Captured All Flags':
        return 'A_flags'
    if gameDict['winningReason'] == 'captured more flags':
        return 'M_flags'
    if gameDict['winningReason'] == 'higher sum of all unit levels':
        return 'levels'
    if gameDict['winningReason'] == 'more crumbs':
        return 'crumbs'
    # This should probably never happen
    if 'random' in gameDict['winningReason']:
        return 'random'

    # Shouldn't get here
    return 'UNKNOWN'


# Should only have 2 games here
def generateGameOutputCode(gameDictList):

    teamAStr = '*'
    teamBStr = '*'

    for gameDict in gameDictList:
        if gameDict['currBotLabel'] == 'A':
            # Formatted as {W|L} {roundNum} {winningReason}
            teamAStr = f"{winLossStr(gameDict)} {gameDict['round']} {winnerReasonStr(gameDict)}"
        if gameDict['currBotLabel'] == 'B':
            teamBStr = f"{winLossStr(gameDict)} {gameDict['round']} {winnerReasonStr(gameDict)}"

    return teamAStr + ', ' + teamBStr


def printTabulateAsGrid(nestedMap, mapsList):
    mapsListIndex = {}
    idx = 1
    for currMap in mapsList:
        mapsListIndex[currMap] = idx
        idx += 1

    dataMatrix = []
    for oppBot in nestedMap:
        rowOutput = [None] * (len(nestedMap[oppBot]) + 1)
        rowOutput[0] = oppBot

        for currMap in nestedMap[oppBot]:
            currOutputCode = generateGameOutputCode(nestedMap[oppBot][currMap])
            currMapIndex = mapsListIndex[currMap]
            rowOutput[currMapIndex] = currOutputCode
        dataMatrix.append(rowOutput)



    print(tabulate(dataMatrix, headers= [' '] + mapsList, tablefmt="grid"))
