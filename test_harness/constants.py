from enum import Enum
from file_operations import getJWTToken, getCurrentGameVersion

DEFAULT_MAPS = ["DefaultMedium", "AceOfSpades", "Ambush", "Canals", "CH3353C4K3F4CT0RY", "Duck", "Fountain", "Hockey", "Rivers", "Soccer"]

BATTLECODE_URL = "https://api.battlecode.org/api/compete/bc24/request/"
HEADERS = {"authority": "api.battlecode.org",
           "accept": "application/json, text/javascript, */*; q=0.01",
           "accept-language": "en-US,en;q=0.9",
           "authorization": f"Bearer {getJWTToken()}",
           "content-type": "application/json",
           "origin": "https://play.battlecode.org",
           "referer": "https://play.battlecode.org/",
           "sec-fetch-mode": "cors",
           "sec-fetch-site": "same-site",
           "user-agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"}

REPLAYS_WATCH_BASE_URL = f'https://releases.battlecode.org/client/battlecode24/{getCurrentGameVersion()}/index.html?gameSource='

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