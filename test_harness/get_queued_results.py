import math
import requests
import time
from collections import defaultdict
from constants import *
from datetime import datetime
from tabulate import tabulate
import os

NUM_RESULTS_PER_PAGE = 10
EXTRA_PAGES_LOADED = 2  #Used in case other ppl queue some games in the meanwhile
BASE_URL = 'https://api.battlecode.org/api/compete/bc24/match/scrimmage/?team_id=766'
class GameStatus(Enum):
    FINISHED = 'OK!'



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

    oppBotExpectedIndex = 1 if queuedGameResponse['side'] == PlayerOrder.REQUESTER_FIRST else 0
    expectedOppBotInfo = currentGameStatus['participants'][oppBotExpectedIndex]


    return expectedOppBotInfo['teamname'] == queuedGameResponse['oppTeamName']


def setGameResultString(gameProgInfo, currentGameStatus, oppBotInfo):
    oppBotName = oppBotInfo['teamname']
    oppBotPlayerIndex = oppBotInfo['player_index'] # either 0 or 1
    gameID = currentGameStatus['id']

    if currentGameStatus['status'] != 'OK!':
        gameProgInfo[oppBotName][oppBotPlayerIndex] = currentGameStatus['status']

    else:
        numGames = len(currentGameStatus['maps'])
        numGamesLost = oppBotInfo['score']
        numGamesWon = numGames - numGamesLost


        victoryStatus = 'T' if numGamesWon == numGamesLost else ('W' if numGamesWon > numGamesLost else 'L')


        gameProgInfo[oppBotName][oppBotPlayerIndex] = f"{victoryStatus}  {numGamesWon}-{numGamesLost} {gameID}"

# Maps [oppBot][side] = ResultString
def displayGames(gameProgInfo):
    dataMatrix = []
    for oppBot in gameProgInfo:
        requesterFirstGameStr = gameProgInfo[oppBot][0]
        requesterLastGameStr = gameProgInfo[oppBot][1]

        rowStr = [oppBot, requesterFirstGameStr, requesterLastGameStr]
        dataMatrix.append(rowStr)

    print(tabulate(dataMatrix, headers= ['Opponent bots', 'Requester First', 'Requester Last'], tablefmt="grid"))




# Matches the games requested with the games in the queue
# matches based on opposingBotName, side, and creation date
def matchGamesFirstTimeTemp(requestedGamesResponse, gameReplaysDict):
    numGamesPlayed = len(requestedGamesResponse) * 2 # List of pairs
    allGamesInfo = getAllResults(numGamesPlayed) # Can parallelize this if needed


    # Maps [oppBot][side] = ResultString
    gameProgInfo = defaultdict(lambda: [None,None])


    completedGamesSet = set() # Is a set of all completed games
    numGamesCompleted = 0
    while numGamesCompleted < numGamesPlayed:

        for game1Info, game2Info in requestedGamesResponse:
            # Yes this is inefficient and I can change into map, but this doesn't actually affect runtime so whatevs

            for currGameResult in allGamesInfo:
                gameMatched = False

                # Match games based on opponent name, player_index, creation time
                if gamesAreMatched(game1Info, currGameResult):
                    setGameResultString(gameProgInfo,currGameResult, currGameResult['participants'][1])
                    gameMatched = True

                elif gamesAreMatched(game2Info, currGameResult):
                    setGameResultString(gameProgInfo,currGameResult, currGameResult['participants'][0])
                    gameMatched = True

                # Check if match is competed, and save the replay url (only once though!)
                if currGameResult['status'] == 'OK!' and gameMatched and currGameResult['id'] not in completedGamesSet:
                    numGamesCompleted += 1
                    completedGamesSet.add(currGameResult['id'])
                    gameReplaysDict[currGameResult['id']] = REPLAYS_WATCH_BASE_URL+ currGameResult['replay_url']

        # Clearing the terminal of prev games
        displayGames(gameProgInfo)
        # Wait for 5 seconds in between refreshing for new list of games
        print(f'{numGamesCompleted} out of {numGamesPlayed} games completed\n')
        allGamesInfo = getAllResults(numGamesPlayed)
        time.sleep(MATCH_RESULTS_REFRESH_DELAY)



def matchGamesFirstTime(requestedGamesResponse):
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


    # Maps [oppBot][side] = ResultString
    gameProgInfo = defaultdict(lambda: [None,None])



    while len(requestToActualID) < numGamesPlayed:

        # These are the games that we've requested, and it's info
        for game1Info, game2Info in requestedGamesResponse:

            # These are the actual games information
            for currGameResult in allGamesInfo:

                # Match games based on opponent name, player_index, creation time
                if gamesAreMatched(game1Info, currGameResult):
                    setGameResultString(gameProgInfo,currGameResult, currGameResult['participants'][1])
                    addToIDMaps(game1Info['id'], currGameResult['id'])

                elif gamesAreMatched(game2Info, currGameResult):
                    setGameResultString(gameProgInfo,currGameResult, currGameResult['participants'][0])
                    addToIDMaps(game2Info['id'], currGameResult['id'])



    displayGames(gameProgInfo)

    return requestToActualID, actualToRequestID

def outputMatchGames(requestedGamesResponse, gameReplaysDict):
    requestToActualID, actualToRequestID = matchGamesFirstTime(requestedGamesResponse)
    lastGameQueued = requestedGamesResponse[-1][1]
    lastGameActualID = requestToActualID[lastGameQueued['id']]
    startingPageNum = getStartingPageNum(lastGameActualID,1)





