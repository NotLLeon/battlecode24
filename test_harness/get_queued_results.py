import math
import requests
import time
from collections import defaultdict
from constants import *
from datetime import datetime
from tabulate import tabulate

from GameResultInfo import GameResultInfo

NUM_RESULTS_PER_PAGE = 10
EXTRA_PAGES_LOADED = 2  #Used in case other ppl queue some games in the meanwhile
BASE_URL = 'https://api.battlecode.org/api/compete/bc24/match/scrimmage/?team_id=766'
class GameStatus(Enum):
    FINISHED = 'OK!'
    QUEUED = 'QUE'
    RUNNING = 'RUN'



def getAllResults(numGames):
    retList = []
    numIters = math.ceil(numGames / NUM_RESULTS_PER_PAGE) + EXTRA_PAGES_LOADED

    for i in range(numIters):
        pageNum = i + 1
        currURL = BASE_URL + f"&page={pageNum}"
        currPageAsJSON = requests.get(currURL, headers=HEADERS).json()
        retList.extend(currPageAsJSON['results'])

    return retList


def getGamesStatusResults(startingPageNum, actualToRequestID):
    gamesMatchedHM = {} # maps actual ID to info about the game (i.e the whole response body for that game)

    pageNum = startingPageNum
    # While not all games have been matched yet
    while len(gamesMatchedHM) < len(actualToRequestID):
        currURL = BASE_URL + f"&page={pageNum}"
        resultsList = requests.get(currURL, headers=HEADERS).json()['results']

        for currResult in resultsList:
            if currResult['id'] in actualToRequestID:
                gamesMatchedHM[currResult['id']] = currResult

        pageNum += 1

    return gamesMatchedHM




# Page num should start at 1
def getStartingPageNum(gameID, prevStartingPageNum):

    pageNum = prevStartingPageNum
    while True:

        currURL = BASE_URL + f"&page={pageNum}"
        currPageAsJSON = requests.get(currURL, headers=HEADERS).json()
        for result in currPageAsJSON['results']:
            if result['id'] == gameID:
                return pageNum

        pageNum += 1


def isCreationTimeMatching(dateTime1Str, dateTime2Str):
    dateTime1, dateTime2 = datetime.fromisoformat(dateTime1Str), datetime.fromisoformat(dateTime2Str)
    timeDiff = abs(dateTime1 - dateTime2)

    return timeDiff.total_seconds() < ACCEPTED_SECONDS_DELTA


def gamesAreMatched(queuedGameResponse, currentGameStatus):
    if not isCreationTimeMatching(queuedGameResponse['creationDateTime'], currentGameStatus['created']):
        return False

    queuedMaps, currGameMaps = queuedGameResponse['mapsList'], currentGameStatus['maps']

    if set(queuedMaps) != set(currGameMaps):
        return False


    # oppBotExpectedIndex = 1 if queuedGameResponse['side'] == PlayerOrder.REQUESTER_FIRST else 0 # This has issue, since we need player_index, not just position in list

    team1Info, team2Info = currentGameStatus['participants']
    oppBotTeamInfo = team1Info if team1Info['team'] != OUR_TEAM_ID else team2Info
    if oppBotTeamInfo['teamname'] != queuedGameResponse['oppTeamName']:
        return False

    expectedOppPlayerIndex = 1 if queuedGameResponse['side'] == PlayerOrder.REQUESTER_FIRST else 0
    return oppBotTeamInfo['player_index'] == expectedOppPlayerIndex



def setGameResultObj(gameProgInfo, currGameResult):

    # Gets the opposite team
    oppBotInfo = currGameResult['participants'][0] if currGameResult['participants'][0]['team'] != OUR_TEAM_ID else currGameResult['participants'][1]
    oppBotName = oppBotInfo['teamname']
    if oppBotName == "It's A Trap? ":
        print('This should not happen!')
    oppBotPlayerIndex = oppBotInfo['player_index'] # either 0 or 1
    gameID = currGameResult['id']

    if gameID not in gameProgInfo[oppBotName][oppBotPlayerIndex]:
        newGameResultObj = GameResultInfo()
        newGameResultObj.setGameID(gameID)
        gameProgInfo[oppBotName][oppBotPlayerIndex][gameID] = newGameResultObj

    if currGameResult['status'] != 'OK!':
        gameProgInfo[oppBotName][oppBotPlayerIndex][gameID].setGameStatusStr(currGameResult['status'])

    else:
        numGames = len(currGameResult['maps'])
        numGamesLost = oppBotInfo['score']
        numGamesWon = numGames - numGamesLost

        gameInfoObj: GameResultInfo = gameProgInfo[oppBotName][oppBotPlayerIndex][gameID]
        gameInfoObj.setIsGameFinished(True)
        gameInfoObj.setGameStatusStr('OK!')
        gameInfoObj.setGameInfo(numGamesWon, numGamesLost)
        gameInfoObj.setReplayURL(currGameResult['replay_url'])

# Where gameProgInfo: [oppBot][sideOfOpponent][gameID] = GameResultInfo
# TODO: reformat the output display here!
# Go through the output of the games, and get the total number of games won and lost.
# Have 'IPR' as the game status if only some of the games have been completed.
# gameProgInfo[oppBotName][oppBotPlayerIndex][gameID] = GameResultInfo(), where gameID is the key to the dict
def displayGames(gameProgInfo):
    def getGamesTotalResultStr(gameResultsHM):
        isAllGamesCompleted = all(gameInfoObj.isGameFinished() for gameID,gameInfoObj  in gameResultsHM.items())
        if not isAllGamesCompleted:
            gameStatusList = [currGameResultObj.getGameStatusStr() for currGameResultObj in gameResultsHM.values()]
            return ','.join(gameStatusList)
        else:
            numWins, numLosses = 0,0
            gameIDList = []
            for currGameResultObj in gameResultsHM.values():
                currWinsCount, currLossesCount = currGameResultObj.getGameInfo()
                numWins += currWinsCount
                numLosses += currLossesCount
                gameIDList.append(str(currGameResultObj.getGameID()))

            gameStatusCode = 'T' if numWins == numLosses else ('W' if numWins > numLosses else 'L')

            return f"{gameStatusCode} {numWins}-{numLosses} {','.join(gameIDList)}"



    dataMatrix = []
    for oppBot in gameProgInfo:
        requesterFirstGamesHM = gameProgInfo[oppBot][1]
        requesterLastGamesHM = gameProgInfo[oppBot][0]

        rowStr = [oppBot, getGamesTotalResultStr(requesterFirstGamesHM), getGamesTotalResultStr(requesterLastGamesHM)]
        dataMatrix.append(rowStr)

    print(tabulate(dataMatrix, headers= ['Opponent bots', 'Requester First', 'Requester Last'], tablefmt="grid"))


def setAndDisplayGameFromHM(currAllGamesStatusHM, gameProgInfo):
    numGamesCompleted = 0
    for currGameStatus in currAllGamesStatusHM.values():
        if currGameStatus['status'] == GameStatus.FINISHED.value:
            numGamesCompleted += 1
        setGameResultObj(gameProgInfo, currGameStatus)

    displayGames(gameProgInfo)

    return numGamesCompleted

def matchGamesFirstTime(requestedGamesResponse, gameProgInfo):
    def addToIDMaps(requestID, actualID):
        if requestID in requestToActualID or actualID in actualToRequestID:
            print("ERROR: Setting the ID map should only happen once!")
        requestToActualID[requestID] = actualID
        actualToRequestID[actualID] = requestID

    numGamesPlayed = len(requestedGamesResponse) * 2 # List of pairs
    allGamesInfo = getAllResults(numGamesPlayed) # Can parallelize this if needed


    # Both these dicts only contain IDs of our games, not any extra!
    requestToActualID = {} # Maps the request's gameID to the actual ID
    actualToRequestID = {} # Maps the actual ID to request's ID



    while len(requestToActualID) < numGamesPlayed:

        # These are the games that we've requested, and it's info
        for game1Info, game2Info in requestedGamesResponse:

            # These are the actual games information
            for currGameResult in allGamesInfo:

                # Match games based on opponent name, player_index, creation time
                if gamesAreMatched(game1Info, currGameResult):

                    setGameResultObj(gameProgInfo, currGameResult)
                    addToIDMaps(game1Info['id'], currGameResult['id'])

                elif gamesAreMatched(game2Info, currGameResult):
                    setGameResultObj(gameProgInfo, currGameResult)
                    addToIDMaps(game2Info['id'], currGameResult['id'])



    return requestToActualID, actualToRequestID

def setReplaysDict(gameReplaysDict, currAllGamesStatusHM):
    for gameID in currAllGamesStatusHM:
        gameReplaysDict[gameID] = REPLAYS_WATCH_BASE_URL + currAllGamesStatusHM[gameID]['replay_url']


def outputMatchGames(requestedGamesResponse, gameReplaysDict):

    # Maps [oppBot][side][actualGameID] = gameInfoDict
    gameProgInfo = defaultdict(lambda: [{}, {}])

    requestToActualID, actualToRequestID = matchGamesFirstTime(requestedGamesResponse,gameProgInfo)
    lastGameQueued = requestedGamesResponse[-1][1]
    # Setting my own team name happens here

    lastGameActualID = requestToActualID[lastGameQueued['id']]
    startingPageNum = getStartingPageNum(lastGameActualID,1)
    #TODO Now need to loop and call getGamesStatusResults(startingPageNum, actualToRequestID) a bunch, until all games have been completed

    numGamesTotal = len(requestToActualID)

    numGamesCompleted = 0

    while numGamesCompleted != numGamesTotal:
        currAllGamesStatusHM = getGamesStatusResults(startingPageNum, actualToRequestID)
        numGamesCompleted = setAndDisplayGameFromHM(currAllGamesStatusHM, gameProgInfo)


        startingPageNum = getStartingPageNum(lastGameActualID, startingPageNum) # Refreshing the starting page to go from
        setReplaysDict(gameReplaysDict, currAllGamesStatusHM) # Setting the replays dict stuff
        print(f'{numGamesCompleted} out of {numGamesTotal} games completed\n')

        time.sleep(MATCH_RESULTS_REFRESH_DELAY)










