
class GameResultInfo:
    def __init__(self):
        self.gameIsFinished = False
        self.statusStr = 'NA'
        self.numGamesWon = -1
        self.numGamesLost = -1
        self.actualGameID = -1
        self.replayURL = ''

    def isGameFinished(self):
        return self.gameIsFinished

    def setIsGameFinished(self, gameStatus):
        self.gameIsFinished = gameStatus

    def setGameStatusStr(self, statusStr):
        self.statusStr = statusStr

    def getGameStatusStr(self):
        return self.statusStr

    def setReplayURL(self, replayURL):
        self.replayURL = replayURL

    def getReplayURL(self):
        return self.replayURL

    def setGameInfo(self, numGamesWon, numGamesLost):
        self.numGamesWon = numGamesWon
        self.numGamesLost = numGamesLost


    def getGameInfo(self):
        return (self.numGamesWon, self.numGamesLost)

    def setGameID(self, actualGameID):
        self.actualGameID = actualGameID

    def getGameID(self):
        return self.actualGameID



