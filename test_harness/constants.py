from enum import Enum
from file_operations import getJWTToken, getCurrentGameVersion

DEFAULT_MAPS = ["DefaultMedium", "AceOfSpades", "Ambush", "Canals", "CH3353C4K3F4CT0RY", "Duck", "Fountain", "Hockey", "Rivers", "Soccer"]

ALL_MAPS = ["DefaultHuge", "DefaultLarge", "DefaultMedium", "DefaultSmall", "AceOfSpades", "Alien", "Ambush", "Battlecode24",
            "BigDucksBigPond", "Canals", "CH3353C4K3F4CT0RY", "Duck", "Fountain", "Hockey", "HungerGames", "MazeRunner",
            "Rivers","Snake", "Soccer", "SteamboatMickey", "Yinyang", "BedWars", "Bunkers", "Checkered","Diagonal", "Divergent",
            "EndAround","FloodGates", "Foxes", "Fusbol", "GaltonBoard", "HeMustBeFreed", "Intercontinental", "Klein",
            "QueenOfHearts","QuestionableChess", "Racetrack", "Rainbow", "TreeSearch"]

JWT_TOKEN = getJWTToken()
BATTLECODE_URL = "https://api.battlecode.org/api/compete/bc24/request/"
HEADERS = {"authority": "api.battlecode.org",
           "accept": "application/json, text/javascript, */*; q=0.01",
           "accept-language": "en-US,en;q=0.9",
           "authorization": f"Bearer {JWT_TOKEN}",
           "content-type": "application/json",
           "origin": "https://play.battlecode.org",
           "referer": "https://play.battlecode.org/",
           "sec-fetch-mode": "cors",
           "sec-fetch-site": "same-site",
           "user-agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"}

GAME_VERSION = getCurrentGameVersion()
REPLAYS_WATCH_BASE_URL = f'https://releases.battlecode.org/client/battlecode24/{GAME_VERSION}/index.html?gameSource='

class PlayerOrder(Enum):
    REQUESTER_FIRST = '+' # This is teamA
    REQUESTER_LAST = '-'  # This is teamB
    ALTERNATING = '?'

class PlayerIndex(Enum):
    REQUESTER_FIRST = 0 # This is teamA
    REQUESTER_LAST = 1  # This is teamB

# 5 seconds between refreshing and getting the results of a game
MATCH_RESULTS_REFRESH_DELAY = 5

# Difference between the response "Creation time" versus the actual Creation time is usually less than a second. Will make it 3 to be safe.
ACCEPTED_SECONDS_DELTA = 3

OUR_TEAM_NAME = "It's A Trap?"
OUR_TEAM_ID = 766

MAX_MAPS_PER_MATCH = 10 # Up to 10 maps played at a single time
